package dev.kabin.util.collections;

import dev.kabin.util.Statistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class IntToIntMapTest {

    @Test
    void addOneValue() {
        IntToIntMap f = new IntToIntMap(2);
        f.put(4, 5);
        Assertions.assertEquals(5, f.get(4));
    }

    @Test
    void addValuesPastCapacity() {
        IntToIntMap f = new IntToIntMap(2);
        f.put(4, 5);
        f.put(3, 6);
        f.put(2, 10);
        Assertions.assertEquals(5, f.get(4));
        Assertions.assertEquals(6, f.get(3));
        Assertions.assertEquals(10, f.get(2));
    }

    @Test
    void overrideValue() {
        IntToIntMap f = new IntToIntMap(2);
        f.put(4, 5);
        Assertions.assertEquals(5, f.get(4));
        f.put(4, 7);
        Assertions.assertEquals(7, f.get(4));
    }

    @Test
    void stressTest() {
        IntToIntMap f = new IntToIntMap();
        Map<Integer, Integer> intToIntMap = IntStream.range(0, 1000).boxed()
                .collect(Collectors.toMap(Function.identity(), i -> Statistics.RANDOM.nextInt()));
        intToIntMap.forEach(f::put);
        intToIntMap.forEach((i, j) -> Assertions.assertEquals(j, f.get(i)));
    }
}