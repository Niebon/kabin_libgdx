package dev.kabin.utilities.linalg;

import dev.kabin.utilities.Procedures;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FloatMatrixTest {


    @Test
    void creation() {
        var identityMatrix = FloatMatrix.identityMatrix(3, 3);
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
        var identityMatrix = FloatMatrix.identityMatrix(3, 2);
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
        var A = FloatMatrix.nullMatrix(2, 2);
        var B = FloatMatrix.nullMatrix(2, 3);
        var multiplicationResult = FloatMatrix.multiplicationResult(A, B);
        Assertions.assertEquals(FloatMatrix.nullMatrix(A.getHeight(), B.getWidth()), multiplicationResult);
    }

    @Test
    void identityMatrixIsIdentityMatrix(){
        var A = FloatMatrix.nullMatrix(6, 6);
        Procedures.forEachIntPairIn(
                0, 6,
                0, 6,
                (i,j) -> A.set(i,j, (float) Math.random()));
        System.out.println(A);
        Assertions.assertEquals(A, FloatMatrix.multiplicationResult(A, FloatMatrix.identityMatrix(6, 6)));
        Assertions.assertEquals(A, FloatMatrix.multiplicationResult(FloatMatrix.identityMatrix(6, 6), A));
    }

    @Test
    void nonTrivialMultiplication() {
        var A = FloatMatrix.nullMatrix(3, 2);
        var B = FloatMatrix.nullMatrix(2, 3);
        var multiplicationResult = FloatMatrix.multiplicationResult(A, B);
        Assertions.assertEquals(FloatMatrix.nullMatrix(A.getHeight(), B.getWidth()), multiplicationResult);
    }




}