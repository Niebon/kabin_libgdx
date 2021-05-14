package dev.kabin.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimedConditionTest {

	@Test
	void isInactiveBeforeStart() {
		var tc = new TimedCondition(true, 1000);
		Assertions.assertFalse(tc.eval());
	}

	@Test
	void init() throws InterruptedException {
		var tc = new TimedCondition(true, 1000);

		// Use once.
		System.out.println("\nPrev:");
		tc.init();
		assertTrue(tc.eval());
		Thread.sleep(1000);

		// Can be reused.
		System.out.println("\nReuse:");
		tc.init();
		assertTrue(tc.eval());
		Thread.sleep(1000);
		assertFalse(tc.eval());
	}

	@Test
	void eval() throws InterruptedException {
		var tc = new TimedCondition(true, 1000);
		tc.init();
		assertTrue(tc.eval());
		Thread.sleep(1000);
		assertFalse(tc.eval());
	}
}