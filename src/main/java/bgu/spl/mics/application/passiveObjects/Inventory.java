package bgu.spl.mics.application.passiveObjects;


import com.google.gson.Gson;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive data-object representing the store inventory.
 * It holds a collection of {@link BookInventoryInfo} for all the
 * books in the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 * GUIDELINE: ONLY INVENTORY TOUCHES THE INSTANCES OF THE BookInventoryInfo!
 */

public class Inventory implements Serializable {

	private final ConcurrentHashMap<String,BookInventoryInfo> myInventory;
	private String[] bookNamesList;
	private boolean isLoaded = false;


	private static class InventoryHolder {
		private static Inventory instance = new Inventory();
	}

	private Inventory() {
		myInventory = new ConcurrentHashMap<>();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Inventory getInstance() {
		return InventoryHolder.instance;
	}
	
	/**
     * Initializes the store inventory. This method adds all the items given to the store
     * inventory.
     * <p>
     * @param inventory 	Data structure containing all data necessary for initialization
     * 						of the inventory.
     */

	//NOT thread safe. BUT only one thread initializes it.
	public void load (BookInventoryInfo[] inventory ) {
		int i = 0;
		for (BookInventoryInfo book: inventory) {
			myInventory.put(book.getBookTitle(), book);
			i++;
		}
		bookNamesList = new String[i];
		for (int j = 0; j < i; j++) {
			bookNamesList[j] = inventory[j].getBookTitle();
		}
		isLoaded = true;
	}
	
	/**
     * Attempts to take one book from the store.
     * <p>
     * @param book 		Name of the book to take from the store
     * @return 	an {@link Enum} with options NOT_IN_STOCK and SUCCESSFULLY_TAKEN.
     * 			The first should not change the state of the inventory while the 
     * 			second should reduce by one the number of books of the desired type.
     */

	public OrderResult take (String book) {

			if (isLoaded) {
				synchronized ((myInventory.get(book))) {
					int result = checkAvailabiltyAndGetPrice(book);
					if (result > 0) {
						myInventory.get(book).decreaseAmount();
						//System.out.println(book + " was successfully taken, copies left in stock: " + myInventory.get(book).getAmountInInventory());
						return OrderResult.SUCCESSFULLY_TAKEN;
					} else {
						System.out.println(book + " is not in stock");
						return OrderResult.NOT_IN_STOCK;
					}
				}
			} else {
				return null;
			}

	}
	
	
	
	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */

	public int checkAvailabiltyAndGetPrice(String book) {
		BookInventoryInfo tmpbook = myInventory.getOrDefault(book, new BookInventoryInfo("default", 0, 0));
		if (tmpbook.getAmountInInventory() > 0) {
			return tmpbook.getPrice();
		} else {
			return -1;
		}
	}
	
	/**
     * 
     * <p>
     * Prints to a file name @filename a serialized object HashMap<String,Integer> which is a Map of all the books in the inventory. The keys of the Map (type {@link String})
     * should be the titles of the books while the values (type {@link Integer}) should be
     * their respective available amount in the inventory. 
     * This method is called by the main method in order to generate the output.
     */


	public void printInventoryToFile(String filename){

		ConcurrentHashMap<String, Integer> clonedInventory = cloneInventory();

		try {
            FileOutputStream CustFile = new FileOutputStream(filename);
            ObjectOutputStream CustObject=new ObjectOutputStream(CustFile);
            CustObject.writeObject(clonedInventory);
            CustObject.close();
            CustFile.close();
		} catch (IOException e) {
            System.out.println("something's wrong with inventory output");;
		}

	}

	// this function created a copy of myInventory.
	private synchronized ConcurrentHashMap<String, Integer> cloneInventory() {
		ConcurrentHashMap<String, Integer> clonedInventory = new ConcurrentHashMap<>();
		for (int i = 0; i < myInventory.size(); i++) {
			clonedInventory.put(bookNamesList[i], myInventory.get(bookNamesList[i]).getAmountInInventory());
		}
		return clonedInventory;
	}

}
