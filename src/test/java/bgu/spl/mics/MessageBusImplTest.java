package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

///AMIR

public class MessageBusImplTest implements MessageBus {

    @Before
    public void setUp() throws Exception {
        this.
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    //PRE: recieve some sort of an event, and a micro-service interested in handling it
    //POST:
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {

    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

    }

    @Override
    public <T> void complete(Event<T> e, T result) {

    }

    @Override
    public void sendBroadcast(Broadcast b) {

    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        return null;
    }

    @Override
    public void register(MicroService m) {

    }

    @Override
    public void unregister(MicroService m) {

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        return null;
    }
}