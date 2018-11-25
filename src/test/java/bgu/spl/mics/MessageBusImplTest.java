package bgu.spl.mics;

import org.junit.After;
import org.junit.Before;
import java.util.Queue;
import java.util.Vector;

import org.junit.Test;

import static org.junit.Assert.*;

///AMIR

public class MessageBusImplTest implements MessageBus {
    private MessageBusImpl Bus;
    private Vector<Queue> listOfQueues;
    @Before
    public void setUp() throws Exception {
        this.Bus=createBus();
        this.listOfQueues=new Vector<Queue>();
    }

    protected MessageBusImpl createBus(){
        return new MessageBusImpl();
    }

    @Test
    //command, divided into command of "subscribe event" of the micro service(insert it into the queue of the micro service)
    //pre: @param "?"!=null
    //pre:,@param m!=null
    //POST:@MicroService m has defined a callback according to the event
    <T> boolean  checkEvent(Class a ,extends Event<T>> Message) {
        return a==null;
    }

    @Test
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

    }

    @Test
    public <T> void complete(Event<T> e, T result) {

    }

    @Test
    public void sendBroadcast(Broadcast b) {

    }

    @Test
    public <T> Future<T> sendEvent(Event<T> e) {
        return null;
    }

    @Test
    public void register(MicroService m) {

    }

    @Test
    public void unregister(MicroService m) {

    }

    @Test
    public Message awaitMessage(MicroService m) throws InterruptedException {
        return null;
    }

    @After
    public void tearDown() throws Exception {
    }
}