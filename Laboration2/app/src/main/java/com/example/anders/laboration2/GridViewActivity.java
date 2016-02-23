package com.example.anders.laboration2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;




/*
Gets all pictures from external content and organizes them as thumbs in a gridview.
Send the uri of the picked picture back to MainActivity as a putextra with the name "result".
Uses a nested adapter class to organize the pictures in a gridview
 */


public class GridViewActivity extends Activity {

    //Define the source of MediaStore.Images.Media, internal or external storage
    final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    final Uri thumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    final String thumb_DATA = MediaStore.Images.Thumbnails.DATA;
    final String thumb_image_ID = MediaStore.Images.Thumbnails.IMAGE_ID;

    MyAdapter mySimpleCursorAdapter;
    GridView myGridView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        myGridView = (GridView) findViewById(R.id.gridview);

        String[] from = {MediaStore.MediaColumns.TITLE};
        int[] to = {android.R.id.text1};

        CursorLoader cursorLoader = new CursorLoader(
                this,
                sourceUri,
                null, null, null,
                MediaStore.Images.Media.TITLE);//Sort by title. Used to be MediaStore.Audio.Media.TITLE

        Cursor cursor = cursorLoader.loadInBackground();

        mySimpleCursorAdapter = new MyAdapter(
                this,
                android.R.layout.simple_list_item_1,
                cursor,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        myGridView.setAdapter(mySimpleCursorAdapter);
        myGridView.setOnItemClickListener(myOnItemClickListener);
    }

    private void sendResult(String path){
        Intent returnIntent = new Intent(this, MainActivity.class);
        returnIntent.putExtra("result", path);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    AdapterView.OnItemClickListener myOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cursor = mySimpleCursorAdapter.getCursor();
            cursor.moveToPosition(position);

            int thColumnIndex = cursor.getColumnIndex(thumb_DATA);
            String thumbPath = cursor.getString(thColumnIndex);

            sendResult(thumbPath);
        }
    };



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

    public class MyAdapter extends SimpleCursorAdapter{

        Cursor myCursor;
        Context myContext;

        public MyAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags){
            super(context, layout, c, from, to, flags);

            myCursor = c;
            myContext = context;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if(row == null){
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row, parent, false);
            }

            ImageView thumbV = (ImageView) row.findViewById(R.id.thumb);

            myCursor.moveToPosition(position);

            int myID = myCursor.getInt(myCursor.getColumnIndex(MediaStore.Images.Media._ID));

            String[] thumbColumns = {thumb_DATA, thumb_image_ID};
            CursorLoader thumbCursorLoader = new CursorLoader(myContext, thumbUri, thumbColumns, thumb_image_ID + "=" + myID, null, null);

            Cursor thumbCursor = thumbCursorLoader.loadInBackground();

            Bitmap myBitmap = null;
            if(thumbCursor.moveToFirst()){
                int thColumnIndex = thumbCursor.getColumnIndex(thumb_DATA);
                String thumbPath = thumbCursor.getString(thColumnIndex);
                myBitmap = BitmapFactory.decodeFile(thumbPath);
                thumbV.setImageBitmap(myBitmap);
            }

            return row;
        }
    }
}
