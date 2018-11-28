package com.vinnik.richest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flir.flironesdk.Device;
import com.flir.flironesdk.FlirUsbDevice;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.vinnik.richest.util.SystemUiHider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;

import static com.vinnik.richest.StartActivity.TAG;

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
public class GLUserPreviewActivity extends Activity implements Device.Delegate, FrameProcessor.Delegate, Device.StreamDelegate {
    private static final int PERMISSION_REQUEST_CODE = 0;
    private double CONSTANT_FACTOR_K;
    private double CONSTANT_FACTOR_M;
    //TODO: Удалить
    private static double CONSTANT_DELTA = 0;

    Realm realm;
    GLSurfaceView thermalSurfaceView;
    private ImageButton takePhoto;

    private volatile boolean imageCaptureRequested = false;
    //private volatile Socket streamSocket = null;
    private boolean chargeCableIsConnected = true;

    private int deviceRotation = 0;
    private OrientationEventListener orientationEventListener;


    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;

    private String lastSavedPath;

    private Device.TuningState currentTuningState = Device.TuningState.Unknown;
    private static boolean notStarted = true;

    // Device Delegate methods

    // Called during device discovery, when a device is connected
    // During this callback, you should save a reference to device
    // You should also set the power update delegate for the device if you have one
    // Go ahead and start frame stream as soon as connected, in this use case
    // Finally we create a frame processor for rendering frames

    public void onDeviceConnected(Device device) {
        Log.i("ExampleApp", "Device connected!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.pleaseConnect).setVisibility(View.GONE);
            }
        });

        flirOneDevice = device;
        flirOneDevice.startFrameStream(this);
    }

    /**
     * Indicate to the user that the device has disconnected
     */
    public void onDeviceDisconnected(Device device) {
        Log.i("ExampleApp", "Device disconnected!");

        final ToggleButton chargeCableButton = (ToggleButton) findViewById(R.id.chargeCableToggle);
        final TextView levelTextView = (TextView) findViewById(R.id.batteryLevelTextView);
        final ImageView chargingIndicator = (ImageView) findViewById(R.id.batteryChargeIndicator);
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
        //orientationEventListener.disable();
    }

    /**
     * If using RenderedImage.ImageType.ThermalRadiometricKelvinImage, you should not rely on
     * the accuracy if tuningState is not Device.TuningState.Tuned
     *
     * @param tuningState
     */
    public void onTuningStateChanged(Device.TuningState tuningState) {
    }

    @Override
    public void onAutomaticTuningChanged(boolean deviceWillTuneAutomatically) {

    }

    private ColorFilter originalChargingIndicatorColor = null;

    // StreamDelegate method
    //Todo: сейчас картинка лагает
    public void onFrameReceived(Frame frame) {
        //Log.v(TAG, "Frame received!");
        if (currentTuningState != Device.TuningState.InProgress) {
            frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
            thermalSurfaceView.requestRender();
        }

    }
    int[] thermalPixels;

    // Frame Processor Delegate method, will be called each time a rendered frame is produced
    public void onFrameProcessed(final RenderedImage renderedImage) {
        //Log.v("Test", "Frame Processing!, type: " + renderedImage.imageType().name());
//        if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
//            // Note: this code is not optimized
//            imageCaptureRequested = checkImage(renderedImage.thermalPixelValues());
//        }
//        else if (renderedImage.imageType() == RenderedImage.ImageType.VisibleAlignedRGBA8888Image
//                && imageCaptureRequested && !isProcessing) {
//            isProcessing=true;
//            Log.i(TAG, "ЕСТЬ ТОЧКА!");
//            savePhoto(renderedImage);
//
//        }

        if(imageCaptureRequested){
            savePhoto(renderedImage);
        }
    }

    public void savePhoto(RenderedImage renderedImage){
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            Log.i("Test", "Activity: permissions needed");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE);
            return;
        }
        imageCaptureRequested = false;
        Log.i(TAG, "Activity: permissions ok");


        final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        final String fileName = "FLIROne-" + formatedDate + ".jpg";
        try {
            lastSavedPath = path + "/" + fileName;
            Log.i(TAG, "Activity: file full name:  " + lastSavedPath);
            Log.i(TAG, "Activity: file full name:  " + renderedImage);
            Log.i(TAG, "Activity: file full name:  " + renderedImage.getFrame());
            renderedImage.getFrame().save(new File(lastSavedPath), frameProcessor);

            Log.i(TAG, "Сохранили");
            File f = new File(lastSavedPath);
            Log.i(TAG, "Создаем намерение");
            Intent i = new Intent(getApplication(), ImageViewer.class);
            i.putExtra("imagePath", lastSavedPath);
            i.putExtra("factorK", CONSTANT_FACTOR_K);
            i.putExtra("factorM", CONSTANT_FACTOR_M);

            Log.i(TAG, "Выполняем намерение");
            startActivity(i);

        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), " Can't save it", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public boolean checkImage(int[] array){
        double min = Integer.MAX_VALUE;
        double avg = 0;
        for (int x : array) {
            avg += x;
            if (x < min)
                min = x;
        }
        avg = avg / array.length;
        return (avg-min) / 100 > CONSTANT_DELTA;
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

    public void onTuneClicked(View v) {
        if (flirOneDevice != null) {
            flirOneDevice.performTuning();
        }

    }

    public void onCaptureImageClicked(View v) {
        if (flirOneDevice != null) {
            Log.i(TAG, "onCaptureImageClicked");
            this.imageCaptureRequested = true;
        }
    }
    /**
     * Example method of starting/stopping a frame stream to a host
     *
     * @param v The toggle button pushed
     */
    public void onVividClicked(View v) {
        final ToggleButton button = (ToggleButton) v;
        frameProcessor.setVividIrEnabled(button.isChecked());
    }

    @Override
    protected void
    onStart() {
        super.onStart();

        takePhoto = (ImageButton) findViewById(R.id.imageButton);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCaptureImageClicked(view);
            }
        });
        if (Device.getSupportedDeviceClasses(this).contains(FlirUsbDevice.class)) {
            findViewById(R.id.pleaseConnect).setVisibility(View.VISIBLE);
        }
        try {
            Device.startDiscovery(this, this);
        } catch (IllegalStateException e) {
            // it's okay if we've already started discovery
        } catch (SecurityException e) {
            // On some platforms, we need the user to select the app to give us permisison to the USB device.
            Toast.makeText(this, "Please insert FLIR One and select " + getString(R.string.app_name), Toast.LENGTH_LONG).show();
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
        //CONSTANT_DELTA = inputIntent.getIntExtra("delta",6);
        CONSTANT_FACTOR_K = inputIntent.getDoubleExtra("factorK",6);
        CONSTANT_FACTOR_M = inputIntent.getDoubleExtra("factorM",6);

        setContentView(R.layout.activity_gl_preview_user);
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View controlsViewTop = findViewById(R.id.fullscreen_content_controls_top);
        final View contentView = findViewById(R.id.fullscreen_content);

        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.ThermalRGBA8888Image;
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage, RenderedImage.ImageType.VisibleAlignedRGBA8888Image), true);
        frameProcessor.setGLOutputMode(defaultImageType);

        thermalSurfaceView = (GLSurfaceView) findViewById(R.id.imageView);
        thermalSurfaceView.setPreserveEGLContextOnPause(true);
        thermalSurfaceView.setEGLContextClientVersion(2);
        thermalSurfaceView.setRenderer(frameProcessor);
        thermalSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        thermalSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);


        final String[] imageTypeNames = new String[]{"Visible", "Thermal", "MSX"};
        final RenderedImage.ImageType[] imageTypeValues = new RenderedImage.ImageType[]{
                RenderedImage.ImageType.VisibleAlignedRGBA8888Image,
                RenderedImage.ImageType.ThermalRGBA8888Image,
                RenderedImage.ImageType.BlendedMSXRGBA8888Image,
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


    }

    @Override
    public void onPause() {
        super.onPause();

        thermalSurfaceView.onPause();
        if (flirOneDevice != null) {
            flirOneDevice.stopFrameStream();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        takePhoto = (ImageButton) findViewById(R.id.imageButton);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCaptureImageClicked(view);
            }
        });
        if (Device.getSupportedDeviceClasses(this).contains(FlirUsbDevice.class)) {
            findViewById(R.id.pleaseConnect).setVisibility(View.VISIBLE);
        }
        try {
            Device.startDiscovery(this, this);
        } catch (IllegalStateException e) {
            // it's okay if we've already started discovery
        } catch (SecurityException e) {
            // On some platforms, we need the user to select the app to give us permisison to the USB device.
            Toast.makeText(this, "Please insert FLIR One and select " + getString(R.string.app_name), Toast.LENGTH_LONG).show();
            // There is likely a cleaner way to recover, but for now, exit the activity and
            // wait for user to follow the instructions;
            finish();
        }

        thermalSurfaceView.onResume();

        if (flirOneDevice != null) {
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
        //   delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

//    Handler mHideHandler = new Handler();
//    Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mSystemUiHider.hide();
//        }
//    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }

    HashSet darkPoints = new HashSet();
    ArrayList<List<Integer>> listOfDarkObjects = new ArrayList<>();

    public void onBackPressed() {
        final Intent i = new Intent(this, StartActivity.class);
        startActivity(i);
    }
}
