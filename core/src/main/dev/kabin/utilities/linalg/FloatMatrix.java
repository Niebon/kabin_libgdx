package dev.kabin.utilities.linalg;

import dev.kabin.utilities.HashCodeUtil;
import dev.kabin.utilities.functioninterfaces.BiIntToFloatFunction;
import dev.kabin.utilities.functioninterfaces.FloatUnaryOperation;
import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represent a matrix of float data using a single underlying float array.
 */
public final class FloatMatrix {

    private final float[] data;
    private final int width;
    private final int height;

    public FloatMatrix(int width, int height) {
        data = new float[width * height];
        this.width = width;
        this.height = height;
    }

    public static FloatMatrix identityMatrix(int height, int width) {
        return FloatMatrix.of(width, height, (i, j) -> (i != j ? 0 : 1));
    }

    public static FloatMatrix nullMatrix(int height, int width) {
        return new FloatMatrix(width, height);
    }

    /**
     * A lazily evaluated computation result of multiplying matrix A with B.
     *
     * @param A the left matrix.
     * @param B the right matrix.
     * @return a lazy unmeoized result of the matrix multiplication. This result will throw if evaluated outside its bounds.
     */
    public static BiIntToFloatFunction lazyMultiplicationResult(FloatMatrix A, FloatMatrix B) {
        if (A.width != B.height) {
            throw new IllegalArgumentException("The multiplication is ill-defined because A.getWidth() != B.getWidth().");
        }
        return (i, j) -> {
            float sum = 0;
            for (int k = 0; k < A.width; k++) {
                sum += A.get(i, k) * B.get(k, j);
            }
            return sum;
        };
    }

    /**
     * An eagerly evaluated computation result of multiplying matrix A and B realized as a new {@link FloatMatrix}.
     *
     * @param A the left matrix.
     * @param B the right matrix.
     * @return a new {@link FloatMatrix}
     */
    @Contract("_,_->new")
    public static FloatMatrix multiplicationResult(FloatMatrix A, FloatMatrix B) {
        return FloatMatrix.of(B.width, A.height, FloatMatrix.lazyMultiplicationResult(A, B));
    }

    public static FloatMatrix of(int width, int height, BiIntToFloatFunction dataSupplier) {
        var floatMatrix = new FloatMatrix(width, height);
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
    public void set(int i, int j, float value) {
        data[width * i + j] = value;
    }

    /**
     * @param i the row number.
     * @param j the column number.
     * @return the float value stored under the given index. That is aij for a matrix A.
     */
    public float get(int i, int j) {
        return data[width * i + j];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        int max = 10;
        var content = IntStream.range(0, Math.min(height, max)).mapToObj(
                j -> IntStream.range(0, Math.min(width, max)).mapToObj(
                        i -> {
                            // i = j = 10.
                            if (i == max - 1 && j == max - 1) {
                                return "  \u22F1";
                            }

                            // i != 10 and j != 10
                            if (i != max - 1 && j != max - 1) {
                                return String.format("%.2f", get(i, j));
                            }

                            if (i == max - 1) {
                                return "\u2026";
                            }

                            return "\u22EE   ";
                        }
                ).collect(Collectors.joining("  "))
        ).collect(Collectors.joining("\n"));
        return "FloatMatrix{\n" + content + "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatMatrix that = (FloatMatrix) o;
        return width == that.width &&
                height == that.height &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = HashCodeUtil.hashCode(width, height);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * Returns the underlying int data of this matrix. Mutating this, will mutate the matrix!
     */
    public float[] data() {
        return data;
    }

    public void modify(int i, int j, FloatUnaryOperation op) {
        int index = width * i + j;
        data[index] = op.eval(data[index]);
    }
}
