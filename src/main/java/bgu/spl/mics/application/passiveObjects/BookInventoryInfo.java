package bgu.spl.mics.application.passiveObjects;

/**
 * Passive data-object representing a information about a certain book in the inventory.
 * You must not alter any of the given public methods of this class. 
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */


public class BookInventoryInfo {

	private String title;
	private int price;
	private volatile int amount;

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
	simultaneously by a number of threads - this is not true, because of the implementation of _public int checkAvailabiltyAndGetPrice(String book) {
	in _Inventory_, which is synchronized on the book it deals with, hence calling this function not concurrently for each book.
	*/
	public synchronized void decreaseAmount() {
		amount--;
	}

	public BookInventoryInfo cloneBook() {
		return new BookInventoryInfo(getBookTitle(),getPrice(),getAmountInInventory());
	}

	
}
