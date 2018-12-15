package bgu.spl.mics.application.passiveObjects;


import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the store finance management. 
 * It should hold a list of receipts issued by the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */

//SHACHAR
public class MoneyRegister {

	private  List<OrderReceipt> issuedReceipts;
	private List<Integer> orderIdList = new ArrayList<>();
	private AtomicInteger total;


	private static class MoneyRegisterHolder {
		private static MoneyRegister instance = new MoneyRegister();
	}

	private MoneyRegister() {
		issuedReceipts = new CopyOnWriteArrayList<>(); // is a thread-safe impl. of List<> interface.
		total = new AtomicInteger(0);
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static MoneyRegister getInstance() {
		return MoneyRegisterHolder.instance;
	}
	
	/**
     * Saves an order receipt in the money register.
     * <p>   
     * @param r		The receipt to save in the money register.
     */

	// need not be synchronized
	public void file (OrderReceipt r) {
			if (r == null)
				return;
			if (orderIdList.contains(r.getOrderId()))
				throw new IllegalArgumentException("there's already a filed receipt with this Id: " + r.getOrderId());
			issuedReceipts.add(r);
			orderIdList.add(r.getOrderId());
	}
	
	/**
     * Retrieves the current total earnings of the store.  
     */
	public int getTotalEarnings() {
		return total.get();
	}
	
	/**
     * Charges the credit card of the customer a certain amount of money.
     * <p>
     * @param amount 	amount to charge
     */

	public void chargeCreditCard(Customer c, int amount) {
		c.pay(amount);
		int currentTotal;
		int updatedCurrentTotal;
		do {
			currentTotal = total.get();
			updatedCurrentTotal = currentTotal + amount;
		} while (!total.compareAndSet(currentTotal,updatedCurrentTotal));
		System.out.println("just charged the credit card of " + c.getName()+" for "+amount+".");
	}
	
	/**
     * Prints to a file named @filename a serialized object List<OrderReceipt> which holds all the order receipts 
     * currently in the MoneyRegister
     * This method is called by the main method in order to generate the output.. 
     */
	public void printOrderReceipts(String filename) {
		List<OrderReceipt> clonedMoneyRegister = cloneMoneyRegister();
		Gson gson = new Gson();

		try {
			FileWriter writer = new FileWriter(filename);
			String json = gson.toJson(clonedMoneyRegister);
			writer.write(json);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized List<OrderReceipt> cloneMoneyRegister() {
		List<OrderReceipt> clonedMoneyRegister = new ArrayList<>();
		for (OrderReceipt order: issuedReceipts) {
			clonedMoneyRegister.add(new OrderReceipt(order.getOrderId(),order.getSeller(),order.getCustomerId(),
													 order.getBookTitle(), order.getPrice(), order.getOrderTick(),
													 order.getProcessTick(), order.getIssuedTick()));
		}
		return clonedMoneyRegister;
	}

	public void setTotal(int num) {///public method,just for testing,needs to be erased later on
		this.total.set(num);
	}
	public void clearIssuedReciepts(){///public method,just for testing,needs to be erased later on
		issuedReceipts.clear();
	}

}
