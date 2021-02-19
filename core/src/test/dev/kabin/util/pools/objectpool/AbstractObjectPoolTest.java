package dev.kabin.util.pools.objectpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;

class AbstractObjectPoolTest {

    @Test
    void takeAndGiveBackObjects() {
        StringListPool pool = new StringListPool(10, i -> new ArrayList<>(), List::clear);
        List<String> takenObject = pool.borrow();
        pool.borrow();
        pool.giveBack(takenObject);
        Assertions.assertEquals(takenObject, pool.borrow());
    }

    @Test
    void takeAndGiveBackObjectsInTheMiddle() {
        StringListPool pool = new StringListPool(10, i -> new ArrayList<>(), List::clear);
        pool.borrow();
        List<String> borrowedObjectNo2 = pool.borrow();
        pool.borrow();
        pool.giveBack(borrowedObjectNo2);
        Assertions.assertEquals(borrowedObjectNo2, pool.borrow());
    }

    @Test
    void giveBackAllExcept() {
        int availableObjects = 10;
        StringListPool pool = new StringListPool(availableObjects, i -> new ArrayList<>(), List::clear);
        List<String> takenObject = pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.giveBackAllExcept(takenObject);

        for (int i = 0; i < availableObjects - 1; i++) {
            //noinspection SimplifiableAssertion: We actually want to disprove that they are the same object.
            Assertions.assertFalse(takenObject == pool.borrow());
        }
    }

    @Test
    void giveBackAllExceptLeavesTakenDataUntouched() {
        int availableObjects = 10;
        StringListPool pool = new StringListPool(availableObjects, i -> new ArrayList<>(), List::clear);
        List<String> takenObject = pool.borrow();
        takenObject.add("important data");
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.borrow();
        pool.giveBackAllExcept(takenObject);
        Assertions.assertEquals("important data", takenObject.get(0));
    }


    @Test
    void stressTestPool() {
        int availableObjects = 3;
        StringListPool pool = new StringListPool(availableObjects, i -> new ArrayList<>(), List::clear);

        List<String> specialBorrow = null;
        for (int test = 0; test < 1000; test++) {

            if (test % 3 == 0) {
                for (int i = 0; i < availableObjects - 1; i++) {
                    List<String> takenObject = pool.borrow();
                    try {
                        takenObject.add("important data: " + i);
                        takenObject.add("important data: " + i);
                    } catch (Exception e) {
                        System.out.println("Caught exception: " + i);
                        Assertions.fail("Caught exception...");
                    }
                }
            }
            if (test % 3 == 1) {
                if (specialBorrow != null) pool.giveBack(specialBorrow);
                specialBorrow = pool.borrow();
                specialBorrow.add("Special: " + test);
            }
            if (test % 3 == 2) {
                pool.giveBackAllExcept(specialBorrow);
                if (specialBorrow != null) Assertions.assertEquals("Special: " + (test - 1), specialBorrow.get(0));
            }
        }


    }

    @Test
    void testMetadata() {
        int availableObjects = 10;
        StringListPool pool = new StringListPool(availableObjects, i -> new ArrayList<>(), List::clear);
        Assertions.assertEquals(10, pool.remaining());
        pool.borrow();
        pool.borrow();
        Assertions.assertEquals(8, pool.remaining());
        Assertions.assertEquals(2, pool.taken());
    }

    static class StringListPool extends AbstractObjectPool<List<String>> {
        public StringListPool(int objectsAvailable, IntFunction<List<String>> mapper, Consumer<List<String>> clearDataProcedure) {
            super(objectsAvailable, mapper, clearDataProcedure);
        }
    }
}