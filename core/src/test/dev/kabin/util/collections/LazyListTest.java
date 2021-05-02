package dev.kabin.util.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class LazyListTest {

    @Test
    void size() {
        Assertions.assertEquals(3, new LazyList<>(i -> "a", 3).size());
    }

    @Test
    void isEmpty() {
        Assertions.assertTrue(new LazyList<>(i -> "a", 0).isEmpty());
    }

    @Test
    void contains() {
        Assertions.assertTrue(new LazyList<>(i -> "a", 3).contains("a"));
    }

    @Test
    void iterator() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        int i = 0;
        for (String s : l) {
            Assertions.assertEquals(backingData[i++], s);
        }
    }

    @Test
    void toArray() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertArrayEquals(backingData, l.toArray());
    }

    @Test
    void testToArray() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertArrayEquals(backingData, l.toArray(String[]::new));
    }

    @Test
    void add() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.add("d"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.add(10, "d"));

    }

    @Test
    void remove() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.remove("d"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.remove(2));
    }

    @Test
    void containsAll() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertTrue(l.containsAll(Set.of("a", "b", "c")));
    }

    @Test
    void addAll() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.addAll(Set.of("x")));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.addAll(10, Set.of("x")));
    }

    @Test
    void removeAll() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.removeAll(Set.of("a")));
    }

    @Test
    void retainAll() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.retainAll(Set.of("a")));
    }

    @Test
    void clear() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, l::clear);
    }

    @Test
    void get() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertEquals("a", l.get(0));
        Assertions.assertEquals("b", l.get(1));
        Assertions.assertEquals("c", l.get(2));
    }

    @Test
    void set() {
        String[] backingData = {"a", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> l.set(0, "x"));
    }


    @Test
    void indexOf() {
        String[] backingData = {"a", "b", "c", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertEquals(2, l.indexOf("c"));
    }

    @Test
    void lastIndexOf() {
        String[] backingData = {"a", "b", "c", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertEquals(4, l.lastIndexOf("c"));
    }

    @Test
    void listIterator() {
    }

    @Test
    void testListIterator() {
    }

    @Test
    void subList() {
        String[] backingData = {"a", "b", "c", "b", "c"};
        var l = new LazyList<>(backingData);
        Assertions.assertEquals(List.of("a", "b", "c"), l.subList(0, 3));
    }
}