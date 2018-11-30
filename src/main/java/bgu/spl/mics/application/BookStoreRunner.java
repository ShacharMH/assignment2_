package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;

import java.awt.print.Book;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Wrapper;

import bgu.spl.mics.application.passiveObjects.Inventory;
import com.google.gson.*;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner implements Serializable {

    //this long int will act as an ID for serialization and deserialization of the class. I don't know if it's really needed.
    private static final long serialVersionUID = 1234L;
    private BookInventoryInfo[] inventory;
    //private Gson gson = new GsonBuilder().create();
    // need to add all the other passive object when time comes :)

    public static void main(String[] args) {
        deserialize();

    }

    private static void deserialize() {
        try {
            //FileInputStream fileIn = new FileInputStream("input.json");
            //ObjectInputStream objIn = new ObjectInputStream(fileIn);
            Gson gson = new Gson();
            //JsonParser jsonParser = new JsonParser();
            Reader reader = new InputStreamReader(new FileInputStream("input.json"), StandardCharsets.UTF_8);
            JsonObject jsonObject = new JsonObject();
            //BookInventoryInfo bookInventoryInfo = gson.fromJson(reader, BookInventoryInfo.class);
            //System.out.println(bookInventoryInfo.getBookTitle());

            /* the above line reads from json file the first object it encounters that is inside the {
            (curly brackets and then assigns in to a new object defined as the right parameter,
            -- what I need to do is find out how to define the object that is being created as the String before the [
            (array brackets).
            */
        } catch (FileNotFoundException e1) {
        } catch (IOException e2) {
        } catch (Exception e3) {}


    }
}
