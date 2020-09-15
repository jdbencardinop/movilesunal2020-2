package co.edu.unal.tictactoe;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import edu.harding.tictactoe.TicTacToeGame;

public class AndroidTicTacToeActivity extends Activity {

    //Represents the internal state of the game
    private TicTacToeGame mGame;

    // Buttons making up the board
    private Button[] mBoardButtons;

    // Various text displayed
    private TextView mInfoTextView;

    //New Game button
    private Button newGameButton;

    //Check if game is over
    private  boolean mGameOver;

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

        mGame = new TicTacToeGame();

        startNewGame();
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