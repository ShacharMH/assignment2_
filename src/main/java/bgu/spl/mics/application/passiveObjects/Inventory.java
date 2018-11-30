package bgu.spl.mics.application.passiveObjects;


import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
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

//SHACHAR - hopefully this is done.
public class Inventory {

	private ConcurrentHashMap<String,BookInventoryInfo> myInventory;
	private String[] bookNamesList;
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

	/* hopefully this is thread-safe:
	1. is synchronized on the relevant book
	2. the answer returned from  _checkAvailabiltyAndGetPrice(book)_ is saved on the thread's stack so
	   other threads can't touch it: https://www.youtube.com/watch?v=otCpCn0l4Wo
	 */
	public OrderResult take (String book) {
		synchronized ((myInventory.get(book))) {
			if (isLoaded) {
				int result = checkAvailabiltyAndGetPrice(book);
				if (result > 0) {
					myInventory.get(book).decreaseAmount();
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

	/* hopefully this is thread-safe:
	1. only one thread can touch one book at a time
	2. doesn't change any object
	 */
	public int checkAvailabiltyAndGetPrice(String book) {
		synchronized (myInventory.get(book)) {
			BookInventoryInfo tmpbook = myInventory.get(book); // this is a pointer
			if (tmpbook.getAmountInInventory() > 0){
				return tmpbook.getPrice();
			}
			else {
				return -1;
			}
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

	/* this function is unsynchronized!
	It calls a synchronized clone() function that creates a copy of _myInventory_ and then works on the said copy.
	 */
	public void printInventoryToFile(String filename){

		ConcurrentHashMap<String, BookInventoryInfo> clonedInventory = cloneInventory();
		Gson gson = new Gson();


		try {
			FileWriter writer = new FileWriter(filename);
			// creating the objetcs to push to the file and pushing them.
			for (int i=0; i < clonedInventory.size(); i++) {
				Object[] book = new Object[2];
				book[0] = bookNamesList[i];
				book[1] = clonedInventory.get(bookNamesList[i]).getAmountInInventory();
				writer.write(gson.toJson(book));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// this function created a copy of myInventory.
	private synchronized ConcurrentHashMap<String, BookInventoryInfo> cloneInventory() { // synchronized on _this_, e.g. on Inventory
		ConcurrentHashMap<String, BookInventoryInfo> clonedInventory = new ConcurrentHashMap<>();
		for (int i = 0; i < myInventory.size(); i++) {
			clonedInventory.put(bookNamesList[i], myInventory.get(bookNamesList[i]).cloneBook());
		}
		return clonedInventory;
	}

}
