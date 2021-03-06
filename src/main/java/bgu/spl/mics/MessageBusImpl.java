package bgu.spl.mics;

import java.util.concurrent.*;

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
	 public    <T> void subscribeEvent (Class <? extends Event<T>> type, MicroService m) {
         synchronized(hashEventToMicroServiceQueue) {
             if (hashEventToMicroServiceQueue.containsKey(type)) {//if event already exists
                 hashEventToMicroServiceQueue.get(type).add(m);//get the queue of microservices for this type of event, add MS
             } else {
                 ConcurrentLinkedQueue<MicroService> EventsMicroServiceList = new ConcurrentLinkedQueue<>();//create queue of microservices for this type of event
                 EventsMicroServiceList.add(m);//add micro-service to the queue,now this microservice(thread) can handle this event too
                 hashEventToMicroServiceQueue.put(type, EventsMicroServiceList);//add this new pair to the list of <event type,microservices that handles events of this type>
                 listOfTypesOfEvents.add(type);
             }
         }

     }




	//The same as above(subscribeEvent), for broadcasts
	public  void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
         synchronized (hashBroadcastToMicroServicesQueue) {
             if (hashBroadcastToMicroServicesQueue.containsKey(type)) {
                 hashBroadcastToMicroServicesQueue.get(type).add(m);
             } else {
                 ConcurrentLinkedQueue<MicroService> BroadcastsMicroserviceList = new ConcurrentLinkedQueue<>();
                 BroadcastsMicroserviceList.add(m);
                 hashBroadcastToMicroServicesQueue.put(type, BroadcastsMicroserviceList);
                 listOfTypesOfBroadcasts.add(type);
             }
         }
    }



//Resolve the event's corresponding future object,AFTER it was sent back to the micro-service who sent the event in the first place
//recieving micro-service uses this method
	public <T> void complete(Event<T> e, T result) {
        MapBetweenEventAndFutureObj.get(e).resolve(result);
	}


	public void sendBroadcast(Broadcast b) {//no need to synchronize,each broadcast type has its own queue of micro-services
	     synchronized (hashBroadcastToMicroServicesQueue) {
             for (MicroService item : hashBroadcastToMicroServicesQueue.get(b.getClass())) {//for every microservice that has subscribed to get this broadcast
                 hashMicroServiceToMessagesQueue.get(item).add(b);//get messagequeue of microservice, add message to it

             }
         }
	}

	//Insert event to messagequeue of micro-service(round robin), return future object to micro-service that sent this event
	public   <T> Future<T>  sendEvent(Event<T> e) {
	     synchronized (hashEventToMicroServiceQueue) {
             if (hashEventToMicroServiceQueue.containsKey(e.getClass())) {
                 Future<T> result = new Future<>();//create future object that will hold the answer to this event
                 MapBetweenEventAndFutureObj.put(e, result);//put event and future object in map
                 ConcurrentLinkedQueue<MicroService> QueueOfEvent = hashEventToMicroServiceQueue.get(e.getClass());//get the queue assigned to this type of event
                 MicroService handleEvent = QueueOfEvent.remove();//holds the micro service that will get the event and handle it.
                 System.out.println(" the name of the micro service that will handle is " + handleEvent.getName());//test I added
                 LinkedBlockingQueue<Message> QueueOfMicroservice = hashMicroServiceToMessagesQueue.get(handleEvent);//get the message queue of the corresponding micro-service
                 if (QueueOfMicroservice == null) System.out.println("the queue is null");
                 QueueOfMicroservice.add(e);//"add" and not "put", this is a non-blocking method,
                 QueueOfEvent.add(handleEvent);//MicroService is returned to the tail of the queue of the event, round robin invariant is maintained.
                 return result;
             } else
                 return null;
         }
	}


	public void register(MicroService m) {
		hashMicroServiceToMessagesQueue.put(m,new LinkedBlockingQueue<>());

    }


	public synchronized void unregister(MicroService m) {
        hashMicroServiceToMessagesQueue.remove(m, hashMicroServiceToMessagesQueue.get(m));//remove MS and its messageQueue
        for (Class<? extends Event> type : listOfTypesOfEvents)//for every type of event, erase the micro service from its handling list
        {
            hashEventToMicroServiceQueue.get(type).remove(m);
        }
        for (Class<? extends Broadcast> type : listOfTypesOfBroadcasts) {//for every type of broadcast, erase the micro service from its handling list
            hashBroadcastToMicroServicesQueue.get(type).remove(m);
        }
        System.out.println(m.getName());//added to see if all MS are unregistered at the end.

    }

        public Message awaitMessage (MicroService m) throws InterruptedException {//need to use interupted mechanism
            if (!hashMicroServiceToMessagesQueue.containsKey(m)) {//if we are trying to get a meesage of a MS that isn't registered
                throw new IllegalStateException();
            }
            try {
                return  hashMicroServiceToMessagesQueue.get(m).take();//get the last Message from the message Queue
            } catch (InterruptedException e) {
                System.out.println("await message thread interupted");
                throw new InterruptedException();
            }

        }
}


