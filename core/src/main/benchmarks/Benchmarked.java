package benchmarks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Tag interface. Slap on a method that is to be subject to a benchmark.
 */
@Target(ElementType.METHOD)
public @interface Benchmarked {
}
