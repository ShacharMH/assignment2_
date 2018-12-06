package bgu.spl.mics.application.passiveObjects;

import java.lang.reflect.Array;
import java.util.List;

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
	private volatile int creditAmount;
	private int creditNumber;
	private OrderReceipt[] orderReceipts;


	public Customer(String name, int Id, String address, int distance, int creditAmount, int creditNumber, OrderReceipt[] orderReceipts) {
		this.name = name;
		this.Id = Id;
		this.address = address;
		this.distance=distance;
		this.creditAmount = creditAmount;
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
		return creditAmount;
	}
	
	/**
     * Retrieves this customers credit card serial number.    
     */
	public int getCreditNumber() {
		return creditNumber;
	}

	// decreases amount by num and returns the current updated amount in credit card.
	public int decreaseAmountBy(int num) {
		//synchronized (this) {
			this.creditAmount = creditAmount - num;
			return creditAmount;
		//}
	}


	public String toString(){
		return name+" id is"+ Id+" distance is"+distance +" "+address+" ,credit amount "+creditAmount+" ,credit number is "+creditNumber+orderReceipts[0].getBookTitle();
	}
}
