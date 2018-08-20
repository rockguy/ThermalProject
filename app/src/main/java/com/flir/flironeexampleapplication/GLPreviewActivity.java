package com.flir.flironeexampleapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.CharacterPickerDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flir.flironeexampleapplication.models.PhotoModel;
import com.flir.flironeexampleapplication.util.Analizer;
import com.flir.flironeexampleapplication.util.SystemUiHider;
import com.flir.flironesdk.Device;
import com.flir.flironesdk.FlirUsbDevice;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.flir.flironeexampleapplication.StartActivity.TAG;

/**
 * An example activity and delegate for FLIR One image streaming and device interaction.
 * Based on an example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 * @see Device.Delegate
 * @see FrameProcessor.Delegate
 * @see Device.StreamDelegate
 * @see Device.PowerUpdateDelegate
 */
public class GLPreviewActivity extends Activity implements Device.Delegate, FrameProcessor.Delegate, Device.StreamDelegate{
    private static final int PERMISSION_REQUEST_CODE = 0;

    Realm realm;
    GLSurfaceView thermalSurfaceView;
    private ImageButton takePhoto;
    RenderedImage _renderedImage;
    int width;

    public static final int INTERNAL_RECTANGLE_WIDTH = 40;
    public static final int INTERNAL_RECTANGLE_HEIGHT = 40;

    private volatile boolean imageCaptureRequested = false;
    //private volatile Socket streamSocket = null;
    private boolean chargeCableIsConnected = true;

    private int deviceRotation = 0;
    private OrientationEventListener orientationEventListener;


    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;

    private String lastSavedPath;

    private Device.TuningState currentTuningState = Device.TuningState.Unknown;
    private TextView ringsStatus;
    private boolean isProcessing;
    private static boolean notStarted = true;
    private int delta;
    // Device Delegate methods

    // Called during device discovery, when a device is connected
    // During this callback, you should save a reference to device
    // You should also set the power update delegate for the device if you have one
    // Go ahead and start frame stream as soon as connected, in this use case
    // Finally we create a frame processor for rendering frames

    public void onDeviceConnected(Device device){
        Log.i("ExampleApp", "Device connected!");
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              findViewById(R.id.pleaseConnect).setVisibility(View.GONE);
                          }
                      });
        
        flirOneDevice = device;
        flirOneDevice.startFrameStream(this);

        final ToggleButton chargeCableButton = (ToggleButton)findViewById(R.id.chargeCableToggle);
        if(flirOneDevice instanceof SimulatedDevice){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeCableButton.setChecked(chargeCableIsConnected);
                    chargeCableButton.setVisibility(View.VISIBLE);
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeCableButton.setChecked(chargeCableIsConnected);
                    chargeCableButton.setVisibility(View.INVISIBLE);
                    findViewById(R.id.connect_sim_button).setEnabled(false);

                }
            });
        }

        orientationEventListener.enable();
    }

    /**
     * Indicate to the user that the device has disconnected
     */
    public void onDeviceDisconnected(Device device){
        Log.i("ExampleApp", "Device disconnected!");

        final ToggleButton chargeCableButton = (ToggleButton)findViewById(R.id.chargeCableToggle);
        final TextView levelTextView = (TextView)findViewById(R.id.batteryLevelTextView);
        final ImageView chargingIndicator = (ImageView)findViewById(R.id.batteryChargeIndicator);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.pleaseConnect).setVisibility(View.GONE);
                levelTextView.setText("--");
                chargeCableButton.setChecked(chargeCableIsConnected);
                chargeCableButton.setVisibility(View.INVISIBLE);
                chargingIndicator.setVisibility(View.GONE);
                findViewById(R.id.tuningProgressBar).setVisibility(View.GONE);
                findViewById(R.id.tuningTextView).setVisibility(View.GONE);
                findViewById(R.id.connect_sim_button).setEnabled(true);
            }
        });
        flirOneDevice = null;
        orientationEventListener.disable();
    }

    /**
     * If using RenderedImage.ImageType.ThermalRadiometricKelvinImage, you should not rely on
     * the accuracy if tuningState is not Device.TuningState.Tuned
     * @param tuningState
     */
    public void onTuningStateChanged(Device.TuningState tuningState){
        Log.i("ExampleApp", "Tuning state changed changed!");

        currentTuningState = tuningState;
        if (tuningState == Device.TuningState.InProgress){
            runOnUiThread(new Thread(){
                @Override
                public void run() {
                    super.run();
                    findViewById(R.id.tuningProgressBar).setVisibility(View.VISIBLE);
                    findViewById(R.id.tuningTextView).setVisibility(View.VISIBLE);
                }
            });
        }else {
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    super.run();
                    findViewById(R.id.tuningProgressBar).setVisibility(View.GONE);
                    findViewById(R.id.tuningTextView).setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onAutomaticTuningChanged(boolean deviceWillTuneAutomatically) {

    }
    private ColorFilter originalChargingIndicatorColor = null;

    // StreamDelegate method
    //Todo: сейчас картинка лагает
    public void onFrameReceived(Frame frame) {
        Log.v(TAG, "Frame received!");
        if (currentTuningState != Device.TuningState.InProgress){
//            Canvas canvas = new Canvas();
//            Paint paint = new Paint();
//            paint.setARGB(1,255,255,255);
//
//
//                frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
//            canvas.drawRect(
//                    (float)(thermalSurfaceView.getWidth()- INTERNAL_RECTANGLE_WIDTH),
//                    (float)((thermalSurfaceView.getHeight()-INTERNAL_RECTANGLE_HEIGHT)/2),
//                    (float)(thermalSurfaceView.getWidth()+ INTERNAL_RECTANGLE_WIDTH),
//                    (float)((thermalSurfaceView.getHeight()-INTERNAL_RECTANGLE_HEIGHT)/2 + INTERNAL_RECTANGLE_HEIGHT),
//                    paint);
//
//            thermalSurfaceView.draw(canvas);
            frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            thermalSurfaceView.requestRender();
        }

    }

    double avgTemp;
    double avgTempK;
    int[] thermalPixels;
    // Frame Processor Delegate method, will be called each time a rendered frame is produced
    public void onFrameProcessed(final RenderedImage renderedImage){
        Log.v(TAG, "Frame Processing!");
        if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage){
        // Note: this code is not optimized

            _renderedImage = renderedImage;
            int[] thermalPixels = renderedImage.thermalPixelValues();
            // average the center 9 pixels for the spot meter

            int width = renderedImage.width();
            int height = renderedImage.height();
            int centerPixelIndex = width * (height/2) + (width/2);
            int[] centerPixelIndexes = new int[] {
                    centerPixelIndex, centerPixelIndex-1, centerPixelIndex+1,
                    centerPixelIndex - width,
                    centerPixelIndex - width - 1,
                    centerPixelIndex - width + 1,
                    centerPixelIndex + width,
                    centerPixelIndex + width - 1,
                    centerPixelIndex + width + 1
            };

            double averageTemp = 0;

            for (int i = 0; i < centerPixelIndexes.length; i++){
                // Remember: all primitives are signed, we want the unsigned value,
                // we've used renderedImage.thermalPixelValues() to get unsigned values
                int pixelValue = (thermalPixels[centerPixelIndexes[i]]);
                averageTemp += (((double)pixelValue) - averageTemp) / ((double) i + 1);
            }
            double averageC = (averageTemp / 100) - 273.15;
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);
            final String spotMeterValue = numberFormat.format(averageC) + "ºC";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView)findViewById(R.id.spotMeterValue)).setText(spotMeterValue);
                }
            });
        }

        /*
        Capture this image if requested.
        */
        if (this.imageCaptureRequested) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)) {
                Log.i("Test", "Activity: permissions needed");
                ActivityCompat.requestPermissions(this,
                        new String[] {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },PERMISSION_REQUEST_CODE);
                return;
            }
            imageCaptureRequested = false;
            Log.i("Test", "Activity: permissions ok");

            doExperiment();
//            new Thread(new Runnable() {
//                public void run() {
//                    final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
//                    String formatedDate = sdf.format(new Date());
//                    final String fileName = "FLIROne-" + formatedDate + ".jpg";
//                    try{
//                        lastSavedPath = path+ "/" + fileName;
//
//                        renderedImage.getFrame().save(new File(lastSavedPath), frameProcessor);
//                        File f = new File(lastSavedPath);
//                        Log.i("Test", "Activity: file exist?  " + f.exists());
//                        Log.i("Test", "Activity: file TotalSpace?  " + f.getTotalSpace());
//
//
//                        MediaScannerConnection.scanFile(context,
//                                new String[]{path + "/" + fileName}, null,
//                                new MediaScannerConnection.OnScanCompletedListener() {
//                                    @Override
//                                    public void onScanCompleted(String path, Uri uri) {
//                                        Log.i("ExternalStorage", "Scanned " + path + ":");
//                                        Log.i("ExternalStorage", "-> uri=" + uri);
//                                    }
//
//                                });
//
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(), " It's saved on:" + path + "/" + fileName,Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        startPerforming();
//
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(getApplicationContext(), " Can't save it",Toast.LENGTH_LONG).show();
//                            }
//                        });
//
//                    }
//                }
//            }).start();
       }
    }


    private float handTemp = 0;
    private void doExperiment(){


        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + '/' + TAG;
                File dir = new File(path);
                if(!dir.exists()) {
                    dir.mkdir();
                }
                String folder = "Опыт №" + dir.listFiles().length;
                dir = new File(path + '/' + folder);
                dir.mkdir();


                Log.i(TAG, "Начало эксперимента");
                showMessage("Внимание, наведите камеру на ладонь", Toast.LENGTH_SHORT);

                GLPreviewActivity.this.wait(3);
                showMessage("Фото ладони", Toast.LENGTH_SHORT);
                Log.i(TAG, "Фото ладони");
                performImage(path, folder, "Фото ладони.jpg",  0, true);

                GLPreviewActivity.this.wait(2);
                showMessage("Внимание, положите металл на ладонь", Toast.LENGTH_SHORT);

                GLPreviewActivity.this.wait(4);
                showMessage("Дождитесь, пока будут сделаны 3 фото", Toast.LENGTH_SHORT);

                GLPreviewActivity.this.wait(5);
                showMessage("Первое фото", Toast.LENGTH_SHORT);
                Log.i(TAG, "Первое фото");
                float startTime = System.currentTimeMillis();

                Log.i("Test", "время:" + startTime);

                performImage(path, folder, "Первое фото.jpg",  System.currentTimeMillis() - startTime, false);

                GLPreviewActivity.this.wait(15);

                showMessage("Второе фото", Toast.LENGTH_SHORT);
                Log.i(TAG, "Второе фото");
                performImage(path, folder, "Второе фото.jpg",  System.currentTimeMillis() - startTime, false);
                GLPreviewActivity.this.wait(15);

                showMessage("Третье фото", Toast.LENGTH_SHORT);
                Log.i(TAG, "Третье фото");
                performImage(path, folder, "Третье фото.jpg",  System.currentTimeMillis() - startTime, false);

                GLPreviewActivity.this.wait(5);

                showMessage("непонятно!", Toast.LENGTH_SHORT);
                handTemp = 0;
            }
        }).start();
    }

    private void performImage(final String path, final String folder, final String fileName, final float time, final boolean avg) {

        imageCaptureRequested = false;
        final Context context = this;
            new Thread(new Runnable() {
                public void run() {
                    try{

                        lastSavedPath = path + '/' + folder + '/' + fileName;
                        File f = new File(lastSavedPath);
                        f.createNewFile();
                        _renderedImage.getFrame().save(f, frameProcessor);

                        int[] thermalPixels = _renderedImage.thermalPixelValues();


                        Log.i("Test", "Activity: file exist?  " + f.exists());
                        Log.i("Test", "Activity: file TotalSpace?  " + f.getTotalSpace());

                        double temp = 0;
                        if(avg){
                            //temp = 0;
                            for (int i = 0; i < thermalPixels.length; i++) {
                                temp += (((double)thermalPixels[i]) - temp) / ((double) i + 1);
                            }

                            Log.i("Test", "новая рука: " + temp);
                        }
                        else {
//                            temp = Double.MAX_VALUE;
//                            for (int i = 0; i < thermalPixels.length; i++) {
//                                if (thermalPixels[i] < temp)
//                                    temp = thermalPixels[i];
//                            }

                            Log.i("Test", "HandTemp: " + handTemp);
                            //TODO: не оптимально
                            List<Integer> list = findAllLessThan(thermalPixels, (handTemp-1 + 273.15)*100 );
                            //temp = 0;t
                            Log.i("Test", "List lenght: " + list.size());
                            for (int i = 0; i < list.size(); i++) {
                                temp += (((double)list.get(i)) - temp) / ((double) i + 1);
                            }

                        }
                        temp = temp / 100 - 273.15;
                        Log.i("Test", "temp c: "+ temp);
                        if(handTemp == 0)
                            handTemp = (float) temp;

                        Realm realm2 = Realm.getDefaultInstance();
                        realm2.beginTransaction();

// Add a person
                        PhotoModel photo = realm2.createObject(PhotoModel.class);
                        photo.setFile_name(fileName);
                        photo.setFolder_name(folder);
                        photo.setMin_temp(temp);
                        photo.setDelta_time(time);
                        photo.hand_temp = handTemp;
                        photo.setResult("Lol result");

                        Log.i("Test", String.valueOf(photo.hand_temp));
                        realm2.commitTransaction();

                        realm2.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {

                            }
                        });


                        realm2.close();

                        File f1 = new File(path + '/' + folder + '/' + folder + ".txt");
                        if (!f1.exists()){
                            f1.createNewFile();
                        }

                        //FileOutputStream fos = new FileOutputStream(f1);
                        FileWriter fw = new FileWriter(f1, true);

                        //OutputStreamWriter osw = new OutputStreamWriter(fos);

                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write("File name: " + fileName + ";");
                        bw.newLine();
                        bw.write("Object temp: " + temp+ ";");
                        bw.newLine();
                        bw.write("Time: " + time+ ";");
                        bw.newLine();
                        bw.newLine();
                        bw.flush();
                        bw.close();
                        //fw.flush(); ХЗ почему
                        Log.i("Test", "File name: " + fileName);

                        Log.i("Test", "Объект сохранен");

                        //showMessage("Минимальная температура " + temp + "по Цельсию", Toast.LENGTH_SHORT);



                    }catch (final Exception e){
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), " Can't save it",Toast.LENGTH_LONG).show();
                                Log.e("Test", e.toString());
                            }
                        });

                    }
                }
            }).start();
    }

    private void startPerforming() {
        Log.i("Test", "Activity: onCreate");
        Intent serviceIntent = new Intent(this, Analizer.class);
        serviceIntent.putExtra("imagePath", lastSavedPath);
        serviceIntent.putExtra("delta", delta);
        startService(serviceIntent);
        Log.i("Test", "Activity: startService");
    }

    public static List<Integer> findAllLessThan(int[] array, double value) {
        //System.out.println("Ищем холодные точки");
        List<Integer> list = new ArrayList<>();
        for	(int i = 0, len = array.length; i < len; i++) {
            if (array[i] < value) {
                list.add(array[i]);
            }
        }
        return list;
    }


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    public void onTuneClicked(View v){
        if (flirOneDevice != null){
            flirOneDevice.performTuning();
        }

    }
    public void onCaptureImageClicked(View v){
        if(flirOneDevice != null) {
            this.imageCaptureRequested = true;
        }
    }
    public void onChangePeakValueClick(View v){
        String options = "0123456789ABCDEFGHIJ";
        Dialog dialog = new CharacterPickerDialog(this, new View(this),null, options, false);
        dialog.show();
    }

    public void onSimulatedChargeCableToggleClicked(View v){
        if(flirOneDevice instanceof SimulatedDevice){
            chargeCableIsConnected = !chargeCableIsConnected;
            ((SimulatedDevice)flirOneDevice).setChargeCableState(chargeCableIsConnected);
        }
    }
    public void onRotateClicked(View v){
        ToggleButton theSwitch = (ToggleButton)v;
        if (theSwitch.isChecked()){
            thermalSurfaceView.setRotation(180);
        }else{
            thermalSurfaceView.setRotation(0);
        }
    }
    public void onChangeViewClicked(View v){
        if (frameProcessor == null){
            ((ToggleButton)v).setChecked(false);
            return;
        }
        ListView paletteListView = (ListView)findViewById(R.id.paletteListView);
        ListView imageTypeListView = (ListView)findViewById(R.id.imageTypeListView);
        if (((ToggleButton)v).isChecked()){
            // only show palette list if selected image type is colorized
            paletteListView.setVisibility(View.INVISIBLE);
            for (RenderedImage.ImageType imageType : frameProcessor.getImageTypes()){
                if (imageType.isColorized()) {
                    paletteListView.setVisibility(View.VISIBLE);
                    break;
                }
            }
            imageTypeListView.setVisibility(View.VISIBLE);
            findViewById(R.id.imageTypeListContainer).setVisibility(View.VISIBLE);
        }else{
            findViewById(R.id.imageTypeListContainer).setVisibility(View.GONE);
        }


    }

    public void onImageTypeListViewClicked(View v){
        int index = ((ListView) v).getSelectedItemPosition();
        RenderedImage.ImageType imageType = RenderedImage.ImageType.values()[index];
        frameProcessor.setGLOutputMode(imageType);
        int paletteVisibility = (imageType.isColorized()) ? View.VISIBLE : View.GONE;
        findViewById(R.id.paletteListView).setVisibility(paletteVisibility);
    }

    public void onPaletteListViewClicked(View v){
        RenderedImage.Palette pal = (RenderedImage.Palette )(((ListView)v).getSelectedItem());
        frameProcessor.setImagePalette(pal);
    }

    /**
     * Example method of starting/stopping a frame stream to a host
     * @param v The toggle button pushed
     */
    public void onVividClicked(View v){
        final ToggleButton button = (ToggleButton)v;
        frameProcessor.setVividIrEnabled(button.isChecked());
    }

    @Override
    protected void onStart(){
        super.onStart();

        takePhoto = (ImageButton) findViewById(R.id.imageButton);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCaptureImageClicked(view);

//                int[] thermalPixels = _renderedImage.thermalPixelValues();
//                // average the center 9 pixels for the spot meter
//
//                int width = _renderedImage.width();
//                int height = _renderedImage.height();
//
//
//                double minTemp = Double.MAX_VALUE;
//
//                for (int i = 0; i < thermalPixels.length; i++){
//                    // Remember: all primitives are signed, we want the unsigned value,
//                    // we've used renderedImage.thermalPixelValues() to get unsigned values
//                    if(thermalPixels[i]<minTemp)
//                        minTemp = thermalPixels[i];
//                }
//                //double averageC = (averageTemp / 100) - 273.15;
//                minTemp = minTemp/100 - 273.15;
                //Toast.makeText(getApplicationContext(), minTemp + " Celsius",Toast.LENGTH_LONG).show();
            }
        });
        if (Device.getSupportedDeviceClasses(this).contains(FlirUsbDevice.class)){
            findViewById(R.id.pleaseConnect).setVisibility(View.VISIBLE);
        }
        try {
            Device.startDiscovery(this, this);
        }catch(IllegalStateException e){
            // it's okay if we've already started discovery
        }catch (SecurityException e){
            // On some platforms, we need the user to select the app to give us permisison to the USB device.
            Toast.makeText(this, "Please insert FLIR One and select "+getString(R.string.app_name), Toast.LENGTH_LONG).show();
            // There is likely a cleaner way to recover, but for now, exit the activity and
            // wait for user to follow the instructions;
            finish();
        }
    }

    ScaleGestureDetector mScaleDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent inputIntent = getIntent();

        delta = inputIntent.getIntExtra("delta", 1);

        setContentView(R.layout.activity_gl_preview);
        ringsStatus = (TextView) findViewById(R.id.ringsStatus);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View controlsViewTop = findViewById(R.id.fullscreen_content_controls_top);
        final View contentView = findViewById(R.id.fullscreen_content);

        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.ThermalRGBA8888Image;
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage), true);
        frameProcessor.setGLOutputMode(defaultImageType);

        thermalSurfaceView = (GLSurfaceView) findViewById(R.id.imageView);
        thermalSurfaceView.setPreserveEGLContextOnPause(true);
        thermalSurfaceView.setEGLContextClientVersion(2);
        thermalSurfaceView.setRenderer(frameProcessor);
        thermalSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        thermalSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);




        final String[] imageTypeNames = new String[]{ "Visible", "Thermal", "MSX" };
        final RenderedImage.ImageType[] imageTypeValues = new RenderedImage.ImageType[]{
                RenderedImage.ImageType.VisibleAlignedRGBA8888Image,
                RenderedImage.ImageType.ThermalRGBA8888Image,
                RenderedImage.ImageType.BlendedMSXRGBA8888Image,
        };

        ListView imageTypeListView = ((ListView)findViewById(R.id.imageTypeListView));
        imageTypeListView.setAdapter(new ArrayAdapter<>(this,R.layout.emptytextview,imageTypeNames));
        imageTypeListView.setSelection(defaultImageType.ordinal());
        imageTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (frameProcessor != null) {
                    //TODO: здесь режим картинки выбирается
                    RenderedImage.ImageType imageType = imageTypeValues[position];
                    frameProcessor.setGLOutputMode(imageType);
                    if (imageType.isColorized()){
                        findViewById(R.id.paletteListView).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.paletteListView).setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        imageTypeListView.setDivider(null);

        // Palette List View Setup
        ListView paletteListView = ((ListView)findViewById(R.id.paletteListView));
        paletteListView.setDivider(null);
        paletteListView.setAdapter(new ArrayAdapter<>(this, R.layout.emptytextview, RenderedImage.Palette.values()));
        paletteListView.setSelection(frameProcessor.getImagePalette().ordinal());
        paletteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (frameProcessor != null){
                    frameProcessor.setImagePalette(RenderedImage.Palette.values()[position]);
                }
            }
        });
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.

        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();

        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                            controlsViewTop.animate().translationY(visible ? 0 : -1 * mControlsHeight).setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                            controlsViewTop.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && !((ToggleButton)findViewById(R.id.change_view_button)).isChecked() && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.change_view_button).setOnTouchListener(mDelayHideTouchListener);


        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceRotation = orientation;
            }
        };
        mScaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.d("ZOOM", "zoom ongoing, scale: " + detector.getScaleFactor());
                frameProcessor.setMSXDistance(detector.getScaleFactor());
                return false;
            }
        });

        findViewById(R.id.fullscreen_content).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

    }

    @Override
    public void onPause(){
        super.onPause();

        thermalSurfaceView.onPause();
        if (flirOneDevice != null){
            flirOneDevice.stopFrameStream();
        }
    }
    @Override
    public void onResume(){
        super.onResume();

        thermalSurfaceView.onResume();

        if (flirOneDevice != null){
            flirOneDevice.startFrameStream(this);
        }
    }
    @Override
    public void onStop() {
        // We must unregister our usb receiver, otherwise we will steal events from other apps
        Log.e("PreviewActivity", "onStop, stopping discovery!");
        Device.stopDiscovery();
        flirOneDevice = null;
        super.onStop();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    HashSet darkPoints = new HashSet();
    ArrayList<List<Integer>> listOfDarkObjects = new ArrayList<>();

    private void findDarkObjects(int startIndex, boolean isNewObject){
        if(darkPoints.contains(startIndex))
            return;
        if(avgTempK - (thermalPixels[startIndex]) > 500){

            if(isNewObject){
                listOfDarkObjects.add(new ArrayList<Integer>());
                Log.d("MastersWork","!!!!!!!Новый объект!!!!!!!");
            }
            darkPoints.add(startIndex);
            listOfDarkObjects.get(listOfDarkObjects.size()-1).add(startIndex);
            findDarkObjects(startIndex - 1, false);
            findDarkObjects(startIndex + 1, false);
            findDarkObjects(startIndex + width, false);
            findDarkObjects(startIndex + width - 1, false);
            findDarkObjects(startIndex + width + 1, false);
        }
    };
    private void wait(int sec){
        try {
            Thread.sleep(sec * 1000);
        } catch (InterruptedException e) {

        }
    }
    public void showMessage(final String message, final int toastLenght){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message , toastLenght).show();
            }
        });

    }
}
