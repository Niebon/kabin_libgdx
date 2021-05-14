package dev.kabin.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleCooldownTest {

	@Test
	void initAndIsComplete() throws InterruptedException {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.init();
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
	}

	@Test
	void repeatedInitAndIsComplete() throws InterruptedException {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.init();
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
		cooldown.init();
		Assertions.assertFalse(cooldown.isCompleted());
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
	}

	@Test
	void pauseAndUnpause() throws InterruptedException {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.init();
		cooldown.pause();
		Thread.sleep(200L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.unpause();
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
	}

	@Test
	void isActive() {
		var cooldown = new SimpleCooldown(100L);
		cooldown.init();
		Assertions.assertTrue(cooldown.isActive());
		cooldown.pause();
		Assertions.assertFalse(cooldown.isActive());
		cooldown.unpause();
		Assertions.assertTrue(cooldown.isActive());
	}

	@Test
	void forceComplete() {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.forceComplete();
		Assertions.assertTrue(cooldown.isCompleted());
	}

}