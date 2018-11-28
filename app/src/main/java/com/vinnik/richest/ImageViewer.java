package com.vinnik.richest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.LoadedFrame;
import com.flir.flironesdk.RenderedImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import io.realm.Realm;

import static com.vinnik.richest.StartActivity.TAG;

public class ImageViewer extends Activity {
    private String imagePath;
    private Intent inputIntent;
    private FrameProcessor frameProcessor;

    ImageView imageView;
    Button saveButton;
    private TextView resultTop;
    private TextView resultBottom;

    int width;

    private double CONSTANT_FACTOR_K;
    private double CONSTANT_FACTOR_M;

    Realm realm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        imageView = (ImageView) findViewById(R.id.imageView);
        saveButton = (Button) findViewById(R.id.save_button);
        resultTop = (TextView) findViewById(R.id.resultTop);
        resultBottom = (TextView) findViewById(R.id.resultBottom);
        inputIntent = getIntent();
        //подгрузить картинку
    }

    @Override
    protected void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();

//        saveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                realm.beginTransaction();
//                ResearcherModel model = realm.createObject(ResearcherModel.class);
//                model.setAvgTemp(avgTemp);
//                model.setMinTemp(minTemp);
//                model.setOutdoorTemp(outdoorTemp);
//                model.setAirHumidity(airHumidity);
//                model.setResult(result.getText().toString());
//                model.setTheWeather(theWeather);
//                model.setTime(new Date());
//                model.setWindSpeed(windSpeed);
//
//                realm.commitTransaction();
//                Toast.makeText(getApplicationContext(),"охранено",Toast.LENGTH_SHORT).show();
//            }
//        });
        imagePath = inputIntent.getStringExtra("imagePath");
        CONSTANT_FACTOR_K = inputIntent.getDoubleExtra("factorK",6);
        CONSTANT_FACTOR_M = inputIntent.getDoubleExtra("factorM",6);

        File frameFile = new File(imagePath);
        resultTop = (TextView) findViewById(R.id.resultTop);
        resultBottom = (TextView) findViewById(R.id.resultBottom);

        Log.i("Test", "Service: frameFile.getName() = " + frameFile.getName());

        final LoadedFrame frame;
        try {
            frame = new LoadedFrame(frameFile);
            Log.i("Test", "Service: frameFile.getName() = " + frameFile.getName());
            frameProcessor = new FrameProcessor(getApplicationContext(), frameReceiver,
                    EnumSet.of(RenderedImage.ImageType.ThermalRadiometricKelvinImage, RenderedImage.ImageType.ThermalRGBA8888Image));
            Log.i("Test", "Service: frameReceiver have set");
            frameProcessor.setImagePalette(frame.getPreviewPalette());
            frameProcessor.processFrame(frame);
            Log.i("Test", "Service: frameProcessor.processFrame(frame) done");

        } catch (Exception e) {

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        realm.close();
    }

    private Bitmap thermalBitmap;
    // private FrameProcessor.Delegate frameReceiver = new FrameProcessor.Delegate() {
    //     @Override
    //       public void onFrameProcessed(RenderedImage renderedImage) {
//            Log.d("Test", "onFrameProcessed: " + renderedImage.imageType().name());
//            int width=1;
////вот тут и будет обработка (видимо)
//            //Log.i("Test", "Service: renderedImage.imageType() = " + renderedImage.imageType());
//            if (renderedImage.imageType() == RenderedImage.ImageType.VisibleAlignedRGBA8888Image) {
//                Log.i("Test", "Цветное изображение");
//                msxRenderedImage = renderedImage;
//                //thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);
//                //thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));
//
//                thermalBitmap = renderedImage.getBitmap();
//
//            }else if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
//                Log.i("Test", "ИК изображение");
//                int[] thermalPixels = renderedImage.thermalPixelValues().clone();
//                width = renderedImage.width();
//
//                double avgTempK = avg(thermalPixels);
//                avgTemp = (avgTempK / 100) - 273.15;
//                double newTempK = avgTempK-(delta*100);
//                newTemp = (newTempK / 100) - 273.15;
//
//                //Log.d("Test","Средняя температура цельсия"+avgTemp);
//                //Индексы холодных точек
////                colds = delta == 0
////                        ? findAllInLimits(thermalPixels, (minTemp + 273.15) * 100, (maxTemp + 273.15) * 100)
////                        : findAllLessThan(thermalPixels, newTempK);
//
//                //colds = findAllLessThan(thermalPixels, avgTempK);
//                Log.d("Test","холодные точки определены. Их " + colds.size());
//
//                Log.i("Test", "Что-то красим");
//                for (int i = 0; i < colds.size(); i++){
//                    int integ = colds.get(i);
//                    int x = integ % width;
//                    int y = integ / width;
//                    //совмещение камер - погрешность
//                    y = (y - 80) > 0 ? (y - 80) : 0;
//                    thermalBitmap.setPixel(x,y,getARGBColor(255,255,0,0));
////                    switch (i%3){
////
////                        case 0:
////                            thermalBitmap.setPixel(x,y, 255);
////                            break;
////                        case 1:
////                            thermalBitmap.setPixel(x,y, 0);
////                    }
//
//                }
//                Log.i("Test", thermalBitmap.toString());
//                Log.i("Test", String.valueOf(thermalBitmap.getByteCount()));
//                Log.i("Test", "Обновляем картинку");
//                imageView.setImageBitmap(thermalBitmap);
////
//            }
//
//        }
//    };


    private class ImageData {
        private double minTemp;
        private double avgTemp;

        ImageData(){
            minTemp = Double.MAX_VALUE;
        }
        public double getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(double minTemp) {
            this.minTemp = minTemp;
        }

        public double getAvgTemp() {
            return avgTemp;
        }

        public void setAvgTemp(double avgTemp) {
            this.avgTemp = avgTemp;
        }
    }

    private FrameProcessor.Delegate frameReceiver = new FrameProcessor.Delegate() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onFrameProcessed(RenderedImage renderedImage) {
            width = renderedImage.width();
            if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRGBA8888Image) {
                Log.i(TAG, "Цветное изображение");
                thermalBitmap = renderedImage.getBitmap();
                imageView.setImageBitmap(thermalBitmap);

            } else if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
                int[] data = renderedImage.thermalPixelValues();
                int[] topData = new int[data.length/2];
                int[] bottomData = new int[data.length/2];
                topData = Arrays.copyOfRange(data,0,data.length/2);
                bottomData = Arrays.copyOfRange(data,data.length/2, data.length);
                ImageData imageData = getImageData(topData);
                double factorM = imageData.getMinTemp();
                double factorD = getFactorD(topData, factorM, imageData.getAvgTemp());
                double factorK = factorD / factorM;

                String result = (factorM > CONSTANT_FACTOR_M && factorK < CONSTANT_FACTOR_K)? "Натуральный" : "Искусственный";
                resultTop.setText(String.format("factorM: %s; factorD: %s; factorK: %s result: %s", factorM, factorD, factorK, result));

                imageData = getImageData(bottomData);
                factorM = imageData.getMinTemp();
                factorD = getFactorD(bottomData, factorM, imageData.getAvgTemp());
                factorK = factorD / factorM;

                result = (factorM > CONSTANT_FACTOR_M && factorK < CONSTANT_FACTOR_K)? "Натуральный" : "Искусственный";
                resultBottom.setText(String.format("factorM: %s; factorD: %s; factorK: %s result: %s", factorM, factorD, factorK, result));

            }
        }
    };

    private ImageData getImageData(int[] data) {
        int count = 0;
        int sum = 0;
        ImageData imageData = new ImageData();
        for (int i : data) {
            if (i < (76 + 273.15) * 100){
                count++;
                sum += i/100-273.15;
                if(imageData.getMinTemp() > i/100-273.15)
                    imageData.setMinTemp(i/100-273.15);
            }
        }
        imageData.setAvgTemp(sum/count);
        return imageData;
    }

    private double getFactorD(int[] data, double factorM, double avgTemp) {
        double factorD = 0;
        int count = 0;
        for (int i : data) {
            if (i < (76 + 273.15) * 100){
                count++;
                factorD += (i / 100 - 273.15 - avgTemp) * (i / 100 - 273.15 - avgTemp);
            }
        }
        factorD = factorD / --count;
        return factorD;
    }

    private int[] findPoint(int[] values, double minTemp) {
        int[] result = new int[2];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == minTemp) {
                result[0] = i % width;
                result[1] = i / width;
                return result;
            }
        }
        return result;
    }

    private Bitmap drawRound(Bitmap bitmap, int[] coord, int radius) {
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setARGB(150, 220, 0, 0);
        paint.setStrokeWidth(10);

        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawCircle(coord[0], coord[1], radius, paint);

        return bmp;

    }

    public double minValue(int[] array) {
        double min = Integer.MAX_VALUE;
        for (int i : array) {
            if (i < min)
                min = i;
        }
        return min;
    }

    public int getARGBColor(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static double avg(int[] array) {
        //System.out.println("Ищем среднюю температуру");
        double sum = 0;
        for (double x : array) {
            sum += x;
        }
        return sum / array.length;
    }

    public List<Integer> findAllLessThan(int[] array, double value) {
        //System.out.println("Ищем холодные точки");
        List<Integer> list = new ArrayList<>();
        for (int i = 0, len = array.length; i < len; i++) {
            //Log.d("Test","сравниваем " + array[i] + "с минимумом +" + value);

            if (array[i] < value) {
                list.add(i);
            }
        }
        return list;
    }

    public List<Integer> findAllInLimits(int[] array, double min, double max) {
        //System.out.println("Ищем холодные точки");
        List<Integer> list = new ArrayList<>();
        for (int i = 0, len = array.length; i < len; i++) {
            //Log.d("Test","сравниваем " + array[i] + "с минимумом +" + value);

            if (array[i] < max && array[i] > min) {
                list.add(i);
            }
        }
        return list;
    }

    @Override
    public void onBackPressed() {
        final Intent i = new Intent(this, GLUserPreviewActivity.class);
        startActivity(i);
    }

    private List<Double> getObjectParams(int[] array) {
        List<Double> result = new ArrayList<>();

        return result;
    }
}
