package benchmarks.hashcodes;

import benchmarks.BenchmarkUtil;
import benchmarks.Benchmarked;
import dev.kabin.utilities.HashCodeUtil;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;

public class BenchMarkHashUtils extends BenchmarkUtil {

    public static final Random RANDOM = new Random(0);

    public static void main(String[] args) {
        benchMarkRunnable(
                BenchMarkHashUtils.class,
                BenchMarkHashUtils::getHash,
                Duration.ofMinutes(20),
                Duration.ofMinutes(5)
        );
    }

    /**
     * @return a hash value using varags.
     */
    @Benchmarked
    private static int getHash() {
        return HashCodeUtil.hashCode(RANDOM.nextInt(), RANDOM.nextInt());
    }

}
