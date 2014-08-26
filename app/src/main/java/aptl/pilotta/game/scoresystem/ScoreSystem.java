package aptl.pilotta.game.scoresystem;

import java.util.ArrayList;
import aptl.pilotta.game.deck.Card;
import aptl.pilotta.game.betsystem.Bet;
import aptl.pilotta.game.deck.Kind;
import aptl.pilotta.game.deck.Value;

/**
 *
 * Created by constantinos on 09/08/2014.
 */
public class ScoreSystem {

    protected ScoreSystem() {
    }

    //get kozi points for all cards in ones' player deck and add them up
    //perform checks for one or 2 declarations of 4-of-kind
    //return sum of cards
    private int findDeclarations(ArrayList<Card> cards) {

        int declTemp = 0;
        int declarationValue = 0;
        int finalDeclarationValue = 0;
        boolean fourOfKind = false;
        boolean pilo = false;
        boolean repilo = false;
        Kind kozi = Bet.getKind();


            for (Card c : cards) {
                declTemp += c.getValue().getKoziPoints();
                if (c.getValue().getKoziPoints() == Value.KING) {
                    repilo = true;
                }
                if (Value.KING && kozi) {
                    pilo = true;
                }
            }

            switch (declTemp) { // 4 of a kind  TODO add combo of 4ofKind
                case (80): // Jacks
                    declarationValue += 200;
                    fourOfKind = true;
                    break;
                case (64): //Nines
                    declarationValue += 150;
                    fourOfKind = true;
                    break;
                case (44): //Aces
                    declarationValue += 100;
                    fourOfKind = true;
                    break;
                case (40): //Tens
                    declarationValue += 100;
                    fourOfKind = true;
                    break;
                case (16): //Kings
                    declarationValue += 100;
                    fourOfKind = true;
                    break;
                case (12): //Queens
                    declarationValue += 100;
                    fourOfKind = true;
                    break;
                default:
                    fourOfKind = false;

            }

            if (!fourOfKind) {
                //check for cards in row
            }

        if (pilo && repilo) {
            declarationValue += 20;
        }

        return declarationValue;
    }


}



