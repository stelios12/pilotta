package aptl.pilotta.game.scoresystem;

/**
 * Created by Stelios on 15/08/2014.
 */
public class Score {

    private int team1;
    private int team2;

    protected Score(int team1, int team2){
        this.team1=team1;
        this.team2=team2;
    }

    public final int getScoreTeam1(){return team1;}

    public final int getScoreTeam2(){return team2;}
}
