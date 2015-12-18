package com.kilianbatzner.identicons;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Handler handler = new Handler();
    Bitmap original;

    int[] foregroundColors = new int[]{0xfff14242, 0xff57c867, 0xff5379e5, 0xff9753e5, 0xff3e3e3e};
    int backgroundColor = 0xffDDDDDD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageview);
        original = BitmapFactory.decodeResource(getResources(), R.drawable.kaleidoscope_source);
        generateProfilePicture(0);
    }

    private void generateProfilePicture(final int foregroundIndex) {
        final int foregroundColor = foregroundColors[foregroundIndex];
        Bitmap bitmap = IdenticonFactory.createIdenticon(original, foregroundColor, backgroundColor);
        imageView.setImageBitmap(bitmap);
        handler.postDelayed(new Runnable() {
            public void run() {
                generateProfilePicture((foregroundIndex+1) % foregroundColors.length);
            }
        }, 500);
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
}
