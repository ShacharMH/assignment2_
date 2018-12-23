package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link //Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link /ResourcesHolder}, {@link/ MoneyRegister}, {@link/ Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	private int speed;
	private int duration;
	private int currentTime;

	public TimeService(int speed,int duration) {
		super("TimeSevice");
		this.speed=speed;
		this.duration=duration;
		this.currentTime=0;
	}

	@Override
	protected void initialize() {

		while (currentTime<=duration){
			sendBroadcast(new TickBroadcast(currentTime,duration));
			currentTime++;
			System.out.println(" time now is "+currentTime);
			try{
				Thread.sleep(speed);
			}
			catch (InterruptedException e){
				System.out.println("Interrupted Time passing");
			}


		}
		//System.out.println(getName()+" is being terminated");
		this.terminate();
		
	}

}
