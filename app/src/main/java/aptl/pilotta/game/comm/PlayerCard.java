package aptl.pilotta.game.comm;

import java.io.Serializable;

import aptl.pilotta.game.deck.Card;

/**
 * Created by constantinos on 12/08/2014.
 */
public class PlayerCard implements Serializable {

    private final Card card;

    public PlayerCard(Card card) {
        this.card = card;
    }

    public final Card getCard() {
        return card;
    }

}
