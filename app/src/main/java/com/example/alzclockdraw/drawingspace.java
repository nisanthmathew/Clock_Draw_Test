/*Created by Nisanth Mathew James for Hochschule Anhalt - 2020*/

package com.example.alzclockdraw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;

public class drawingspace extends AppCompatActivity {
    String imagename; // variable to store patient id
    String patienttime; // variable to store patient id


    Bitmap clockimage;
    Canvas saveimage;
    static int count = 0;
    static boolean allowdrawing = false;
    static Timer drawtimer;
    RelativeLayout viewGroup;
    View drawing_area;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b =getIntent().getExtras(); //extracting the patient id
        if(b != null){
            imagename = b.getString("patientid");
            patienttime = b.getString("patienttime");
        }
        setContentView(R.layout.activity_drawingspace);
        ((TextView) findViewById(R.id.messagetopatient)).setText("Please Draw the time " + "'" + patienttime + "'" + " on an analog clock");

        // Add a new drawing area to existing layout

        if(allowdrawing){
            findViewById(R.id.messagetopatient).setVisibility(View.INVISIBLE);
            viewGroup = findViewById(R.id.my_viewgroup);
            drawing_area = new clockdrawer(this);
            viewGroup.addView(drawing_area);
        }

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

                                       int[] xaxishistogram = Xaxis_histogram(clockimage,xaxis_limits[0],xaxis_limits[1]);
                                       int[] yaxishistogram = Yaxis_histogram(clockimage,yaxis_limits[0],yaxis_limits[1]);

                                       yaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.3)); //histogram y marking
                                       yaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.7)); //histogram x marking

                                       xaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.32)); //histogram y marking
                                       xaxisvalue_marker(saveimage,(int)(width*0.7),(int)(height*0.72)); //histogram x marking

                                       displaytime(saveimage,(int)(width*0.75),(int)(height*0.9));

                                       //image analysis functions
                                       clockshapeuniformity(saveimage, xaxis_limits, yaxis_limits);
                                       clocknumberverticaldistributionchecker(saveimage, xaxishistogram, xaxis_limits[0], yaxis_limits);
                                       clocknumberhorizontaldistributionchecker(saveimage, yaxishistogram, yaxis_limits[0], xaxis_limits);

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



    int[] first_and_last_clockpixel_finder_xaxis( Bitmap inputimage ){ //finsding start and end positions of clock on x axis
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

    int[] first_and_last_clockpixel_finder_yaxis( Bitmap inputimage ){ //finsding start and end positions of clock on y axis
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

    int[] Xaxis_histogram(Bitmap inputimage, int firstpixel_position, int lastpixel_position){ //finding the histogramm of the image along x axis
        /*accesing pixel for histogram with x aixs as reference**/
        int[] histogram_xaxis = new int[(lastpixel_position-firstpixel_position)];
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

    int[] Yaxis_histogram(Bitmap inputimage, int firstpixel_position, int lastpixel_position){ //finding the histogramm of the image along y axis
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

    int max_value(int [] inputarray){ // function to find maximum value in an array
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

    void displaytime(Canvas inputimage, int xaxis_origin,int yaxis_origin){ //function to dispaly the taken taken for the user to finsih drawing on the canvas
        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLUE);
        textpaint.setTextSize(60);
        inputimage.drawText("Time Taken: "+String.valueOf((int)(count)) + " sec",xaxis_origin, yaxis_origin, textpaint);
        count = 0;

    }

    void clockshapeuniformity(Canvas inputimage, int[] xaxispixelpositions, int[] yaxispixelpositions ){ //clock shape roundness checker
        int horizontallength = xaxispixelpositions[1] - xaxispixelpositions[0];
        int verticallength = yaxispixelpositions[1] - yaxispixelpositions[0];
        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLUE);
        textpaint.setTextSize(35);

        if(abs(horizontallength-verticallength) > horizontallength*0.1 || abs(horizontallength-verticallength) > verticallength*0.1){ //checking whether the difference between horizontal diameter and vertical diameter is greater than 10%.

            inputimage.drawText("Clock shape not uniform",inputimage.getWidth()*0.05f, inputimage.getHeight()*0.85f, textpaint);

        }
        else{
            inputimage.drawText("Clock shape is uniform",inputimage.getWidth()*0.05f, inputimage.getHeight()*0.85f, textpaint);
        }
    }

    void clocknumberverticaldistributionchecker(Canvas inputimage, int[] histogram, int xaxis_starting_pixel, int [] yaxis_limits){ //x axis image analysis
        Paint textpaint = new Paint(); //paint for text
        textpaint.setColor(Color.BLUE);
        textpaint.setTextSize(35);

        Paint peakpaint = new Paint(); //paint to draw peak lines
        peakpaint.setColor(Color.RED);
        peakpaint.setStrokeWidth(2);

        Paint segmentpaint = new Paint(); //paint to draw segment start lines
        segmentpaint.setColor(Color.DKGRAY);
        segmentpaint.setStrokeWidth(1);
        segmentpaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));


        int[] tempstorageenvlp = new int[7];
        int iterator = 0;
        /****************************envelope extraction*******************************************/
        while(iterator + 7 < histogram.length) {
            for (int j = 0; j < 7; j++) // taking a window of 7 pixel which is the width of one stroke
            {
                tempstorageenvlp[j] = histogram[j + iterator]; // storing data in a temp array
            }

            int maxvalue = tempstorageenvlp[0];
            for (int i = 1; i < tempstorageenvlp.length; i++) {
                if (tempstorageenvlp[i] > maxvalue) {
                    maxvalue = tempstorageenvlp[i];
                }
            }

            for (int j = 0; j < 7; j++) {
                histogram[j+iterator] = maxvalue; //replacing all values using max value - inorder to remove random spikes and smooth out the envelop
            }
            iterator += 7;
        }
        /**************************finding the average of histogram********************************/
        int averageXaxis = 0;
        for(int i=0; i<histogram.length; i++){
            averageXaxis += histogram[i];

        }
        averageXaxis = (averageXaxis/(histogram.length/3));//dividing by histogram/3 since the data only has 1/3 of the data due to sampling and remaining elements in the array are zeros.


        /*******dividing the clock vertically into 7 segments and look for peaks corrresponding to numbers in each window****/

        int peakcounter = 0;
        int runningcounter = 0; //temporary buffer
        int segmentcounter = 1;
        int segmentsize = (histogram.length/3)/7;
        Log.d("segment size", String.valueOf(segmentsize));

        int segmentstartposition = 0;

        while(segmentcounter <= 7) { //iterating through each segements

            inputimage.drawLine((float) (segmentstartposition*3)+xaxis_starting_pixel, yaxis_limits[0], //indicating segment start positions using lines
                    (float) (segmentstartposition*3)+xaxis_starting_pixel, yaxis_limits[1],
                    segmentpaint);
            for (int i = segmentstartposition; i < segmentsize*segmentcounter; i++) {
                if (histogram[i] > averageXaxis) {
                    runningcounter++;
                    Log.d("segment location", String.valueOf(i));

                }
                else{
                    runningcounter = 0;
                }
                if (runningcounter >= 3) { //checking for atleast 3 closly lying values greater than average value
                    //inputimage.drawLine((float) (i*3)+xaxis_starting_pixel, yaxis_limits[0], (float) (i*3)+xaxis_starting_pixel, yaxis_limits[1], peakpaint);
                    peakcounter++;
                    runningcounter = 0;
                    Log.d("segment position", String.valueOf(segmentstartposition));
                    i =  segmentcounter*segmentsize;


                }
            }
            segmentstartposition += segmentsize; // incrementing the loop start position to the start position of next segment after finding a peak
            segmentcounter++;
        }

        if(peakcounter <= 11 && peakcounter >=7){ //checking wether the peaks detected falls in the tolerence window
            inputimage.drawText("Numbering in x axis is symmetric.",inputimage.getWidth()*0.05f, inputimage.getHeight()*0.90f, textpaint);
        }
        else{
            inputimage.drawText("Numbering in x axis is asymmetric.",inputimage.getWidth()*0.05f, inputimage.getHeight()*0.90f, textpaint);
        }
    }


    void clocknumberhorizontaldistributionchecker(Canvas inputimage, int[] histogram, int yaxis_starting_pixel, int[] xaxis_limits){ //y axis image analysis

        Paint textpaint = new Paint(); //paint for text
        textpaint.setColor(Color.BLUE);
        textpaint.setTextSize(35);

        Paint peakpaint = new Paint(); //paint to draw peak lines
        peakpaint.setColor(Color.RED);
        peakpaint.setStrokeWidth(2);

        Paint segmentpaint = new Paint(); //paint to draw segment start lines
        segmentpaint.setColor(Color.DKGRAY);
        segmentpaint.setStrokeWidth(1);
        segmentpaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));


        int[] tempstorageenvlp = new int[7];
        int iterator = 0;
        /****************************envelope extraction*******************************************/
        while(iterator + 7 < histogram.length) {
            for (int j = 0; j < 7; j++) // taking a window of 7 pixel which is the width of one stroke
            {
                tempstorageenvlp[j] = histogram[j + iterator]; // storing data in a temp array
            }

            int maxvalue = tempstorageenvlp[0];
            for (int i = 1; i < tempstorageenvlp.length; i++) {
                if (tempstorageenvlp[i] > maxvalue) {
                    maxvalue = tempstorageenvlp[i];
                }
            }

            for (int j = 0; j < 7; j++) {
                histogram[j+iterator] = maxvalue; //replacing all values using max value - inorder to remove random spikes and smooth out the envelop
            }
            iterator += 7;
        }
        /**************************finding the average of histogram********************************/
        int averageYaxis = 0;
        for(int i=0; i<histogram.length; i++){
            averageYaxis += histogram[i];

        }
        averageYaxis = (averageYaxis/(histogram.length/3));//dividing by histogram/3 since the data only has 1/3 of the data due to sampling and remaining elements in the array are zeros.


        /*******dividing the clock vertically into 7 segments and look for peaks corrresponding to numbers in each window****/

        int peakcounter = 0;
        int runningcounter = 0; //temporary buffer
        int segmentcounter = 1;
        int segmentsize = (histogram.length/3)/7;
        Log.d("segment size", String.valueOf(segmentsize));

        int segmentstartposition = 0;

        while(segmentcounter <= 7) { //iterating through each segements

            inputimage.drawLine((float)  xaxis_limits[0], (segmentstartposition*3)+yaxis_starting_pixel, //indicating segment start positions using lines
                    xaxis_limits[1],(float) (segmentstartposition*3)+yaxis_starting_pixel,
                    segmentpaint);
            for (int i = segmentstartposition; i < segmentsize*segmentcounter; i++) {
                if (histogram[i] > averageYaxis) {
                    runningcounter++;
                    Log.d("segment location", String.valueOf(i));

                }
                else{
                    runningcounter = 0;
                }
                if (runningcounter >= 3) { //checking for atleast 3 closly lying values greater than average value
                   // inputimage.drawLine(xaxis_limits[0], (float) (i*3)+yaxis_starting_pixel, xaxis_limits[1],(float) (i*3)+yaxis_starting_pixel,  peakpaint);
                    peakcounter++;
                    runningcounter = 0;
                    Log.d("segment position", String.valueOf(segmentstartposition));
                    i =  segmentcounter*segmentsize;


                }
            }
            segmentstartposition += segmentsize; // incrementing the loop start position to the start position of next segment after finding a peak
            segmentcounter++;
        }

        if(peakcounter <= 11 && peakcounter >=7){ //checking whether the peaks detected falls in the tolerence window
            inputimage.drawText("Numbering in y axis is symmetric.",inputimage.getWidth()*0.05f, inputimage.getHeight()*0.95f, textpaint);
        }
        else{
            inputimage.drawText("Numbering in y axis is asymmetric.",inputimage.getWidth()*0.05f, inputimage.getHeight()*0.95f, textpaint);
        }
    }



}
