package bgu.spl.mics;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {

	//fields:
	private volatile boolean resolved;//changed to volatile
	private T result;
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		this.resolved=false;
		this.result=null;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public synchronized T get() {
		if (result!=null) return result;
		else{
			try {
				while (result == null) this.wait();
			}
			catch (InterruptedException e){}
		}
		return result;

	}
	
	/**
     * Resolves the result of this Future object.
     */
	public synchronized void resolve (T result) {
		resolved=true;
		this.result=result;
		this.notifyAll();
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() { // why not just "return resolved"?
		return resolved;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timeout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public T get(long timeout, TimeUnit unit) {//did it in the "sleep & check" guarding method
		for(int i=0;i<3;i++) {
			try {
				unit.sleep(timeout / 3);
			}
			catch (InterruptedException e){
				System.out.println("Exception: " );
			}
			if (isDone()) return result;
		}
		return null;

	}



	//************Methods for tests
	public T getResult() {
		return this.result;
	}










}
