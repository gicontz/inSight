package com.example.melchy.insight;


import android.app.Activity;
import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.melchy.insight.helper.ImageHelper_Face;
import com.example.melchy.insight.helper.LogHelper;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collector;

/**
 * Created by Melchizedek Aguinaldo on 19/02/2017.
 */


public class ExternalCamFace extends Activity implements GestureDetector.OnGestureListener,
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
    private TextView describeResult;

    public TextView mEditText;
    public float scores;


    private Uri mUriPhotoTaken1;
    String words ="";
    int totalFaces = 0;
    //String Captions = "";
    List<String> Captions = new ArrayList<>();
    String filePath = "";
    String filePath1 = "";
    TextToSpeech t1;
    int totalPerson;
    String personInfo = "";
    List<String> Characteristics = new ArrayList<>();
    public File fileDIr ;
    private ImageView imageViewbottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_external_cam_face);
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);

        ftpclient = new MyFTPClientFunctions();
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        describeResult = (TextView)findViewById(R.id.editTextResult);
        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }
        String[] params = new String[] {url, name};
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));
        MainActivity.t1.speak("Please wait Setting up Camera", TextToSpeech.QUEUE_FLUSH,null);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                firstSettingCam();
            }
        }, 1000*2);
        imageViewbottom = (ImageView)findViewById(R.id.imageView3);

    }
    public void firstSettingCam(){
        WifiInfo info = manager.getConnectionInfo();
        if(info.getSSID().equals("\"insight\"" ) && isNetworkAvailable()){
            MainActivity.t1.speak("Welcome to Face Recognition. To change a mode Use volume up and down. To take a picture use the Button of the headset.", TextToSpeech.QUEUE_FLUSH,null);

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
                        WifiInfo info = manager.getConnectionInfo();
                        String results = info.getSSID();
                        if(info.getSSID().equals("\"insight\"") && isNetworkAvailable()){
                            MainActivity.t1.speak("Connection Success. Welcome to Face Recognition. To change a mode Use volume up and down. To take a picture use the Button of the headset.", TextToSpeech.QUEUE_FLUSH,null);
                        }else{
                            MainActivity.t1.speak("Connection Failed", TextToSpeech.QUEUE_FLUSH,null);
                            enable(false);
                            Timer timer = new Timer();

                            TimerTask delayedThreadStartTask = new TimerTask() {
                                @Override
                                public void run() {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(ExternalCamFace.this, SelectionActivity.class);
                                            startActivity(intent);
                                        }
                                    }).start();
                                }
                            };

                            timer.schedule(delayedThreadStartTask, 1 * 1000);



                        }
                        Log.d(TAG,checkSSid() + " " +results +" "+ isNetworkAvailable());

                    }
                }, 1000*3);
            }else{
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        WifiInfo info = manager.getConnectionInfo();
                        String results = info.getSSID();
                        if(info.getSSID().equals("\"insight\"") && isNetworkAvailable()){
                            MainActivity.t1.speak("Connection Success. Welcome to Face Recognition. To change a mode Use volume up and down. To take a picture use the Button of the headset.", TextToSpeech.QUEUE_FLUSH,null);
                        }else{
                            MainActivity.t1.speak("Connection Failed", TextToSpeech.QUEUE_FLUSH,null);
                            enable(false);
                            Timer timer = new Timer();

                            TimerTask delayedThreadStartTask = new TimerTask() {
                                @Override
                                public void run() {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(ExternalCamFace.this, SelectionActivity.class);
                                            startActivity(intent);
                                        }
                                    }).start();
                                }
                            };

                            timer.schedule(delayedThreadStartTask, 1 * 1000);

                        }
                        Log.d(TAG,checkSSid() + " " +results +" "+ isNetworkAvailable());

                    }
                }, 1000*3);
            }


        }

    }
    private  boolean checkSSid(){
        manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        if(info.getSSID().equals("\"insight\"")){
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
        describeResult.setText("");
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
                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(mBitmap);
            }



           checkInternet();




        }

    }
    public static Bitmap RotateBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    //Face list adpator

    private class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        List<Face> faces;

        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;

        // Initialize with detection result.
        FaceListAdapter(Face[] detectionResult) {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();
            totalPerson = 0;
            if (detectionResult != null) {
                faces = Arrays.asList(detectionResult);
                for (Face face : faces) {
                    try {
                        // Crop face thumbnail with five main landmarks drawn from original image.
                        faceThumbnails.add(ImageHelper_Face.generateFaceThumbnail(
                                mBitmap,face.faceRectangle));
                    } catch (IOException e) {
                        // Show the exception when generating face thumbnail fails.
                        setInfo(e.getMessage());
                    }
                    totalPerson++;
                }
            }
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_face_with_description, parent, false);
            }
            convertView.setId(position);

            // Show the face thumbnail.
            ((ImageView) convertView.findViewById(R.id.face_thumbnail)).setImageBitmap(
                    faceThumbnails.get(position));

            // Show the face details.
            DecimalFormat formatter = new DecimalFormat("#0.0");

            String emotions = "";

            if((faces.get(position).faceAttributes.smile) >= 0.60  && (faces.get(position).faceAttributes.smile) <= 1.0 ){
                emotions = "Happy";
            }else{
                if((faces.get(position).faceAttributes.smile) < 0.60){
                    emotions = "Sad";
                }
            }
            String face_description = "";

            String genderVal = faces.get(position).faceAttributes.gender;
            if(genderVal.equals("male")){
                float beardval = 0;
                float moustacheval = 0;
                String breadinfo = "";
                String moustacheinfo ="";

                moustacheval = valueOutput((float) faces.get(position).faceAttributes.facialHair.moustache);
                beardval = valueOutput((float) faces.get(position).faceAttributes.facialHair.beard);

                if (beardval >=50){
                    breadinfo = "have a ";
                }else{
                    breadinfo = "doesn't have a ";
                }
                if (moustacheval >=50){
                    moustacheinfo = "have a ";
                }else{
                    moustacheinfo = "doesn't have a ";
                }
                EmotionClass em = new EmotionClass();

                face_description = "Age: " + formatter.format(faces.get(position).faceAttributes.age) + "\n"
                        + "Gender: " + faces.get(position).faceAttributes.gender + "\n"
                        + "Beard: " + breadinfo  + "Beard \n"
                        + "Mustache: " + moustacheinfo  + "Mustache \n"
                        + "Glasses: " + faces.get(position).faceAttributes.glasses + "\n"
                        + "Emotions: " + em.emotionTextResult;
                //Toast.makeText(getBaseContext(), em.hellowworld()+ "  " +scores,Toast.LENGTH_LONG).show();

            }else{
                face_description = "Age: " + formatter.format(faces.get(position).faceAttributes.age) + "\n"
                        + "Gender: " + faces.get(position).faceAttributes.gender + "\n"
                        //+ "Head pose(in degree): roll(" + formatter.format(faces.get(position).faceAttributes.headPose.roll) + "), "
                        // + "yaw(" + formatter.format(faces.get(position).faceAttributes.headPose.yaw) + ")\n"
                        + "Glasses: " + faces.get(position).faceAttributes.glasses + "\n"
                        + "Emotions: " + emotions;

            }


            Captions.clear();
            ((TextView) convertView.findViewById(R.id.text_detected_face)).setText(face_description);
            //setInfo(Captions);
            Characteristics.add(face_description);
            //  personInfo = "Person Number" + (totalFaces + 1) + " " +((TextView) convertView.findViewById(R.id.text_detected_face)).getText().toString()+
            if (faces.size()>1) {
                personInfo += "Person Number" + (totalFaces + 1) + " " + ((TextView) convertView.findViewById(R.id.text_detected_face)).getText().toString() + ".";
                Captions.add(personInfo);
            }else{
                Captions.add( "Person in front of you have this characteristic." + ((TextView) convertView.findViewById(R.id.text_detected_face)).getText().toString() + ".");
            }

            personInfo();
            //setInfo(Captions);
            //personInfo(totalFaces);
            totalFaces++;

            //Toast.makeText(getBaseContext(),Captions.size()+"  "+((TextView) convertView.findViewById(R.id.text_detected_face)).getText().toString(),Toast.LENGTH_LONG).show();
            //Toast.makeText(getBaseContext(), Captions,Toast.LENGTH_LONG).show();
            // Toast.makeText(getBaseContext(),totalPerson+" ",Toast.LENGTH_LONG).show();
            return convertView;
        }
    }

    float valueOutput (float score){
        score = score * 100;
        return score;
    }
    public void personInfo () {

        //t1.speak(words,TextToSpeech.QUEUE_FLUSH,null);
        //Toast.makeText(getBaseContext(),"Meron "+ Captions.size() + " " +totalFaces,Toast.LENGTH_LONG).show();
        try {
            for (int i = 0; i < totalPerson; i++) {
                //t1.speak(Captions.get(i), TextToSpeech.QUEUE_FLUSH, null);
                MainActivity.t1.speak(Captions.get(i),TextToSpeech.QUEUE_FLUSH,null);
                setInfo("");
            }
        }catch (Exception e){
            setInfo(e.getMessage());
        }
    }

    //-----------------------------------------------FACE
    private void addLog(String log) {
        LogHelper.addDetectionLog(log);
    }
    ProgressDialog mProgressDialog;

    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient =new FaceServiceRestClient(getString(R.string.subscription_key_face));

            try {

                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        true,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[] {
                                FaceServiceClient.FaceAttributeType.Age,
                                FaceServiceClient.FaceAttributeType.Gender,
                                FaceServiceClient.FaceAttributeType.Glasses,
                                FaceServiceClient.FaceAttributeType.Smile,
                                FaceServiceClient.FaceAttributeType.HeadPose,
                                FaceServiceClient.FaceAttributeType.FacialHair,
                        });
            } catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                addLog(e.getMessage());
                return null;
            }

        }



        @Override
        protected void onPreExecute() {
            enable(false);
            mProgressDialog.show();
            addLog("Request: Detecting in image " + mImageUri);
            MainActivity.t1.speak("Please Wait. Analyzing Image.",TextToSpeech.QUEUE_FLUSH,null);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
            if(progress[0].equals("Unable to resolve host \"api.projectoxford.ai\": No address associated with hostname")){
                setInfo("No internet connection");
                MainActivity.t1.speak("Error: No Internet Connection",TextToSpeech.QUEUE_FLUSH,null);
            }else{
                setInfo(progress[0]);
            }

        }

        @Override
        protected void onPostExecute(Face[] result) {
            if (mSucceed) {
                addLog("Response: Success. Detected " + (result == null ? 0 : result.length)
                        + " face(s) in " + mImageUri);
                Log.e(TAG, "" + Captions.size());
            }


            enable(true);
            // Show the result on screen when detection is done.
            setUiAfterDetection(result, mSucceed);
            Log.e(TAG, "" + result);
        }
    }

    private void setUiAfterDetection(Face[] result, boolean succeed) {

        // Detection is done, hide the progress dialog.
        mProgressDialog.dismiss();

        // Enable all the buttons.
        // Disable button "detect" as the image has already been detected.
        // mButtonSelectImage.setEnabled(false);


        if (succeed) {
            android.view.ViewGroup.LayoutParams layoutParams = imageViewbottom.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = 300;
            // The information about the detection result.
            String detectionResult ="";
            if (result != null) {
                detectionResult = result.length + " face"
                        + (result.length != 1 ? "s" : "") + " detected";

                if(result.length == 0 ){
                    MainActivity.t1.speak("No Face Detected",TextToSpeech.QUEUE_FLUSH,null);
                    setInfo("No Face Detected");
                }
                // t1.speak(words,TextToSpeech.QUEUE_FLUSH,null);


                // Show the detected faces on original image.
                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(ImageHelper_Face.drawFaceRectanglesOnBitmap(mBitmap,result,true));

                // Set the adapter of the ListView which contains the details of the detected faces.
                FaceListAdapter faceListAdapter = new FaceListAdapter(result);

                // Show the detailed list of detected faces.
                ListView listView = (ListView) findViewById(R.id.list_detected_faces);
                listView.setAdapter(faceListAdapter);

            } else {

                // words = mEditText.toString();
                // detectionResult = "0 face detected" ;
                // t1.speak(words,TextToSpeech.QUEUE_FLUSH,null);

            }
            //Toast.makeText(getBaseContext(),Captions.size() + " " + Characteristics.size(),Toast.LENGTH_LONG).show();
          //  setInfo(detectionResult + Captions);
        }

        mImageUri = null;
        mBitmap = null;

    }




    //---------Start internet
    private class StartInternet extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            dissableWifi();
            //manager.setWifiEnabled(true);

          /*  String networkSSID = getString(R.string.IpNetUsername);
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
            }*/

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
        protected String doInBackground(String... strings) {
            try {
                MainActivity.t1.speak("Please Wait. Analyzing Image.", TextToSpeech.QUEUE_FLUSH,null);
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            //describeResult.setText("");
            if (e != null) {
                if(e.getMessage().equals("Unable to resolve host \"api.projectoxford.ai\": No address associated with hostname")){
                    describeResult.setText("Error: No Internet Connection");
                    //speak.speakVoice("Error: No Internet Connection");
                    MainActivity.t1.speak("Error: No Internet Connection",TextToSpeech.QUEUE_FLUSH,null);
                }else{
                    describeResult.setText(e.getMessage());
                    // speak.speakVoice("Error: No Internet Connection");
                }
                this.e = null;
            } else {
                String resultWord = "";
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);
                for (Caption caption: result.description.captions) {
                    resultWord += caption.text;
                    describeResult.append(caption.text + "\n");
                    // words = caption.text.toString();
                }
                //speak.speakVoice(resultWord);
                MainActivity.t1.speak(resultWord, TextToSpeech.QUEUE_FLUSH,null);
            }
            enable(true);
            //btn_takephoto.setEnabled(true);
        }
    }
    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories","Description"};
        String[] details = {};
        // Put the image into an input stream for detection.
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.analyzeImage(inputStream, features, details);

        String result = gson.toJson(v);
        Log.d("result", result);

        return result;
    }



    public void sendToInsight(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EmotionClass em = new EmotionClass();
                em.emotionRecognize(mImageUri,mBitmap);

                FaceListAdapter faceListAdapter = new FaceListAdapter(null);
                ListView listView = (ListView) findViewById(R.id.list_detected_faces);
                listView.setAdapter(faceListAdapter);

                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
                new DetectionTask().execute(inputStream);
            }
        }, 5000);


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
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);

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
        Toast.makeText(this,"double tap",Toast.LENGTH_LONG).show();
      //  sendCommand();
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
             Toast.makeText(this , " Swipe Left " , Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ExternalCamOcr.class);
            startActivity(intent);
            return true;
        }

        if(motionEvent2.getX() - motionEvent1.getX() > 50) {
             Toast.makeText(this, " Swipe Right ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ExternalCamActivity.class);
            startActivity(intent);

            return true;
        }
        else {

            return true ;
        }*/
       return false;
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
                    Intent intent = new Intent(this, ExternalCamOcr.class);
                    startActivity(intent);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(action == KeyEvent.ACTION_DOWN){
                    Intent intent = new Intent(this, ExternalCamActivity.class);
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
