package bgu.spl.mics;

import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private final ConcurrentHashMap<Class <? extends Event>,Queue<MicroService>> hashEventToMicroServiceQueue;
	private final ConcurrentHashMap<Class<? extends Broadcast>,Queue<MicroService>> hashBroadcastToMicroServicesQueue;
	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> hashMicroServiceToMessagesQueue;
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

	 public   <T> void subscribeEvent (Class <? extends Event<T>> type, MicroService m) {
		//see if event already exists.
		// if so, add the ms to the event's queue.
		//else, push the new couple (event, ms), while creating the ms's queue for the event.
		if(hashEventToMicroServiceQueue.containsKey(type)){
            Queue<MicroService> q = hashEventToMicroServiceQueue.get(type);
            q.add(m);
        }
        else {
            Queue<MicroService> q = null;
            q.add(m);
            hashEventToMicroServiceQueue.put(type, q);
        }
        }




	//The same as above(subscribeEvent), for broadcasts
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        if(hashBroadcastToMicroServicesQueue.containsKey(type)){
            Queue<MicroService> q = hashBroadcastToMicroServicesQueue.get(type);
            q.add(m);
        }
        else {
            Queue<MicroService> q = null;
            q.add(m);
            hashBroadcastToMicroServicesQueue.put(type, q);
        }
    }


	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	
	//not finished, need to think of return value.
	public <T> Future<T> sendEvent(Event<T> e) {
        Queue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(e);//get the queue assigned to this type of event
        BlockingQueue<Message> QueueOfMicroservice=hashMicroServiceToMessagesQueue.get(QueueOfEvent.element());//get the message queue of the corresponding micro-service
		QueueOfMicroservice.add(e);
        return complete(e, e);///I am here
	}

	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {//need to use interupted mechanism
		// TODO Auto-generated method stub
		return null;
	}

	



}
