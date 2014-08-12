package aptl.pilotta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.ResourceBundle;

import aptl.pilotta.GameActivity;
import aptl.pilotta.R;
import aptl.pilotta.game.deck.Card;
import aptl.pilotta.game.utils.Utils;

/**
 * Created by constantinos on 12/08/2014.
 */
public class GameView extends SurfaceView {

    private ArrayList<Card> cards;
    private static Bitmap[] cardRes = new Bitmap[32];

    public GameView(Context context) {
        super(context);
        cards = new ArrayList<Card>();

        for (int i = 0; i < cardRes.length; i++) {
            cardRes[i] = BitmapFactory.decodeResource(getResources(), Utils.imageResources[i]);
        }
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawARGB(255,52,124,23);
        int width = getWidth();
        int height = getHeight();
        int offset = width/10;
        for (Card c : cards) {
            Bitmap map = cardRes[c.getImageRes()];
            Bitmap resized = Bitmap.createScaledBitmap(map, 150, 200, false);
            canvas.drawBitmap(resized,offset,0,null);
            offset += width/11;
        }

    }
}
