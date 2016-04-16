package com.ivchen.flyershare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.support.v4.view.GestureDetectorCompat;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

public class MainBoardActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{

    private GridView gv;
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    private int j = 0;
    private GestureDetector detector;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private int[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7
    };
    private ImageView expandedImageView;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String [] mThumbIds2 = new String [mThumbIds.length];
        setContentView(R.layout.activity_main_board);
        Firebase.setAndroidContext(this);
        this.gestureDetector = new GestureDetectorCompat(this,this);
        gestureDetector.setOnDoubleTapListener(this);
        detector = new GestureDetector(this, new SwipeGestureDetector());

        GridView gridview = (GridView) findViewById(R.id.gridview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.listitem,mThumbIds2);
        gridview.setAdapter(adapter);
        registerForContextMenu(gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                j = position;
                zoomImageFromThumb(v, mThumbIds[position]);
                Toast.makeText(MainBoardActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
            mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        String [] mThumbIds2 = new String [mThumbIds.length];
        if(v.getId() == R.id.gridview){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(mThumbIds2[info.position]);
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for(int i = 0; i < menuItems.length; i++){
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String [] mThumbIds2 = new String [mThumbIds.length];
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String [] menuItems = getResources().getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = mThumbIds2[info.position];

        TextView text = (TextView) findViewById(R.id.footer);
        text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
        return true;
    }

    class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater layoutInflater;

        public ImageAdapter(MainBoardActivity activity) {
            layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View listem = convertView;
            int p = position;
            ImageView imageView;
            if (listem == null) {
                listem = layoutInflater.inflate(R.layout.single_grid_image, null);
            }
            ImageView iv = (ImageView) listem.findViewById(R.id.mThumbIds);
            iv.setBackgroundResource(mThumbIds[p]);
            return listem;
        }
    }
        private void zoomImageFromThumb(final View thumbView, int imageResId) {
            if (mCurrentAnimator != null) {
                mCurrentAnimator.cancel();
            }
            expandedImageView = (ImageView) findViewById(R.id.expanded_image);

            expandedImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (detector.onTouchEvent(event)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            expandedImageView.setImageResource(imageResId);
            final Rect startBounds = new Rect();
            final Rect finalBounds = new Rect();
            final Point globalOffset = new Point();

            thumbView.getGlobalVisibleRect(startBounds);
            findViewById(R.id.gridview).getGlobalVisibleRect(finalBounds,globalOffset);

            startBounds.offset(-globalOffset.x, -globalOffset.y);
            finalBounds.offset(-globalOffset.x, -globalOffset.y);

            float startScale;

            if((float)finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()){
                startScale = (float) startBounds.height() / finalBounds.height();
                float startWidth = startScale * finalBounds.width();
                float deltaWidth = (startWidth - startBounds.width()) / 2;
                startBounds.left -= deltaWidth;
                startBounds.right += deltaWidth;
            }else{
                startScale = (float) startBounds.width() / finalBounds.width();
                float startHeight = startScale * finalBounds.height();
                float deltaHeight = (startHeight = startBounds.height()) / 2;
                startBounds.top -= deltaHeight;
                startBounds.bottom += deltaHeight;

            }
            thumbView.setAlpha(0f);
            expandedImageView.setVisibility(View.VISIBLE);

            expandedImageView.setPivotX(0f);
            expandedImageView.setPivotY(0f);

            AnimatorSet set = new AnimatorSet();

            set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left, finalBounds.left))
                    .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                    .with(ObjectAnimator.ofFloat(expandedImageView,View.SCALE_X,startScale, 1f))
                    .with(ObjectAnimator.ofFloat(expandedImageView,View.SCALE_Y,startScale, 1f));

            set.setDuration(mShortAnimationDuration);
            set.setInterpolator(new DecelerateInterpolator());
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation){
                    mCurrentAnimator = null;
                }
            });
            set.start();
            mCurrentAnimator = set;


            final float startScaleFinal = startScale;
            expandedImageView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    if (mCurrentAnimator != null) {
                        mCurrentAnimator.cancel();
                    }
                    Animator set = new AnimatorSet();

                    set.setDuration(mShortAnimationDuration);
                    set.setInterpolator(new DecelerateInterpolator());
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            thumbView.setAlpha(1f);
                            expandedImageView.setVisibility(View.GONE);
                            mCurrentAnimator = null;
                        }
                    });
                    set.start();
                    mCurrentAnimator = set;
                }
            });
        }
        private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener{
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float VelocityY){
                try{
                    if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        if (mThumbIds.length > j) {
                            j++;
                            if (j < mThumbIds.length) {
                                expandedImageView.setImageResource(mThumbIds[j]);
                                return true;
                            } else {
                                j = 0;
                                expandedImageView.setImageResource(mThumbIds[j]);
                                return true;
                            }
                        }
                    }else if(e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
                                if(j > 0){
                                    j--;
                                    expandedImageView.setImageResource(mThumbIds[j]);
                                    return true;
                                }else{
                                    j = mThumbIds.length - 1;
                                    expandedImageView.setImageResource(mThumbIds[j]);
                                    return true;
                                }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                return false;
                }
}


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void onClickFavorite(View view) {
        Intent intent = new Intent(MainBoardActivity.this, FavoritesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public void onClickTrash(View view) {
        Intent intent = new Intent(MainBoardActivity.this, TrashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onClickLogOut(View view) {

        Toast.makeText(MainBoardActivity.this, "Logging out . . .", Toast.LENGTH_SHORT).show();

        //log-out code
        Firebase ref = new Firebase("https://flyershare.firebaseio.com");
        ref.unauth();

        //intent for page navigation
        Intent intent = new Intent(MainBoardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onClickCreate(View view) {
        Intent intent = new Intent(MainBoardActivity.this, CreateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onClickHelp(View view) {
        Toast.makeText(MainBoardActivity.this, "Tip: Long-click to favorite a flyer! Click to enlarge flyer which will provide options to favorite or delete as well!", Toast.LENGTH_LONG).show();
    }
}
