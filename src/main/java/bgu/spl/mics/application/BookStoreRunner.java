package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner implements Serializable {

    //this long int will act as an ID for serialization and deserialization of the class. I don't know if it's really needed.
    private static final long serialVersionUID = 1234L;
    private BookInventoryInfo[] inventory;
    // need to add all the other passive object when time comes :)

    public static void main(String[] args) {
        deserialize();

    }

    private static void deserialize() {
                   // FileInputStream fileIn = new FileInputStream("input.json");
            //ObjectInputStream objIn = new ObjectInputStream(fileIn);


    }
}
