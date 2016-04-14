package com.ivchen.flyershare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.Firebase;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.ivchen.flyershare.StringData;


public class CreateActivity extends AppCompatActivity   {

    int REQUEST_CAMERA = 0; //needs to be initialized for onClickButtonCamera method to work
    private static int SELECT_FILE = 1; //needs to be initialized for startActivityForResult method to work
    ImageView image;
    EditText title, date, school;
    TextView text, text2;
    Uri selectedImage;
    String x = "";


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Firebase.setAndroidContext(this);






        image = (ImageView) findViewById(R.id.imageView2);
        title = (EditText) findViewById(R.id.editText);
        date = (EditText) findViewById(R.id.editText2);
        school = (EditText) findViewById(R.id.editText3);
        text = (TextView) findViewById(R.id.textView2);
       // text2 = (TextView) findViewById(R.id.textView3);



            Firebase ref = new Firebase("https://flyershare.firebaseio.com/posts");
        final ValueEventListener valueEventListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Flyer post = postSnapshot.getValue(Flyer.class);
                    x = post.getImage();

                    //x = "hey";
                }

                //x = "hey";
                //text.setText(x);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });


       // StringData.x = "hey";
     // text.setText(StringData.x);
       byte[] decodedString = Base64.decode(x, Base64.DEFAULT);
       Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
       image.setImageBitmap(decodedByte);

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


       try{

           String editTitle = title.getText().toString();
           String editDate = date.getText().toString();
           String editSchool = school.getText().toString();
           if(editTitle.trim().length() > 0 && editDate.trim().length() > 0 && editSchool.trim().length() > 0) {
               Bitmap bmp = ((BitmapDrawable) image.getDrawable()).getBitmap();
               ByteArrayOutputStream bYtE = new ByteArrayOutputStream();
               bmp.compress(Bitmap.CompressFormat.JPEG, 100, bYtE);
               bmp.recycle();
               byte[] byteArray = bYtE.toByteArray();
               String imageFile = Base64.encodeToString(byteArray, Base64.DEFAULT);

               Firebase ref = new Firebase("https://flyershare.firebaseio.com");
               Firebase postRef = ref.child("posts");
               Map<String, String> flyer = new HashMap<String, String>();
               flyer.put("image", imageFile);
               flyer.put("date", editDate);
               flyer.put("title", editTitle);
               flyer.put("school", editSchool);
               postRef.push().setValue(flyer);

               Toast.makeText(CreateActivity.this, "Uploading . . .", Toast.LENGTH_SHORT).show();
           }else{
               Toast.makeText(CreateActivity.this, "Make sure all fiels are completed!", Toast.LENGTH_SHORT).show();
           }
       }catch(Exception e){
           Toast.makeText(CreateActivity.this, "Make sure image is selected!", Toast.LENGTH_SHORT).show();

        }







    }

    //this method will handle image chosen from gallery or taken with camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == SELECT_FILE && resultCode == RESULT_OK) {

                selectedImage = data.getData();
                image.setImageURI(selectedImage);

            } else if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK){
                //camera crashes after picture is taken!!
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                image.setImageBitmap(thumbnail);
            }
    }

}
