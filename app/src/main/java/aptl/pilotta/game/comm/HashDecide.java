package aptl.pilotta.game.comm;

import java.io.Serializable;

/**
 * Created by constantinos on 12/08/2014.
 */
public class HashDecide implements Serializable {

    private final int hashcode;

    public HashDecide(int hashCode) {
        hashcode = hashCode;
    }

    public final int getHashcode() {
        return  hashcode;
    }
}
