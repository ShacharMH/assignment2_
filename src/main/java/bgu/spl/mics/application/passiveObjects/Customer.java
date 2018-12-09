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

	//private AtomicInteger reservedAmount = new AtomicInteger(0);
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

	// decreases amount by num and returns the current updated amount in credit card.
	public int decreaseAmountBy(int num) {
		int currentCreditAmount;
		int updatedCurrentCreditAmount;
		do {
			currentCreditAmount = getAvailableCreditAmount();
			updatedCurrentCreditAmount = currentCreditAmount + num;
		} while (!creditAmount.compareAndSet(currentCreditAmount,updatedCurrentCreditAmount));
		return getAvailableCreditAmount();
	}


	public String toString(){
		return name+" id is"+ Id+" distance is"+distance +" "+address+" ,credit amount "+creditAmount+" ,credit number is "+creditNumber+orderReceipts[0].getBookTitle();
	}

	/*
	public boolean reserveAmount(int amount) {


		//add a condition: if amount > credit card amount -> return false;

		int currentReservedAmount;
		int updatedCurrentReservedAmount;
		do {
			currentReservedAmount = reservedAmount.get();
			updatedCurrentReservedAmount = reservedAmount.get() + amount;
		} while (!reservedAmount.compareAndSet(currentReservedAmount,updatedCurrentReservedAmount));
		decreaseAmountBy(amount);
	}
	*/
}
