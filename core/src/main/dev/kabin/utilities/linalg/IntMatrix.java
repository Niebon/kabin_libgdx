package dev.kabin.utilities.linalg;

import dev.kabin.utilities.functioninterfaces.IntBinaryOperator;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represent a matrix of float data using a single underlying float array.
 */
public final class IntMatrix {

    private final int[] data;
    private final int width;
    private final int height;

    public IntMatrix(int width, int height) {
        data = new int[width * height];
        this.width = width;
        this.height = height;
    }

    public static IntMatrix identityMatrix(int height, int width) {
        return IntMatrix.of(width, height, (i, j) -> (i != j ? 0 : 1));
    }

    public static IntMatrix nullMatrix(int height, int width) {
        return new IntMatrix(width, height);
    }

    /**
     * A lazily evaluated computation result of multiplying matrix A with B.
     *
     * @param A the left matrix.
     * @param B the right matrix.
     * @return a lazy unmeoized result of the matrix multiplication. This result will throw if evaluated outside its bounds.
     */
    public static IntBinaryOperator lazyMultiplicationResult(IntMatrix A, IntMatrix B) {
        if (A.width != B.height) {
            throw new IllegalArgumentException("The multiplication is ill-defined because A.getWidth() != B.getWidth().");
        }
        return (i, j) -> {
            int sum = 0;
            for (int k = 0; k < A.width; k++) {
                sum += A.get(i, k) * B.get(k, j);
            }
            return sum;
        };
    }

    /**
     * An eagerly evaluated computation result of multiplying matrix A and B realized as a new {@link IntMatrix}.
     *
     * @param A the left matrix.
     * @param B the right matrix.
     * @return a new {@link IntMatrix}
     */
    @Contract("_,_->new")
    public static IntMatrix multiplicationResult(IntMatrix A, IntMatrix B) {
        return IntMatrix.of(B.width, A.height, IntMatrix.lazyMultiplicationResult(A, B));
    }

    public static IntMatrix of(int width, int height, IntBinaryOperator dataSupplier) {
        var floatMatrix = new IntMatrix(width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                floatMatrix.set(i, j, dataSupplier.eval(i, j));
            }
        }
        return floatMatrix;
    }

    /**
     * @param i     the row number.
     * @param j     the column number.
     * @param value the value.
     */
    public void set(int i, int j, int value) {
        data[width * i + j] = value;
    }

    /**
     * @param i the row number.
     * @param j the column number.
     * @return the float value stored under the given index. That is aij for a matrix A.
     */
    public int get(int i, int j) {
        return data[width * i + j];
    }

    public void decrement(int i, int j) {
        int index = width * i + j;
        data[index] = data[index] - 1;
    }

    public void increment(int i, int j) {
        int index = width * i + j;
        data[index] = data[index] + 1;
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        var content = IntStream.range(0, height).mapToObj(
                j -> IntStream.range(0, width)
                        .mapToObj(i -> get(i, j))
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        ).collect(Collectors.joining("\n"));
        return "IntMatrix{\n" + content + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntMatrix that = (IntMatrix) o;
        return width == that.width &&
                height == that.height &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(width, height);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * Returns the underlying int data of this matrix. Mutating this, will mutate the matrix!
     */
    public int[] data(){
        return data;
    }

    public void clear(){
        Arrays.fill(data, 0);
    }


}
