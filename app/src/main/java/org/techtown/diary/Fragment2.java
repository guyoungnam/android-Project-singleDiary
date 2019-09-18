package org.techtown.diary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.channguyen.rsv.RangeSliderView;

import java.io.File;
import java.util.Date;

import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {

    private static final String TAG = "Fragment2";

    Context context;
    OnTabItemSelectedListener listener;
    OnRequestListener requestListener;

    ImageView weatherIcon;
    TextView dateTextView;
    TextView locationTextView;

    EditText contentsInput;
    ImageView pictureImageView;

    boolean isPhotoCapture;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    File file;
    Bitmap resultPhotoBitmap;


    // 무슨 작업이지??
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;

        if (context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener) context;
        }

        if (context instanceof OnRequestListener) {
            requestListener = (OnRequestListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (context != null) {
            context = null;
            listener = null;
            requestListener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        //check current location

        if (requestListener != null) {
            requestListener.onRequest("getCurrentLocation");
        }

        return rootView;
    }

    private void initUI(ViewGroup rootView) {
        
        weatherIcon =rootView.findViewById(R.id.weatherIcon);
        dateTextView = rootView.findViewById(R.id.dateTextView);
        locationTextView = rootView.findViewById(R.id.locationTextView);
        
        contentsInput = rootView.findViewById(R.id.contentsInput);
        pictureImageView = rootView.findViewById(R.id.pictureExistsImageView);
        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCanceled || isPhotoFileSaved){
                    showDialog(AppConstants.CONTENT_PHOTO_EX);
                }else{
                    showDialog(AppConstants.CONTENT_PHOTO);
                }
            }
        });


        
        Button saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTableSelected(0);
                }
            }
        });

        Button deleteButton = rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTableSelected(0);
                }
            }
        });

        Button closeButton = rootView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onTableSelected(0);
                }
            }

        });

        RangeSliderView silderView = rootView.findViewById(R.id.sliderView);
        silderView.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                Toast.makeText(context, "moodIndex changed to" + index, Toast.LENGTH_SHORT).show();
            }
        });

        silderView.setInitialIndex(2);


    }

    public void setWeather(String data) {
        if (data != null) {
            if (data.equals("맑음")) {
                weatherIcon.setImageResource(R.drawable.weather_1);
            } else if (data.equals("구름 조금")) {
                weatherIcon.setImageResource(R.drawable.weather_2);
            } else if (data.equals("구름 많음")) {
                weatherIcon.setImageResource(R.drawable.weather_3);

            } else if (data.equals("흐림")) {
                weatherIcon.setImageResource(R.drawable.weather_4);
            } else if (data.equals("비")) {
                weatherIcon.setImageResource(R.drawable.weather_5);
            } else if (data.equals("눈/비")) {
                weatherIcon.setImageResource(R.drawable.weather_6);
            } else if (data.equals("눈")) {
                weatherIcon.setImageResource(R.drawable.weather_7);
            } else {
                Log.d("Fragment2", "Unknown weather string:" + data);
            }
        }

    }

    public void setAddress(String data) {
        locationTextView.setText(data);
    }

    public void setDateString(String dateString) {
        dateTextView.setText(dateString);
    }

    public void showPhotoCaptureActivity(){
        if (file == null){
            file = createFile();
        }
    }

    public void showDialog(int id){
        AlertDialog.Builder builder = null;

        switch (id){

            case AppConstants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);
        }
    }


    private File createFile() {

        String filename = "capture.jsp";
        File storageDir = Environment.getExternalStorageDirectory();
        File outFile = new File(storageDir, filename);

        return outFile;
    }

    public void showPhotoSelectionActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }

    //public void showPhotoSelectionActivity(){}



    public static Bitmap decodeSampleBitmapFromResource(File res, int reqWidth, int reqHeight){

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(),options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(),options);



    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){

       // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height;
            final int halfWidth = width;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;



    }

    private String createFilename(){

        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;

    }
}



