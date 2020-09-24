package co.edu.unal.tictactoe;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import edu.harding.tictactoe.TicTacToeGame;

public class AndroidTicTacToeActivity extends Activity implements PopupMenu.OnMenuItemClickListener {

    //Represents the internal state of the game
    private TicTacToeGame mGame;

    // Buttons making up the board
    private Button[] mBoardButtons;

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

    //Check if game is over
    private boolean mGameOver;

    //Keep track of who is the winner
    private int playerWins;
    private int machineWins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardButtons[0] = (Button) findViewById(R.id.one);
        mBoardButtons[1] = (Button) findViewById(R.id.two);
        mBoardButtons[2] = (Button) findViewById(R.id.three);
        mBoardButtons[3] = (Button) findViewById(R.id.four);
        mBoardButtons[4] = (Button) findViewById(R.id.five);
        mBoardButtons[5] = (Button) findViewById(R.id.six);
        mBoardButtons[6] = (Button) findViewById(R.id.seven);
        mBoardButtons[7] = (Button) findViewById(R.id.eight);
        mBoardButtons[8] = (Button) findViewById(R.id.nine);

        mInfoTextView = (TextView) findViewById(R.id.information);
        mInfoTextView2 = (TextView) findViewById(R.id.information2);
        mInfoTextView3 = (TextView) findViewById(R.id.information3);
        mInfoTextView3.setText("");

        playerWins = 0;
        machineWins = 0;

        mGame = new TicTacToeGame();

        startNewGame();
    }

    public void showDifficultyMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.difficulty_menu);
        popupMenu.show();
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
        switch (menuItem.getItemId()){
            case R.id.easy:
                Toast.makeText(AndroidTicTacToeActivity.this,"Easy Selected", Toast.LENGTH_SHORT).show();
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                break;
            case R.id.hard:
                Toast.makeText(this,"Hard Selected", Toast.LENGTH_SHORT).show();
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                break;
            case R.id.expert:
                Toast.makeText(this,"Expert Selected", Toast.LENGTH_SHORT).show();
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

        // Reset all buttons
        int i = 0;
        for (Button mBoardButton : mBoardButtons) {
            mBoardButton.setText("");
            mBoardButton.setEnabled(true);
            mBoardButton.setOnClickListener(new ButtonClickListener(i++));
        }

        String text = "You go first.";
        mInfoTextView.setText(text);
        mInfoTextView2.setText(getDifficultyText());
    }

    private String getDifficultyText(){
        if(mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Easy){
            return "Easy";
        } else if(mGame.getDifficultyLevel() == TicTacToeGame.DifficultyLevel.Harder){
            return "Hard";
        } else {
            return "Expert";
        }
    }

    private class ButtonClickListener implements View.OnClickListener {
        int location;

        public ButtonClickListener(int location) {
            this.location = location;
        }

        @Override
        public void onClick(View view) {
            if (mBoardButtons[location].isEnabled() && !mGameOver) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);

                //If no winner yet let the computer make a move
                int winner = mGame.checkForWinner();
                String text;
                if (winner == 0) {
                    text = "It's Android's turn.";
                    mInfoTextView.setText(text);

                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0)
                    text = "It's your turn.";
                else if (winner == 1)
                    text = "It's a tie!";
                else if (winner == 2)
                    text = "You won!";
                else
                    text = "Android won!";

                mInfoTextView.setText(text);
                mGameOver = winner != 0;
            }
        }

        private void setMove(char player, int location) {
            mGame.setMove(player, location);
            mBoardButtons[location].setEnabled(false);
            mBoardButtons[location].setText(String.valueOf(player));
            if (player == TicTacToeGame.HUMAN_PLAYER)
                mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
            else
                mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
        }
    }
}