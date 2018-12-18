package bgu.spl.mics.application.passiveObjects;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a customer of the store.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Customer {

	private String name;
	private int Id;
	private String address;
	private int distance;
	private AtomicInteger creditAmount;
	private int creditNumber;
	private OrderReceipt[] orderReceipts;
	private AtomicInteger sumOfReservedAmount = new AtomicInteger(0);
	private List<Integer> reservedAmount = new CopyOnWriteArrayList<>();


	public Customer(String name, int Id, String address, int distance, int creditAmount, int creditNumber, OrderReceipt[] orderReceipts) {
		this.name = name;
		this.Id = Id;
		this.address = address;
		this.distance=distance;
		this.creditAmount = new AtomicInteger(creditAmount);
		this.creditNumber = creditNumber;
		this.orderReceipts = orderReceipts;
	}
	/**
     * Retrieves the name of the customer.
     */
	public String getName() {
		return name;
	}

	/**
     * Retrieves the ID of the customer  . 
     */
	public int getId() {
		return Id;
	}
	
	/**
     * Retrieves the address of the customer.  
     */
	public String getAddress() {
		return address;
	}
	
	/**
     * Retrieves the distance of the customer from the store.  
     */
	public int getDistance() {
		return distance;
	}

	
	/**
     * Retrieves an Array of receipts for the purchases this customer has made.
     * <p>
     * @return An Array of receipts.
     */
	public OrderReceipt[] getCustomerReceiptList() {
		return orderReceipts;
	}
	
	/**
     * Retrieves the amount of money left on this customers credit card.
     * <p>
     * @return Amount of money left.   
     */
	public int getAvailableCreditAmount() {
		return creditAmount.get();
	}

	public int getReservedAmount() {
		return sumOfReservedAmount.get();
	}
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditNumber;
	}

	public int checkIfThereIsEnoughMoneyInCreditCard(int amount) {
		return getAvailableCreditAmount()-amount;
	}

	// decreases amount in reservedAmount
	// this should work but in this implementation each sum is going to get touched by one micro-serice so the implementation can be simpler
	public void pay(int amount) {
		reservedAmount.remove(Integer.valueOf(amount));
		sumOfReservedAmount.addAndGet(-amount);
	}


	public String toString(){
		return name+" id is"+ Id+" distance is"+distance +" "+address+" ,credit amount "+creditAmount+" ,credit number is "+creditNumber+orderReceipts[0].getBookTitle();
	}

	// transfers amount from creditAmount to ReservedAmount. returns false if there's not enough money to reserve.
	public boolean reserveAmount(int amount) {
		synchronized (creditAmount) {
			if (checkIfThereIsEnoughMoneyInCreditCard(amount) < 0)
				return false;
		}
		creditAmount.addAndGet(-amount);
		reservedAmount.add(amount);
		sumOfReservedAmount.addAndGet(amount);
		return true;
	}

	// returns amount from reservedAmount to creditAmount.
	public void releaseAmount(int amount) {
		reservedAmount.remove(Integer.valueOf(amount));
		sumOfReservedAmount.addAndGet(-amount);
		creditAmount.addAndGet(amount);
	}
}
