package benchmarks.hashcodes;

import benchmarks.BenchmarkUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class BenchMarkObjectsDotHashCode extends BenchmarkUtil {

    public static void main(String[] args) {
        final Random r = new Random(0);
        benchMarkRunnable(
                BenchMarkObjectsDotHashCode.class,
                () -> Objects.hash(r.nextInt(), r.nextInt()),
                Duration.ofSeconds(5),
                Duration.ofSeconds(1)
        );
    }

}
