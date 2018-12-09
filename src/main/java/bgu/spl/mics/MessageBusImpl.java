package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleEvent;
import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private final ConcurrentHashMap<Class <? extends Event>,ConcurrentLinkedQueue<MicroService>> hashEventToMicroServiceQueue;//for each type of event, queue of microservices that handle it
	private final ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedQueue<MicroService>> hashBroadcastToMicroServicesQueue;//for each type of broadcast, queue of microservices that handle it
	private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> hashMicroServiceToMessagesQueue;//each micro-service and its message queue
    private final ConcurrentHashMap<Event,Future> MapBetweenEventAndFutureObj;//addition, for finding the event its corresponding future object in method "complete"
    private final ConcurrentLinkedQueue<Class <? extends Event>> listOfTypesOfEvents;//every type of event, for "unregister" method
    private final ConcurrentLinkedQueue<Class<? extends Broadcast>> listOfTypesOfBroadcasts;//every type of broadcast, for "unregister" method
    private ExampleEvent exampleEvent=new ExampleEvent("lalala");//just for testing
    private Message message;//just for tests

    private static class BusHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

	public static MessageBusImpl getInstance(){
		return BusHolder.instance;
	}

	private MessageBusImpl () {
		this.hashBroadcastToMicroServicesQueue = new ConcurrentHashMap<>();
		this.hashEventToMicroServiceQueue = new ConcurrentHashMap<>();
		this.hashMicroServiceToMessagesQueue = new ConcurrentHashMap<>();
        this.MapBetweenEventAndFutureObj= new ConcurrentHashMap<>();
        this.listOfTypesOfEvents=new ConcurrentLinkedQueue<>();
        this.listOfTypesOfBroadcasts=new ConcurrentLinkedQueue<>();
	}





	/**
	 * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
	 * <p>
	 * @param <T>  The type of the result expected by the completed event.
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service..
	 */


    //see if event already exists.  if so, add the ms to the event's queue. else, push the new couple (event, ms), while creating the ms's queue for the event.
	 public   <T> void subscribeEvent (Class <? extends Event<T>> type, MicroService m) {
             if (hashEventToMicroServiceQueue.containsKey(type)) {//if event already exists
                 ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = hashEventToMicroServiceQueue.get(type);//get the queue of microservices for this type of event
                 EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
             }
             else {
                 ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = new ConcurrentLinkedQueue<>();//create queue of microservices for this event
                 EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
                 hashEventToMicroServiceQueue.put(type, EventsMicroServiceList);//add this new pair to the list of <events,microservices that handle these events>
                 listOfTypesOfEvents.add(type);
             }

     }




	//The same as above(subscribeEvent), for broadcasts
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {//problem with compilation

             if (hashBroadcastToMicroServicesQueue.containsKey(type)) {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = hashBroadcastToMicroServicesQueue.get(type);
                 BroadcastsMicroserviceList.add(m);
             } else {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = new ConcurrentLinkedQueue<>();
                 BroadcastsMicroserviceList.add(m);
                 hashBroadcastToMicroServicesQueue.put(type, BroadcastsMicroserviceList);
                 listOfTypesOfBroadcasts.add(type);
             }

    }



//Resolve the event's corresponding future object,AFTER it was sent back to the micro-service who sent the event in the first place
//recieving micro-service uses this method
	public <T> void complete(Event<T> e, T result) {
        Future EventsAnswer=MapBetweenEventAndFutureObj.get(e);
        EventsAnswer.resolve(result);
	}


	public void sendBroadcast(Broadcast b) {//no need to synchronize,each broadcast type has its own queue of micro-services
		ConcurrentLinkedQueue<MicroService> ListOfMicros=hashBroadcastToMicroServicesQueue.get(b.getClass());
		for (MicroService item:ListOfMicros) {//for every microservice that has subscribed to get this broadcast
            LinkedBlockingQueue<Message> addBroadcastHere = hashMicroServiceToMessagesQueue.get(item);//get messagequeue of microservice
            addBroadcastHere.add(b);//add the broadcast to the messagequeue
        }
	}

	//Insert event to messagequeue of micro-service(round robin), return future object to micro-service that sent this event
    //need to synchronize this fellow
	public synchronized  <T> Future<T>  sendEvent(Event<T> e) {
	     if (hashEventToMicroServiceQueue.containsKey(e.getClass())) {
             ConcurrentLinkedQueue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(e.getClass());//get the queue assigned to this type of event
             MicroService handleEvent = QueueOfEvent.remove();//holds the micro service that will get the event and handle it.
             System.out.println(" the name of the micro service that will handle is "+ handleEvent.getName());//test i added
            LinkedBlockingQueue<Message> QueueOfMicroservice = hashMicroServiceToMessagesQueue.get(handleEvent);//get the message queue of the corresponding micro-service
             if(QueueOfMicroservice==null) System.out.println("the queue is null");
             QueueOfMicroservice.add(e);//"add" and not "put", this is a non-blocking method,
             QueueOfEvent.add(handleEvent);//MicroService is returned to the tail of the queue of the event, round robin invariant is maintained.
             Future<T> result = new Future<>();//create future object that will hold the answer to this event
             MapBetweenEventAndFutureObj.put(e,result);//put event and future object in map
             return result;
         }
	     else
	         return null;
	}


	public void register(MicroService m) {
       LinkedBlockingQueue<Message> MessageList=new LinkedBlockingQueue<>();
		hashMicroServiceToMessagesQueue.put(m, MessageList);

    }


	public void unregister(MicroService m) {
        ConcurrentLinkedQueue<MicroService> erasedQueue;
        for (Class<? extends Event> type : listOfTypesOfEvents)//for every type of event, erase the micro service from its handling list
        {
            erasedQueue = hashEventToMicroServiceQueue.get(type);
            erasedQueue.remove(m);
        }
        for (Class<? extends Broadcast> type : listOfTypesOfBroadcasts) {//for every type of broadcast, erase the micro service from its handling list
            erasedQueue = hashBroadcastToMicroServicesQueue.get(type);
            erasedQueue.remove(m);
        }
        LinkedBlockingQueue erasedQueueOfMessages = hashMicroServiceToMessagesQueue.get(m);
        hashMicroServiceToMessagesQueue.remove(m, erasedQueueOfMessages);
    }

//need to understand the difference between waiting for a message and being in the middle of handling one
        public Message awaitMessage (MicroService m) throws InterruptedException {//need to use interupted mechanism
            if (!hashMicroServiceToMessagesQueue.containsKey(m)) {
                throw new IllegalStateException();
            }
            try {
                return  hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
            } catch (InterruptedException e) {
                System.out.println("wait message thread interupted");
                throw new InterruptedException();
            }
            //finally {
            //  Message handleNow = hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
            //    return handleNow;
            //  }

            //while(hashMicroServiceToMessagesQueue.isEmpty())
        }



//***************methods for tests
        public MicroService getMicroServiceOfEvent(Class <? extends Event> type){//ONLY FOR TESTS
            ConcurrentLinkedQueue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(type);//get the queue assigned to this type of event
            MicroService handleEvent = QueueOfEvent.remove();//holds the micro service that will get the event and handle it.
            return handleEvent;
	    }

        public MicroService getMicroServiceOfBroadcast(Class <? extends Broadcast> type){//ONLY FOR TESTS
            ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = hashBroadcastToMicroServicesQueue.get(type);//get the queue assigned to this type of broadcast
            MicroService handleEvent = BroadcastsMicroserviceList.remove();//holds the micro service that will get the event and handle it.
            return handleEvent;
        }

        public Future returnFutureOfEvent(Event event){//only for tests
	     return MapBetweenEventAndFutureObj.get(event);
        }
    }


