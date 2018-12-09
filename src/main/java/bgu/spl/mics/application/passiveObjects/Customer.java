package bgu.spl.mics.application.passiveObjects;

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

	private AtomicInteger reservedAmount = new AtomicInteger(0);
	//private Object lock = new Object();



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
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditNumber;
	}

	public int checkIfThereIsEnoughMoneyInCreditCard(int amount) {
		return getAvailableCreditAmount()-amount;
	}

	// decreases amount in reservedAmount and returns the updated reservedAmount.
	public void decreaseAmountBy(int num) {
		int currentReservedAmount;
		int updatedCurrentReservedAmount;
		do {
			currentReservedAmount = getAvailableCreditAmount();
			updatedCurrentReservedAmount = currentReservedAmount + num;
		} while (!reservedAmount.compareAndSet(currentReservedAmount,updatedCurrentReservedAmount));
		//return getAvailableReservedAmount();
	}

	public int getAvailableReservedAmount() {
		return reservedAmount.get();
	}

	public String toString(){
		return name+" id is"+ Id+" distance is"+distance +" "+address+" ,credit amount "+creditAmount+" ,credit number is "+creditNumber+orderReceipts[0].getBookTitle();
	}

	// transfers amount from creditAmount to ReservedAmount. returns false if there's not enough money to reserve.
	public synchronized boolean reserveAmount(int amount) {

		if (checkIfThereIsEnoughMoneyInCreditCard(amount)<0)
			return false;
		creditAmount.set(creditAmount.get() - amount);
		reservedAmount.set(reservedAmount.get() + amount);
		return true;
	}

	// returns amount from reservedAmount to creditAmount. returns false if the amount to return is more than there is in reservedAmount
	public synchronized boolean releaseAmount(int amount) {

		if (amount > getAvailableReservedAmount())
			return false;
		reservedAmount.set(reservedAmount.get() - amount);
		creditAmount.set(creditAmount.get() + amount);
		return true;
	}

}
