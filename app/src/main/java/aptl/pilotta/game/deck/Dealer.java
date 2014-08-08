package aptl.pilotta.game.deck;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by constantinos on 08/08/2014.
 */
public class Dealer {

    private final ArrayList<Card> deck;

    public Dealer(){
        deck = new ArrayList<Card>();
        populateDeck();
    }

    public void resetDeck() {
        populateDeck();
    }

    private void populateDeck() {
        deck.clear();

        int imageRes = 0;
        for (Value v : Value.values()) {
            for (Kind k : Kind.values()) {
                Card c = new Card(k,v,imageRes);
                deck.add(c);
                imageRes++;
            }
        }
    }

    public Card giveCard() {
        Random rand = new Random();
        int index = rand.nextInt(deck.size());
        Card c = deck.get(index);
        deck.remove(c);
        return c;
    }

}
