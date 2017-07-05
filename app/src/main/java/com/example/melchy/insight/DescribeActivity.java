package com.example.melchy.insight;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import com.google.gson.Gson;


//------------------------Vision API


import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
//import com.microsoft.projectoxford.vision.contract.Category;
//import com.microsoft.projectoxford.vision.contract.Face;
//import com.microsoft.projectoxford.vision.contract.Tag;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

//---------------------FACE API

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.example.melchy.insight.helper.LogHelper;
import android.app.ProgressDialog;
import com.example.melchy.insight.helper.ImageHelper_Face;
import android.widget.BaseAdapter;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.roughike.bottombar.BottomBar;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
/**
 * Created by Melchizedek Aguinaldo on 19/02/2017.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DescribeActivity extends Activity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

    //Bing TTS
    XmlTextToSpeech speak = new XmlTextToSpeech();

    /*-----------GLOBAL VARIABLE----------*/
    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    private static final int REQUEST_TAKE_PHOTO = 0;

    // The button to select an image
    private Button mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    private ImageView imageViewbottom;
    // The edit to show status and result.
    public TextView mEditText;
    public float scores;
    private VisionServiceClient client;

    private Uri mUriPhotoTaken;

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

    private GestureDetectorCompat mDetector;

    private boolean inProgress = true;

    /* ----------------Cameara Api2  --------------*/
    private static final String TAG = "DescribeActivity";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private TextureView textureView;
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mState;

    private Button msamplebutton;
    private Button sasample;

    /*-----------------Geustures events -------------------*/
    @Override
    public boolean onTouchEvent(MotionEvent event){

        mDetector.onTouchEvent(event);
        if(inProgress){
            return super.onTouchEvent(event);
        }else {
            return false;
        }

    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        //Toast.makeText(this,"double tap",Toast.LENGTH_LONG).show();
        takePicture();
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

        if(motionEvent1.getX() - motionEvent2.getX() > 50){
           // Toast.makeText(this , " Swipe Left " , Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, OcrActivity.class);
            startActivity(intent);
            return true;
        }

        if(motionEvent2.getX() - motionEvent1.getX() > 50) {
           // Toast.makeText(this, " Swipe Right ", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ActionRecognition.class);
            startActivity(intent);

            return true;
        }
        else {

            return true ;
        }
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
            msamplebutton.setEnabled(false);
            enable(false);
            inProgress = false;
            mProgressDialog.show();
            addLog("Request: Detecting in image " + mImageUri);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
            if(progress[0].equals("Unable to resolve host \"api.projectoxford.ai\": No address associated with hostname")){
                setInfo("No internet connection");
                MainActivity.t1.speak("No internet connection",TextToSpeech.QUEUE_FLUSH,null);
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

            try{
                Captions.clear();

            }catch (Exception e){
                Log.e(TAG,e.getMessage());
            }
            msamplebutton.setEnabled(true);
            enable(true);
            // Show the result on screen when detection is done.
            setUiAfterDetection(result, mSucceed);
            Log.e(TAG, "" + result);
        }
    }



    ProgressDialog mProgressDialog;

    private void addLog(String log) {
        LogHelper.addDetectionLog(log);
    }

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
    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(info);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_describe);

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_dialog_title));

        if (client==null){
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }

        mButtonSelectImage = (Button)findViewById(R.id.buttonSelectImage);
        mEditText = (TextView) findViewById(R.id.editTextResult);
        msamplebutton = (Button)findViewById(R.id.buttonSelectImage123);
        imageViewbottom = (ImageView)findViewById(R.id.imageView3);
        sasample = (Button)findViewById(R.id.button);
        sasample.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EmotionClass em = new EmotionClass();
                Toast.makeText(getBaseContext(), em.emotionTextResult+ "  " +scores + em.hellowworld(),Toast.LENGTH_LONG).show();
            }
        });
        msamplebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(),Captions.size() + " " + Characteristics.size(),Toast.LENGTH_LONG).show();
                takePicture();
            }
        });


        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        //CAMERA API 2

        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        /*BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);

        bottomBar.setDefaultTabPosition(0);

        bottomBar.setEnabled(false);*/
        MainActivity.t1.speak("Face And Emotion.", TextToSpeech.QUEUE_FLUSH,null);
        //speak.speakVoice("Welcome to Face Recognition. To change a mode swipe left or right. To take a picture use double tap.");
        //MainActivity.t1.speak("Build in camera is being use",TextToSpeech.QUEUE_FLUSH,null);

    }





    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) throws IOException {
        mEditText.setText("");


        takePicture();


    }


    /*-----------------------------------------------CAMERA 2 API */
    private void mDescribe(File file) {

        mImageUri = Uri.fromFile(file);

        mBitmap = com.example.melchy.insight.ImageHelper.loadSizeLimitedBitmapFromUri(
                mImageUri, getContentResolver());

        Uri tempUri = getImageUri(getApplicationContext(), mBitmap);
        File finalFile = new File(getRealPathFromURI(tempUri));
        filePath = finalFile.getAbsolutePath();
        if (mBitmap != null) {
            // Show the image on screen.
                ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                imageView.setImageBitmap(mBitmap);

            // Add detection log.
            Log.d("DescribeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                    + "x" + mBitmap.getHeight());

            EmotionClass em = new EmotionClass();

            em.emotionRecognize(mImageUri,mBitmap);

            MainActivity.t1.speak("Please Wait. Analyzing Image.",TextToSpeech.QUEUE_FLUSH,null);

            FaceListAdapter faceListAdapter = new FaceListAdapter(null);
            ListView listView = (ListView) findViewById(R.id.list_detected_faces);
            listView.setAdapter(faceListAdapter);

            setInfo("");
            // Enable button "detect" as the image is selected and not detected.


            ByteArrayOutputStream output = new ByteArrayOutputStream();
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

            // Start a background task to detect faces in the image.
            new DetectionTask().execute(inputStream);


        }
    }



    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            //configureTransform(width,height);
            openCamera();
            configureTransform(width,height);
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
            configureTransform(width,height);
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    /*--------Ang function na to is for rotation ng screen sa camera para maging responsive-------*/
    private void configureTransform(int width, int height) {
        if (imageDimension == null ||  textureView == null){
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRef = new RectF(0, 0, width, height);
        RectF previewRef = new RectF(0, 0, imageDimension.getHeight(), imageDimension.getWidth());
        float centerX = textureRef.centerX();
        float centerY = textureRef.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            //position sa textureview (X,Y)
            previewRef.offset(centerX - previewRef.centerX(),centerY - previewRef.centerY());
            //set sa matrix
            matrix.setRectToRect(textureRef, previewRef, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float)width/imageDimension.getWidth(),(float)height/imageDimension.getHeight());
            //pang zoom sa camera
            matrix.postScale(scale,scale,centerX,centerY+50);
            matrix.postRotate(90*(rotation-2),centerX,centerY);
        }
        textureView.setTransform(matrix);
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
           // Toast.makeText(DescribeActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    protected void takePicture() {
        if(null == cameraDevice) {
            Log.e(TAG, "cameraDevice Not found");
            return;
        }
        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File files = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(files);
            } catch (IOException e) {
            }
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    //  Toast.makeText(DescribeActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();

                    mDescribe(file);
                    fileDIr = file;
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //Toast.makeText(DescribeActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {


        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DescribeActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            // Toast.makeText(getBaseContext(),cameraId,Toast.LENGTH_LONG).show();
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
               // Toast.makeText(DescribeActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        //startBackgroundThread();
        if (textureView.isAvailable()) {
            configureTransform(imageDimension.getWidth(),imageDimension.getHeight());
            openCamera();
            MainActivity.t1.speak("Welcome to Face Recognition. To change a mode swipe left or right. To take a picture use double tap.",TextToSpeech.QUEUE_FLUSH,null);
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

    }
    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        //stopBackgroundThread();
        super.onPause();
    }




    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("DescribeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if(resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();




                    mBitmap = com.example.melchy.insight.ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());



                    //-----------------VISION DECTECTION
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("DescribeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());



                        Uri tempUri = getImageUri(getApplicationContext(), mBitmap);
                        File finalFile = new File(getRealPathFromURI(tempUri));
                        filePath = finalFile.getAbsolutePath();




                        //------------------FACE DETECTION
                        FaceListAdapter faceListAdapter = new FaceListAdapter(null);
                        ListView listView = (ListView) findViewById(R.id.list_detected_faces);
                        listView.setAdapter(faceListAdapter);

                        setInfo("");
                        // Enable button "detect" as the image is selected and not detected.

                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

                        // Start a background task to detect faces in the image.
                        new DetectionTask().execute(inputStream);



                    }
                }
                break;
            default:
                break;
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }






    private void setUiAfterDetection(Face[] result, boolean succeed) {

        // Detection is done, hide the progress dialog.
        mProgressDialog.dismiss();

        // Enable all the buttons.
        mButtonSelectImage.setEnabled(true);

        msamplebutton.setEnabled(true);
        // Disable button "detect" as the image has already been detected.
        // mButtonSelectImage.setEnabled(false);


        if (succeed) {
            //imageViewbottom.setHeight();
            android.view.ViewGroup.LayoutParams layoutParams = imageViewbottom.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = 300;

            // The information about the detection result.
            String detectionResult ="";
            if (result != null) {
                detectionResult = result.length + " face"
                        + (result.length != 1 ? "s" : "") + " detected";

                if(result.length == 0 ){
                    setInfo("No Face Detected");
                    MainActivity.t1.speak("No Face Detected",TextToSpeech.QUEUE_FLUSH,null);
                }
                // t1.speak(words,TextToSpeech.QUEUE_FLUSH,null);
                MainActivity.t1.speak(words,TextToSpeech.QUEUE_FLUSH,null);

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

        }

        mImageUri = null;
        mBitmap = null;

    }
    public void personInfo () {
        inProgress = true;
        //t1.speak(words,TextToSpeech.QUEUE_FLUSH,null);
        //Toast.makeText(getBaseContext(),"Meron "+ Captions.size() + " " +totalFaces,Toast.LENGTH_LONG).show();
        try {
            for (int i = 0; i < totalPerson; i++) {
                //t1.speak(Captions.get(i), TextToSpeech.QUEUE_FLUSH, null);
                MainActivity.t1.speak(Captions.get(i),TextToSpeech.QUEUE_FLUSH,null);
                //setInfo(Captions.get(i));
                setInfo("");
            }
        }catch (Exception e){
            setInfo(e.getMessage());
        }
    }



}
