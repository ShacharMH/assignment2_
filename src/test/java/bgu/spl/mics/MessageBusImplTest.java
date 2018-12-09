package bgu.spl.mics;

import bgu.spl.mics.application.services.LogisticsService;
import bgu.spl.mics.application.services.SellingService;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    //for the tests i'll need microservices,events,broadcasts and a couple of threads.
    private MessageBusImpl bus = null;
    MicroService ExampleService;
    MicroService ExampleSendService;
    MicroService BroadcastListenerService;
    ExampleEvent Delivery;
    ExampleBroadcast broadcast;
    Thread T1;
    Thread T2;
    Thread T3;



    @Before
    public void setUp(){
        bus = bus.getInstance();
        String[] a={"4"};
        String[] b={"broadcast"};
        ExampleService =new ExampleEventHandlerService("broadcast",a);
        ExampleSendService =new ExampleMessageSenderService("AMIR",b);
        BroadcastListenerService =new ExampleBroadcastListenerService("AMIR",a);
        Delivery=new ExampleEvent("lalala");
         broadcast=new ExampleBroadcast("777");
        Thread T1;
        Thread T2;
        Thread T3;
    }//

    @After

    @Test
    public void getInstance() {
        bus = MessageBusImpl.getInstance();
        assertNotNull(bus);

    }


    @Test
    public void subscribeEvent() {//you subscribe to an event of a certain type
        bus.subscribeEvent(ExampleEvent.class,ExampleService);
    }


    @Test
    public void subscribeBroadcast() {
        bus.subscribeBroadcast(ExampleBroadcast.class,ExampleSendService);
    }

    @Test
    public void complete() {//complete for a specific event
        bus.register(ExampleService);
        bus.subscribeEvent(ExampleEvent.class,ExampleService);
       // bus.sendEvent(Delivery);
        String A="DFFF";
        bus.complete(Delivery,A);
    }

    @Test
    public void sendBroadcast() {
        setUp();
        bus.register(ExampleService);
        bus.register(ExampleSendService);
        bus.subscribeBroadcast(ExampleBroadcast.class,ExampleService);
        bus.sendBroadcast(broadcast);
    }

    @Test
    public void sendEvent() {
        setUp();
        bus.register(ExampleService);
        bus.subscribeEvent(ExampleEvent.class,ExampleService);
       // bus.subscribeEvent(ExampleEvent.class,ExampleSendService);
       // bus.sendEvent(Delivery);
    }

    @Test
    public void register() {
        bus.register(ExampleSendService);
        bus.register(ExampleService);
    }

    @Test
    public void unregister() {
        bus.unregister(ExampleService);
    }

    @Test
    public void awaitMessage() {
        bus.register(ExampleService);
        bus.register(ExampleSendService);
        bus.subscribeEvent(ExampleEvent.class, ExampleService);
       // bus.subscribeEvent(ExampleEvent.class,ExampleSendService);
       // bus.sendEvent(Delivery);
        ExampleEvent D1=new ExampleEvent("EVENT1");
        ExampleEvent D2=new ExampleEvent("EVENT2");
        //bus.sendEvent(D1);
       // bus.sendEvent(D2);
        Runnable runnable2=()-> {
            try {
                Message get = bus.awaitMessage(ExampleService);//name of service is broadcast
                if (get!=null) System.out.println("awaitmessage returns not null");
                if (get==null) System.out.println("awaitmessage returns null");
            } catch (InterruptedException e) {
                System.out.println("awaiting interupted");
            }
        };
        Runnable runnable1 = () -> {
            try {
                Message get2 = bus.awaitMessage(ExampleSendService);//name of service is AMIR
                if (get2!=null) System.out.println("awaitmessage2 returns not null");
                if (get2==null) System.out.println("awaitmessage returns null");
            }
            catch (InterruptedException e){
                System.out.println("awaiting interupted");
            }
        };
        T1 = new Thread(runnable1);
        T2= new Thread(runnable2);
        T1.start();
        T2.start();
    }
}