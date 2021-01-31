package benchmarks;

import com.google.common.base.Strings;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkUtil {

    private static final Logger LOGGER = Logger.getLogger(BenchmarkUtil.class.getName());

    private static final MathContext MATH_CONTEXT = new MathContext(3);

    protected static void benchMarkRunnable(Class<?> caller,
                                            Runnable r,
                                            Duration durationBenchmark,
                                            Duration durationWarmup) {

        // Warmup
        final long warmupTimeMinutes = durationWarmup.getSeconds() / 60;
        {
            LOGGER.info("Beginning warmup. Will warmup for %s minutes.".formatted(warmupTimeMinutes));
            final var duration = durationWarmup.toNanos();
            final var now = System.nanoTime();
            while (System.nanoTime() - now < duration) {
                r.run();
            }
            LOGGER.info("Finished warmup.");
        }

        // Benchmark
        {
            final long benchMarkTimeMinutes = durationBenchmark.getSeconds() / 60;
            LOGGER.info("Beginning benchmark. Will warmup for %s minutes.".formatted(benchMarkTimeMinutes));

            final var now = System.nanoTime();
            final var duration = durationBenchmark.toNanos();
            final var timePerPass = new Statistics();
            final var throughPut = new Statistics();

            long passes = 0;
            while (System.nanoTime() - now < duration) {
                final var before = System.nanoTime();
                r.run();
                timePerPass.add(System.nanoTime() - before);
                passes++;
                throughPut.add((double) passes / System.nanoTime());
            }


            final String benchMarkResult = """
                    Here are the results from the benchmark:
                    warmup time    [minutes] = %s
                    benchmark time [minutes] = %s
                    time/pass      [ns/pass] = %s
                    throughput     [pass/ns] = %s
                    passes         [pass]    = %s
                    """.formatted(
                    warmupTimeMinutes,
                    benchMarkTimeMinutes,
                    mean_std(timePerPass.getMean(), timePerPass.getStd()),
                    mean_std(throughPut.getMean(), throughPut.getStd()),
                    passes);

            LOGGER.info(benchMarkResult);

            final String pathString = "core/src/main/"
                    + caller.getCanonicalName().replace(".", "/")
                    + "_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_kkmmss"))
                    + ".txt";
            System.out.println(pathString);
            try {
                Files.write(Path.of(pathString), List.of(benchMarkResult), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String mean_std(double mean, double std) {
        String meanFormatted = String.valueOf(BigDecimal.valueOf(mean).round(MATH_CONTEXT));
        String meanStd = String.valueOf(BigDecimal.valueOf(std).round(MATH_CONTEXT));

        int powerOfTen = 1;
        int power = 0;
//        while (Math.abs(mean) < 1 || Math.abs(std) < 1) {
//            mean *= 10;
//            std *= 10;
//            powerOfTen *= 10;
//            power++;
//        }


        return powerOfTen > 1 ? "%sE%s ± %sE%s".formatted((long) mean, -power, (long) std, -power)
                : "%s ± %s".formatted(mean, std);
    }


    /*
     * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
     * See Online algorithms.
     */
    public static class Statistics {

        private double var, mean;
        private long count;


        public void add(double val) {
            count++;
            mean = mean + (val - mean) / count;

            // This prevents division by zero.
            if (count < 2) return;

            var = (var * (count - 1) + ((double) count / (count - 1)) * (val - mean) * (val - mean)) / count;
        }

        double getMean() {
            return mean;
        }

        double getStd() {
            return Math.sqrt(var);
        }

    }


    static class NumberRepresentation {

        private final List<Character> digits;

        // If number = x.xxxxxx, then comma position = 0.
        // If number = xxxxxx.x, then comma position = 5;
        private final int zeros; // relative to first digit.
        private final Sign sign;

        NumberRepresentation(List<Character> digits,
                             int commaPosition,
                             Sign sign) {
            this.digits = digits;
            this.zeros = commaPosition;
            this.sign = sign;
        }

        /**
         * Parses an real number on exponential form.
         * @param exponentialForm the real number to be parsed.
         * @return the corresponding representation.
         */
        static NumberRepresentation parse(String exponentialForm) {
            final String[] es = exponentialForm.split("E");
            final String digitsAndComma = es[0];
            final String exponent = es[1];
            final LinkedList<Character> digits = digitsAndComma.chars()
                    .filter(i -> i != '.')
                    .filter(i -> i != '-')
                    .mapToObj(i -> (char) i)
                    .collect(Collectors.toCollection(LinkedList::new));
            while (Character.valueOf('0').equals(digits.getLast())) {
                digits.removeLast();
            }
            final int zeros = Integer.parseInt(exponent);
            return new NumberRepresentation(digits, zeros, digitsAndComma.charAt(0) == '-' ? Sign.NEGATIVE : Sign.POSITIVE);
        }

        public String coefficient(int exponent) {
            if (exponent == zeros) {
                return Stream.builder()
                        .add(sign.getSymbol())
                        .add(String.valueOf(digits.get(0)))
                        .add("")
                        .add(digits.subList(1, digits.size()).stream().map(String::valueOf).collect(Collectors.joining()))
                        .build()
                        .map(String::valueOf)
                        .collect(Collectors.joining());
            }



//            int newCommaPosition = zeros - exponent;
//            var sb = new StringBuilder();
//            sb.append(sign == Sign.POSITIVE ? "" : "-");
//            if (newCommaPosition < 0) {
//                if (newCommaPosition == -1) {
//                    sb.append("0.0");
//                }
//                else {
//                    sb.append("0.").append(Strings.repeat("0", Math.abs(newCommaPosition)));
//                }
//                digits.forEach(sb::append);
//            }
//            else {
//                int digitsBeforeComma = Math.min(digits.size(), newCommaPosition) + 1;
//                if (digitsBeforeComma < digits.size()) {
//                    List<Character> digitsWithComma = new LinkedList<>(digits);
//                    digitsWithComma.add(digitsBeforeComma, '.');
//                    digitsWithComma.forEach(sb::append);
//                }
//                else {
//                    digits.forEach(sb::append);
//                    sb.append(Strings.repeat("0", newCommaPosition + 1 - digits.size()));
//                }
//            }
//            return sb.toString();
        }



        enum Sign {
            POSITIVE(""),
            NEGATIVE("-");

            private final String symbol;

            Sign(String symbol) {
                this.symbol = symbol;
            }

            String getSymbol(){
                return symbol;
            }
        }
    }


}
