package bgu.spl.mics.application.passiveObjects;

import java.awt.print.Book;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive data-object representing a information about a certain book in the inventory.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */


public class BookInventoryInfo {

	private String title;
	private int price;
	private int amount;

	//constructor:
	public BookInventoryInfo(String title, int price, int amount) {
		if (title == null || price < 0 || amount < 0)
			try {
				throw new Exception("why you try to sabotage BookInventoryInfo man??");
			} catch (Exception e) {
				e.printStackTrace();
			}
		this.amount = amount;
		this.price = price;
		this.title = title;
	}
	/**
     * Retrieves the title of this book.
     * <p>
     * @return The title of this book.   
     */
	public String getBookTitle() {
		return title;
	}

	/**
     * Retrieves the amount of books of this type in the inventory.
     * <p>
     * @return amount of available books.      
     */
	public int getAmountInInventory() {
		return amount;
	}

	/**
     * Retrieves the price for  book.
     * <p>
     * @return the price of the book.
     */
	public int getPrice() {
		return price;
	}

	/* reflections on thread-safety: initialization of this object happens only once, so I don't mind the constructor
	not being thread-safe. BUT, when a book gets sold, then the amount should decrease by 1, and that function may be activate
	simultaneously by a number of threads. hence it's synchronized. on the other hand's other hand, there must be a (more) efficient
	way for synchronizing without blocking the other threads.
	*/
	public synchronized void decreaseAmount() {
		amount--;
	}
	
	

	
}
