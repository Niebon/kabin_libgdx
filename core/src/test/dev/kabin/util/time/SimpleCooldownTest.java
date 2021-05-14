package dev.kabin.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleCooldownTest {

	@Test
	void doesNotStartAsComplete() {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
	}

	@Test
	void initAndIsComplete() throws InterruptedException {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.start();
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
		Assertions.assertTrue(cooldown.isCompleted());
	}

	@Test
	void repeatedInitAndIsComplete() throws InterruptedException {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.start();
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
		cooldown.reset();
		cooldown.start();
		Assertions.assertFalse(cooldown.isCompleted());
		Thread.sleep(200L);
		Assertions.assertTrue(cooldown.isCompleted());
	}

	@Test
	void pauseAndUnpause() throws InterruptedException {
		var cooldown = new SimpleCooldown(100L);
		Assertions.assertFalse(cooldown.isCompleted());
		cooldown.start();
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
		cooldown.start();
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