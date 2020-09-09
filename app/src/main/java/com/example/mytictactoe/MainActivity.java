package com.example.mytictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    DialogFragment dialog;
    public static String SETTINGS_KEY = "AndroidTicTacToePreferences";
    private SharedPreferences preferences;
    HomeWatcher mHomeWatcher;
    private boolean musciStatus;
    public TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // ((Button) findViewById(R.id.singleplayer)).setClickable(false);

        preferences = getSharedPreferences(this.SETTINGS_KEY, MODE_PRIVATE);
        musciStatus = preferences.getBoolean("music", true);
        //button = findViewById(R.id.musicButton);
        //BIND Music Service
        if (musciStatus) {
            playMusic();
        }
    }

    public void playTwoPGame(View view) {
        Intent intent = new Intent(this,PlayerName.class);
        if(intent.resolveActivity(getPackageManager())!=null) {
            startActivity(intent);
            finish();
            dialog.dismiss();
        }

    }

    public void playSinglePGame(View view) {
        Intent intent = new Intent(this, PlayerNameWithComputer.class);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
            finish();
            dialog.dismiss();
        }
    }

    public void settings(View view) {
        dialog = new SettingsDialog();
        dialog.show(getFragmentManager(), "Settings Dialog");
    }

    public void gameSelection(View view) {

        dialog = new GameDialog();
        dialog.show(getFragmentManager(), "Game Dialog");
    }


    public void playMusic() {
        //textView.setText("Music ON");
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

    public void musicOn(View view) {
     //  System.out.println(musciStatus);
       SharedPreferences.Editor editor = preferences.edit();
       musciStatus = preferences.getBoolean("music", true);
       if(musciStatus) {
           editor.putBoolean("music",false);
           //UNBIND music service
           doUnbindService();
           Intent music = new Intent();
           music.setClass(this,MusicService.class);
           stopService(music);
           //textView.setText("Music OFF");

       } else {
           editor.putBoolean("music",true);
           musciStatus = preferences.getBoolean("music", true);
           playMusic();
           //textView.setText("Music ON");
       }
       editor.apply();
    }

    public static class GameDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Activity activity = getActivity();

            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_game_selection, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            return builder.setView(layout)
                    .setTitle("New Game")
                    .create();
        }
    }
    public static class SettingsDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();

            final LayoutInflater inflater = activity.getLayoutInflater();
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final View layout = inflater.inflate(R.layout.dialog_settings_new, null);

           // SharedPreferences preferences = activity.getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);
          //  CheckBox toggleHighlight = layout.findViewById(R.id.toggleHighlight);
          //  toggleHighlight.setChecked(preferences.getBoolean("music", true));
            //CheckBox toggleHints = layout.findViewById(R.id.toggleHints);
         //   toggleHints.setChecked(preferences.getBoolean("hints", true));
           // CheckBox toggleError = layout.findViewById(R.id.toggleErrorDetection);
           // toggleError.setChecked(preferences.getBoolean("autoError", true));

            return builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            })
                    .setTitle("Settings")
                    .setView(layout)
                    .create();
        }
    }
    /*public void settingsListener(View view) {
        if (view instanceof CheckBox) {
            SharedPreferences.Editor editor = preferences.edit();
            CheckBox setting = (CheckBox) view;

            switch (view.getId()) {
                case R.id.toggleHighlight: {
                    editor.putBoolean("music", setting.isChecked());
                    break;
                }
               /* case R.id.toggleHints: {
                    editor.putBoolean("hints", setting.isChecked());
                    break;
                }
                case R.id.toggleErrorDetection: {
                    editor.putBoolean("autoError", setting.isChecked());
                    break;
                }
            }

            editor.apply();
        }
    }*/
//    public void exitGame(View view){
//        finish();
//        System.exit(0);
//    }

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
    @Override
    public void onBackPressed() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        builder.setMessage("Are you sure you want to Exit?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                // moveTaskToBack(true);
                //   android.os.Process.killProcess(andoid.os.Process.myPid());
                System.exit(1);
                finish();
                MainActivity.super.onBackPressed();

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

}
