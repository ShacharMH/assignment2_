package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.*;

import java.awt.print.Book;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;


import bgu.spl.mics.application.services.*;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import org.junit.Assert;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner implements java.io.Serializable{

    //this long int will act as an ID for serialization and deserialization of the class. I don't know if it's really needed.
    private static HashMap<Integer,Customer> customerHashMap=new HashMap<>();//addition, for printing later on
    private static Object randomLock=new Object();//just an object so deserialize will be performed before printInTheEnd

    // need to add all the other passive object when time comes :)

    public static void main(String[] args) {

            deserialize();


            // JsonReader jsonReader=new JsonReader();
            printInTheEnd();


    }
    private static void printInTheEnd(){
      try {
          //********printing the list of OrderReciepts
          String printList = "printList.txt";//file name needs to be changed to "args[3]"
          MoneyRegister moneyRegister = MoneyRegister.getInstance();
          moneyRegister.printOrderReceipts(printList);
          //********printing Hashmap of Customers
          FileOutputStream CustFile = new FileOutputStream("CustomersMap.txt");//file name needs to be changed to "args[1]"
          ObjectOutputStream CustObject=new ObjectOutputStream(CustFile);
          CustObject.writeObject(customerHashMap);
          CustObject.close();
          CustFile.close();
          //*********printing books and their remained amount
          Inventory inventory=Inventory.getInstance();
          inventory.printInventoryToFile("inventory.txt");//file name needs to be changed to "args[2]"
          //*********print MoneyRegister object
          FileOutputStream MoneyRegisterFile = new FileOutputStream("MoneyRegister.txt");//file name needs to be changed to "args[4]"
          ObjectOutputStream MoneyRegisterObject=new ObjectOutputStream(MoneyRegisterFile);
          MoneyRegisterObject.writeObject(moneyRegister);
          MoneyRegisterObject.close();
         // MoneyRegisterFile.close();

          //****testing output
          FileInputStream Test= new FileInputStream("CustomersMap.txt");
          Reader reader=new InputStreamReader(Test);
          JsonParser jsonParser=new JsonParser();
          JsonElement jsonElement=jsonParser.parse(reader);
          JsonObject jsonObject=jsonElement.getAsJsonObject();
          JsonObject customer=jsonObject.get("0").getAsJsonObject();
          String name= customer.get("name").getAsString();
          System.out.println(name);


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
            Object object = jsonParser.parse(new FileReader("input.json"));////file name needs to be changed to "args[0]"
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
            Thread time = new Thread(timeService);//experiment

//checked parsing of time,works.
            int numOfSellServices = services.get("selling").getAsInt();
            int numOfInventoryServices = services.get("inventoryService").getAsInt();
            int numOfLogisticServices = services.get("logistics").getAsInt();
            int numOfResourcesServices = services.get("resourcesService").getAsInt();
            JsonArray Customers = services.getAsJsonArray("customers");
            CountDownLatch countDownLatch=new CountDownLatch(numOfSellServices+numOfInventoryServices+numOfLogisticServices+numOfResourcesServices+Customers.size());
            ////*****Selling service


            for (int i = 0; i < numOfSellServices; i++) {
                String name = "Selling Service number" + i;
                SellingService sell = new SellingService(name,countDownLatch);
                Thread sellThread = new Thread(sell);
                sellThread.start();
                System.out.println(name);//test
            }

//checked parsing of selling service,works.


            ////**********Inventory Service

            for (int i = 0; i < numOfInventoryServices; i++) {
                String name = "Inventory Service number" + i;
                InventoryService NewInventory = new InventoryService(name,countDownLatch);
                Thread InventoryThread = new Thread(NewInventory);
                InventoryThread.start();
                System.out.println(name);//test
            }


            //********Logistics Service

            for (int i = 0; i < numOfLogisticServices; i++) {
                String name = "Logistics Service number" + i;
                LogisticsService NewLogistics= new LogisticsService(name,countDownLatch);
                Thread LogisticsThread = new Thread(NewLogistics);
                LogisticsThread.start();
                System.out.println(name);//test
            }

            //**************Resources Services

            for (int i = 0; i < numOfResourcesServices; i++) {
                String name = "Resources Service number" + i;
                ResourceService NewResources= new ResourceService(name,countDownLatch);
                Thread ResourcesThread = new Thread(NewResources);
                ResourcesThread.start();
                System.out.println(name);//test
            }


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
                APIService apiService = new APIService(ApiName, customer.getCustomerReceiptList(),customer,countDownLatch);
                Thread APIserviceThread = new Thread(apiService);
                APIserviceThread.start();
                countCustomer++;
                System.out.println(customer.toString());//test
            }
            //Making a countDown with number of MS that have to be initialized before TimeService can start


            countDownLatch.await();//wait for all MS to initialize before beginning the timer
            time.start();



//  checked parsing of Customer,works.







        } catch (FileNotFoundException e) {
            System.out.println("file not found,check if file is in root directory");
        }
        catch (InterruptedException e) {
            System.out.println("Countdown interupted");
        }
















    }
}

