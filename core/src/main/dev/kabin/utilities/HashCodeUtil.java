package dev.kabin.utilities;

/**
 * This is offered as an alternative to {@link java.util.Objects#hash(Object...) Objects.hash(Object... values)}.
 * The method {@code Objects.hash(Object... values)} takes an array. If we can help it, then we want to not allocate an
 * array at all.
 * <p>
 * This class offers implementations to build hash-codes for composite objects. Instead of taking parameters
 * <pre>value1,value2,value3,...</pre>
 * we take the corresponding hash-codes as parameters
 * <pre>hashcode1,hashcode2,hashcode3,...</pre>
 * Of course, these are implemented only for a finite number of parameters.
 * <p>
 * When implemented, calling
 * <pre> Objects.hashCode(value1,value2,...) </pre>
 * returns the same hash as calling
 * <pre> HashCodeUtil.hashCode(value1.hashCode(), value2.hashCode(), ...) </pre>
 */
public class HashCodeUtil {

    public static int hashCode(int hashCode1, int hashCode2) {
        return accumulate(init(hashCode1), hashCode2);
    }

    public static int hashCode(int hashCode1, int hashCode2, int hashCode3) {
        return accumulate(accumulate(init(hashCode1), hashCode2), hashCode3);
    }

    public static int hashCode(int hashCode1, int hashCode2, int hashCode3, int hashCode4) {
        return accumulate(accumulate(accumulate(init(hashCode1), hashCode2), hashCode3), hashCode4);
    }

    /**
     * A hash-code builder for a composite object.
     *
     * @param accumulatedHash   the accumulated hash-code.
     * @param hashCodeToBeAdded the hash-code of the element to be added.
     * @return 31 * accumulatedHash + hashCodeToBeAdded.
     */
    private static int accumulate(int accumulatedHash, int hashCodeToBeAdded) {
        return 31 * accumulatedHash + hashCodeToBeAdded;
    }

    /**
     * Helper method. To be used together with {@link #accumulate(int, int)}.
     * For example:
     * <pre>
     *     accumulate(init(value1.hashCode()), value2.hashCode());
     * </pre>
     * is equivalent to {@code Objects.hashCode(value1, value2)}.
     */
    private static int init(int initialHashCode) {
        return 31 + initialHashCode;
    }

}