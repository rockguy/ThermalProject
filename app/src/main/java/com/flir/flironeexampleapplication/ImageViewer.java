package com.flir.flironeexampleapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.flir.flironeexampleapplication.util.Analizer;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.LoadedFrame;
import com.flir.flironesdk.RenderedImage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ImageViewer extends Activity {
    private String imagePath;
    private Intent intent;
    private int delta;
    private FrameProcessor frameProcessor;
    List<Integer> colds;
    ImageView imageView;
    private int maxTemp;
    private int minTemp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);

        imageView = (ImageView) findViewById(R.id.imageView);

        intent = getIntent();
        //подгрузить картинку
    }

    @Override
    protected void onStart() {
        super.onStart();

        imagePath = intent.getStringExtra("imagePath");
        delta = intent.getIntExtra("delta", 0);
        minTemp = intent.getIntExtra("minTemp", 0);
        maxTemp = intent.getIntExtra("maxTemp", 0);
        Log.i("Test", "Service: imagePath = " + imagePath);
        Log.i("Test", "Service: delta = " + delta);
        Log.i("Test", "Service: min = " + minTemp);
        Log.i("Test", "Service: max = " + maxTemp);
        File frameFile = new File(imagePath);

        Log.i("Test", "Service: frameFile.getName() = " + frameFile.getName());

        final LoadedFrame frame;
        try {
            frame = new LoadedFrame(frameFile);
            Log.i("Test", "Service: frameFile.getName() = " + frameFile.getName());
            frameProcessor = new FrameProcessor(getApplicationContext(), frameReceiver,
                    EnumSet.of( RenderedImage.ImageType.ThermalRadiometricKelvinImage, RenderedImage.ImageType.VisibleAlignedRGBA8888Image));
            Log.i("Test", "Service: frameReceiver have set");
            frameProcessor.setImagePalette(frame.getPreviewPalette());
            frameProcessor.processFrame(frame);
            Log.i("Test", "Service: frameProcessor.processFrame(frame) done");

        }catch (Exception e){

        }

    }

    private RenderedImage msxRenderedImage;
    private Bitmap thermalBitmap;
    private FrameProcessor.Delegate frameReceiver = new FrameProcessor.Delegate() {
        @Override
        public void onFrameProcessed(RenderedImage renderedImage) {
            Log.d("Test", "onFrameProcessed: " + renderedImage.imageType().name());
            int width=1;
//вот тут и будет обработка (видимо)
            //Log.i("Test", "Service: renderedImage.imageType() = " + renderedImage.imageType());
            if (renderedImage.imageType() == RenderedImage.ImageType.VisibleAlignedRGBA8888Image) {
                Log.i("Test", "Цветное изображение");
                msxRenderedImage = renderedImage;
                //thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);
                //thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));

                thermalBitmap = renderedImage.getBitmap();

            }else if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
                Log.i("Test", "ИК изображение");
                int[] thermalPixels = renderedImage.thermalPixelValues().clone();
                width = renderedImage.width();

                double avgTempK = avg(thermalPixels);
                double avgTemp = (avgTempK / 100) - 273.15;
                double newTemp = avgTempK-(delta*100);
                Log.d("Test","Средняя температура кельвины"+avgTempK);
                Log.d("Test","Средняя температура кельвины"+avgTemp);
                Log.d("Test","Ищем ниже чем K: "+newTemp);
                Log.d("Test","Ищем ниже чем C: "+(newTemp/100-273.15));

                Log.d("Test","Минимальная K: "+minValue(thermalPixels));
                Log.d("Test","Минимальная C: "+(minValue(thermalPixels)/100-273.15));


                Log.d("Test","А еще минимум в К : "+ ((minTemp + 273.15) * 100));
                Log.d("Test","И максимум в К: "+((maxTemp + 273.15) * 100));

                //Log.d("Test","Средняя температура цельсия"+avgTemp);
                //Индексы холодных точек
                colds = delta == 0
                        ? findAllInLimits(thermalPixels, (minTemp + 273.15) * 100, (maxTemp + 273.15) * 100)
                        : findAllLessThan(thermalPixels, newTemp);

                //colds = findAllLessThan(thermalPixels, avgTempK);
                Log.d("Test","холодные точки определены. Их " + colds.size());

                Log.i("Test", "Что-то красим");
                for (int i = 0; i < colds.size(); i++){
                    int integ = colds.get(i);
                    int x = integ % width;
                    int y = integ / width;
                    //совмещение камер - погрешность
                    y = (y - 80) > 0 ? (y - 80) : 0;
                    thermalBitmap.setPixel(x,y,getARGBColor(255,255,0,0));
//                    switch (i%3){
//
//                        case 0:
//                            thermalBitmap.setPixel(x,y, 255);
//                            break;
//                        case 1:
//                            thermalBitmap.setPixel(x,y, 0);
//                    }

                }
                Log.i("Test", thermalBitmap.toString());
                Log.i("Test", String.valueOf(thermalBitmap.getByteCount()));
                Log.i("Test", "Обновляем картинку");
                imageView.setImageBitmap(thermalBitmap);
//
            }

        }
    };

    public double minValue(int[] array){
        double min = Integer.MAX_VALUE;
        for(int i: array){
            if(i<min)
                min = i;
        }
        return min;
    }

    public int getARGBColor (int a, int r, int g, int b){
        return (a<<24) | (r<<16) | (g<<8) | b;
    }

    public static double avg(int[] array) {
        //System.out.println("Ищем среднюю температуру");
        double sum = 0;
        for	(double x : array) {
            sum += x;
        }
        return sum / array.length;
    }

    public List<Integer> findAllLessThan(int[] array, double value) {
        //System.out.println("Ищем холодные точки");
        List<Integer> list = new ArrayList<>();
        for	(int i = 0, len = array.length; i < len; i++) {
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
        for	(int i = 0, len = array.length; i < len; i++) {
            //Log.d("Test","сравниваем " + array[i] + "с минимумом +" + value);

            if (array[i] < max && array[i] > min) {
                list.add(i);
            }
        }
        return list;
    }
}
