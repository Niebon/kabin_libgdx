package dev.kabin.util.collections;

import dev.kabin.util.lambdas.Function;
import dev.kabin.util.lambdas.IntFunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.IntSupplier;

public class LazyList<T> implements List<T>, IntFunction<T> {

    private final IntFunction<T> getter;
    private final IntSupplier size;

    public LazyList(T[] entries) {
        this.getter = i -> entries[i];
        this.size = () -> entries.length;
    }

    public LazyList(IntFunction<T> getter, IntSupplier size) {
        this.getter = getter;
        this.size = size;
    }

    @Override
    public <R> LazyList<R> andThen(Function<T, R> f) {
        return new LazyList<>(getter.andThen(f), size);
    }

    @Override
    public int size() {
        return internalSize();
    }

    private int internalSize() {
        return size.getAsInt();
    }

    @Override
    public boolean isEmpty() {
        return internalSize() == 0;
    }

    @Override
    public boolean contains(Object o) {
        int size = internalSize();
        for (int i = 0; i < size; i++) {
            var t = internalGet(i);
            if (t != null) return true;
        }
        return false;
    }

    private T internalGet(int index) {
        return getter.apply(index);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new LazyListIterator<>(this);
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        int size = internalSize();
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = internalGet(i);
        }
        return array;
    }

    @NotNull
    @Override
    public <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        int size = internalSize();
        //noinspection unchecked
        T1[] array = (a.length < size) ? (T1[]) new Object[size] : a;
        for (int i = 0; i < size; i++) {
            //noinspection unchecked
            array[i] = (T1) internalGet(i);
        }
        return array;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        int size = internalSize();
        for (int i = 0; i < size; i++) {
            if (!c.contains(internalGet(i))) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return internalGet(index);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        int size = internalSize();
        for (int i = 0; i < size; i++) {
            if (Objects.equals(o, internalGet(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int size = internalSize();
        for (int i = size - 1; i > 0; i--) {
            if (Objects.equals(o, internalGet(i))) {
                return i;
            }
        }
        return -1;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return new LazyListIterator<>(this);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return new LazyListIterator<>(subList(index, internalSize()));
    }

    @NotNull
    @Override
    public LazyList<T> subList(int fromIndex, int toIndex) {
        int size = internalSize();
        if (fromIndex > size) throw new IndexOutOfBoundsException(fromIndex);
        return new LazyList<>(i -> internalGet(i + fromIndex), () -> Math.min(size, toIndex));
    }

    @Override
    public T apply(int input) {
        return internalGet(input);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof List<?> l) {

            if (internalSize() != l.size()) {
                return false;
            }

            return containsAll(l);
        } else return false;
    }

    // Copied from abstract collection.
    public String toString() {
        Iterator<T> it = iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            T e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    private static class LazyListIterator<T> implements ListIterator<T> {

        private final LazyList<T> instance;
        private int curr = 0;

        private LazyListIterator(LazyList<T> instance) {
            this.instance = instance;
        }

        @Override
        public boolean hasNext() {
            return curr < instance.internalSize();
        }

        @Override
        public T next() {
            return instance.internalGet(curr++);
        }

        @Override
        public boolean hasPrevious() {
            return curr > 0;
        }

        @Override
        public T previous() {
            return instance.internalGet(--curr);
        }

        @Override
        public int nextIndex() {
            return curr + 1;
        }

        @Override
        public int previousIndex() {
            return curr - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }


}
