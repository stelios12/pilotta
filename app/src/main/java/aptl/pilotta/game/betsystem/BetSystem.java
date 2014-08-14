package aptl.pilotta.game.betsystem;

import aptl.pilotta.game.deck.Kind;


public class BetSystem  {

    private int userId;
    private int betNumber;
    private Kind kind;

    protected BetSystem(int userId, int betNumber, Kind kind){
        this.userId=userId;
        this.betNumber=betNumber;
        this.kind=kind;
    }



}
