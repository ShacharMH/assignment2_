package bgu.spl.mics;

import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
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

	private final ConcurrentHashMap<Class <? extends Event>,ConcurrentLinkedQueue<MicroService>> hashEventToMicroServiceQueue;
	private final ConcurrentHashMap<Class<? extends Broadcast>,ConcurrentLinkedQueue<MicroService>> hashBroadcastToMicroServicesQueue;
	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> hashMicroServiceToMessagesQueue;
	private final ConcurrentHashMap<MicroService,Queue<Integer>> listOfMessagesMicroServiceSubscribedTo;//addition, helps to know where else to delete microservice from
    private final ConcurrentHashMap<Event,Future> MapBetweenEventAndFutureObj;//addition
    private final ConcurrentHashMap<Integer,Class <? extends Event>> MapBetweenNumberAndEvent;//addition
    private final ConcurrentHashMap<Integer,Class<? extends Broadcast>> MapBetweenNumberAndBroadcast;//addition
    private volatile Integer  EventCount=1;
    private volatile Integer  BroadcastCount=1;
	private static MessageBusImpl bus=null;

	public static MessageBusImpl getInstance(){
		if (bus==null){
			bus = new MessageBusImpl();
		}
		return bus;
	}

	private MessageBusImpl () {
		this.hashBroadcastToMicroServicesQueue = new ConcurrentHashMap<>();
		this.hashEventToMicroServiceQueue = new ConcurrentHashMap<>();
		this.hashMicroServiceToMessagesQueue = new ConcurrentHashMap<>();
		this.listOfMessagesMicroServiceSubscribedTo = new ConcurrentHashMap<>();//POSITIVE IDENTIFIER FOR EVENT,NEGATIVE FOR BROADCAST
        this.MapBetweenNumberAndEvent= new ConcurrentHashMap<>();
        this.MapBetweenNumberAndBroadcast= new ConcurrentHashMap<>();
        this.MapBetweenEventAndFutureObj= new ConcurrentHashMap<>();
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
         synchronized (EventCount) {
             if (hashEventToMicroServiceQueue.containsKey(type)) {//if event already exists
                 ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = hashEventToMicroServiceQueue.get(type);//get the queue of microservices for this event
                 EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
                 MapBetweenNumberAndEvent.put(EventCount, type);//add the event an identifier
                 Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                 M.add(EventCount);//event identifier is in micro-service's list
                 EventCount++;//update event counter, synchronized

             } else {
                 ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = null;//create queue of microservices for this event
                 EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
                 hashEventToMicroServiceQueue.put(type, EventsMicroServiceList);//add this new pair to the list of <events,microservices that handle them>
                 MapBetweenNumberAndEvent.put(EventCount, type);//add the event an identifier
                 Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                 M.add(EventCount);//event identifier is in micro-service's list
                 EventCount++;//update event counter, synchronized
             }
         }
     }




	//The same as above(subscribeEvent), for broadcasts
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {//problem with compilation
	     synchronized (BroadcastCount) {
             if (hashBroadcastToMicroServicesQueue.containsKey(type)) {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = hashBroadcastToMicroServicesQueue.get(type);
                 BroadcastsMicroserviceList.add(m);
                 //Queue<Message> M=listOfMessagesMicroServiceSubscribedTo.get(m);//Adding broadcast to list of broadcast/events
                 //M.add(type);//not sure why there is a compilation problem
                 MapBetweenNumberAndBroadcast.put(BroadcastCount,type);//add broadcast an identifier
                 Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                 M.add(BroadcastCount * (-1));//broadcast identifier is in micro-service's list
                 BroadcastCount++;//update broadcast counter, synchronized


             } else {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = null;
                 BroadcastsMicroserviceList.add(m);
                 hashBroadcastToMicroServicesQueue.put(type, BroadcastsMicroserviceList);
                 MapBetweenNumberAndBroadcast.put(BroadcastCount,type);//add broadcast identifier
                 Queue<Integer> M = listOfMessagesMicroServiceSubscribedTo.get(m);
                 M.add(BroadcastCount * (-1));//broadcast identifier is in micro-service's list
                 BroadcastCount++;//update broadcast counter, synchronized
             }
         }
    }



//Resolve the event's corresponding future object,AFTER it was sent back to the micro-service who sent the event in the first place
//recieving micro-service uses this method
	public <T> void complete(Event<T> e, T result) {
        Future EventsAnswer=MapBetweenEventAndFutureObj.get(e);
        EventsAnswer.resolve(result);
	}


	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> ListOfMicros=hashBroadcastToMicroServicesQueue.get(b);
		for (MicroService item:ListOfMicros) {//for every microservice that has subscribed to get this broadcast
            BlockingQueue<Message> addBroadcastHere = hashMicroServiceToMessagesQueue.get(item);//get messagequeue
            addBroadcastHere.add(b);//add the broadcast to the messagequeue
        }
	}

	//Insert event to messagequeue of micro-service(round robin), return future object to micro-service that sent this event
	public <T> Future<T> sendEvent(Event<T> e) {
	     if (hashEventToMicroServiceQueue.containsKey(e)) {
             ConcurrentLinkedQueue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(e);//get the queue assigned to this type of event
             MicroService handleEvent = QueueOfEvent.remove();//holds the micro service that will get the event and handle it.
             BlockingQueue<Message> QueueOfMicroservice = hashMicroServiceToMessagesQueue.get(handleEvent);//get the message queue of the corresponding micro-service
             QueueOfMicroservice.add(e);//"add" and not "put", this is a non-blocking method
             QueueOfEvent.add(handleEvent);//MicroService is returned to the tail of the queue of the event, round robin invariant is maintained.
             Future<T> result = null;//create future object that will hold the answer to this event
             MapBetweenEventAndFutureObj.put(e,result);//put event and future object in map
             return result;
         }
	     else
	         return null;
	}


	public void register(MicroService m) {
		BlockingQueue<Message> MessageList=null;//make message queue
		Queue<Integer> SubscribeList=null;//make a queue for all the events/broadcasts m will subscribe to-problematic solution
		hashMicroServiceToMessagesQueue.put(m, MessageList);
		listOfMessagesMicroServiceSubscribedTo.put(m,SubscribeList);
	}

//see in to how micro-service remembers the messages it has subscribed to, current solution is problematic
	public void unregister(MicroService m) {

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
	 }

//is it ok to put return
	public Message awaitMessage(MicroService m) throws InterruptedException {//need to use interupted mechanism
              if (!hashMicroServiceToMessagesQueue.containsKey(m)) {
                 throw new IllegalStateException();
             }
                try {
                    Message handleNow = hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
                    return handleNow;
                }
                catch (InterruptedException e) {
                    throw new InterruptedException();
                }
	 }









	



}
