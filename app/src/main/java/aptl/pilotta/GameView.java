package aptl.pilotta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import aptl.pilotta.game.deck.Card;
import aptl.pilotta.game.deck.Kind;
import aptl.pilotta.game.deck.Value;
import aptl.pilotta.game.utils.Utils;

/**
 * Created by constantinos on 12/08/2014.
 */
public class GameView extends SurfaceView {

    private ArrayList<Card> cards;
    private static CardSorter cardSorter;
    private static Bitmap[] cardRes = new Bitmap[32];
    private final int cardHeight=200;
    private final int cardWidth=150;

    public GameView(Context context) {
        super(context);
        cards = new ArrayList<Card>();
        cardSorter = new CardSorter();

        for (int i = 0; i < cardRes.length; i++) {
            cardRes[i] = BitmapFactory.decodeResource(getResources(), Utils.imageResources[i]);
            cardRes[i] = Bitmap.createScaledBitmap(cardRes[i], cardWidth, cardHeight, false);
        }

    }

    public void addCard(Card card) {
        cards.add(card);
        Collections.sort(cards,cardSorter);

    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawARGB(255,52,124,23);
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int offsetX=((screenWidth-((cards.size())*cardWidth))/2);
        int offsetY = screenHeight-(cardHeight);
        for (Card c : cards) {
            Bitmap map = cardRes[c.getImageRes()];
            offsetX += cardWidth;
            canvas.drawBitmap(map,offsetX,offsetY,null);

        }
        invalidate();
    }

    private class CardSorter implements Comparator<Card> {

        @Override
        public int compare(Card lhs, Card rhs) {

            Kind k1 = lhs.getKind();
            Kind k2 = rhs.getKind();
            Value v1 = lhs.getValue();
            Value v2 = rhs.getValue();

            if (sortKind(k1) < sortKind(k2)) {
                return -1;
            } else if (sortKind(k1) == sortKind(k2)) {
                if (sortValue(v1) < sortValue(v2)) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
        public boolean equals(Object object) {
            return false;
        }

        private int sortKind(Kind k) {
            switch (k) {
                case CLUBS:
                    return 1;
                case HEARTS:
                    return 2;
                case SPADES:
                    return 3;
                case DIAMONDS:
                    return 4;
                default:
                    return 5;
            }
        }

        private int sortValue(Value v) {
            switch (v) {
                case ACE:
                    return 1;
                case KING:
                    return 2;
                case QUEEN:
                    return 3;
                case JACK:
                    return 4;
                case TEN:
                    return 5;
                case NINE:
                    return 6;
                case EIGHT:
                    return 7;
                case SEVEN:
                    return 8;
                default:
                    return 9;
            }
        }

    }
}
