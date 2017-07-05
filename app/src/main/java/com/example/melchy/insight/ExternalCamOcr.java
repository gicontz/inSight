package com.example.melchy.insight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaActionSound;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Melchizedek Aguinaldo on 19/02/2017.
 */


public class ExternalCamOcr extends Activity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener  {
    private MyFTPClientFunctions ftpclient = null;
    private static final String TAG = "ExternalCamActivity";
    private Uri mUriPhotoTaken;
    private Uri mImageUri;
    private Bitmap mBitmap;
    private WifiManager manager;
    private String name;
    private String url;
    private VisionServiceRestClient client;
    private GestureDetectorCompat mDetector;
    private TextView ocrResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_external_cam_ocr);
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(3).setChecked(true);
        ftpclient = new MyFTPClientFunctions();
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        ocrResult = (TextView)findViewById(R.id.ocr_editTextResult);
        ocrResult.setMovementMethod(new ScrollingMovementMethod());
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }
        String[] params = new String[] {url, name};

        MainActivity. t1.speak("Welcome to OCR or Optical Character Recognition. To change a mode Use volume up and down. To take a picture use the Button of the headset.", TextToSpeech.QUEUE_FLUSH,null);
       // MainActivity.t1.speak("Please wait Setting up Camera", TextToSpeech.QUEUE_FLUSH,null);
        /*new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                firstSettingCam();
            }
        }, 1000*2);*/

    }
    public void firstSettingCam(){
        WifiInfo info = manager.getConnectionInfo();
        if(info.getSSID() == "insight"){
            contextInitialized();

        }else {
            manager.setWifiEnabled(true);
            String networkSSID = "insight";
            String networkPass = "billionaires";

            WifiConfiguration wfc = new WifiConfiguration();

            wfc.SSID = "\"".concat(networkSSID).concat("\"");
            wfc.status = WifiConfiguration.Status.DISABLED;
            wfc.priority = 40;

            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            wfc.preSharedKey = "\"".concat(networkPass).concat("\"");

            int networkId = manager.addNetwork(wfc);
            if (networkId != -1) {
                manager.enableNetwork(networkId,true);
                Log.d(TAG,"Success");
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                      //  MainActivity.t1.speak("Connection Success", TextToSpeech.QUEUE_FLUSH,null);
                    }
                }, 1000*2);
            }else{
                MainActivity.t1.speak("Connection Failed", TextToSpeech.QUEUE_FLUSH,null);
                Log.d(TAG,"Failed");
            }


        }

    }
    private  boolean checkSSid(){
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        if(info.getSSID().equals("\"insight\"" )){
            return true;
        }else{
            return false;
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void contextInitialized() {
        // Do your startup work here
        MainActivity.t1.speak("Please wait Setting up Camera", TextToSpeech.QUEUE_FLUSH,null);

        Timer timer = new Timer();

        TimerTask delayedThreadStartTask = new TimerTask() {
            @Override
            public void run() {

                //captureCDRProcess();
                //moved to TimerTask
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean status = false;
                        status = ftpclient.ftpConnect(getString(R.string.IpAdd), getString(R.string.UsernameFtp), getString(R.string.PasswordFtp), 21);
                        if (status == true) {
                            Log.d(TAG, "Connection Success");
                            // handler.sendEmptyMessage(0);
                            MainActivity.t1.speak("Connection Success", TextToSpeech.QUEUE_FLUSH,null);
                        } else {
                            Log.d(TAG, "Connection failed");
                            MainActivity.t1.speak("Connection Failed", TextToSpeech.QUEUE_FLUSH,null);
                            // handler.sendEmptyMessage(-1);
                        }
                    }
                }).start();
            }
        };

        timer.schedule(delayedThreadStartTask, 3 * 1000); // minute
    }

    public void checkInternet(){
        Timer timer = new Timer();

        TimerTask delayedThreadStartTask = new TimerTask() {
            @Override
            public void run() {

                //captureCDRProcess();
                //moved to TimerTask
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new StartInternet().execute();
                    }
                }).start();
            }
        };

        timer.schedule(delayedThreadStartTask, 1 * 1000);
    }



    public void sendCommand(){
        enable(false);
        ocrResult.setText("");
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(checkSSid()){
                    new AsyncSendCommand().execute();
                }else{
                    String networkSSID = "insight";
                    String networkPass = "billionaires";

                    WifiConfiguration wfc = new WifiConfiguration();

                    wfc.SSID = "\"".concat(networkSSID).concat("\"");
                    wfc.status = WifiConfiguration.Status.DISABLED;
                    wfc.priority = 40;

                    wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                    wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                    wfc.preSharedKey = "\"".concat(networkPass).concat("\"");

                    int networkId = manager.addNetwork(wfc);
                    if (networkId != -1) {
                        Log.d(TAG,"Success");
                        manager.enableNetwork(networkId,true);
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                new AsyncSendCommand().execute();
                            }
                        }, 1000);

                    }else{
                        MainActivity.t1.speak("Connection to camera has been disconnected.", TextToSpeech.QUEUE_FLUSH,null);
                        Log.d(TAG,"Failed to connect to cam");
                    }

                }
            }
        }, 1000*3);

    }
    private class AsyncSendCommand extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL("http://169.254.206.45:80/command.php");
                conn  = (HttpURLConnection)url.openConnection();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    MediaActionSound sound = new MediaActionSound();
                    sound.play(MediaActionSound.SHUTTER_CLICK);
                    Log.d(TAG,"Success Result: "+ response.toString());

                }
            } catch (Exception e) {
                Log.d(TAG,e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //new doRequest().execute();
            new SendHttpRequestTask().execute();
        }
    }



    private class SendHttpRequestTask extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL("http://169.254.206.45:80/image.jpg");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                // com.example.melchy.insight.ImageHelper.loadSizeLimitedBitmapFromUri(mImageUri, getContentResolver());

                return myBitmap;
            }catch (Exception e){
                Log.d(TAG,e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            //mImageUri = Uri.fromFile(file);

            mBitmap = RotateBitmap(result,90);
            if (mBitmap != null) {
                ImageView imageView = (ImageView) findViewById(R.id.ocr_selectedImage);
                imageView.setImageBitmap(mBitmap);
            }
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
            //new DetectionTask().execute(inputStream);
            //manager.setWifiEnabled(false);
            checkInternet();



        }

    }
    public static Bitmap RotateBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private class StartInternet extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            dissableWifi();
            //manager.setWifiEnabled(true);
            /*
            String networkSSID = getString(R.string.IpNetUsername);
            String networkPass = getString(R.string.IpNetPassword);

            WifiConfiguration wfc = new WifiConfiguration();

            wfc.SSID = "\"".concat(networkSSID).concat("\"");
            wfc.status = WifiConfiguration.Status.DISABLED;
            wfc.priority = 40;

            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            wfc.preSharedKey = "\"".concat(networkPass).concat("\"");
            int networkId = manager.addNetwork(wfc);
            if (networkId != -1) {
                Log.d(TAG,"Success");
                manager.enableNetwork(networkId,true);
            }else{
                Log.d(TAG,"Failed");
            }
            */

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            sendToInsight();

        }
    }

    private class doRequestAction extends AsyncTask<String, String, String> {
        private Exception e = null;
        public doRequestAction() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                MainActivity.t1.speak("Please Wait. Analyzing Image.", TextToSpeech.QUEUE_FLUSH,null);
                return process();
            } catch (Exception e) {
                this.e = e;// Store error

            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            if (e != null) {
                if(e.getMessage().equals("Unable to resolve host \"api.projectoxford.ai\": No address associated with hostname")){
                    ocrResult.setText("Error: No Internet Connection");
                    MainActivity.t1.speak("Error: No Internet Connection",TextToSpeech.QUEUE_FLUSH,null);

                }else{
                    ocrResult.setText(e.getMessage());
                    //speak.speakVoice("Error: No Internet Connection");
                }

                this.e = null;
            }else{
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                String result = "";

                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            result += word.text + " ";
                        }
                        result += "\n";
                    }
                    result += "\n\n";
                }
                ocrResult.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                ocrResult.setText(result);
                MainActivity.t1.speak(result,TextToSpeech.QUEUE_FLUSH,null);
                if(result == null || result ==""){
                    MainActivity.t1.speak("No Text Detected",TextToSpeech.QUEUE_FLUSH,null);
                    ocrResult.setText("No Text Detected");
                }
                //speak.speakVoice(result);
                Log.d(TAG,result);
                Log.d(TAG,result);
            }
            //btn_takephoto.setEnabled(true);
            enable(true);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        //OCR send the image file to the server to analyze
        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.English, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }



    public void sendToInsight(){
        ocrResult.setText("Please Wait. Analyzing Image.");
        //new doRequest().execute();
        Timer timer = new Timer();

        TimerTask delayedThreadStartTask = new TimerTask() {
            @Override
            public void run() {

                //captureCDRProcess();
                //moved to TimerTask
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        new doRequestAction().execute();
                    }
                }).start();
            }
        };

        timer.schedule(delayedThreadStartTask, 3 * 1000);


    }




    private void takeImage() {
        boolean status = false;
        status = ftpclient.ftpDownload("/home/pi/Desktop/picture/image.jpg", Environment.getExternalStorageDirectory() + "/TAGFtp/image.jpg");
        if (status == true) {
            Log.d(TAG, "Download success");
            // handler.sendEmptyMessage(2);


        } else {
            Log.d(TAG, "Download failed");
            //handler.sendEmptyMessage(-1);
        }

    }

    /*-----------------Geustures events -------------------*/
    @Override
    public boolean onTouchEvent(MotionEvent event){
        mDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //Toast.makeText(this,"double tap",Toast.LENGTH_LONG).show();
        //sendCommand();
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
        }*/

        /*if(motionEvent1.getX() - motionEvent2.getX() > 50){
            //Toast.makeText(this , " Swipe Left " , Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ExternalCamObject.class);
            startActivity(intent);
            return true;
        }

        if(motionEvent2.getX() - motionEvent1.getX() > 50) {
            //  Toast.makeText(this, " Swipe Right ", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, ExternalCamFace.class);
            startActivity(intent);

            return true;
        }
        else {

            return true ;
        }*/
        return true;
    }
    protected boolean enabled = true;
    public void enable(boolean b) {
        enabled = b;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return enabled ?
                super.dispatchTouchEvent(ev) :
                true;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (action == KeyEvent.ACTION_DOWN) {
                    Log.d("Status:","Taken picture");
                    sendCommand();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    Intent intent = new Intent(this, ExternalCamObject.class);
                    startActivity(intent);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(action == KeyEvent.ACTION_DOWN){
                    Intent intent = new Intent(this, ExternalCamFace.class);
                    startActivity(intent);
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
        // Toast.makeText(getBaseContext(),String.valueOf(keyCode),Toast.LENGTH_LONG).show();
        //return super.dispatchKeyEvent(event);
    }

    private void dissableWifi() {
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(false);

    }
}
