package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;

import java.awt.print.Book;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;

import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import org.junit.Assert;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner implements Serializable {

    //this long int will act as an ID for serialization and deserialization of the class. I don't know if it's really needed.
    private static HashMap<Integer,Customer> customerHashMap=new HashMap<>();//addition, for printing later on

    // need to add all the other passive object when time comes :)

    public static void main(String[] args) {
        deserialize();
        // JsonReader jsonReader=new JsonReader();
        printInTheEnd();

    }
    private static void printInTheEnd(){
      try {
          String printList = "printList.txt";//printing the list of OrderReciepts
          MoneyRegister moneyRegister = MoneyRegister.getInstance();
          moneyRegister.printOrderReceipts(printList);
          FileOutputStream CustFile = new FileOutputStream("CusmersMap.txt");
          ObjectOutputStream CustObject=new ObjectOutputStream(CustFile);
          //CustObject.writeObject(customerHashMap);
          CustObject.close();
          CustFile.close();
      }
      catch (FileNotFoundException e){
          System.out.println("file not found,check if file is in root directory");
      }
      catch (IOException e){
          System.out.println("something is wrong with the input/output");
      }
    }


    private static void deserialize() {
        JsonParser jsonParser = new JsonParser();

        /*
        File file=new File("src/input.json");
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e){
            inputStream=null;
        }
        Reader reader=new InputStreamReader(inputStream);
        JsonElement jsonElement=jsonParser.parse(reader);
        JsonObject jsonObject=jsonElement.getAsJsonObject();
        */


        try {
            Object object = jsonParser.parse(new FileReader("input.json"));
            JsonObject jsonObject = (JsonObject) object;


            //***********Getting books
            JsonArray AllBooks = jsonObject.getAsJsonArray("initialInventory");//Array of all the books
            Iterator ReadBooks = AllBooks.iterator();//Iterator for the books
            int countBook = 0;//counter for every book
            BookInventoryInfo[] inventory = new BookInventoryInfo[AllBooks.size()];//Here is where we are going to put the books
            while (ReadBooks.hasNext()) {
                JsonObject CurrentBook = (JsonObject) ReadBooks.next();
                String TitleOfBook = CurrentBook.get("bookTitle").getAsString();
                int AmountOfBook = CurrentBook.get("amount").getAsInt();
                int PriceOfBook = CurrentBook.get("price").getAsInt();
                BookInventoryInfo Book = new BookInventoryInfo(TitleOfBook, PriceOfBook, AmountOfBook);
                inventory[countBook] = Book;
                countBook++;
                System.out.println(Book.getPrice());//test
            }

            Inventory inventory1 = Inventory.getInstance();//Load the books
            inventory1.load(inventory);//inventory is ready

//checked The parsing of books,works.


            //******************Getting the cars
            JsonArray otherInitialResources = jsonObject.getAsJsonArray("initialResources");
            JsonObject Cars = otherInitialResources.get(0).getAsJsonObject();
            JsonArray ArrayOfCars = Cars.getAsJsonArray("vehicles");
            Iterator ReadCars = ArrayOfCars.iterator();
            int countCar = 0;
            DeliveryVehicle[] vehicles = new DeliveryVehicle[ArrayOfCars.size()];
            while (ReadCars.hasNext()) {
                JsonObject CurrentCar = (JsonObject) ReadCars.next();
                int license = CurrentCar.get("license").getAsInt();
                int speed = CurrentCar.get("speed").getAsInt();
                DeliveryVehicle Car = new DeliveryVehicle(license, speed);
                vehicles[countCar] = Car;
                countCar++;
                System.out.println(Car.getLicense());//test
            }

            ResourcesHolder resourcesHolder = ResourcesHolder.getInstance();//get the instance(empty instance)
            resourcesHolder.load(vehicles);//Load the vehicles,ResourcesHolder is ready

//checked the parsing of vehicles,works.


            ///*****************Get the rest of the micro-services
            JsonObject services = jsonObject.getAsJsonObject("services");//IMPORTANT

            //***Time service
            JsonObject Time = services.getAsJsonObject("time");
            int speed = Time.get("speed").getAsInt();
            int duration = Time.get("duration").getAsInt();
            TimeService timeService = new TimeService(speed, duration);//DON'T FORGET:start this with a thread AFTER we initialize all the micro-services
            System.out.println(speed + " " + duration);//test

//checked parsing of time,works.


            ////*****Selling service

            int numOfSellServices = services.get("selling").getAsInt();
            for (int i = 0; i < numOfSellServices; i++) {
                String name = "Selling Service number" + i;
                SellingService sell = new SellingService(name);
                Thread sellThread = new Thread(sell);
                sellThread.start();
                System.out.println(name);//test
            }

//checked parsing of selling service,works.


            ////**********Inventory Service
            int numOfInventoryServices = services.get("inventoryService").getAsInt();
            for (int i = 0; i < numOfInventoryServices; i++) {
                String name = "Inventory Service number" + i;
                InventoryService NewInventory = new InventoryService(name);
                Thread InventoryThread = new Thread(NewInventory);
                InventoryThread.start();
                System.out.println(name);//test
            }


            //********Logistics Service
            int numOfLogisticServices = services.get("logistics").getAsInt();
            for (int i = 0; i < numOfLogisticServices; i++) {
                String name = "Logistics Service number" + i;
                LogisticsService NewLogistics= new LogisticsService(name);
                Thread LogisticsThread = new Thread(NewLogistics);
                LogisticsThread.start();
                System.out.println(name);//test
            }


            int numOfResourcesServices = services.get("resourcesService").getAsInt();
            for (int i = 0; i < numOfResourcesServices; i++) {
                String name = "Resources Service number" + i;
                ResourceService NewResources= new ResourceService(name);
                Thread ResourcesThread = new Thread(NewResources);
                ResourcesThread.start();
                System.out.println(name);//test
            }

            JsonArray Customers = services.getAsJsonArray("customers");
            Iterator ReadCustomers = Customers.iterator();
            int countCustomer = 0;
            while (ReadCustomers.hasNext()) {
                JsonObject CurrentCustomer = (JsonObject) ReadCustomers.next();
                int id = CurrentCustomer.get("id").getAsInt();
                String name = CurrentCustomer.get("name").getAsString();
                String address = CurrentCustomer.get("address").getAsString();
                int distance = CurrentCustomer.get("distance").getAsInt();
                System.out.println(distance);
                JsonObject CreditCard = CurrentCustomer.getAsJsonObject("creditCard");
                int creditNum = CreditCard.get("number").getAsInt();
                int creditAmount = CreditCard.get("amount").getAsInt();
                JsonArray orders = CurrentCustomer.get("orderSchedule").getAsJsonArray();
                Iterator ReadOrders = orders.iterator();
                OrderReceipt[] orderReceipts = new OrderReceipt[orders.size()];
                int countOrders = 0;
                while (ReadOrders.hasNext()) {
                    JsonObject CurrentOrder = (JsonObject) ReadOrders.next();
                    String title = CurrentOrder.get("bookTitle").getAsString();
                    int tickToSendBook = CurrentOrder.get("tick").getAsInt();
                    //int price = 0;
                   // for (BookInventoryInfo b : inventory) {/// Probably unnecessary part, gets price of book upon construction.
                     //   if (b.getBookTitle().equals(title)) {//It is uneccesary because we will checkavailabality and get the price for
                       //     price = b.getPrice();///each order anyway...
                        //}
                    //}
                    OrderReceipt orderReceipt = new OrderReceipt(countOrders, "", id, title, 0, tickToSendBook, 0, 0);
                    orderReceipts[countOrders] = orderReceipt;
                    countOrders++;
                    System.out.println(orderReceipt.getPrice());
                }
                Customer customer = new Customer(name, id, address, distance, creditAmount, creditNum,orderReceipts);
                customerHashMap.put(id,customer);
                String ApiName = "API Service number " + countCustomer;
                APIService apiService = new APIService(ApiName, customer.getCustomerReceiptList(),customer);
                Thread APIserviceThread = new Thread(apiService);
                APIserviceThread.start();
                countCustomer++;
                System.out.println(customer.toString());//test

                Thread time = new Thread(timeService);//experiment
                time.start();

            }



//  checked parsing of Customer,works.







        } catch (FileNotFoundException e) {
            System.out.println("file not found,check if file is in root directory");
        }










       /*
        File file=new File("src/input.json");
        try{
            InputStream inputStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e){};
        Assert.assertNotNull(inp);








        try{
            FileInputStream fileIn = new FileInputStream("input.json");
            fileIn=null;
            Assert.assertNull(fileIn);//the file gets
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            Assert.assertNotNull(objIn);
            Gson gson = new Gson();
            JsonParser jsonParser = new JsonParser();
            Reader reader = new InputStreamReader(new FileInputStream("input.json"), StandardCharsets.UTF_8);
            JsonObject jsonObject = new JsonObject();
            BookInventoryInfo bookInventoryInfo = gson.fromJson(reader, BookInventoryInfo.class);
            Assert.assertNotNull(bookInventoryInfo);
            System.out.println(bookInventoryInfo.getBookTitle());

            /* the above line reads from json file the first object it encounters that is inside the {
            (curly brackets and then assigns in to a new object defined as the right parameter,
            -- what I need to do is find out how to define the object that is being created as the String before the [
            (array brackets).
            */
        //} catch (FileNotFoundException e1) {
        //} catch (IOException e2) {
        //} catch (Exception e3) {}


    }
}

