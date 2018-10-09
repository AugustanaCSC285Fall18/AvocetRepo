package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TimePointTest {

	@Test
	void testSetandGetMethods() {
		TimePoint tp1 = new TimePoint(34, 53, 3);
		TimePoint tp2 = new TimePoint(124, 200, 25);
		
		assertEquals(34, tp1.getX());
		assertEquals(53, tp1.getY());
		assertEquals(3, tp1.getFrameNum());
		
		tp2.setX(12.5);
		tp2.setY(65.7);
		assertEquals(12.5, tp2.getX());
		assertEquals(65.7, tp2.getY());
		
		assertEquals(22, tp2.getTimeDiffAfter(tp1));
		assertEquals(22, tp2.compareTo(tp1));
		
		tp1.setX(3);
		tp1.setY(10);
		tp2.setX(7);
		tp2.setY(13);
		
		assertEquals(5, tp1.getDistanceTo(tp2));
		
		assertFalse(tp1.equals(tp2));
		
		TimePoint tp3 = new TimePoint(5, 6, 7);
		TimePoint tp4 = new TimePoint(5, 6, 7);
		assertTrue(tp3.equals(tp4));
		
		java.awt.Point testPoint = new java.awt.Point((int)5,(int)6);
		assertEquals(testPoint, tp3.getPointAWT());
		
		org.opencv.core.Point testPoint2 = new org.opencv.core.Point(5,6);
		assertEquals(testPoint2, tp3.getPointOpenCV());
	}

}
