package org.techtown.diary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Date;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {

    private static final String TAG = "Fragment2";

    Context context;
    OnTabItemSelectedListener listener;
    OnRequestListener requestListener;

    ImageView weatherIcon;
    TextView dateTextView;
    TextView locationTextView; //현재 위치

    EditText contentsInput; //메모글 입력
    ImageView pictureImageView;

    boolean isPhotoCapture;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    File file;
    Bitmap resultPhotoBitmap;

    int mMode = AppConstants.MODE_INSERT;
    int _id = -1;
    int weatherIndex = 0;

    RangeSliderView moodSlider;
    int moodIndex = 2;

    Note item;


    // 플래그먼트 onAttach 상태
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

    // 플래그먼트 onDetch 상태
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

        Toast.makeText(context, "onCreateView 호출",Toast.LENGTH_LONG).show();
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        //check current location

        if (requestListener != null) {
            requestListener.onRequest("getCurrentLocation");
        }

        //applyItem();

        return rootView;
    }



    private void initUI(ViewGroup rootView) {

        weatherIcon =rootView.findViewById(R.id.weatherIcon);
        dateTextView = rootView.findViewById(R.id.dateTextView);
        locationTextView = rootView.findViewById(R.id.locationTextView);

        contentsInput = rootView.findViewById(R.id.contentsInput);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);
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


// 저장 버튼
        Button saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //구분 , 새로 만드는지 / 수정하는지

                if(mMode == AppConstants.MODE_INSERT) {
                    saveNote();
                }else if(mMode == AppConstants.MODE_MODIFY){
                    modifyNote();
                }
                if (listener != null) {
                    listener.onTableSelected(0);
                }
            }
        });

        //삭제 버튼
        Button deleteButton = rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteNote();
                if (listener != null) {
                    listener.onTableSelected(0);
                }
            }
        });

        // 닫기 버튼
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


    //메모 저장

    private void saveNote() {

        String address = locationTextView.getText().toString();
        String contents = contentsInput.getText().toString();

        String picturePath = savePicture();

        String sql = "insert into " + NoteDatabase.TABLE_NOTE +
                "(WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE) values(" +
                "'"+ weatherIndex + "', " +
                "'"+ address + "', " +
                "'"+ "" + "', " +
                "'"+ "" + "', " +
                "'"+ contents + "', " +
                "'"+ moodIndex + "', " +
                "'"+ picturePath + "')";

        Log.d(TAG, "sql : " + sql);
        NoteDatabase database = NoteDatabase.getInstance(context);
        database.execSQL(sql);
    }

    private void modifyNote() {

        if(item != null){
            String address = locationTextView.getText().toString();
            String contents = contentsInput.getText().toString();

            String picturePath = savePicture();

            // 업데이트
            String sql = "update " + NoteDatabase.TABLE_NOTE +
                    " set " +
                    "   WEATHER = '" + weatherIndex + "'" +
                    "   ,ADDRESS = '" + address + "'" +
                    "   ,LOCATION_X = '" + "" + "'" +
                    "   ,LOCATION_Y = '" + "" + "'" +
                    "   ,CONTENTS = '" + contents + "'" +
                    "   ,MOOD = '" + moodIndex + "'" +
                    "   ,PICTURE = '" + picturePath + "'" +
                    " where " +
                    "   _id = " + item._id;

            Log.d(TAG, "sql :"+ sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }


    //사진 저장 하기

    private String savePicture() {

        if (resultPhotoBitmap == null){
            AppConstants.println("No piture to be saved"); //사진없을 때 뜨는 메시지
            return   "";
        }

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);

        if(!photoFolder.isDirectory()){
            Log.d(TAG, "create photo folder:" +photoFolder);
            photoFolder.mkdir();
        }
        String photoFilename = createFilename();
        String picturePath = photoFolder + File.separator +photoFilename;

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(picturePath);
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    return picturePath;
    }

    private void deleteNote(){
        AppConstants.println("deleteNote called");

        if(item !=null){
            String sql = "delete from" + NoteDatabase.TABLE_NOTE +
                    "where" + "_id = " +item._id;

            Log.d(TAG, "sql:" + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }




    // 기상청의 현재 날씨 문자열을 받아 아이콘을 설정하는 역할

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

    //주소 문자열을 받아 텍스트뷰에 보여주는 역할

    public void setAddress(String data) {
        locationTextView.setText(data);
        Log.i("myTag",data);
    }

    //현재 일자
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

            //case 1
            case AppConstants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        selectedPhotoMenu = whichButton;

                    }
                });
                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (selectedPhotoMenu == 0) {
                            showPhotoCaptureActivity();
                        } else if (selectedPhotoMenu == 1) {
                            showPhotoCaptureActivity();
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                break;

                //case 2

                case AppConstants.CONTENT_PHOTO_EX:
                    builder = new AlertDialog.Builder(context);

                    builder.setTitle("사진 메뉴 선택");
                    builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            selectedPhotoMenu = whichButton;
                        }
                    });

                    builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(selectedPhotoMenu == 0){
                                showPhotoCaptureActivity();
                            }else if(selectedPhotoMenu ==1){
                                showPhotoCaptureActivity();
                            }else if(selectedPhotoMenu ==2){
                                isPhotoCanceled =true;
                                isPhotoCapture = false;

                                pictureImageView.setImageResource(R.drawable.picture_128);
                            }
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });
                    break;

                    default:
                        break;
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showPhotoCatureActivity(){
        if (file == null){
            file = createFile();
        }

        Uri fileUri = FileProvider.getUriForFile(context,"org.techown.diary.fileprovider",file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        if(intent.resolveActivity(context.getPackageManager()) != null){
            startActivityForResult(intent, AppConstants.REQ_PHOTO_CAPTURE);
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode,intent);

        if(intent !=null){
            switch (requestCode){
                case AppConstants.REQ_PHOTO_CAPTURE:
                    Log.i(TAG, "onActivityResult() for REQ_PHOTO_CATURE");
                    Log.i(TAG, "resultCode:" + resultCode);

                    resultPhotoBitmap = decodeSampleBitmapFromResource(file, pictureImageView.getWidth(),
                            pictureImageView.getHeight());
                    pictureImageView.setImageBitmap(resultPhotoBitmap);

                    break;

                    case AppConstants.REQ_PHOTO_SELECTION:
                        Log.i(TAG,"onActivityResult( ) for REQ_PHOTO_SELECTION");

                        Uri selectedImage = intent.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                        Cursor cursor = context.getContentResolver().query(selectedImage,filePathColumn,null,null,null);
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        cursor.close();

                        resultPhotoBitmap = decodeSampleBitmapFromResource(new File(filePath),pictureImageView.getWidth(),
                                pictureImageView.getHeight());
                        pictureImageView.setImageBitmap(resultPhotoBitmap);
                        isPhotoCapture = true;

                        break;

            }
        }
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



