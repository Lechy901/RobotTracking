package util;

/**
 * A data class containing info about the program command lines arguments after parsing
 * 
 * @author ALechovsky
 *
 */

public class ProgramArguments {
	public boolean captureTrainImages = false;
	public int captureTrainImagesFileNumber = 0;
	public String captureTrainImagesDirPath = "bg";
	
	public boolean generateBgFile = false;
	public String generateBgFileDirPath = "bg";
	public String generateBgFileFileName = "bg.txt";
	
	public boolean convertImages = false;
	public String convertImagesDirPath = "img";
	
	public boolean downloadImages = false;
	public String downloadImagesDirPath = "paper-images";
	
	public int lineWidth = 0;
	public int robotsNumber = 3;
	public int pointGroupDistance = 20;
}
