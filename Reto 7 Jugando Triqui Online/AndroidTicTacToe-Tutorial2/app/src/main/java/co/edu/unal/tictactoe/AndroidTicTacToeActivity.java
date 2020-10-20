package co.edu.unal.tictactoe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.harding.tictactoe.TicTacToeGame;

public class AndroidTicTacToeActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    //Represents the internal state of the game
    private TicTacToeGame mGame;
    private boolean isChallengingPlayer = false;

    //View Making the board
    private BoardView mBoardView;

    // Various text displayed
    private TextView mInfoTextView;
    private TextView mInfoTextView2;
    private TextView mInfoTextView3;

    //New Game button
    private Button newGameButton;

    //Difficulty Button
    private Button difficultyButton;

    //Quit Button
    private Button quitButton;

    //Sound Effects
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;
    private boolean mSoundOn = true;


    //Check if game is over
    private boolean mGameOver=false;
    private boolean mTurn = true;

    //Keep track of who is the winner
    private int playerWins;
    private int machineWins;

    //Keep track of who is playing
    //private char curplayer;

    //SharedPreferences
    private SharedPreferences mPrefs;

    //FireBase Connection
    private String uuidPlayer;
    private String keyGame;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference gamesRef = database.getReference("games");

    // Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;

            if (!mGameOver && mTurn) {
                if (setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {

                    if (mSoundOn) {
                        try {
                            mHumanMediaPlayer.start(); // Play the sound effect
                        } catch (Exception e) {

                        }
                    }

                    int winner = mGame.checkForWinner();
                    gamesRef.child(keyGame).child("board").setValue(new String(mGame.getBoardState()));
                    mTurn = false;

                    if (winner == 0) {
                        //mInfoTextView.setText(R.string.turn_computer);
                        //turnComputer();
                        mInfoTextView.setText("Turno del oponente");
                    } else {
                        endturn(winner);
                        gamesRef.child(keyGame).child("state").setValue("finalized");
                    }
                }
            }
            // So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGame = new TicTacToeGame();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = mPrefs.getBoolean("sound", true);
        String difficultyLevel = mPrefs.getString("difficulty_level",
                getResources().getString(R.string.difficulty_harder));
        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        else
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);


        newGameButton = (Button) findViewById(R.id.newGame);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewGame();
            }
        });

        difficultyButton = (Button) findViewById(R.id.difficulty);
        difficultyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDifficultyMenu(view);
            }
        });

        quitButton = (Button) findViewById(R.id.quit);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuitMenu();
            }
        });

        mInfoTextView = (TextView) findViewById(R.id.information);
        mInfoTextView2 = (TextView) findViewById(R.id.information2);
        mInfoTextView3 = (TextView) findViewById(R.id.information3);
        mInfoTextView3.setText("");

        playerWins = 0;
        machineWins = 0;

//        curplayer = TicTacToeGame.HUMAN_PLAYER;

        mBoardView = findViewById(R.id.board);
        mBoardView.setGame(mGame);

        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);

        uuidPlayer = getIntent().getStringExtra("uuidPlayer");
        keyGame = getIntent().getStringExtra("keyGame");
        String StateChallengingPlayer = getIntent().getStringExtra("isChallengingPlayer");

        if(StateChallengingPlayer.equals("true")) {
            isChallengingPlayer = true;
            mInfoTextView.setText("It's The opponent's turn.");
            gamesRef.child(keyGame).child("uuidChallengingPlayer").setValue(uuidPlayer);
            gamesRef.child(keyGame).child("state").setValue("inprogress");
            mGame.HUMAN_PLAYER = 'O';
            mGame.COMPUTER_PLAYER = 'X';
            mTurn = false;
        } else {
            isChallengingPlayer = false;
            mInfoTextView.setText("It's your turn.");
            mGame.HUMAN_PLAYER = 'X';
            mGame.COMPUTER_PLAYER = 'O';
        }

        gamesRef.child(keyGame).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!mTurn) {
                    Game game = dataSnapshot.getValue(Game.class);

                    if(game.board.equals(new String(mGame.getBoardState()))) {
                        return;
                    }

                    mGame.setBoardState(game.board.toCharArray());
                    System.out.println(game.board);

                    mBoardView.invalidate();
                    if (mSoundOn) {
                        try {
                            mComputerMediaPlayer.start(); // Play the sound effect
                        } catch (Exception e) {
                        }
                    }
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText("It's your turn.");
                    } else {
                        endturn(winner);

                    }

                    mTurn = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        } );

        startNewGame();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pc);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_CANCELED){
            mSoundOn = mPrefs.getBoolean("sound", true);

            String difficultyLevel = mPrefs.getString("difficulty_level",
                    getResources().getString(R.string.difficulty_harder));

            if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
            else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
            else
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

            mInfoTextView2.setText(getDifficultyText());
        }
    }

    public void showDifficultyMenu(View v) {
//        PopupMenu popupMenu = new PopupMenu(this, v);
//        popupMenu.setOnMenuItemClickListener(this);
//        popupMenu.inflate(R.menu.difficulty_menu);
//        popupMenu.show();
        startActivityForResult(new Intent(this, Settings.class), 0);
    }

    public void showQuitMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AndroidTicTacToeActivity.this);
        builder.setMessage(R.string.quit_question)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AndroidTicTacToeActivity.this.finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.easy:
                Toast.makeText(AndroidTicTacToeActivity.this, "Easy Selected", Toast.LENGTH_SHORT).show();
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                break;
            case R.id.hard:
                Toast.makeText(this, "Hard Selected", Toast.LENGTH_SHORT).show();
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                break;
            case R.id.expert:
                Toast.makeText(this, "Expert Selected", Toast.LENGTH_SHORT).show();
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                break;
            default:
                return false;
        }
        mInfoTextView2.setText(getDifficultyText());
        return true;
    }

    private void startNewGame() {
        mGame.clearBoard();
        mGameOver = false;

        mBoardView.invalidate();   // Redraw the board

//        curplayer = TicTacToeGame.HUMAN_PLAYER;
        if(!isChallengingPlayer){
            String text = "You go first.";
            mInfoTextView.setText(text);
        }
        mInfoTextView2.setText(getDifficultyText());
    }

    private String getDifficultyText() {
        if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Easy) {
            return "Easy";
        } else if (mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Harder) {
            return "Hard";
        } else {
            return "Expert";
        }
    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redraw the board
            return true;
        }
        return false;
    }

    private void endturn(int winner) {
        String text;

        if (winner == 0) {
            text = "It's your turn.";
//            curplayer = TicTacToeGame.HUMAN_PLAYER;
        } else if (winner == 1)
            text = "It's a tie!";
        else if (winner == 2)
            text = mPrefs.getString("victory_message", getResources().getString(R.string.result_human_wins));
        else
            text = "Opponent won!";

        mInfoTextView.setText(text);
        mGameOver = winner != 0;
    }

    private void turnComputer() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                int move = mGame.getComputerMove();
                setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                if(mSoundOn)
                    mComputerMediaPlayer.start();

                int winner = mGame.checkForWinner();

                endturn(winner);
            }
        }, 1000);
    }
}