package com.example.melchy.insight;


import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;

import android.view.MotionEvent;

import android.view.Window;
import android.view.WindowManager;

import android.widget.Toast;

import com.microsoft.speech.tts.Synthesizer;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends Activity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,TextToSpeech.OnInitListener{

    XmlTextToSpeech speak = new XmlTextToSpeech();

    String greetings = "Hi and welcome to insight, in here you can choose four different mode which is. Face which describe the age and gender,age,what glasses been wearing and other characteristic of the face. Second is Action which can describe what is being seen. Third is Object which describe an object and its color. last is OCR or optical character recognition which can recognize text. To repeat this instruction  double tap the screen. To Begin touch the screen.\n ";

    private GestureDetectorCompat mDetector;
    WifiManager manager;
    public static TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);
        //speak.speakVoice(greetings);


         manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        if(manager.isWifiEnabled()) {
            //manager.setWifiEnabled(false);
        }
        t1 = new TextToSpeech(this,this);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                t1.speak(greetings, TextToSpeech.QUEUE_FLUSH,null);
            }
        },1000*3);

    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /*-----------------Touche Guesture ------------------*/

    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Intent intent = new Intent(this, SelectionActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //speak.speakVoice(greetings);
        t1.speak(greetings, TextToSpeech.QUEUE_FLUSH,null);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent1, MotionEvent motionEvent2, float X, float Y) {
        /*
        if(motionEvent1.getY() - motionEvent2.getY() > 50){

            Toast.makeText(MainActivity.this , " Swipe Up " , Toast.LENGTH_LONG).show();

            return true;
        }*/

       /* if(motionEvent2.getY() - motionEvent1.getY() > 50){

            Toast.makeText(MainActivity.this , " Swipe Down " , Toast.LENGTH_LONG).show();

            return true;
        }

        if(motionEvent1.getX() - motionEvent2.getX() > 50){

            Toast.makeText(MainActivity.this , " Swipe Left " , Toast.LENGTH_LONG).show();

            return true;
        }

        if(motionEvent2.getX() - motionEvent1.getX() > 50) {

            Toast.makeText(MainActivity.this, " Swipe Right ", Toast.LENGTH_LONG).show();



            return true;
        }
        else {

            return true ;
        }
        */
       return false;
    }


    @Override
    public void onInit(int status) {
        if(status != TextToSpeech.ERROR) {
            if(t1.isLanguageAvailable(Locale.ENGLISH)==TextToSpeech.LANG_AVAILABLE)
            t1.setSpeechRate(0.9f);
            t1.setLanguage(Locale.US);
        }
    }
}