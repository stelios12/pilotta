package aptl.pilotta.game.deck;

/**
 * Created by constantinos on 08/08/2014.
 */
public class Card {

    private final Kind kind;
    private final Value value;
    private final int imageRes;

    public Card(Kind kind,Value value,int imageRes) {
        this.kind = kind;
        this.value = value;
        this.imageRes = imageRes;
    }

    public final Kind getKind() {
        return kind;
    }

    public final Value getValue() {
        return value;
    }

    public final int getImageRes() {return imageRes; }

    @Override
    public String toString() {
        return value.toString() + " of " + kind.toString();
    }
}
