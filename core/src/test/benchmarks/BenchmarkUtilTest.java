package benchmarks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.DoubleStream;

class BenchmarkUtilTest {


    @Test
    void mean() {
        var stats = new BenchmarkUtil.Statistics();
        stats.add(1);
        stats.add(2);
        stats.add(3);

        Assertions.assertEquals(2d, stats.getMean(), 0.01d);
    }

    @Test
    void std() {
        var stats = new BenchmarkUtil.Statistics();
        stats.add(1);
        stats.add(2);
        stats.add(3);

        double mean = 2d;
        long n = 3;

        Assertions.assertEquals(Math.sqrt(DoubleStream.of(1, 2, 3).map(x -> (x - mean) * (x - mean)).sum() / n), stats.getStd(), 0.01d);
    }

    @Test
    void numberRepresentation_positiveNumber_positiveExponent() {
        Assertions.assertEquals("0.010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(9));
        Assertions.assertEquals("0.10848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(8));
        Assertions.assertEquals("1.0848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(7));
        Assertions.assertEquals("10.848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(6));
        Assertions.assertEquals("108.48323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(5));
        Assertions.assertEquals("1084.8323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(4));
        Assertions.assertEquals("10848.323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(3));
        Assertions.assertEquals("108483.23290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(2));
        Assertions.assertEquals("1084832.3290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(1));
        Assertions.assertEquals("10848323.290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(0));
        Assertions.assertEquals("108483232.90354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-1));
        Assertions.assertEquals("1084832329.0354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-2));
        Assertions.assertEquals("10848323290.354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-3));
        Assertions.assertEquals("108483232903.54501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-4));
        Assertions.assertEquals("1084832329035.4501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-5));
        Assertions.assertEquals("10848323290354.501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-6));
        Assertions.assertEquals("108483232903545.01", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-7));
        Assertions.assertEquals("1084832329035450.1", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-8));
        Assertions.assertEquals("10848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-9));
        Assertions.assertEquals("108483232903545010", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-10));
        Assertions.assertEquals("1084832329035450100", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E+7").coefficient(-11));
    }

    @Test
    void numberRepresentation_positiveNumber_negativeExponent() {
        Assertions.assertEquals("108.48323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-9));
        Assertions.assertEquals("10.848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-8));
        Assertions.assertEquals("1.0848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-7));
        Assertions.assertEquals("0.10848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-6));
        Assertions.assertEquals("0.010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-5));
        Assertions.assertEquals("0.0010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-4));
        Assertions.assertEquals("0.00010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-3));
        Assertions.assertEquals("0.000010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-2));
        Assertions.assertEquals("0.0000010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-1));


        Assertions.assertEquals("0.00000010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(0));
        Assertions.assertEquals("0.0000010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-1));
        Assertions.assertEquals("0.000010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-2));
        Assertions.assertEquals("0.00010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-3));
        Assertions.assertEquals("0.0010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-4));
        Assertions.assertEquals("0.010848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-5));
        Assertions.assertEquals("0.10848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-6));
        Assertions.assertEquals("1.0848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-7));
        Assertions.assertEquals("10.848323290354501", BenchmarkUtil.NumberRepresentation.parse("1.0848323290354501E-7").coefficient(-8));
    }

    @Test
    void numberRepresentation_negative_positiveExponent() {
        Assertions.assertEquals("-0.010848323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(9));
        Assertions.assertEquals("-0.10848323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(8));
        Assertions.assertEquals("-1.0848323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(7));
        Assertions.assertEquals("-10.848323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(6));
        Assertions.assertEquals("-108.48323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(5));
        Assertions.assertEquals("-1084.8323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(4));
        Assertions.assertEquals("-10848.323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(3));
        Assertions.assertEquals("-108483.23290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(2));
        Assertions.assertEquals("-1084832.3290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(1));
        Assertions.assertEquals("-10848323.290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(0));
        Assertions.assertEquals("-108483232.90354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-1));
        Assertions.assertEquals("-1084832329.0354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-2));
        Assertions.assertEquals("-10848323290.354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-3));
        Assertions.assertEquals("-108483232903.54501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-4));
        Assertions.assertEquals("-1084832329035.4501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-5));
        Assertions.assertEquals("-10848323290354.501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-6));
        Assertions.assertEquals("-108483232903545.01", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-7));
        Assertions.assertEquals("-1084832329035450.1", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-8));
        Assertions.assertEquals("-10848323290354501", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-9));
        Assertions.assertEquals("-108483232903545010", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-10));
        Assertions.assertEquals("-1084832329035450100", BenchmarkUtil.NumberRepresentation.parse("-1.0848323290354501E+7").coefficient(-11));
    }
}