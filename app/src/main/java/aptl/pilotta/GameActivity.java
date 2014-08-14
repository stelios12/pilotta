package aptl.pilotta;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;
import java.util.List;

import aptl.pilotta.game.comm.HashDecide;
import aptl.pilotta.game.comm.PlayerCard;
import aptl.pilotta.game.deck.Card;
import aptl.pilotta.game.deck.Dealer;
import aptl.pilotta.game.utils.Serializer;

public class GameActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoomUpdateListener,
        RoomStatusUpdateListener,
        RealTimeMessageReceivedListener,
        RealTimeMultiplayer.ReliableMessageSentCallback,
        View.OnClickListener {

    private static final String TAG = "GameActivity";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;


    /* Request codes */
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // are we already playing?
    boolean mPlaying = false;

    // at least 2 players required for our game
    final static int MIN_PLAYERS = 4;

    private static String mRoomId;
    private static String mMyId;
    private static ArrayList<Participant> mParticipants;
    private static int myHashcode;
    private static int times = 0;
    private static boolean decider = true;
    private static GameView gameView;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
        initialize();
    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */

    private void initialize(){
        setContentView(R.layout.main_screen);
        findViewById(R.id.bExit).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.bLogOut).setOnClickListener(this);
        findViewById(R.id.bChallengeFriends).setOnClickListener(this);
        findViewById(R.id.bInbox).setOnClickListener(this);
        findViewById(R.id.bQuickGame).setOnClickListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.bLogOut).setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Games.API)
                    .addApi(Plus.API)
                    .addScope(Games.SCOPE_GAMES)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    // Optionally, add additional APIs and scopes if required.
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle extras;
        RoomConfig roomConfig;

        switch (requestCode) {
        case REQUEST_CODE_RESOLUTION:
            retryConnecting();
            break;
            case RC_SELECT_PLAYERS:
                if (resultCode != Activity.RESULT_OK) {
                    // user canceled
                    return;
                }

                // get the invitee list
                extras = data.getExtras();
                final ArrayList<String> invitees =
                        data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

                // get auto-match criteria
                Bundle autoMatchCriteria = null;
                int minAutoMatchPlayers =
                        data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
                int maxAutoMatchPlayers =
                        data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

                if (minAutoMatchPlayers > 0) {
                    autoMatchCriteria =
                            RoomConfig.createAutoMatchCriteria(
                                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
                } else {
                    autoMatchCriteria = null;
                }

                // create the room and specify a variant if appropriate
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.addPlayersToInvite(invitees);
                if (autoMatchCriteria != null) {
                    roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
                }
                roomConfig = roomConfigBuilder.build();
                Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            case RC_WAITING_ROOM:
                if (resultCode == Activity.RESULT_OK) {
                    startGame();
                    // (start game)
                }
                else if (resultCode == Activity.RESULT_CANCELED) {
                    // Waiting room was dismissed with the back button. The meaning of this
                    // action is up to the game. You may choose to leave the room and cancel the
                    // match, or do something else like minimize the waiting room and
                    // continue to connect in the background.

                    // in this example, we take the simple approach and just leave the room:
                    Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player wants to leave the room.
                    Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                break;
            case RC_INVITATION_INBOX:
                if (resultCode != Activity.RESULT_OK) {
                    // canceled
                    return;
                }

                // get the selected invitation
                extras = data.getExtras();
                Invitation invitation =
                        extras.getParcelable(Multiplayer.EXTRA_INVITATION);

                // accept it!
                roomConfig = makeBasicRoomConfigBuilder()
                        .setInvitationIdToAccept(invitation.getInvitationId())
                        .build();
                Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfig);

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // go to game screen
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // show sign-out button, hide the sign-in button
        if (findViewById(R.id.sign_in_button) != null) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.bLogOut).setVisibility(View.VISIBLE);
        }

        Log.i(TAG, "GoogleApiClient connected");
        // TODO: Start making API requests.

        if (connectionHint != null) {
            Invitation inv =
                    connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder =
                        makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // go to game screen
            }
        }
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.bLogOut).setVisibility(View.GONE);

        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    retryConnecting();
                }
            }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){

            case (R.id.bExit):
                finish();
                break;
            case (R.id.bChallengeFriends):    // enter the rest of the button attributes
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 3, 3);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                break;
            case (R.id.sign_in_button):
                beginUserInitiatedSignIn();
                break;
            case (R.id.bLogOut):
                if (mGoogleApiClient.isConnected()){
                    signOut();
                    // show sign-in button, hide the sign-out button
                    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                    findViewById(R.id.bLogOut).setVisibility(View.GONE);
                }
                break;
            case (R.id.bInbox):
                if (mGoogleApiClient.isConnected()) {
                    // launch the intent to show the invitation inbox screen
                    intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                    startActivityForResult(intent, RC_INVITATION_INBOX);
                }
                break;
            case (R.id.bQuickGame):
                if (mGoogleApiClient.isConnected()){
                    startQuickGame();
                }
                break;
            default:
                Log.d("on Click", "invalid Opeation");
        }

    }

    /* Sign in the user */
    private void beginUserInitiatedSignIn() {
        if (mGoogleApiClient.isConnected()) {
            return;
        } else if (mGoogleApiClient.isConnecting()) {
            Log.d("Connection","Already trying to sign in");
            return;
        }
        mGoogleApiClient.connect();
    }

    /* Sign out and disconnect from the APIs. */
    private void signOut() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        // for Plus, "signing out" means clearing the default account and
        // then disconnecting
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);

        // For the games client, signing out means calling signOut and
        // disconnecting
        Games.signOut(mGoogleApiClient);

        // Ready to disconnect
        mGoogleApiClient.disconnect();

        Log.d("Connection", "Google API Client disconnected");
    }


    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {
        // peer declined invitation -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
        // peer left -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onConnectedToRoom(Room room) {
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));


        // print out the list of participants (for debug purposes)
        Log.d("wim", "Room ID: " + mRoomId);
        Log.d("wim", "My ID " + mMyId);
        Log.d("wim", "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {
        mParticipants = room.getParticipants();
        if (mPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            // start game!
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {
        mParticipants = room.getParticipants();
        if (mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
        }
        else if (shouldCancelGame(room)) {
            // cancel the game
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            initialize();
        }
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    @Override
    public void onRoomCreated(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }

        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));


        // get waiting room intent
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(intent, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }

        // get waiting room intent
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(intent, RC_WAITING_ROOM);

    }

    @Override
    public void onLeftRoom(int i, String s) {
        times = 0;
        decider = true;
        recreate();
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        if (i != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
        }
    }

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(3, 3, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen
    }

    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    // returns whether there are enough players to start the game
    private boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    private boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return true;
    }

    private void startGame() {

        gameView = new GameView(this);
        gameView.setBackgroundResource(R.drawable.green_back);
        setContentView(gameView);

        myHashcode = Games.Players.getCurrentPlayer(mGoogleApiClient).getPlayerId().hashCode();
        HashDecide decide = new HashDecide(myHashcode);
        byte[] encoded = Serializer.serialize(decide);
        for (Participant p : mParticipants) {
            if (!mMyId.equals(p.getParticipantId())) {
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient,this,encoded,mRoomId,p.getParticipantId());
            }
        }

    }




    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {

        byte[] data = realTimeMessage.getMessageData();
        Object decoded = Serializer.deserialize(data);

        if (decoded instanceof HashDecide) {
            times++;
            myHashcode = Games.Players.getCurrentPlayer(mGoogleApiClient).getPlayerId().hashCode();
            HashDecide hd = (HashDecide)decoded;
            decider = decider &&  myHashcode > hd.getHashcode();
            if (times == 3) {
                if (decider) {
                    Log.d("decide","master");
                    Dealer d = new Dealer();
                    Log.d("cards",Integer.toString(d.deck.size()));
                    Log.d("size",Integer.toString(mParticipants.size()));
                    Log.d("mmyid",mMyId);
                    for (Participant p : mParticipants) {
                        if (!mMyId.equals(p.getParticipantId())) {
                            for (int i = 0; i <8; i++) {
                                Card c = d.giveCard();
                                PlayerCard pc = new PlayerCard(c);
                                byte[] encoded = Serializer.serialize(pc);
                                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient,this,encoded,mRoomId,p.getParticipantId());
                            }
                            Log.d(p.getParticipantId()," gave 8 cards");
                        }
                    }
                    Log.d("Size of deck",Integer.toString(d.deck.size()));
                    //give cards to host
                    for (int i = 0; i <8; i++) {
                        Card c = d.giveCard();
                        gameView.addCard(c);
                    }
                    gameView.invalidate();
                } else {
                    Log.d("decide", "not master");
                }
            }
        } else if (decoded instanceof PlayerCard) {
            Log.d("Card","setting card");
            PlayerCard pc = (PlayerCard)decoded;
            Card c = pc.getCard();
            gameView.addCard(c);
            //gameView.invalidate();
        }
    }

    @Override
    public void onRealTimeMessageSent(int i, int i2, String s) {

    }
}
