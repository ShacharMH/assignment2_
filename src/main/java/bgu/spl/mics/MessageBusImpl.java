package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleEvent;
import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	private final ConcurrentHashMap<MicroService, ArrayBlockingQueue<Message>> hashMicroServiceToMessagesQueue;//each micro-service and its message queue
	//private final ConcurrentHashMap<MicroService,Queue<Integer>> listOfMessagesMicroServiceSubscribedTo;//probably unnecessary. addition, helps to know where else to delete microservice from
    private final ConcurrentHashMap<Event,Future> MapBetweenEventAndFutureObj;//addition
   // private final ConcurrentHashMap<Integer,Class <? extends Event>> MapBetweenNumberAndEvent;//addition,probably unnecessary
    //private final ConcurrentHashMap<Integer,Class<? extends Broadcast>> MapBetweenNumberAndBroadcast;//addition/probably unnecessary
    //private volatile Integer  EventCount; probably unnecessary
    //private volatile Integer  BroadcastCount; probably unnecessary
    private final ConcurrentLinkedQueue<Class <? extends Event>> listOfTypesOfEvents;
    private final ConcurrentLinkedQueue<Class<? extends Broadcast>> listOfTypesOfBroadcasts;
    private ExampleEvent exampleEvent=new ExampleEvent("lalala");//just for testing
    private Message message;

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
		//this.listOfMessagesMicroServiceSubscribedTo = new ConcurrentHashMap<>();//POSITIVE IDENTIFIER FOR EVENT,NEGATIVE FOR BROADCAST
       // this.MapBetweenNumberAndEvent= new ConcurrentHashMap<>();
       // this.MapBetweenNumberAndBroadcast= new ConcurrentHashMap<>();
        this.MapBetweenEventAndFutureObj= new ConcurrentHashMap<>();
        //this.EventCount=1;
       // this.BroadcastCount=1;
        this.listOfTypesOfEvents=new ConcurrentLinkedQueue<>();
        this.listOfTypesOfBroadcasts=new ConcurrentLinkedQueue<>();
	}

//



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
                 // MapBetweenNumberAndEvent.put(EventCount, type);//add the event an identifier
                 // Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                 //M.add(EventCount);//event identifier is in micro-service's list
                 // EventCount++;//update event counter, synchronized

             }
             else {
                 ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = new ConcurrentLinkedQueue<MicroService>();//create queue of microservices for this event
                 EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
                 hashEventToMicroServiceQueue.put(type, EventsMicroServiceList);//add this new pair to the list of <events,microservices that handle these events>
                 //MapBetweenNumberAndEvent.put(EventCount, type);//add the event an identifier
                // Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                // M.add(EventCount);//event identifier is in micro-service's list
               //  EventCount++;//update event counter, synchronized
                 listOfTypesOfEvents.add(type);
             }

     }




	//The same as above(subscribeEvent), for broadcasts
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {//problem with compilation

             if (hashBroadcastToMicroServicesQueue.containsKey(type)) {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = hashBroadcastToMicroServicesQueue.get(type);
                 BroadcastsMicroserviceList.add(m);
                 //Queue<Message> M=listOfMessagesMicroServiceSubscribedTo.get(m);//Adding broadcast to list of broadcast/events
                 //M.add(type);//not sure why there is a compilation problem
                // MapBetweenNumberAndBroadcast.put(BroadcastCount,type);//add broadcast an identifier
               //  Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                // M.add(BroadcastCount * (-1));//broadcast identifier is in micro-service's list
                // BroadcastCount++;//update broadcast counter, synchronized


             } else {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = new ConcurrentLinkedQueue<MicroService>();
                 BroadcastsMicroserviceList.add(m);
                 hashBroadcastToMicroServicesQueue.put(type, BroadcastsMicroserviceList);
                // MapBetweenNumberAndBroadcast.put(BroadcastCount,type);//add broadcast identifier
                // Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                // M.add(BroadcastCount * (-1));//broadcast identifier is in micro-service's list
               // BroadcastCount++;//update broadcast counter, synchronized
                 listOfTypesOfBroadcasts.add(type);
             }

    }



//Resolve the event's corresponding future object,AFTER it was sent back to the micro-service who sent the event in the first place
//recieving micro-service uses this method
	public <T> void complete(Event<T> e, T result) {
        Future EventsAnswer=MapBetweenEventAndFutureObj.get(e);
        EventsAnswer.resolve(result);
	}


	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> ListOfMicros=hashBroadcastToMicroServicesQueue.get(b.getClass());
		for (MicroService item:ListOfMicros) {//for every microservice that has subscribed to get this broadcast
            ArrayBlockingQueue<Message> addBroadcastHere = hashMicroServiceToMessagesQueue.get(item);//get messagequeue of microservice
            addBroadcastHere.add(b);//add the broadcast to the messagequeue
        }
	}

	//Insert event to messagequeue of micro-service(round robin), return future object to micro-service that sent this event
	public <T> Future<T> sendEvent(Event<T> e) {
	     if (hashEventToMicroServiceQueue.containsKey(e.getClass())) {
             ConcurrentLinkedQueue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(e.getClass());//get the queue assigned to this type of event
             MicroService handleEvent = QueueOfEvent.remove();//holds the micro service that will get the event and handle it.
             System.out.println(" the name of the micro service that will handle is "+ handleEvent.getName());//test i added
             ArrayBlockingQueue<Message> QueueOfMicroservice = hashMicroServiceToMessagesQueue.get(handleEvent);//get the message queue of the corresponding micro-service
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
        ArrayBlockingQueue<Message> MessageList=new ArrayBlockingQueue<Message>(100);
		//Queue<Integer> SubscribeList=null;//make a queue for all the events/broadcasts m will subscribe to-problematic solution
		hashMicroServiceToMessagesQueue.put(m, MessageList);
		//listOfMessagesMicroServiceSubscribedTo.put(m,SubscribeList);
       // MessageList.add(exampleEvent);
    }


	public void unregister(MicroService m) {
	     ConcurrentLinkedQueue<MicroService> erasedQueue;
            for(Class <? extends Event> type:listOfTypesOfEvents)//for every type of event, erase the micro service from its handling list
            {
                erasedQueue=hashEventToMicroServiceQueue.get(type);
                erasedQueue.remove(m);
            }
            for(Class <? extends Broadcast> type:listOfTypesOfBroadcasts){//for every type of broadcast, erase the micro service from its handling list
                erasedQueue=hashBroadcastToMicroServicesQueue.get(type);
                erasedQueue.remove(m);
            }
            ArrayBlockingQueue erasedQueueOfMessages=hashMicroServiceToMessagesQueue.get(m);
            hashMicroServiceToMessagesQueue.remove(m,erasedQueueOfMessages);

/*
    //include deleting it from events/broadcast queue

        Queue<Integer> DeleteList=listOfMessagesMicroServiceSubscribedTo.get(m);
        Integer EvOrBrod;
        Class<? extends Broadcast> Broadcasttt=null;
        Class<? extends Event> Eventtt=null;
        for(int i=0;i<DeleteList.size();i++) {//deleting the micro service from the hashmaps of the events/broadcasts that it has subscribed to
            EvOrBrod=DeleteList.remove();
            if(EvOrBrod<0){//In case this is a broadcast that the micoservice handles
                Broadcasttt =MapBetweenNumberAndBroadcast.get(EvOrBrod);
                ConcurrentLinkedQueue<MicroService> MicroList=hashBroadcastToMicroServicesQueue.get(Broadcasttt);
                MicroList.remove(m);
            }
            if(EvOrBrod>0){//In case this is a event that the micoservice handles
                Eventtt =MapBetweenNumberAndEvent.get(EvOrBrod);
                ConcurrentLinkedQueue<MicroService> MicroList=hashBroadcastToMicroServicesQueue.get(Eventtt);
                MicroList.remove(m);
            }
        }
        */
	 }


        public Message awaitMessage (MicroService m) throws InterruptedException {//need to use interupted mechanism
            if (!hashMicroServiceToMessagesQueue.containsKey(m)) {
                throw new IllegalStateException();
            }
            try {
                Message handleNow = hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
                return handleNow;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
            finally {
                Message handleNow = hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
                return handleNow;
            }
        }









	



}

