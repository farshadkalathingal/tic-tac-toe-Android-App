package com.example.mytictactoe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerNameWithComputer extends AppCompatActivity {

    private SharedPreferences preferences;
    HomeWatcher mHomeWatcher;
    private boolean musciStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name_with_computer);
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
    public void submitName(View view) {
        EditText player1NameBox = (EditText) findViewById(R.id.player1);

        String player1NameText = player1NameBox.getText().toString();

        String number = "999";



//        if(player1NameText.equals("")||player2NameText.equals("")){
//            toastMessage("Please enter names of both the players");
//            return;
//        }
        if(player1NameText.equals("")){
            player1NameText="Human";
            player1NameBox.setText("Human");
        }

        Intent intent = new Intent(this,PlayGameWithComputer.class);
        intent.putExtra("Player 1",player1NameText);
        intent.putExtra("Number",number);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
            finish();
        }
    }
//    private void toastMessage(String string)
//    {
//        Context context = getApplicationContext();
//        int duration = Toast.LENGTH_SHORT;
//        Toast toast = Toast.makeText(context, string,duration);
//        toast.show();
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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
