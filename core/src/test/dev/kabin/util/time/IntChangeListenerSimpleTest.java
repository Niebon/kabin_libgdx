package dev.kabin.util.time;

import dev.kabin.util.events.IntChangeListenerSimple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IntChangeListenerSimpleTest {

    @Test
    void set() {
        var c = new IntChangeListenerSimple();
        c.set(1);
        Assertions.assertEquals(0, c.last());

        // Last should still be zero after the second call to set(1).
        c.set(1);
        Assertions.assertEquals(0, c.last());
    }

}