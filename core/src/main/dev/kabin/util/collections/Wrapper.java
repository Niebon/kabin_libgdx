package dev.kabin.util.collections;

public final class Wrapper<E> {
	private E obj;

	private Wrapper(E obj) {
		this.obj = obj;
	}

	public static <E> Wrapper<E> of(E obj) {
		return new Wrapper<>(obj);
	}

	public static <E> Wrapper<E> empty() {
		return new Wrapper<>(null);
	}

	public E getObj() {
		return obj;
	}

	public void setObj(E obj) {
		this.obj = obj;
	}

	public boolean isEmpty() {
		return obj == null;
	}
}
