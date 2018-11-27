package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

///SHACHAR
public class MicroServiceTest {

    private MicroService ms;
    private Event event;
    private Broadcast broadcast;
    private Callback callback = (event) -> System.out.println(event.toString());

    @Before
    public void testsetUp() throws Exception {
    }

    @After
    public void testtearDown() throws Exception {
    }

    @Test//:/i want to see that the event and its corresponding callback are stored together,
    // and that the output of the the callback is the address of the event in memory.
    public void testsubscribeEvent_insertingnotnullevent() {
        try {
            ms.subscribeEvent(event.getClass(), callback);
        } catch (Exception e) {
            fail("something went wrong, threw exception while shouldn't have.");
        }
    }

    public void testsubsctibeEvent_eventisnull() {
        boolean thrown = false;
        try {
            ms.subscribeEvent(null, callback);
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test // trying to insert a null broadcast
    public void testsubscribeBroadcast_broadcastisnull() {
        boolean thrown = false;
        try {
            ms.subscribeBroadcast(null, callback);
        } catch (Exception e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testsendEvent() {
    }

    @Test
    public void testsendBroadcast() {
    }

    @Test
    public void testcomplete() {
    }

    @Test
    public void testterminate() {
    }

    @Test
    public void testgetName() {
    }

    @Test
    public void testrun() {

    }
}