package benchmarks;

import com.google.common.base.Strings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkUtil {

    private static final Logger LOGGER = Logger.getLogger(BenchmarkUtil.class.getName());

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

            final long now = System.nanoTime();
            final long duration = durationBenchmark.toNanos();
            final var timePerPass = new Statistics();
            final var throughPut = new Statistics();

            long passes = 0;
            while (System.nanoTime() - now < duration) {
                final long before = System.nanoTime();
                r.run();
                timePerPass.add(System.nanoTime() - before);
                passes++;
                throughPut.add((double) passes / (System.nanoTime() - now));
            }


            final String benchMarkResult = """
                    Here are the results from the benchmark:
                    warmup     [minutes] = %s
                    benchmark  [minutes] = %s
                    time/pass  [ns/pass] = %s
                    throughput [pass/ns] = %s
                    passes     [pass]    = %s
                    """.formatted(
                    warmupTimeMinutes,
                    benchMarkTimeMinutes,
                    formatMeanAndStd(timePerPass.getMean(), timePerPass.getStd()),
                    formatMeanAndStd(throughPut.getMean(), throughPut.getStd()),
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

    private static String formatMeanAndStd(double mean, double std) {
        String meanFormatted = new DecimalFormat("0.######E0").format(mean);
        String stdFormatted = new DecimalFormat("0.######E0").format(std);
        return NumberRepresentation.parse(meanFormatted).coefficient(0) +
                "±" +
                NumberRepresentation.parse(stdFormatted).coefficient(0);
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
        private final int zeros; // A negative if relative to first significant digit, or positive if relative to last significant digit.
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
         *
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
            final int exponentialShift = zeros - exponent;
            if (exponentialShift == 0) {
                return Stream.builder()
                        .add(sign.getSymbol())
                        .add(String.valueOf(digits.get(0)))
                        .add(".")
                        .add(digits.subList(1, digits.size()).stream().map(String::valueOf).collect(Collectors.joining()))
                        .build()
                        .map(String::valueOf)
                        .collect(Collectors.joining());
            } else if (exponentialShift < 0) {
                return Stream.builder().add(sign.getSymbol())
                        .add("0")
                        .add(".")
                        .add(Strings.repeat("0", Math.abs(exponentialShift + 1)))
                        .add(digits.stream().map(String::valueOf).collect(Collectors.joining()))
                        .build()
                        .map(String::valueOf)
                        .collect(Collectors.joining());
            } else {
                int zerosToAppend = Math.max(exponentialShift + 1 - digits.size(), 0);
                if (zerosToAppend == 0) {
                    return Stream.builder().add(sign.getSymbol())
                            .add(digits.subList(0, exponentialShift + 1).stream().map(String::valueOf).collect(Collectors.joining()))
                            .add(exponentialShift + 1 == digits.size() ? "" : ".")
                            .add(digits.subList(exponentialShift + 1, digits.size()).stream().map(String::valueOf).collect(Collectors.joining()))
                            .build()
                            .map(String::valueOf)
                            .collect(Collectors.joining());
                } else {
                    return Stream.builder().add(sign.getSymbol())
                            .add(digits.stream().map(String::valueOf).collect(Collectors.joining()))
                            .add(Strings.repeat("0", zerosToAppend))
                            .build()
                            .map(String::valueOf)
                            .collect(Collectors.joining());
                }
            }
        }


        enum Sign {
            POSITIVE(""),
            NEGATIVE("-");

            private final String symbol;

            Sign(String symbol) {
                this.symbol = symbol;
            }

            String getSymbol() {
                return symbol;
            }
        }
    }


}
