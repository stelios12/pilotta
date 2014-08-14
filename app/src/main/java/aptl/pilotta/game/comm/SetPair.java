package aptl.pilotta.game.comm;

import java.io.Serializable;

/**
 * Created by constantinos on 14/08/2014.
 */
public class SetPair implements Serializable{

    private final String topPlayer;
    private final String leftPlayer;
    private final String rightPlayer;

    public SetPair (String topPlayer,String leftPlayer,String rightPlayer) {
        this.topPlayer = topPlayer;
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
    }

    public String getTopPlayer() {
        return topPlayer;
    }

    public String getRightPlayer() {
        return rightPlayer;
    }

    public String getLeftPlayer() {
        return leftPlayer;
    }
}
