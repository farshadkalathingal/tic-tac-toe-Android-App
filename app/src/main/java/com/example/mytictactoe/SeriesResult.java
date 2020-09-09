package com.example.mytictactoe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SeriesResult extends AppCompatActivity {
    private SharedPreferences preferences;
    HomeWatcher mHomeWatcher;
    private boolean musciStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_series_result);
        Intent intent = getIntent();
        String player1Wins = String.valueOf(intent.getExtras().getInt("Player 1 Wins"));
        String player2Wins = String.valueOf(intent.getExtras().getInt("Player 2 Wins"));
        String draws = String.valueOf(intent.getExtras().getInt("Draws"));
        String player1Name = intent.getExtras().getString("Player 1 Name");
        String player2Name = intent.getExtras().getString("Player 2 Name");
        TextView player1NameView = (TextView) findViewById(R.id.p1name);
        TextView player2NameView = (TextView) findViewById(R.id.p2name);
        TextView player1WinsView = (TextView) findViewById(R.id.p1wins);
        TextView player2WinsView = (TextView) findViewById(R.id.p2wins);
        TextView drawsView = (TextView) findViewById(R.id.draws);


        if(Integer.parseInt(player1Wins)>Integer.parseInt(player2Wins)){
            player1NameView.setTextColor(Color.GREEN);
            player2NameView.setTextColor(Color.RED);
        }
        else if(Integer.parseInt(player1Wins)<Integer.parseInt(player2Wins)){
            player2NameView.setTextColor(Color.GREEN);
            player1NameView.setTextColor(Color.RED);
        }
        else
        {
            player2NameView.setTextColor(Color.YELLOW);
            player1NameView.setTextColor(Color.YELLOW);
        }
        player1NameView.setText(player1Name);
        player2NameView.setText(player2Name);
        player1WinsView.setText(player1Wins);
        player2WinsView.setText(player2Wins);
        drawsView.setText(draws);


        preferences = getSharedPreferences(MainActivity.SETTINGS_KEY, MODE_PRIVATE);
        musciStatus = preferences.getBoolean("music", true);
        if( musciStatus ) {
            //BIND Music Service
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
    public void onClickContinue(View view){
        Intent intent = new Intent(this,MainActivity.class);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
            finish();
        }
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
