package aptl.pilotta.game.deck;

/**
 * Created by constantinos on 08/08/2014.
 */
public enum Value {
    ACE(11,11),KING(4,4),QUEEN(3,3),JACK(2,20),TEN(10,10),NINE(0,14),EIGHT(0,0);

    private int normalPoints;
    private int koziPoints;

    Value(int normalPoints,int koziPoints) {
        this.normalPoints = normalPoints;
        this.koziPoints = koziPoints;
    }

    public int getNormalPoints() {
        return normalPoints;
    }

    public int getKoziPoints() {
        return koziPoints;
    }
}
