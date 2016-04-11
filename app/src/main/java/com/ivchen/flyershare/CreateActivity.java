package com.ivchen.flyershare;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateActivity extends AppCompatActivity {
    int REQUEST_CAMERA = 0; //needs to be initialized for onClickButtonCamera method to work
    private static int SELECT_FILE = 1; //needs to be initialized for startActivityForResult method to work
    ImageView image;

    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Firebase.setAndroidContext(this);
        image = (ImageView) findViewById(R.id.imageView2);

    }

    public void onClickHome(View view) {
        Intent intent = new Intent(CreateActivity.this, MainBoardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    public void onClickbuttonCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    public void onClickbuttonPhotoLibrary(View view) {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(
                Intent.createChooser(intent, "Choose File"),
                SELECT_FILE);
    }


    public void onClickbuttonUpload(View view) {


            Bitmap bmp = ((BitmapDrawable) image.getDrawable()).getBitmap();
            ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bYtE);
            bmp.recycle();
            byte[] byteArray = bYtE.toByteArray();
            String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Firebase ref = new Firebase("https://flyershare.firebaseio.com");
            ref.child("flyershare").setValue(imageFile);
            Toast.makeText(CreateActivity.this, "Uploading . . .", Toast.LENGTH_SHORT).show();


    }

    //this method will handle image chosen from gallery or taken with camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {

                selectedImage = data.getData();
                image.setImageURI(selectedImage);

            } else if(requestCode == REQUEST_CAMERA){
                //camera crashes after picture is taken!!
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.setImageBitmap(thumbnail);
            }
    }

}
