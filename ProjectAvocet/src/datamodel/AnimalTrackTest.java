package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class AnimalTrackTest {

	public AnimalTrack createAnimalTrack() {
		AnimalTrack test = new AnimalTrack("Tester");
		TimePoint tp1 = new TimePoint(34, 53, 0);
		TimePoint tp2 = new TimePoint(124, 200, 25);
		TimePoint tp3 = new TimePoint(75, 168, 50);
		test.add(tp1);
		test.add(tp2);
		test.add(tp3);
		
		return test;
	}
	
	@Test
	void testAnimalTrackID() {
		AnimalTrack test = createAnimalTrack();
		
		assertTrue(test.hasIDAssigned());
		assertEquals("Tester", test.getID());	
	}
	
	@Test
	void testGetTimePointMethods() {
		AnimalTrack test = createAnimalTrack();
		TimePoint tp1 = new TimePoint(34, 53, 0);
		TimePoint tp2 = new TimePoint(124, 200, 25);
		TimePoint tp3 = new TimePoint(75, 168, 50);
		
		assertEquals(tp1, test.getTimePointAtIndex(0));
		assertEquals(tp2, test.getTimePointAtIndex(1));
		assertEquals(tp3, test.getTimePointAtIndex(2));
		
		assertEquals(tp1, test.getTimePointAtTime(0));
		assertEquals(tp2, test.getTimePointAtTime(25));
		assertEquals(tp3, test.getTimePointAtTime(50));
		
		assertEquals(tp3, test.getFinalTimePoint());
	}
	
	@Test
	void testGetPositions() {
		AnimalTrack test = createAnimalTrack();
		TimePoint tp1 = new TimePoint(34, 53, 0);
		TimePoint tp2 = new TimePoint(124, 200, 25);
		TimePoint tp3 = new TimePoint(75, 168, 50);
		
		List<TimePoint> positions = test.getPositions();
		
		assertEquals(tp1, positions.get(0));
		assertEquals(tp2, positions.get(1));
		assertEquals(tp3, positions.get(2));
	}

}
