package bgu.spl.mics;

import bgu.spl.mics.Future;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import  java.lang.Thread;
import java.sql.Time;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


///SHACHAR - done
import static org.junit.Assert.*;

public class testFutureClassTest extends Future {//test

    private Future<Integer> future;

    @Before
    public void setUp() throws Exception {
        this.future = createFuture();
    }

    // @return a reference to a Future<T>
    private Future<Integer> createFuture() {
        return new Future<>();
    }

    @Test public void testGet_resultalreadyresolved() {
    // i expect from this function to give the result at all costs: if it's already there - then get it. if not - wait until it's there and get it.
        future.resolve(3);
        // now, the value "3" need to be in the field "result".
        int result = future.get();
        assertEquals(3, result);
        future.resolve(null);
    }

    @Test public void testGet_resultisnotyetresolved() {
        int result = future.get();
        // now get should wait until there is a value in the field result.
        Thread t = new Thread() {
            public void run() {
                try {
                    sleep(1000); // 1 sc
                    future.resolve(3);
                }
                catch (InterruptedException e) {}
            }
        };
        t.start();
        assertEquals(null,result);
        future.resolve(null);
    }

    @Test public void testResolve_42() {
        int num = 42;
        future.resolve(num);
        // now the value of the field _result_ should be 42.
        int get = future.get();
        assertEquals(num,get);
        future.resolve(null);
    }

    @Test public void testresolve_null() {
        try {
            future.resolve(null);
        }
        catch(Exception e) {
            Assert.fail("should throw exception. shouldn't be able to place _null_ in result");
        }
    }

    // this function should return true if there is a value in _result_.
    @Test public void testIsDone_resultisanumber() {
        future.resolve(5);
        assertEquals(true, future.isDone());
        future.resolve(null);
    }

    // null is default.
    @Test public void testIsDone_resultisnull() {
        future.resolve(null);
        assertEquals(false, future.isDone());
    }

    /* things to check _GetTimeout_:
        1. if the result is resolved, return the result
        2. if the result is not resolved, it waits the ,maximum amount of time to get the reslt:
            a. if the result is resolved prior max_time i time t, it returns the result in time t
            b. if the result is not resolved until max_time, returns null.
    */
    @Test public void testGetTimeout_resultisresolved() {
        future.resolve(42);
        Calendar start = Calendar.getInstance();
        int result = future.get(2, TimeUnit.SECONDS);
        Calendar end = Calendar.getInstance();
        assertEquals(42,result);
        future.resolve(null);
        System.out.println("start: " + start.toString());
        System.out.println("end: " + end.toString());
    }

    @Test public void testGetTimeout_resultisresolvedpriortomaxtime() {
        int result = future.get(2, TimeUnit.SECONDS);
        future.resolve(42);
        // should get to the next line with _42_ in _result_
        assertEquals(42, result);
        future.resolve(null);
    }

    @Test public void testGetTimeout_resultisnotresolved() {
        Calendar start = Calendar.getInstance();
        int result = future.get(2, TimeUnit.SECONDS);
        Calendar end = Calendar.getInstance();
        // should get to the next line with _42_ in _result_
        assertEquals(null, result);
        System.out.println("start: " + start.toString());
        System.out.println("end: " + end.toString());
    }

    @After
    public void tearDown() throws Exception {
        this.future = null;
    }
}