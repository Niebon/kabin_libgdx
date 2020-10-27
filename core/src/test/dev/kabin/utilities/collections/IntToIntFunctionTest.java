package dev.kabin.utilities.collections;

import dev.kabin.utilities.Statistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class IntToIntFunctionTest {

    @Test
    void addOneValue() {
        IntToIntFunction f = new IntToIntFunction(2);
        f.put(4, 5);
        Assertions.assertEquals(5, f.eval(4));
    }

    @Test
    void addValuesPastCapacity() {
        IntToIntFunction f = new IntToIntFunction(2);
        f.put(4, 5);
        f.put(3, 6);
        f.put(2, 10);
        Assertions.assertEquals(5, f.eval(4));
        Assertions.assertEquals(6, f.eval(3));
        Assertions.assertEquals(10, f.eval(2));
    }

    @Test
    void overrideValue() {
        IntToIntFunction f = new IntToIntFunction(2);
        f.put(4, 5);
        Assertions.assertEquals(5, f.eval(4));
        f.put(4, 7);
        Assertions.assertEquals(7, f.eval(4));
    }

    @Test
    void stressTest() {
        IntToIntFunction f = new IntToIntFunction();
        Map<Integer, Integer> intToIntMap = IntStream.range(0, 1000).boxed()
                .collect(Collectors.toMap(Function.identity(), i -> Statistics.RANDOM.nextInt()));
        intToIntMap.forEach(f::put);
        intToIntMap.forEach((i, j) -> Assertions.assertEquals(j, f.eval(i)));
    }
}