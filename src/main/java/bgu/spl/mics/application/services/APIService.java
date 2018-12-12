package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.OrderBookEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link //BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link// ResourcesHolder}, {@link //MoneyRegister}, {@link //Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService{
private OrderReceipt[] orderReceipts;
volatile int CurrentTime=0;
private Customer customer;

	public APIService(String name,OrderReceipt[] orders,Customer customer) {
		super(name);
		this.customer=customer;
		this.orderReceipts=orders;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, TickBroadcastCallback -> {
			CurrentTime=TickBroadcastCallback.getCurrentTime();
			if (TickBroadcastCallback.getCurrentTime()==TickBroadcastCallback.getDuration()) terminate();
			else
			{
				Future<OrderReceipt> future=new Future<>();
				for(OrderReceipt o:orderReceipts){ // should'nt it be "while" we may miss launching some of the orderBookEvent this way
					if (o.getOrderTick()==CurrentTime)
						 future=sendEvent(new OrderBookEvent(o.getBookTitle(),CurrentTime,customer));

				}
			}
		});
	}

}
