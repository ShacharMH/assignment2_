package bgu.spl.mics.application.passiveObjects;


import java.awt.print.Book;
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

//SHACHAR
public class Inventory {

	private ConcurrentHashMap<String,BookInventoryInfo> myInventory;
	private boolean isLoaded = false;

	/* this tiny class helps us achieve lazy initialization (so we don't get an instance the second we import this class.
	!!Because only a single thread loads classes, only one instance will be created!!
	 */
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

	//should be thread safe. only one thread initializes it.
	public void load (BookInventoryInfo[ ] inventory ) {
		for (BookInventoryInfo book: inventory) {
			myInventory.put(book.getBookTitle(), book);
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
	/* should be thread safe.
	if(checkAvailabiltyAndGetPrice) > 0 { -> means the book in in inventory.
	take(book)
	book.decreaseamount();
	 -> should be synchronized.
	 2. lock only on the certain book.
	 Things to notice:
	 a. I still need to check the invariants are true after _take_ calls _checkAvailabiltyAndGetPrice_.
	 */

	public OrderResult take (String book) {
		synchronized ((myInventory.get(book))) {
			if (isLoaded) {
				if (checkAvailabiltyAndGetPrice(book) > 0) {
					// what do I do if another thread gets involved with __checkAvailabiltyAndGetPrice__ and then it's no longer true?
					return OrderResult.SUCCESSFULLY_TAKEN;
				}
				else {
					return OrderResult.NOT_IN_STOCK;
				}
			} else {
				return null;
			}
		}
	}
	
	
	
	/**
     * Checks if a certain book is available in the inventory.
     * <p>
     * @param book 		Name of the book.
     * @return the price of the book if it is available, -1 otherwise.
     */
	public int checkAvailabiltyAndGetPrice(String book) {
		//TODO: Implement this
		return -1;
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
		//TODO: Implement this
	}
}
