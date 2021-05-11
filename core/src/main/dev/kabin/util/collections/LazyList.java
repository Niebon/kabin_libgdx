package dev.kabin.util.collections;

import dev.kabin.util.lambdas.Function;
import dev.kabin.util.lambdas.IntFunction;
import dev.kabin.util.lambdas.IntObjPredicate;
import dev.kabin.util.lambdas.IntToIntFunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * Models a view of a list where {@link #get(int)} is a lazy getter.
 * An instance of this list behaves much like an intermediate stream, but because this is a list, it can be reused,
 * and elements may be inspected by index.
 *
 * @param <T> the type parameter.
 */
public class LazyList<T> implements List<T>, IntFunction<T> {

    // Constants.
    private static final LazyList<?> EMPTY_LAZY_LIST = new LazyList<>(i -> 0, () -> 0);

    // Fields.
    private final IntFunction<T> getter;
    private final IntSupplier size;

    public LazyList(T entry) {
        this.getter = i -> entry;
        this.size = () -> 1;
    }

    @SafeVarargs
    public LazyList(T... entries) {
        this.getter = i -> entries[i];
        this.size = () -> entries.length;
    }

    public LazyList(IntFunction<T> getter, IntSupplier size) {
        this.getter = getter;
        this.size = size;
    }

    public static <T> LazyList<T> empty() {
        //noinspection unchecked
        return (LazyList<T>) EMPTY_LAZY_LIST;
    }

    public LazyList<T> memoize() {
        @SuppressWarnings("unchecked")
        T[] entries = (T[]) new Object[internalSize()];
        for (int i = 0, n = internalSize(); i < n; i++) {
            entries[i] = internalGet(i);
        }
        return new LazyList<>(entries);
    }

    public T reduce(BinaryOperator<T> associativeBinOp) {
        if (internalSize() == 0) {
            return null;
        } else {
            T acc = internalGet(0);
            for (int i = 1, n = internalSize(); i < n; i++) {
                acc = associativeBinOp.apply(acc, internalGet(i));
            }
            return acc;
        }
    }

    /**
     * A call to this method {@link #compose(IntToIntFunction) composes} this lazy list
     * with an {@code int[]} that remaps indices such that the resulting lazy list is sorted with
     * respect to the given comparator.
     *
     * @param comparator a comparator that compares list entries.
     * @return a sorted lazy list with respect to the given comparator.
     * @implNote A call to this method has <i>O(n)</i> memory complexity that
     * will be occupied for the lifetime of the resulting list. This comes from
     * the {@code int} array that is used for index remapping - as explained above.
     */
    public LazyList<T> sortBy(Comparator<T> comparator) {
        if (isEmpty()) return empty();
        int n = internalSize();
        int[] indexReMapper = new int[n];
        for (int i = 0; i < n; i++) indexReMapper[i] = i;
        dev.kabin.util.Arrays.quickSort(indexReMapper, this::internalGet, comparator);
        return compose(i -> indexReMapper[i]);
    }

    public boolean allMatch(Predicate<T> predicate) {
        for (int i = 0, n = internalSize(); i < n; i++) {
            if (!predicate.test(internalGet(i))) return false;
        }
        return true;
    }

    public boolean allMatch(IntObjPredicate<T> predicate) {
        for (int i = 0, n = internalSize(); i < n; i++) {
            if (!predicate.test(i, internalGet(i))) return false;
        }
        return true;
    }

    public boolean noneMatch(IntObjPredicate<T> predicate) {
        for (int i = 0, n = internalSize(); i < n; i++) {
            if (predicate.test(i, internalGet(i))) return false;
        }
        return true;
    }

    public boolean noneMatch(Predicate<T> predicate) {
        for (int i = 0, n = internalSize(); i < n; i++) {
            if (predicate.test(internalGet(i))) return false;
        }
        return true;
    }

    public LazyList<LazyList<T>> split(Comparator<T> comparator) {
        if (isEmpty()) return empty();
        LazyList<T> sorted = sortBy(comparator);
        int size = internalSize();
        int[] partitions = new int[size + 1];
        T compareCandidate;
        int numberOfPartitions = 0;
        int index = 0;
        do {
            compareCandidate = sorted.internalGet(index);
            while (comparator.compare(compareCandidate, sorted.internalGet(index)) == 0) {
                index++;
            }
            partitions[++numberOfPartitions] = index;
        } while (index < size - 1);
        partitions[++numberOfPartitions] = index + 1;
        int finalNumberOfPartitions = numberOfPartitions;
        return new LazyList<>(i -> sorted.subList(partitions[i], partitions[i + 1]), () -> finalNumberOfPartitions);
    }

    @Override
    public LazyList<T> compose(IntToIntFunction composer) {
        return new LazyList<>(getter.compose(composer), size);
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
            T t = internalGet(i);
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
        if (index < 0 || index >= size()) throw new IndexOutOfBoundsException(index);
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
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) throw new IllegalArgumentException();
        return new LazyList<>(i -> internalGet(i + fromIndex), () -> toIndex - fromIndex);
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

            int i = 0;
            for (var e : l) {
                if (!Objects.equals(e, internalGet(i))) return false;
                i++;
            }
            return true;
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
