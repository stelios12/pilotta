package aptl.pilotta.game.utils;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {

    //Serializes an object
    public static byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(obj);
            return b.toByteArray();
        } catch (IOException e) {
            Log.d("SERIALIZE","SERIALIZATION UNSUCCESSFUL");
        }
        return null;
    }

    //Deserializes an obest
    public static Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            return o.readObject();
        } catch (Exception e) {
            Log.d("SERIALIZE","DESERIALIZATION UNSUCCESSFUL");
        }
        return null;
    }
}