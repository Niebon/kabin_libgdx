package dev.kabin.utilities.linalg;

import dev.kabin.utilities.Procedures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IntMatrixTest {


    @Test
    void creation() {
        var identityMatrix = IntMatrix.identityMatrix(3, 3);
        Procedures.forEachIntPairIn(
                0, 3,
                0, 3,
                (i, j) -> {
                    if (i == j) {
                        Assertions.assertEquals(1, identityMatrix.get(i, j), "Diagonal elements check.");
                    } else {
                        Assertions.assertEquals(0, identityMatrix.get(i, j), "Non-diagonal elements check.");
                    }
                }
        );
    }


    @Test
    void asymmetricalMatrix() {
        var identityMatrix = IntMatrix.identityMatrix(3, 2);
        System.out.println(identityMatrix);

        // Diagonal
        Assertions.assertEquals(1, identityMatrix.get(0,0));
        Assertions.assertEquals(1, identityMatrix.get(1,1));

        // Off diagonal
        Assertions.assertEquals(0, identityMatrix.get(1,0));
        Assertions.assertEquals(0, identityMatrix.get(2,0));
        Assertions.assertEquals(0, identityMatrix.get(0,1));
        Assertions.assertEquals(0, identityMatrix.get(2,1));
    }

    @Test
    void trivialMultiplication() {
        var A = IntMatrix.nullMatrix(2, 2);
        var B = IntMatrix.nullMatrix(2, 3);
        var multiplicationResult = IntMatrix.multiplicationResult(A, B);
        Assertions.assertEquals(IntMatrix.nullMatrix(A.getHeight(), B.getWidth()), multiplicationResult);
    }

    @Test
    void identityMatrixIsIdentityMatrix(){
        var A = IntMatrix.nullMatrix(6, 6);
        Procedures.forEachIntPairIn(
                0, 6,
                0, 6,
                (i,j) -> A.set(i,j, (int) (Math.random() * 10)));
        System.out.println(A);
        Assertions.assertEquals(A, IntMatrix.multiplicationResult(A, IntMatrix.identityMatrix(6, 6)));
        Assertions.assertEquals(A, IntMatrix.multiplicationResult(IntMatrix.identityMatrix(6, 6), A));
    }

    @Test
    void nonTrivialMultiplication() {
        var A = IntMatrix.nullMatrix(3, 2);
        var B = IntMatrix.nullMatrix(2, 3);
        var multiplicationResult = IntMatrix.multiplicationResult(A, B);
        Assertions.assertEquals(IntMatrix.nullMatrix(A.getHeight(), B.getWidth()), multiplicationResult);
    }

    @Test
    void increment(){
        var A = IntMatrix.nullMatrix(3, 2);
        A.increment(1, 2);
        Assertions.assertEquals(1, A.get(1, 2));
    }

    @Test
    void decrement(){
        var A = IntMatrix.nullMatrix(3, 2);
        A.decrement(1, 2);
        Assertions.assertEquals(-1, A.get(1, 2));
    }


}