package main;

import base.*;
import training_utils.ImageUtils;
import training_utils.TestImageCapturer;
import util.ProgramArguments;

public class TrackingMain {

	public static void main(String[] args) {
		
		ProgramArguments pa = parseArgs(args);
		
		if (pa.captureTrainImages) {
			TestImageCapturer tic = new TestImageCapturer(pa.captureTrainImagesDirPath);
			tic.start();
			return;
		}
		if (pa.generateBgFile) {
			ImageUtils.generateBgFile(pa.generateBgFileDirPath, pa.generateBgFileFileName);
			return;
		}
		if (pa.convertImages) {
			ImageUtils.convertImagesFromStorage(pa.convertImagesDirPath);
			return;
		}
		if (pa.downloadImages) {
			ImageUtils.loadAndConvertImagesFromURLs(pa.downloadImagesDirPath);
			return;
		}
		
		WindowControl wc = new WindowControl(pa.lineWidth, pa.robotsNumber, pa.pointGroupDistance);
		wc.start();
	}
	
	/**
	 * Parse program arguments
	 * @param args The arguments in a string array
	 * @return An object containing the arguments
	 */
	private static ProgramArguments parseArgs(String[] args) {
		ProgramArguments r = new ProgramArguments();
		
		for(int i = 0; i < args.length; i += 2) {
			String next = args[i + 1];
			try {
				switch(args[i].toLowerCase()) {
				case "-capturetrainimages":
					r.captureTrainImages = Boolean.parseBoolean(next);
					break;
				case "-capturetrainimagesfilenumber":
					r.captureTrainImagesFileNumber = Integer.parseInt(next);
					break;
				case "-capturetrainimagesdirpath":
					r.captureTrainImagesDirPath = next;
					break;
				case "-generatebgfile":
					r.generateBgFile = Boolean.parseBoolean(next);
					break;
				case "-generatebgfiledirpath":
					r.generateBgFileDirPath = next;
					break;
				case "-generatebgfilefilename":
					r.generateBgFileFileName = next;
					break;
				case "-convertimages":
					r.convertImages = Boolean.parseBoolean(next);
					break;
				case "-convertimagesdirpath":
					r.convertImagesDirPath = next;
					break;
				case "-downloadimages":
					r.downloadImages = Boolean.parseBoolean(next);
					break;
				case "-downloadimagesdirpath":
					r.downloadImagesDirPath = next;
					break;
				case "-linewidth":
					r.lineWidth = Integer.parseInt(next);
					break;
				case "-robotsnumber":
					r.robotsNumber = Integer.parseInt(next);
					break;
				case "-pointgroupdistance":
					r.pointGroupDistance = Integer.parseInt(next);
					break;
				default:
					System.err.println("Unknown command line argument: " + args[i]);
				}
			} catch (NumberFormatException ex) {
				System.err.println("Error while parsing number: " + next);
			}
		}
		
		return r;
	}

}
