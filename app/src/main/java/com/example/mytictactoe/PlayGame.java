package com.example.mytictactoe;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class PlayGame extends AppCompatActivity {
    int turn = 1;
    int win = 0;
    int gamov = 0;
    int flagEndGame=0;
    int flag;
    String displayTurn;
    GridLayout grid;
    Button playBoard[][] = new Button[3][3];
    TextView playerTurn;
    String player1Name;
    String player2Name;
    String numberText;
    int number;
    int counter = 0;
    int player1Win = 0, player2Win = 0, draw = 0;
    int flipValue=0;
    AlertDialog.Builder builder;
    private SharedPreferences preferences;
    HomeWatcher mHomeWatcher;
    private boolean musciStatus;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
  //  private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);
        playerTurn = (TextView) findViewById(R.id.player);
        builder = new AlertDialog.Builder(this);
        Intent intent = getIntent();
        player1Name = intent.getExtras().getString("Player 1");
        player2Name = intent.getExtras().getString("Player 2");
        numberText = intent.getExtras().getString("Number");
        number = Integer.parseInt(numberText);
        grid = (GridLayout) findViewById(R.id.grid);
        displayTurn=player1Name + "'s turn (X)";
        playerTurn.setText(displayTurn);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                playBoard[i][j] = (Button) grid.getChildAt(3 * i + j);
            }
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
      //  client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        preferences = getSharedPreferences(MainActivity.SETTINGS_KEY, MODE_PRIVATE);
        musciStatus = preferences.getBoolean("music", true);
        //BIND Music Service
        if( musciStatus ) {
            playMusic();
        }
    }
    public void playMusic() {
        doBindService();
        Intent music = new Intent();
        music.setClass(this, MusicService.class);
        startService(music);

        //Start HomeWatcher
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }

            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();
    }
    public void playmove(View view) {
        int index = grid.indexOfChild(view);
        int i = index / 3;
        int j = index % 3;
        flag = 0;
        if (turn == 1 && gamov == 0 && !(playBoard[i][j].getText().toString().equals("X")) && !(playBoard[i][j].getText().toString().equals("O"))) {


            if(flipValue==0){
                displayTurn=player2Name + "'s turn (O)";
                playerTurn.setText(displayTurn);
            }
            else if(flipValue==1){
                displayTurn=player1Name + "'s turn (O)";
                playerTurn.setText(displayTurn);
            }
            playBoard[i][j].setText("X");
            turn = 2;

        } else if (turn == 2 && gamov == 0 && !(playBoard[i][j].getText().toString().equals("X")) && !(playBoard[i][j].getText().toString().equals("O"))) {
            if(flipValue==0){
                displayTurn=player1Name + "'s turn (X)";
                playerTurn.setText(displayTurn);
            }
            else if(flipValue==1){
                displayTurn=player2Name + "'s turn (X)";
                playerTurn.setText(displayTurn);
            }
            playBoard[i][j].setText("O");
            turn = 1;

        }
        checkWin();
        if (gamov == 1) {
            if (win == 1) {
                builder.setMessage(player1Name + " wins!").setTitle("Game over");
                if(flagEndGame==0){
                    player1Win++;
                    counter++;
                }


            } else if (win == 2) {
                builder.setMessage(player2Name + " wins!").setTitle("Game over");
                if(flagEndGame==0){
                    player2Win++;
                    counter++;
                }

            }
            flagEndGame=1;
            builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    newGame(new View(getApplicationContext()));
                }

            }).setNegativeButton("Result", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(getApplicationContext(), SeriesResult.class);
                    intent.putExtra("Player 1 Wins", player1Win);
                    intent.putExtra("Player 2 Wins", player2Win);
                    intent.putExtra("Draws", draw);
                    intent.putExtra("Player 1 Name", player1Name);
                    intent.putExtra("Player 2 Name", player2Name);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                        finish();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();



        }
        if (gamov == 0) {
            for (i = 0; i < 3; i++) {
                for (j = 0; j < 3; j++) {
                    if (!playBoard[i][j].getText().toString().equals("X") && !playBoard[i][j].getText().toString().equals("O")) {
                        flag = 1;
                        break;

                    }
                }
            }
            if (flag == 0) {
                builder.setMessage("It's a draw!").setTitle("Game over");
                if(flagEndGame==0){
                    counter++;
                    draw++;
                }
                flagEndGame=1;
                builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        newGame(new View(getApplicationContext()));
                    }

                }).setNegativeButton("Result", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getApplicationContext(), SeriesResult.class);
                        intent.putExtra("Player 1 Wins", player1Win);
                        intent.putExtra("Player 2 Wins", player2Win);
                        intent.putExtra("Draws", draw);
                        intent.putExtra("Player 1 Name", player1Name);
                        intent.putExtra("Player 2 Name", player2Name);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();


            }


        }


    }

    public void newGame(View view) {

        win = 0;
        gamov = 0;
        turn=1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                playBoard[i][j].setText(" ");
                playBoard[i][j].setTextColor(Color.BLACK);
            }
        }

        if(flipValue==0){
            if(flagEndGame==1){
                flipValue=1;
                displayTurn=player2Name + "'s turn (X)";
                playerTurn.setText(displayTurn);
            }
            else{
                displayTurn=player1Name + "'s turn (X)";
                playerTurn.setText(displayTurn);
            }


        }
        else if(flipValue==1 ){
            if(flagEndGame==1){
                flipValue=0;
                displayTurn=player1Name + "'s turn (X)";
                playerTurn.setText(displayTurn);
            }
            else{
                displayTurn=player2Name + "'s turn (X)";
                playerTurn.setText(displayTurn);
            }



        }
        flagEndGame=0;
    }

    public void checkWin() {
        for (int i = 0; i < 3; i++) {
            if (playBoard[i][0].getText().toString().equals(playBoard[i][1].getText().toString()) && playBoard[i][0].getText().toString().equals(playBoard[i][2].getText().toString())) {
                if (playBoard[i][0].getText().toString().equals("X")) {
                    gamov = 1;
                    if(flipValue==0)
                        win = 1;
                    else if(flipValue==1)
                        win=2;


                } else if (playBoard[i][0].getText().toString().equals("O")) {
                    gamov = 1;
                    if(flipValue==0)
                        win = 2;
                    else if(flipValue==1)
                        win=1;

                }
                if (!playBoard[i][0].getText().toString().equals(" ")) {
                    playBoard[i][0].setTextColor(Color.RED);
                    playBoard[i][1].setTextColor(Color.RED);
                    playBoard[i][2].setTextColor(Color.RED);

                }

            }
            if (playBoard[0][i].getText().toString().equals(playBoard[1][i].getText().toString()) && playBoard[0][i].getText().toString().equals(playBoard[2][i].getText().toString())) {
                if (playBoard[0][i].getText().toString().equals("X")) {
                    gamov = 1;
                    if(flipValue==0)
                        win = 1;
                    else if(flipValue==1)
                        win=2;


                } else if (playBoard[0][i].getText().toString().equals("O")) {
                    gamov = 1;
                    if(flipValue==0)
                        win = 2;
                    else if(flipValue==1)
                        win=1;

                }
                if (!playBoard[0][i].getText().toString().equals(" ")) {
                    playBoard[0][i].setTextColor(Color.RED);
                    playBoard[1][i].setTextColor(Color.RED);
                    playBoard[2][i].setTextColor(Color.RED);
                }

            }


        }
        if (playBoard[0][0].getText().toString().equals(playBoard[1][1].getText().toString()) && playBoard[0][0].getText().toString().equals(playBoard[2][2].getText().toString())) {
            if (playBoard[0][0].getText().toString().equals("X")) {
                gamov = 1;
                if(flipValue==0)
                    win = 1;
                else if(flipValue==1)
                    win=2;


            } else if (playBoard[0][0].getText().toString().equals("O")) {
                gamov = 1;
                if(flipValue==0)
                    win = 2;
                else if(flipValue==1)
                    win=1;

            }
            if (!playBoard[0][0].getText().toString().equals(" ")) {
                playBoard[0][0].setTextColor(Color.RED);
                playBoard[1][1].setTextColor(Color.RED);
                playBoard[2][2].setTextColor(Color.RED);
            }


        }
        if (playBoard[0][2].getText().toString().equals(playBoard[1][1].getText().toString()) && playBoard[0][2].getText().toString().equals(playBoard[2][0].getText().toString())) {
            if (playBoard[0][2].getText().toString().equals("X")) {
                gamov = 1;
                if(flipValue==0)
                    win = 1;
                else if(flipValue==1)
                    win=2;


            } else if (playBoard[0][2].getText().toString().equals("O")) {
                gamov = 1;
                if(flipValue==0)
                    win = 2;
                else if(flipValue==1)
                    win=1;

            }
            if (!playBoard[2][0].getText().toString().equals(" ")) {
                playBoard[2][0].setTextColor(Color.RED);
                playBoard[1][1].setTextColor(Color.RED);
                playBoard[0][2].setTextColor(Color.RED);
            }


        }
    }

   /* @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "PlayGame Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.mytictactoe/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "PlayGame Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.mytictactoe/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }*/
    public void newMatch(View view){
        Intent intent = new Intent(this,PlayerName.class);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setMessage("Are you Sure, Exit this Match?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(PlayGame.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    //Bind/Unbind music service
    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        //Detect idle screen
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //UNBIND music service
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);

    }
}
