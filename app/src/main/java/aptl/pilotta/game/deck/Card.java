package aptl.pilotta.game.deck;

import java.io.Serializable;

/**
 * Created by constantinos on 08/08/2014.
 */
public class Card implements Serializable {

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

    public final int getImageRes() {
        return imageRes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (imageRes != card.imageRes) return false;
        if (kind != card.kind) return false;
        if (value != card.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + imageRes;
        return result;
    }

    @Override
    public String toString() {
        return value.toString() + " of " + kind.toString();
    }
}
