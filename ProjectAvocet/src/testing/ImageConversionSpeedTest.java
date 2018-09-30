package testing;

import java.io.FileNotFoundException;

import org.opencv.core.Core;

import datamodel.Video;
import utils.UtilsForOpenCV;

public class ImageConversionSpeedTest {

	public static void main(String[] args) throws FileNotFoundException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Video vid = new Video("/home/forrest/data/shara_chicks_tracking/sample1.mp4");
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			UtilsForOpenCV.matToJavaFXImage(vid.readFrame());
		}
		long stopTime = System.currentTimeMillis();
		
		System.out.println("Time: " + (stopTime - startTime));

	}

}
