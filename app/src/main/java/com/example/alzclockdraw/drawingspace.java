/*Created by Nisanth Mathew James for Hochschule Anhalt - 2020*/

package com.example.alzclockdraw;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static java.lang.Math.abs;

public class drawingspace extends AppCompatActivity {
    String imagename; // variable to store patient id
    Bitmap clockimage;
    Canvas saveimage;
    static int count = 0;
    static boolean allowdrawing = false;
    static Timer drawtimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b =getIntent().getExtras(); //extracting the patient id
        if(b != null){
            imagename = b.getString("patientid");
        }
        setContentView(R.layout.activity_drawingspace);
        final RelativeLayout viewGroup = findViewById(R.id.my_viewgroup);
        final View drawing_area = new clockdrawer(this);
        // Add a new drawing area to existing layout
        if(allowdrawing){
        viewGroup.addView(drawing_area);}

        /********************Create new drawing space*****************************/
        final FloatingActionButton addimage = findViewById(R.id.newimage);
        addimage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            allowdrawing = true;
                                            recreate();
                                            if(drawtimer == null){
                                                drawtimer = new Timer();
                                                 drawtimer.scheduleAtFixedRate(new TimerTask(){
                                                @Override
                                                public void run(){
                                                    count++;
                                                }
                                            },1000,1000);
                                        }
                                        }
                                    }
        );


        /***************Save patient information**************************************/
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawtimer != null){
                drawtimer.cancel();
                drawtimer = null;
                }

                allowdrawing = false;
                clockimage = Bitmap.createBitmap(viewGroup.getWidth(), viewGroup.getHeight(), Bitmap.Config.ARGB_8888);
                saveimage = new Canvas(clockimage);
                viewGroup.draw(saveimage);
                int height = clockimage.getHeight();
                int width = clockimage.getWidth();

                int [] xaxis_limits = first_and_last_clockpixel_finder_xaxis(clockimage);

                int [] yaxis_limits = first_and_last_clockpixel_finder_yaxis(clockimage);

                Xaxis_histogram(clockimage,xaxis_limits[0],xaxis_limits[1]);
                Yaxis_histogram(clockimage,yaxis_limits[0],yaxis_limits[1]);

                yaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.3)); //histogram y marking
                yaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.7)); //histogram x marking

                xaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.32)); //histogram y marking
                xaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.72)); //histogram x marking

                displaytime(saveimage,(int)(width*0.75),(int)(height*0.9));

                for(int imagecolumn = 0; imagecolumn < width*0.2; imagecolumn++){ //removing the floating buttons
                    for(int imagerow= 0; imagerow < (height*0.2) ; imagerow++){
                            clockimage.setPixel(imagecolumn,imagerow,Color.WHITE); //removing buttons
                    }
                }

                /*saving data*/
                File myDir = new File(Environment.getExternalStorageDirectory(), "/PatientData/");
                if(!myDir.exists()){
                    if(myDir.mkdirs()) {
                        Log.d("path", String.valueOf(myDir));
                    }
                    else
                        Log.d("path not created", String.valueOf(myDir));
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fname = imagename+"_"+ timeStamp +".jpg";

                File file = new File(myDir, fname);
                if (file.exists()) file.delete ();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    clockimage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                viewGroup.removeView(drawing_area);
                viewGroup.setBackground(new BitmapDrawable(getResources(),clockimage));
            }
            }
        );

    }



int[] first_and_last_clockpixel_finder_xaxis( Bitmap inputimage ){
    int[] xaxis_pixels = new int[2];
    int height = inputimage.getHeight();
    int width = inputimage.getWidth();
    boolean firstpixel_flag = false;

    for(int imagecolumn= 0; imagecolumn<(width*0.69); imagecolumn++){ //iterating through the image
        for(int imagerow= 0; imagerow<height; imagerow++){
            int pixelvalues = inputimage.getPixel(imagecolumn,imagerow);
            if(pixelvalues == Color.BLACK && !firstpixel_flag){
                //when detecting the first pixel
                xaxis_pixels[0] = imagecolumn; // store column id
                firstpixel_flag = true; //detected
            }
            else if(pixelvalues == Color.BLACK && firstpixel_flag){
                xaxis_pixels[1] = imagecolumn;
            }
        }
    }
    return xaxis_pixels;
}

    int[] first_and_last_clockpixel_finder_yaxis( Bitmap inputimage ){
        int[] yaxis_pixels = new int[2];
        int height = inputimage.getHeight();
        int width = inputimage.getWidth();
        boolean firstpixel_flag = false;

        for(int imagerow= 0; imagerow<height; imagerow++){
            for(int imagecolumn= 0; imagecolumn<(width*0.69); imagecolumn++){ //iterating through the image
                int pixelvalues = inputimage.getPixel(imagecolumn,imagerow);
                if(pixelvalues == Color.BLACK && !firstpixel_flag){
                    //when detecting the first pixel
                    yaxis_pixels[0] = imagerow; // store column id
                    firstpixel_flag = true; //detected
                }
                else if(pixelvalues == Color.BLACK && firstpixel_flag){
                    yaxis_pixels[1] = imagerow;
                }
            }
        }
        return yaxis_pixels;
    }

    int[] Xaxis_histogram(Bitmap inputimage, int firstpixel_position, int lastpixel_position){
        /*accesing pixel for histogram with x aixs as reference**/
        int[] histogram_xaxis = new int[lastpixel_position-firstpixel_position];
        int xaxis_sampling_factor = 3;
        int xaxis_sample_number = 0;
        for(int imagecolumn= firstpixel_position; imagecolumn<lastpixel_position; imagecolumn=imagecolumn+xaxis_sampling_factor){
            histogram_xaxis[xaxis_sample_number]=0; //initializing count
            for(int imagerow= 0; imagerow<inputimage.getHeight(); imagerow++){
                int pixelvalues = clockimage.getPixel(imagecolumn,imagerow);
                if(pixelvalues == Color.BLACK) {
                    //The pixel is black
                    histogram_xaxis[xaxis_sample_number]++; //incrementing count
                }
            }
            xaxis_sample_number++;
        }

        //drawing histogram x axis
        int xaxis_pixel_itr = 0;
        for(int imagecolumn = (int) ((inputimage.getWidth()*0.7)+3); imagecolumn< (xaxis_sample_number+((inputimage.getWidth()*0.7)+3)); imagecolumn++){
            for(int itr = (int) ((inputimage.getHeight()*0.3)+3); itr <= (histogram_xaxis[xaxis_pixel_itr] + (int)  ((inputimage.getHeight()*0.3)+3)) ;itr++) {
                clockimage.setPixel(imagecolumn,abs(itr-inputimage.getHeight()),Color.GREEN);
            }
            xaxis_pixel_itr++;
        }

    return histogram_xaxis;
    }

    int[] Yaxis_histogram(Bitmap inputimage, int firstpixel_position, int lastpixel_position){
        int[] histogram_yaxis = new int[lastpixel_position-firstpixel_position];
        int yaxis_sampling_factor = 3;
        int yaxis_sample_number = 0;
        for(int imagerow= firstpixel_position; imagerow<lastpixel_position; imagerow=imagerow+yaxis_sampling_factor){
            histogram_yaxis[yaxis_sample_number]=0; //initializing count
            for(int imagecolumn= 0; imagecolumn<inputimage.getHeight()*0.69; imagecolumn++){
                int pixelvalues = clockimage.getPixel(imagecolumn, imagerow);
                if(pixelvalues == Color.BLACK) {
                    //The pixel is black
                    histogram_yaxis[yaxis_sample_number]++; //incrementing count
                }
            }
            yaxis_sample_number++;
        }
        //drawing histogram y axis
        int yaxis_pixel_itr = 0;
        for(int imagecolumn = (int) ((inputimage.getWidth()*0.7)+3); imagecolumn< (yaxis_sample_number+((inputimage.getWidth()*0.7)+3)); imagecolumn++){
            for(int itr = (int)  ((inputimage.getHeight()*0.7)+3); itr <= (histogram_yaxis[yaxis_pixel_itr] + (int) ((inputimage.getHeight()*0.7)+3)) ;itr++) {
                clockimage.setPixel(imagecolumn,abs(itr-inputimage.getHeight()),Color.BLUE);
            }
            yaxis_pixel_itr++;
        }

        return histogram_yaxis;
    }

    int max_value(int [] inputarray){
        int maxvalue=inputarray[0];
        for(int i=1; i<inputarray.length; i++){
            if(inputarray[i]>maxvalue){
                maxvalue = inputarray[i];
            }
        }
        return maxvalue;
    }



    void yaxisvalue_marker(Canvas inputimage, int xaxis_origin,int yaxis_origin){ //marks labels incrementing at 100 pixels per grid line
        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(30);
        float y;
        for (int i = 0; i < 5; i++) {
            y = yaxis_origin - i * 100;
            inputimage.drawText(String.valueOf((int)(i * 100)),(float) (xaxis_origin), y, textpaint);
        }
    }

    void xaxisvalue_marker(Canvas inputimage, int xaxis_origin,int yaxis_origin){ //marks labels incrementing at 100 pixels per grid line
        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(30);
        float xlabel;
        for (int i = 0; i < 8; i++) {
            xlabel = xaxis_origin + i * 100;
            inputimage.drawText(String.valueOf((int)(i * 300)),xlabel, yaxis_origin, textpaint);
        }
    }

    void displaytime(Canvas inputimage, int xaxis_origin,int yaxis_origin){
        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLUE);
        textpaint.setTextSize(60);
        inputimage.drawText("Time Taken: "+String.valueOf((int)(count)) + " sec",xaxis_origin, yaxis_origin, textpaint);
        count = 0;

    }


}
