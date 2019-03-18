package base;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.bytedeco.javacpp.indexer.Indexer;

import util.Direction;
import util.Utils;

public class GraphFinder {
	public void findGraph(Mat input) {
		Mat scaled = new Mat();
		Utils.scaleImageToFit(input, scaled);
		
		Mat gray = new Mat();
		cvtColor(scaled, gray, COLOR_BGR2GRAY);
		
		Mat blurred = new Mat();
		GaussianBlur(gray, blurred, new Size(5, 5), 0);
		
		Mat dilated = new Mat();
		Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(11, 11));
		dilate(blurred, dilated, kernel, new Point(-1, -1), 1, BORDER_CONSTANT, morphologyDefaultBorderValue());
		
		Mat edges = new Mat();
		Canny(dilated, edges, 50, 150);
		Utils.display(edges, "edges");
		
		final int reach = 15;
		Indexer edgeIndexer = edges.createIndexer();
		for(int i = reach; i < edgeIndexer.cols() - reach; i++) {
			for(int j = reach; j < edgeIndexer.rows() - reach; j++) {
				int white_count = 0;
				boolean left = false, up = false, right = false, down = false;
				for(int k = 0; k < reach; k++) {
					if(edgeIndexer.getDouble(j-k, i, 0) > 200) {
						white_count++;
						left = true;
						break;
					}
				}
				for(int k = 0; k < reach; k++) {
					if(edgeIndexer.getDouble(j+k, i, 0) > 200) {
						white_count++;
						right = true;
						break;
					}
				}
				for(int k = 0; k < reach; k++) {
					if(edgeIndexer.getDouble(j, i+k, 0) > 200) {
						white_count++;
						down = true;
						break;
					}					
				}
				for(int k = 0; k < reach; k++) {
					if(edgeIndexer.getDouble(j, i-k, 0) > 200) {
						white_count++;
						up = true;
						break;
					}
					
				}
				
				if (white_count == 3) {
					if (!left) {
						edgeIndexer.putDouble(new long[] {j, i, 0}, 125.0 );
					} else if (!up) {
						edgeIndexer.putDouble(new long[] {j, i, 0}, 126.0 );
					} else if (!right) {
						edgeIndexer.putDouble(new long[] {j, i, 0}, 127.0 );
					} else {
						edgeIndexer.putDouble(new long[] {j, i, 0}, 128.0 );
					}
					
				}
			}
		}
		
		List<EndVertex> endVertices = new ArrayList<EndVertex>();
		final int edgeSquareSide = 6;
		for(int i = reach; i < edgeIndexer.cols() - reach; i++) {
			for(int j = reach; j < edgeIndexer.rows() - reach; j++) {
				if (edgeIndexer.getDouble(j, i, 0) > 124 && edgeIndexer.getDouble(j, i, 0) < 129) {
					boolean isGraySquare = true;
					for(int i2 = 0; i2 < edgeSquareSide; i2++) {
						for(int j2 = 0; j2 < edgeSquareSide; j2++) {
							if (edgeIndexer.getDouble(j + j2,i + i2,0) < 124 || edgeIndexer.getDouble(j + j2, i + i2, 0) > 129) {
								isGraySquare = false;
							}
						}
					}
					if (isGraySquare) {
						double squarePixel = edgeIndexer.getDouble(j + (edgeSquareSide / 2), i + (edgeSquareSide / 2), 0);
						if (squarePixel < 125.5) {
							endVertices.add(new EndVertex(new Point(j + (edgeSquareSide / 2), i + (edgeSquareSide / 2)), Direction.LEFT));
						} else if (squarePixel < 126.5) {
							endVertices.add(new EndVertex(new Point(j + (edgeSquareSide / 2), i + (edgeSquareSide / 2)), Direction.UP));
						} else if (squarePixel < 127.5) {
							endVertices.add(new EndVertex(new Point(j + (edgeSquareSide / 2), i + (edgeSquareSide / 2)), Direction.RIGHT));
						} else {
							endVertices.add(new EndVertex(new Point(j + (edgeSquareSide / 2), i + (edgeSquareSide / 2)), Direction.DOWN));
						}
						
						
						// BFS to color neighboring gray pixels black
						Queue<Integer> q = new LinkedList<Integer>();
						q.add(j);
						q.add(i);
						while(!q.isEmpty()) {
							int x = q.remove();
							int y = q.remove();
							edgeIndexer.putDouble(new long[] {x, y, 0}, 0.0);
							double neighborPixel = edgeIndexer.getDouble(x - 1, y, 0);
							if (neighborPixel > 124 && neighborPixel < 129) {
								q.add(x - 1);
								q.add(y);
							}
							neighborPixel = edgeIndexer.getDouble(x + 1, y, 0);
							if (neighborPixel > 124 && neighborPixel < 129) {
								q.add(x + 1);
								q.add(y);
							}
							neighborPixel = edgeIndexer.getDouble(x, y - 1, 0);
							if (neighborPixel > 124 && neighborPixel < 129) {
								q.add(x);
								q.add(y - 1);
							}
							neighborPixel = edgeIndexer.getDouble(x, y + 1, 0);
							if (neighborPixel > 124 && neighborPixel < 129) {
								q.add(x);
								q.add(y + 1);
							}
						}
					}
				}
			}	
		}
		
		Utils.display(edges, "graphDetected");
		System.out.println(endVertices.size());
		ImageGraph ig = new ImageGraph(endVertices);
	}
	
}
