package benchmarks.hashcodes;

import benchmarks.BenchmarkUtil;
import benchmarks.Benchmarked;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class BenchMarkObjectsDotHashCode extends BenchmarkUtil {

    public static final Random RANDOM = new Random(0);

    public static void main(String[] args) {
        benchMarkRunnable(
                BenchMarkObjectsDotHashCode.class,
                BenchMarkObjectsDotHashCode::getHash,
                Duration.ofSeconds(5),
                Duration.ofSeconds(1)
        );
    }

    /**
     * @return a hash value using varags.
     */
    @Benchmarked
    private static int getHash() {
        return Objects.hash(RANDOM.nextInt(), RANDOM.nextInt());
    }

}
