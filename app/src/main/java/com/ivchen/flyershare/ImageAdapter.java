package com.ivchen.flyershare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.ivchen.flyershare.MainBoardActivity;


/**
 * Created by Owner on 4/9/2016.
 */
public class ImageAdapter extends BaseAdapter {


    public ImageView imageView;

    public String[] images;


    public Bitmap[] imagesDecoded;

    public String holdsImage = "";

    public int i = 0;



    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }




        //setting up array for images extracted from database
        Firebase ref = new Firebase("https://flyershare.firebaseio.com/posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long flyerCount = snapshot.getChildrenCount();
                int flyerCountInt = (int) flyerCount;
                imagesDecoded = new Bitmap[flyerCountInt];
                //images = new String[flyerCountInt];
                //int j = 0;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Flyer post = postSnapshot.getValue(Flyer.class);
                    holdsImage = post.getTitle();
                    //turnImageStringToImages(holdsImage);
                    byte[] decodedString = Base64.decode(holdsImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imagesDecoded[i] = decodedByte;
                    i++;
                    //j++;


                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });




        imageView.setImageBitmap(imagesDecoded[position]);
        return imageView;
    }







    private void turnImageStringToImages(String s){

            byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imagesDecoded[i] = decodedByte;
            i++;

    }





    // references to our images
    private Bitmap[] mThumbIds = {





            /*R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7*/
    };
}

