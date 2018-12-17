package com.vinnik.richest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.LoadedFrame;
import com.flir.flironesdk.RenderedImage;
import com.vinnik.richest.Adapters.ImageViewAdapter;
import com.vinnik.richest.models.DiamondModel;
import com.vinnik.richest.util.Analizer;
import com.vinnik.richest.util.TemperatureHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import io.realm.Realm;

import static com.vinnik.richest.StartActivity.DIAMOND_FACTORS;
import static com.vinnik.richest.StartActivity.FACTOR_K;
import static com.vinnik.richest.StartActivity.TAG;

public class ImageViewer extends Activity {
    private String imagePath;
    private Intent inputIntent;
    private FrameProcessor frameProcessor;
    ImageViewAdapter adapter;

    ImageView imageView;
    ListView resultsListView;
    Button saveButton;

    SharedPreferences preferences;


    int width;

    private float CONSTANT_FACTOR_K;
//    private float CONSTANT_FACTOR_M;

    private float DIAMOND_TOP_TEMPERATURE_C = 76;
    private float DIAMOND_TOP_TEMPERATURE_K = TemperatureHelper.celsiusToKelvin(DIAMOND_TOP_TEMPERATURE_C);

    Realm realm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        preferences = getSharedPreferences(DIAMOND_FACTORS, MODE_PRIVATE);

        imageView = (ImageView) findViewById(R.id.imageView);
        inputIntent = getIntent();

        resultsListView = (ListView) findViewById(R.id.resultsListView);

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
        CONSTANT_FACTOR_K = preferences.getFloat(FACTOR_K, 0);
//        CONSTANT_FACTOR_M = preferences.getFloat(FACTOR_M, 0);

        File frameFile = new File(imagePath);

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
        private float minTemp;
        private float avgTemp;

        ImageData() {
            minTemp = Float.MAX_VALUE;
        }

        public float getMinTemp() {
            return minTemp;
        }

        public void setMinTemp(float minTemp) {
            this.minTemp = minTemp;
        }

        public float getAvgTemp() {
            return avgTemp;
        }

        public void setAvgTemp(float avgTemp) {
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
                int[] thermalPixels = renderedImage.thermalPixelValues().clone();
                int width = renderedImage.width();
                renderedImage = null;
                double avgTempK = avg(thermalPixels);
                List<Analizer.Pair> colds = Analizer.findAllLessThan(thermalPixels, avgTempK - 300);/*-delta*/
                List<DiamondModel> diamonds = new ArrayList<>();

                /*checkGroup(colds, width);
                Log.d("Test","группы определены, их" + count );
                int lenght = 0;
                int i = 0;
                do{
                    ArrayList data = new ArrayList();
                    for (int j = 0; j < colds.size(); j++) {
                        if (colds.get(j).group == i)
                            data.add(thermalPixels[colds.get(j).index]);
                    }

                    lenght = data.size();

                    if(lenght == 0){
                        i++;
                        continue;
                    }
                    ImageData imageData = getImageData(data);
                    float factorM = imageData.getMinTemp();
                    float factorD = getFactorD(data, factorM, imageData.getAvgTemp());
                    float factorK = factorD / factorM;

                    DiamondModel diamond = new DiamondModel();
                    diamond.setFactorD(factorD);
                    diamond.setFactorM(factorM);
                    diamond.setFactorK(factorK);
                    //diamond.setType(factorK < CONSTANT_FACTOR_K);

                    diamonds.add(diamond);
                    i++;
                }while (i < 40);
*/
                ArrayList data = new ArrayList();
                for (int j = 0; j < colds.size(); j++) {
                    data.add(thermalPixels[colds.get(j).index]);
                }


                ImageData imageData = getImageData(data);
                float factorM = imageData.getMinTemp();
                float factorD = getFactorD(data, factorM, imageData.getAvgTemp());
                float factorK = factorD / factorM;

                DiamondModel diamond = new DiamondModel();
                diamond.setFactorD(factorD);
                diamond.setFactorM(factorM);
                diamond.setFactorK(factorK);
                //diamond.setType(factorK < CONSTANT_FACTOR_K);

                diamonds.add(diamond);

                adapter = new ImageViewAdapter(diamonds, (Context) ImageViewer.this);
                resultsListView.setAdapter(adapter);
                /*
                int[] data = renderedImage.thermalPixelValues();
                int[] topData = Arrays.copyOfRange(data, 0, data.length / 2);
                int[] bottomData = Arrays.copyOfRange(data, data.length / 2, data.length);
                ImageData imageData = getImageData(topData);
                float factorM = imageData.getMinTemp();
                float factorD = getFactorD(topData, factorM, imageData.getAvgTemp());
                float factorK = factorD / factorM;

                String result = (factorM > CONSTANT_FACTOR_M && factorK < CONSTANT_FACTOR_K) ? "Натуральный" : "Искусственный";
                resultTop.setText(String.format("factorM: %s; factorD: %s; factorK: %s result: %s", factorM, factorD, factorK, result));

                imageData = getImageData(bottomData);
                factorM = imageData.getMinTemp();
                factorD = getFactorD(bottomData, factorM, imageData.getAvgTemp());
                factorK = factorD / factorM;

                result = (factorM > CONSTANT_FACTOR_M && factorK < CONSTANT_FACTOR_K) ? "Натуральный" : "Искусственный";
                resultBottom.setText(String.format("factorM: %s; factorD: %s; factorK: %s result: %s", factorM, factorD, factorK, result));
*/
            }
        }
    };

    private ImageData getImageData(int[] data) {
        int count = 0;
        int sum = 0;
        ImageData imageData = new ImageData();
        for (int i : data) {
            if (i < DIAMOND_TOP_TEMPERATURE_K) {
                count++;
                float iC = TemperatureHelper.kelvinToCelsius(i);
                sum += iC;
                if (imageData.getMinTemp() > iC)
                    imageData.setMinTemp(iC);
            }
        }
        imageData.setAvgTemp(sum / count);
        return imageData;
    }

    private ImageData getImageData(List<Integer> data) {
        int count = 0;
        int sum = 0;
        ImageData imageData = new ImageData();
        for (int i : data) {
            if (i < DIAMOND_TOP_TEMPERATURE_K) {
                count++;
                float iC = TemperatureHelper.kelvinToCelsius(i);
                sum += iC;
                if (imageData.getMinTemp() > iC)
                    imageData.setMinTemp(iC);
            }
        }
        imageData.setAvgTemp(sum / count);
        return imageData;
    }

    private float getFactorD(int[] data, float factorM, float avgTemp) {
        float factorD = 0;
        int count = 0;
        for (int i : data) {
            if (i < DIAMOND_TOP_TEMPERATURE_K) {
                count++;
                factorD += Math.pow(TemperatureHelper.kelvinToCelsius(i) - avgTemp, 2);
            }
        }
        factorD = factorD / --count;
        return factorD;
    }

    private float getFactorD(List<Integer> data, float factorM, float avgTemp) {
        float factorD = 0;
        int count = 0;
        for (int i : data) {
            if (i < DIAMOND_TOP_TEMPERATURE_K) {
                count++;
                factorD += Math.pow(TemperatureHelper.kelvinToCelsius(i) - avgTemp, 2);
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

    class ResultModel {

    }
}
