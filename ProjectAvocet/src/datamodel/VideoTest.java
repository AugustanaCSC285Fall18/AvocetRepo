package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;

class VideoTest {
	
	@BeforeAll
	static void initialize() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
	}
	
	@Test
	void testGetandSet() throws FileNotFoundException {
		Video vid = new Video("TestVideos/CircleTest1_no_overlap.mp4");
		vid.setCurrentFrameNum(5);
		assertEquals(5, vid.getCurrentFrameNum());
		
		assertEquals("TestVideos/CircleTest1_no_overlap.mp4", vid.getFilePath());
		vid.setEmptyFrameNum(10);
		vid.setStartFrameNum(15);
		vid.setEndFrameNum(30);
		vid.setXPixelsPerCm(100);
		vid.setYPixelsPerCm(150);
		assertEquals(10, vid.getEmptyFrameNum());
		assertEquals(15, vid.getStartFrameNum());
		assertEquals(30, vid.getEndFrameNum());
		assertEquals(100, vid.getXPixelsPerCm());
		assertEquals(150, vid.getYPixelsPerCm());
		assertEquals(125, vid.getAvgPixelsPerCm());
	}

}
