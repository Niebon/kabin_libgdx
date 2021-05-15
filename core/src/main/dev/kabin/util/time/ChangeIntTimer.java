package dev.kabin.util.time;

public class ChangeIntTimer {

	private int curr;
	private int last;

	public int curr() {
		return curr;
	}

	public void set(int value) {
		this.last = curr;
		this.curr = value;
	}

	public int last() {
		return last;
	}

	@Override
	public String toString() {
		return "ChangeIntTimer{" +
				"curr=" + curr +
				", last=" + last +
				'}';
	}
}
