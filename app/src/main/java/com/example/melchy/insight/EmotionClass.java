package com.example.melchy.insight;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.example.melchy.insight.ImageHelper;

import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.vision.contract.Tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
/**
 * Created by Melchizedek Aguinaldo on 19/02/2017.
 */

public class EmotionClass extends AppCompatActivity {
    private static final String TAG = "EmotionClass";
    DescribeActivity ds = new DescribeActivity();
    private EmotionServiceClient client;
    private Bitmap mBitmap;
    private File picDIr = ds.fileDIr;
    private Uri mImageUri;
    public float resultValue;
    static  public float valueResult;
    static  public String emotionTextResult = "";


    public  void emotionRecognize (Uri imgUri,Bitmap imgBitmap){

        ds.scores = (float) 1.2;

        if (client == null) {
            client = new EmotionServiceRestClient("YOUR_EMOTION_API_KEY");
        }
      mImageUri = imgUri;
      Log.e(TAG,"Value: "+mImageUri);
      mBitmap = imgBitmap;
      doRecognizeEmotions();


  }

    public void doRecognizeEmotions() {
        // Do emotion detection using auto-detected faces.
        try {
            new doRequest(false).execute();
        } catch (Exception e) {
            Log.d(TAG,e.getMessage());
        }

        String faceSubscriptionKey = "9b84de5a475544ecbe11dd88ec867068";
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {

        } else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                new doRequest(true).execute();
            } catch (Exception e) {
                Log.d(TAG,e.getMessage());
            }
        }
    }

    //Functions sa emotion
    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();

        List<RecognizeResult> result = null;

        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));

        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.subscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");

            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            resultValue = (float) 2.0;
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... params) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    this.e = e;
                }

            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    this.e = e;
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);

            if (this.useFaceRectangles == false) {
                Log.e(TAG, "Recognizing emotions with auto-detected face rectangles...");
            } else {
                Log.e(TAG, "Recognizing emotions with existing face rectangles from Face API...");
            }
            if (e != null) {
                Log.e(TAG, "Erorr" + e.getMessage());
            } else {
                if (result.size() == 0) {
                    Log.e(TAG, "No emotion");
                } else {
                    float angerVal = 0;
                    float contemptVal = 0;
                    float disgustVal = 0;
                    float fearVal = 0;
                    float happinessVal = 0;
                    float neutralVal = 0;
                    float sadnessVal = 0 ;
                    float supriseVal = 0;
                  for (RecognizeResult r : result) {
                        //((EditText)findViewById(R.id.editText1)).append(r.scores.happiness + "");
                        Log.e(TAG,r.scores.happiness + "");
                       // mEditText.append(r.scores.happiness+"");
                       angerVal = ComputationPercent((float) r.scores.anger);
                       contemptVal = ComputationPercent((float)r.scores.contempt);
                       disgustVal = ComputationPercent((float)r.scores.disgust);
                       fearVal = ComputationPercent((float)r.scores.fear);
                       happinessVal = ComputationPercent((float)r.scores.happiness);
                       neutralVal = ComputationPercent((float)r.scores.neutral);
                       sadnessVal = ComputationPercent((float)r.scores.sadness);
                       supriseVal = ComputationPercent((float)r.scores.surprise);
                    }
                    if(result.size() == 0){
                        emotionTextResult = "No emotions";
                    }else if((angerVal >= 50) && (angerVal > contemptVal) && (angerVal > disgustVal) && (angerVal > fearVal) && (angerVal > happinessVal) && (angerVal > neutralVal) && (angerVal > sadnessVal) && (angerVal > supriseVal)){
                        emotionTextResult = "anger";
                    }else if ((contemptVal >= 50) && (contemptVal > angerVal) && (contemptVal > disgustVal) && (contemptVal > fearVal) && (contemptVal > happinessVal) && (contemptVal > neutralVal) && (contemptVal > sadnessVal) && (contemptVal > supriseVal)){
                        emotionTextResult = "Contempt";
                    }else if ((disgustVal >= 50) && (disgustVal > angerVal) && (disgustVal > contemptVal) && (disgustVal > fearVal) && (disgustVal > happinessVal) && (disgustVal > neutralVal) && (disgustVal > sadnessVal) && (disgustVal > supriseVal)) {
                        emotionTextResult = "disgust";
                    }else if ((fearVal >= 50) && (fearVal > angerVal) && (fearVal > contemptVal) && (fearVal > disgustVal) && (fearVal > happinessVal) && (fearVal > neutralVal) && (fearVal > sadnessVal) && (fearVal > supriseVal)){
                        emotionTextResult = "fear";
                    }else if ((happinessVal >= 50) && (happinessVal > angerVal) && (happinessVal > contemptVal) && (happinessVal > disgustVal) && (happinessVal > fearVal) && (happinessVal > neutralVal) && (happinessVal > sadnessVal) && (happinessVal > supriseVal)){
                        emotionTextResult = "happiness";
                    }else if ((neutralVal >= 50) && (neutralVal > angerVal) && (neutralVal > contemptVal) && (neutralVal > disgustVal) && (neutralVal > fearVal) && (neutralVal > happinessVal) && (neutralVal > sadnessVal) && (neutralVal > supriseVal)){
                        emotionTextResult = "neutral";
                    }else if ((sadnessVal >= 50) && (sadnessVal > angerVal) && (sadnessVal > contemptVal) && (sadnessVal > disgustVal) && (sadnessVal > fearVal) && (sadnessVal > happinessVal) && (sadnessVal > neutralVal) && (sadnessVal > supriseVal)){
                        emotionTextResult = "sadness";
                    }else if ((supriseVal >= 50) && (supriseVal > angerVal) && (supriseVal > contemptVal) && (supriseVal > disgustVal) && (supriseVal > fearVal) && (supriseVal > happinessVal) && (supriseVal > neutralVal) && (supriseVal > sadnessVal)) {
                        emotionTextResult = "surprise";
                    }else{
                        emotionTextResult = "No emotions";
                    }
                    Log.e(TAG,"Anger Value: " + angerVal + "Contempt: " +contemptVal + "disgust: " +disgustVal + " fear: "+ fearVal+ " happy: "+ happinessVal + " neutral: "+ neutralVal+ " sadness: " + sadnessVal + " suprise: " + supriseVal);
                    Log.e(TAG,emotionTextResult);
                }
            }
        }

        private float ComputationPercent(float resultVal) {
            float score = resultVal * 100;
            int finalscore = Math.round(score);
            return finalscore;
        }

       //Tapos na ang async task dito
    }

    public String hellowworld(){
        String hellow = "Hellow";
        return  hellow;
    }


}
