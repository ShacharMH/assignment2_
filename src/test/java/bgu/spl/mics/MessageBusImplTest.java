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
    private MicroService ExampleService;
    private MicroService ExampleSendService;
    private MicroService BroadcastListenerService;
    private ExampleEvent Delivery;
    private ExampleBroadcast broadcast;
    private Thread T1;
    private Thread T2;
    private Thread T3;
    private Thread T4;



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
        MicroService m=bus.getMicroServiceOfEvent(ExampleEvent.class);
        assertEquals(ExampleService,m);
    }


    @Test
    public void subscribeBroadcast() {
        bus.subscribeBroadcast(ExampleBroadcast.class, ExampleService);
        MicroService m=bus.getMicroServiceOfBroadcast(ExampleBroadcast.class);
        assertEquals(ExampleService,m);

    }


    @Test
    public void complete() {//complete for a specific event
        bus.register(ExampleService);
        bus.subscribeEvent(ExampleEvent.class,ExampleService);
        bus.sendEvent(Delivery);
        String A="DFFF";
        bus.complete(Delivery,A);
        Future f=bus.returnFutureOfEvent(Delivery);
        assertEquals(A,f.getResult());
    }

    @Test
    public void sendBroadcast() {
        setUp();
        bus.register(ExampleService);
        bus.register(ExampleSendService);
        bus.subscribeBroadcast(ExampleBroadcast.class,ExampleService);
        bus.sendBroadcast(broadcast);
        Message a=null;
        try {
            a = bus.awaitMessage(ExampleService);
        }
        catch (InterruptedException e){}
        assertEquals(broadcast,a);

    }

    @Test
    public void sendEvent() {
        setUp();
        bus.register(ExampleService);
        bus.subscribeEvent(ExampleEvent.class,ExampleService);
        bus.subscribeEvent(ExampleEvent.class,ExampleSendService);
        bus.sendEvent(Delivery);
        Message a=null;
        try {
             a = bus.awaitMessage(ExampleService);
        }
        catch (InterruptedException e){};
        assertEquals(Delivery,a);


    }

    @Test
    public void register() {//works
        bus.register(ExampleSendService);
        bus.register(ExampleService);
    }

    @Test
    public void unregister() {//works
        bus.register(ExampleService);
        bus.unregister(ExampleService);
        try {
            Message m=bus.awaitMessage(ExampleService);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IllegalStateException e){ System.out.println("unregister test works");}
    }

    @Test
    public void awaitMessage() {
        bus.register(ExampleService);
        bus.register(ExampleSendService);
        bus.subscribeEvent(ExampleEvent.class, ExampleService);
        bus.sendEvent(Delivery);
        ExampleEvent D1=new ExampleEvent("EVENT1");
        ExampleEvent D2=new ExampleEvent("EVENT2");
        bus.sendEvent(D1);
        bus.sendEvent(D2);

            try {
                Message get2 = bus.awaitMessage(ExampleSendService);
                if (get2 == null) System.out.println("awaitmessage11 returns shit");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        Runnable runnable1 = () -> {
            try {
                Message get2 = bus.awaitMessage(ExampleService);//name of service is AMIR
                if (get2==Delivery) System.out.println("awaitmessage3 returns Delivery");
                if (get2==D1) System.out.println("awaitmessage3 returns D1");
                if (get2==D2) System.out.println("awaitmessage3 returns D2");
            }
            catch (InterruptedException e){
                System.out.println("awaiting interupted");
            }
        };
        Runnable runnable2 = () -> {
            try {
                Message get2 = bus.awaitMessage(ExampleService);//name of service is AMIR
                if (get2==Delivery) System.out.println("awaitmessage2 returns Delivery");
                if (get2==D1) System.out.println("awaitmessage2 returns D1");
                if (get2==D2) System.out.println("awaitmessage2 returns D2");
            }
            catch (InterruptedException e){
                System.out.println("awaiting interupted");
            }
        };
        T2=new Thread(runnable2);
        T1 = new Thread(runnable1);
        //T3=new Thread(runnable0);
        //T3.start();
        T2.start();
        T1.start();

    }
}