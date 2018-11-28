package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner implements Serializable {

    //this long int will act as an ID for serialization and deserialization of the class. I don't know if it's really needed.
    private static final long serialVersionUID = 1234L;
    private BookInventoryInfo[] inventory;
    private Gson parser = new Gson();
    // need to add all the other passive object when time comes :)

    public static void main(String[] args) {
        deserialize();

    }

    private static void deserialize() {
        try {
            FileInputStream fileIn = new FileInputStream("input.json");
            ObjectInputStream objIn = new ObjectInputStream(fileIn);

        } catch (FileNotFoundException e1) {
        } catch (IOException e2) {
        } catch (Exception e3) {}


    }
}
