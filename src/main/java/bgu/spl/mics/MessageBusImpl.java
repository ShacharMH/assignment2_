package bgu.spl.mics;

import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private HashMap<Event,Queue<MicroService>> hashEventToMicroServiceQueue;
	private HashMap<Broadcast,Queue<MicroService>> hashBroadcastToMicroServicesQueue;
	private HashMap<MicroService,Queue<Message>> hashMicroServiceToMessagesQueue;
	private static MessageBusImpl bus=null;

	public static MessageBusImpl getInstance(){
		if (bus==null){
			bus = new MessageBusImpl();
		}
		return bus;
	}

	private MessageBusImpl () {
		this.hashBroadcastToMicroServicesQueue = new HashMap<>();
		this.hashEventToMicroServiceQueue = new HashMap<>();
		this.hashMicroServiceToMessagesQueue = new HashMap<>();
	}



	@Override
	//command, divided into command of "subscribe event" of the micro service(insert it into the queue of the micro service)
	//@pre: @param "?"!=null
	//@pre:@param m!=null,
	//@post:@MicroService m has the event in its message queue, queue.size==pre:queue.size+1
	//@post:@param type has a field for @param T
	 public   <T> void subscribeEvent (Class <? extends Event<T>> type, MicroService m) {
		PriorityQueue<Event<?>> q=new PriorityQueue<Event<?>>();//Create a queue for these kind of messages
		listOfQueues.add(q);//add the queue to the list
		Integer a=listOfQueues.size();//get the identifier for the queue
		map.put(m.getName(),a-1);//establish the connection between the micro-service and its queue
		q.add(type);

		Callback c
		m.subscribeEvent(type.getClass(),);
			@Override

		}



	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {

		PriorityQueue p=listOfQueues.elementAt(map.get(e.getClass()));//get the suitable queue for this kind of event
		p.add(e);//insert the event to this queue


		return null;
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
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	



}
