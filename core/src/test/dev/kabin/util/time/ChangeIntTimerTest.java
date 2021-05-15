package dev.kabin.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeIntTimerTest {
	@Test
	void set() {
		var c = new ChangeIntTimer();
		c.set(1);
		Assertions.assertEquals(0, c.last());

		// Last should stil be zero after the second call to set(1).
		c.set(1);
		Assertions.assertEquals(0, c.last());
	}
}