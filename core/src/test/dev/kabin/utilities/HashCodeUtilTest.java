package dev.kabin.utilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Random;

class HashCodeUtilTest {


    @Test
    void stressTest() {
        final int trails = 10_000_000;
        final Random r = new Random(0);
        for (int trail = 0; trail < trails; trail++) {
            int hashCode1 = r.nextInt();
            int hashCode2 = r.nextInt();
            int hashCode3 = r.nextInt();
            int hashCode4 = r.nextInt();
            Assertions.assertEquals(Objects.hash(hashCode1, hashCode2), HashCodeUtil.hashCode(hashCode1, hashCode2));
            Assertions.assertEquals(Objects.hash(hashCode1, hashCode2, hashCode3), HashCodeUtil.hashCode(hashCode1, hashCode2, hashCode3));
            Assertions.assertEquals(Objects.hash(hashCode1, hashCode2, hashCode3, hashCode4), HashCodeUtil.hashCode(hashCode1, hashCode2, hashCode3, hashCode4));
        }
    }


}