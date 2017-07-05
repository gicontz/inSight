package com.example.melchy.insight;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Melchy on 3/7/2017.
 */

public class OcrClass extends AppCompatActivity {
    private Bitmap mBitmap;
    private Uri mImageUri;
    private VisionServiceClient client;

    static  public String Ocrresult = "A";

    public  void ocrRecognize (Uri imgUri, Bitmap imgBitmap){
        if (client==null){
            client = new VisionServiceRestClient("YOUR_COMPUTER_VISION_API");
        }

        mImageUri = imgUri;
       // Log.e(TAG,"Value: "+mImageUri);
        mBitmap = imgBitmap;
        doRecognize();


    }



    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        //OCR send the image file to the server to analyze
        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.English, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }

    public void doRecognize() {
      //  btn_takephoto.setEnabled(false);
       // ocrResult.setText("Analyzing...");
        try {
            new doRequest().execute();
        } catch (Exception e) {
          //  ocrResult.setText("Error encountered. Exception is: "+e.getMessage());
            Ocrresult = e.getMessage();
        }
    }

    private class doRequest extends AsyncTask<String, String, String> {
        private Exception e = null;
        public doRequest() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            if (e != null) {
               // ocrResult.setText("Error:" + e.getMessage());
                Ocrresult = e.getMessage();
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
              //  ocrResult.setText(result);
               // speak.speakVoice(result);
                //Toast.makeText(OcrClass.this, result, Toast.LENGTH_SHORT).show();
                Ocrresult = result;
            }

           // btn_takephoto.setEnabled(true);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
