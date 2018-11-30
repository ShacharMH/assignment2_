package bgu.spl.mics;

import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
	private final ConcurrentHashMap<MicroService, Queue<Message>> listOfMessagesMicroServiceSubscribedTo;//addition, helps to know where else to delete it from
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
		this.listOfMessagesMicroServiceSubscribedTo = new ConcurrentHashMap<>();
	}



	@Override
	//command, divided into command of "subscribe event" of the micro service(insert it into the queue of the micro service)
	//@pre: @param "?"!=null
	//@pre:@param m!=null,
	//@post:@MicroService m has the event in its message queue, queue.size==pre:queue.size+1
	//@post:@param type has a field for @param T

	/**
	 * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
	 * <p>
	 * @param <T>  The type of the result expected by the completed event.
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service..
	 */

	 public   <T> void subscribeEvent (Class <? extends Event<T>> type, MicroService m) {//problem with compilation
	     /*
		//see if event already exists.
		// if so, add the ms to the event's queue.
		//else, push the new couple (event, ms), while creating the ms's queue for the event.
		if(hashEventToMicroServiceQueue.containsKey(type)){//if event exists
            ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = hashEventToMicroServiceQueue.get(type);//get the queue of microservices for this event
            EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
            Queue<Message> M=listOfMessagesMicroServiceSubscribedTo.get(m);//Adding broadcast to list of broadcast/events
            M.add(type);//not sure why there is a compilation problem
        }
        else{
            ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = null;//create queue of microservices for this event
            EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
            hashEventToMicroServiceQueue.put(type, EventsMicroServiceList);//add this new pair to the list of <events,microservices that handle them>
            Queue<Message> M=listOfMessagesMicroServiceSubscribedTo.get(m);//Adding broadcast to list of broadcast/events
            M.add(type);//not sure why there is a compilation problem
        }
        */
	 }




	//The same as above(subscribeEvent), for broadcasts
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {//problem with compilation
	     /*
        if(hashBroadcastToMicroServicesQueue.containsKey(type)){
            ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = hashBroadcastToMicroServicesQueue.get(type);
            BroadcastsMicroserviceList.add(m);
            Queue<Message> M=listOfMessagesMicroServiceSubscribedTo.get(m);//Adding broadcast to list of broadcast/events
            M.add(type);//not sure why there is a compilation problem


        }
        else {
            ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = null;
            BroadcastsMicroserviceList.add(m);
            hashBroadcastToMicroServicesQueue.put(type, BroadcastsMicroserviceList);
        }*/
    }


	@Override

	public <T> void complete(Event<T> e, T result) {//not sure about the future object and implementing this



		// TODO Auto-generated method stub

	}


	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> ListOfMicros=hashBroadcastToMicroServicesQueue.get(b);
		for (MicroService item:ListOfMicros) {//for every microservice that has subscribed to get this broadcast
            BlockingQueue<Message> addBroadcastHere = hashMicroServiceToMessagesQueue.get(item);//get messagequeue
            addBroadcastHere.add(b);//add the broadcast to the messagequeue
        }
	}

	
	//not finished, need to think of return value.
	public <T> Future<T> sendEvent(Event<T> e) {
	     if (hashEventToMicroServiceQueue.containsKey(e)) {
             ConcurrentLinkedQueue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(e);//get the queue assigned to this type of event
             MicroService handleEvent = QueueOfEvent.remove();//holds the micro service that will get the event and handle it.
             BlockingQueue<Message> QueueOfMicroservice = hashMicroServiceToMessagesQueue.get(handleEvent);//get the message queue of the corresponding micro-service
             QueueOfMicroservice.add(e);//"add" and not "put", this is a non-blocking method
             QueueOfEvent.add(handleEvent);//MicroService is returned to the tail of the queue of the event, round robin invariant is maintained.
             Future<T> result = null;//not sure about these last two lines
             return result;
         }
	     else
	         return null;
	}

	@Override
	public void register(MicroService m) {
		BlockingQueue<Message> MessageList=null;//make message queue
		Queue<Message> SubscribeList=null;//make a queue for all the events/broadcasts m will subscribe to
		hashMicroServiceToMessagesQueue.put(m, MessageList);
		listOfMessagesMicroServiceSubscribedTo.put(m,SubscribeList);
	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub
    //include deleting it from events/broadcast queue

        Queue<Message> DeleteList=listOfMessagesMicroServiceSubscribedTo.get(m);
        Message eventOrBroadcast;
        for(int i=0;i<DeleteList.size();i++) {//deleting the micro service from the hashmaps of the events/broadcasts that it has subscribed to
            eventOrBroadcast=DeleteList.remove();
            if(eventOrBroadcast instanceof Event){//In case this is an event that the micoservice handles
                ConcurrentLinkedQueue Q=hashEventToMicroServiceQueue.get(eventOrBroadcast);
                Q.remove(m);
            }
            if(eventOrBroadcast instanceof Broadcast){//In case this is a broadcast that the micoservice handles
                ConcurrentLinkedQueue Q=hashBroadcastToMicroServicesQueue.get(eventOrBroadcast);
                Q.remove(m);
            }
        }
	 }


	public Message awaitMessage(MicroService m) throws InterruptedException {//need to use interupted mechanism
        // TODO Auto-generated method stub
        if (hashMicroServiceToMessagesQueue.containsKey(m)) {
            Message handleNow = hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
            return handleNow;
        }
        else throw new IllegalStateException();
    }





}
