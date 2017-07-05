package com.example.melchy.insight;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
/**
 * Created by Melchizedek Aguinaldo on 19/02/2017.
 */


public class SelectionActivity extends Activity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{
    XmlTextToSpeech speak = new XmlTextToSpeech();
    String instructions = "Please choose your camera mode. for external camera  double tap the screen. and for build in camera tap the screen.";
    private GestureDetectorCompat mDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        //speak.speakVoice(instructions);
        MainActivity.t1.speak(instructions,TextToSpeech.QUEUE_FLUSH,null);
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        Intent intent = new Intent(this, DescribeActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        Intent intent = new Intent(this, ExternalCamFace.class);
        startActivity(intent);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
