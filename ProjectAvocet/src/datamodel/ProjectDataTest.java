package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;

class ProjectDataTest {

	@BeforeAll
	static void initialize() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
	}
	
	public ProjectData createProjectData() throws FileNotFoundException {
		ProjectData project = new ProjectData("TestVideos/CircleTest1_no_overlap.mp4");
		AnimalTrack track1 = new AnimalTrack("tester1");
		AnimalTrack track2 = new AnimalTrack("tester2");
		project.getTracks().add(track1);
		project.getTracks().add(track2);
		
		track1.add(new TimePoint(100,200,0));
		track1.add(new TimePoint(105,225,30));
		
		track2.add(new TimePoint(300,400,90));
		
		AnimalTrack track3 = new AnimalTrack("unassigned");
		track3.add(new TimePoint(150,225,60));
		track3.add(new TimePoint(57,360,120));
		
		AnimalTrack track4 = new AnimalTrack("unassigned2");
		track4.add(new TimePoint(126,65,70));
		track4.add(new TimePoint(10,95,130));
		
		project.getUnassignedSegments().add(track3);
		project.getUnassignedSegments().add(track4);
		
		return project;
	}
	
	@Test
	void testVideo() throws FileNotFoundException {
		ProjectData project = createProjectData();
		assertEquals("TestVideos/CircleTest1_no_overlap.mp4", project.getVideo().getFilePath());
	}
	
	@Test
	void testAnimalTracks() throws FileNotFoundException {
		ProjectData project = createProjectData();
		
		assertEquals(new TimePoint(100,200,0), project.getTracks().get(0).getTimePointAtTime(0));
		assertEquals(new TimePoint(300,400,90), project.getTracks().get(1).getTimePointAtTime(90));
		
		assertEquals(new TimePoint(150,225,60), project.getUnassignedSegments().get(0).getTimePointAtTime(60));
		assertEquals(new TimePoint(57,360,120), project.getUnassignedSegments().get(0).getTimePointAtTime(120));
		assertEquals(new TimePoint(126,65,70), project.getUnassignedSegments().get(1).getTimePointAtTime(70));
		assertEquals(new TimePoint(10,95,130), project.getUnassignedSegments().get(1).getTimePointAtTime(130));
	}

}
