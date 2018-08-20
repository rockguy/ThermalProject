package com.flir.flironeexampleapplication.util;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

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

public class Analizer extends Service {

    String imagePath;
    private FrameProcessor frameProcessor;
    volatile RenderedImage msxRenderedImage;
    volatile Bitmap thermalBitmap;
    File frameFile;
    private FrameProcessor.Delegate frameReceiver = new FrameProcessor.Delegate() {
        @Override
        public void onFrameProcessed(RenderedImage renderedImage) {
            //Log.d(LOG_TAG, "onFrameProcessed");
//вот тут и будет обработка (видимо)
            Log.i("Test", "Service: renderedImage.imageType() = " + renderedImage.imageType());
            if (renderedImage.imageType() == RenderedImage.ImageType.BlendedMSXRGBA8888Image) {
                msxRenderedImage = renderedImage;
                thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);
                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));
//
            }else if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {
                int[] thermalPixels = renderedImage.thermalPixelValues().clone();
                int width = renderedImage.width();
                renderedImage = null;
                double avgTempK = avg(thermalPixels);
                //double avgTemp = (avgTempK / 100) - 273.15;
                Log.d("Test","Средняя температура кельвины"+avgTempK);
                //Log.d("Test","Средняя температура цельсия"+avgTemp);
                List<Pair> colds = findAllLessThan(thermalPixels, avgTempK-delta*100);

                Log.d("Test","холодные точки определены. Их " + colds.size());
                //int count =
                        checkGroup(colds, width);
                //Log.d("Test","группы определены, их" + count );
                double[][] l = new double[40][3];
                int[] l2 = new int[40];
                for(Pair p: colds){
                    l[p.group][0] += (((double)thermalPixels[p.index]) -  l[p.group][0]) / (++l2[p.group]);
                    l[p.group][1] += ((p.index % width) - l[p.group][1]) / l2[p.group];//ширина
                    l[p.group][2] += ((p.index / width) - l[p.group][2]) / l2[p.group];//высота
                }

                File output = new File(imagePath.replace(".jpg",".txt"));

                try {
//                    OutputStreamWriter osw = new OutputStreamWriter(getApplicationContext().openFileOutput("config.txt", Context.MODE_PRIVATE));
//                    File fout = new File("out.txt");

                    FileOutputStream fos = new FileOutputStream(output);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                    bw.write("Средняя температура: " + ((avgTempK / 100) - 273.15));
                    bw.newLine();
                    bw.flush();


                    int count = 0;
                    for(int i = 0; i < 40; i++){
                        if(l[i][0] != 0){
                            bw.write("Группа: " + ++count + " температура: " + ((l[i][0] / 100) - 273.15) + "Координата Х = " + l[i][1] + "; Координата Y = " + l[i][2]);
                            bw.newLine();
                            bw.flush();
                        }
                        else{

                        }
                    }
                    Log.d("Test","группы определены, их" + count );

                    bw.close();
                    fos.close();
                    Log.i("Test", "Test complite");
                }
                catch (IOException e) {
                    Log.e("Exception", "File write failed: " + e.toString());
                }

            }
        }
    };
    private int delta;

    private void writeToFile(String data,Context context) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("Test", "Service: onCreate");
        Notification.Builder builder = new Notification.Builder(this)
                ;
        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();
        startForeground(777, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Это что-то типо main
        Log.i("Test", "Service: onStartCommand");
        imagePath = intent.getStringExtra("imagePath");
        delta = intent.getIntExtra("delta",1);
        Log.i("Test", "Service: imagePath = " + imagePath);
        Log.i("Test", "Service: delta = " + delta);
        File frameFile = new File(imagePath);

        Log.i("Test", "Service: frameFile.getName() = " + frameFile.getName());

        final LoadedFrame frame;
        try {
            frame = new LoadedFrame(frameFile);
            Log.i("Test", "Service: frameFile.getName() = " + frameFile.getName());
            frameProcessor = new FrameProcessor(getApplicationContext(), frameReceiver,
                    EnumSet.of(frame.getPreviewImageType(), RenderedImage.ImageType.ThermalRadiometricKelvinImage));
            Log.i("Test", "Service: frameReceiver have set");
            frameProcessor.setImagePalette(frame.getPreviewPalette());
            frameProcessor.processFrame(frame);
            Log.i("Test", "Service: frameProcessor.processFrame(frame) done");

            onDestroy();
        }catch (final RuntimeException ex){
            Log.i("Test", "Service: Exception = " + ex.getMessage());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Test", "Service: onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("Test", "Service: onTaskRemoved");
    }

    enum Direction {

        RIGHT        { public int move(int index, int width) { return ++index;         } },
        RIGHT_BOTTOM { public int move(int index, int width) { return ++index + width; } },
        BOTTOM       { public int move(int index, int width) { return   index + width; } },
        LEFT_BOTTOM  { public int move(int index, int width) { return --index + width; } },
        LEFT         { public int move(int index, int width) { return --index;         } },
        LEFT_TOP     { public int move(int index, int width) { return --index - width; } },
        TOP          { public int move(int index, int width) { return   index - width; } },
        RIGHT_TOP    { public int move(int index, int width) { return ++index - width; } };

        public static final Direction[] VALUES = values();

        public abstract int move(int index, int width);
    }

    static class Pair {

        public static Pair of(int index) {
            return of(index, 0);
        }

        public static Pair of(int index, int group) {
            return new Pair(index, group);
        }

        int index;
        int group;

        Pair(int index, int group) {
            this.index = index;
            this.group = group;
        }

        public boolean equals(Object pair) {
            return index == ((Pair) pair).index;
        }

        public boolean isNear(Pair pair, int width) {
            int gr = pair.group;
            int ind = pair.index;
            for (Direction direction : Direction.VALUES) {
                if (ind == direction.move(index, width)) {
                    return true;
                }
            }
            return false;
        }

        public int[] round(int width) {
            int x = index % width;
            //int y = index / width;
            int indexPlusWidth = index + width;
            if (x * (x - --width) < 0) {
                return new int[]{index + 1, indexPlusWidth + 1, indexPlusWidth, indexPlusWidth - 1};
            }
            if (x == 0) {
                return new int[]{index + 1, indexPlusWidth + 1, indexPlusWidth};
            } else {
                return new int[]{indexPlusWidth - 1, indexPlusWidth};
            }

        }

        public String toString() {
            return "(" + this.index + '=' + this.group + ')';
        }
    }

    public static double avg(int[] array) {
        //System.out.println("Ищем среднюю температуру");
        double sum = 0;
        for	(double x : array) {
            sum += x;
        }
        return sum / array.length;
    }

    public static List<Pair> findAllLessThan(int[] array, double value) {
        //System.out.println("Ищем холодные точки");
        List<Pair> list = new ArrayList<>();
        for	(int i = 0, len = array.length; i < len; i++) {
            if (array[i] < value) {
                list.add(Pair.of(i));
            }
        }
        return list;
    }

    public static int checkGroup(List<Pair> pairs, int width) {
        //System.out.println("Разбиваем точки на группы");
        int iterator = 0;
        int iterator1 = 0;
        int countOfGroup = 0;
        for (Pair p : pairs) {
            Log.i("Test", String.valueOf(++iterator));
            // p - текущая точка
            if (p.group == 0) {
                p.group = ++countOfGroup;
                //System.out.println("Count of group: " + countOfGroup);
            }
            //System.out.println('/');
            // перебираем точки вокруг текущей точки
            for (int dir : p.round(width)) {
                Pair currentPair;
                // есть ли такая точка в списке?
                //System.out.println(dir);
                //System.out.println(pairs.indexOf(dir));
                if ((currentPair = find(pairs, dir)) != null) {
                    // есть
                    // есть у точки группа?

                    int currentGroup = currentPair.group;
                    if (currentGroup == 0) {
                        currentPair.group = p.group;
                        continue;
                    }
                    // отличается от группы текущей точки
                    else if (currentGroup != p.group) {
                        Log.i("Test", "Перекраска: " + String.valueOf(++iterator1));
                        // сбрасываем группы точек
                        for (Pair p2 : pairs) {
                            if (p2.group == 0) {
                                break;
                            }
                            if (p2.group == currentGroup) {
                                p2.group = p.group;
                            }
                        }
                    }
                }
            }
        }
        return countOfGroup;
    }

    public static Pair find(List<Pair> pairs, int dir) {
        for (Pair pair : pairs) {
            if (pair.index == dir) {
                return pair;
            }
        }
        return null;
    }

}
