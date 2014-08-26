package aptl.pilotta.game.betsystem;

import aptl.pilotta.game.deck.Kind;

public class Bet {

    private final Kind kind;
    private final int lastBetScore;

    private   Bet (Kind kind, int lastBetScore){
        this.kind=kind;
        this.lastBetScore=lastBetScore;
    }
    
    public final Kind getKind(){
        return kind;
    }
    public final int getLastBetScore(){
        return lastBetScore;
    }


}
