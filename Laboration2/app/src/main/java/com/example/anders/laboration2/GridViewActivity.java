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

//Exempel från http://android-er.blogspot.se/2012/11/list-mediastoreimagesthumbnails-in.html


/*
Hämtar samtliga bilder från external content och organiserar i en gridview.
Skickar tillbaka bildens contactPhotoUri till mainactivity som putextra med namnet "result".
Har en inbyggd adapter-klass för att hantera bilderna i gridview.
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

        myGridView = (GridView) findViewById(R.id.gridview); //gridview ar deklarerad i activity_main-layout vi har.
        System.out.println("Inne i GridViewActivity");
        String[] from = {MediaStore.MediaColumns.TITLE};//För att hämta titlar sen.
        int[] to = {android.R.id.text1};//Gridviewen kommer bestå av textviews av denna typen

        //Ladda alla filer från external content. Tror "media.EXTERNAL_STORAGE_URI" innebär det.
        CursorLoader cursorLoader = new CursorLoader(
                this,
                sourceUri, //Pathen till bilderna på external content. Eller typ alla filer kanske. Media!
                null, null, null,
                MediaStore.Audio.Media.TITLE);//Sortera efter titel.

        Cursor cursor = cursorLoader.loadInBackground();//Ladda in external content i cursorn.

        //Skapa en adapter som populerar gridviewen med bilder från cursorn
        mySimpleCursorAdapter = new MyAdapter(
                this,
                android.R.layout.simple_list_item_1, //Layouten för griden
                cursor,//Cursorn som håller datat vi vill visa i griden
                from,//Vi hämtar titlarna från kolumner i tabellen
                to,//Varje titel hamnar i en egen TextView som bygger upp griden
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);//Aviserar om datat från cursorn ändras. Ska ej behövas tror jag.

        myGridView.setAdapter(mySimpleCursorAdapter);
        myGridView.setOnItemClickListener(myOnItemClickListener);
    }

    private void sendResult(String path){
        //Cursor cursor = mySimpleCursorAdapter.getCursor();
       // cursor.moveToPosition(position);//Få cursorn att peka på den bild användaren tryckt på

        //int int_ID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));//Hämta dess ID

        Intent returnIntent = new Intent(this, MainActivity.class);
        returnIntent.putExtra("result", path);//ID:et ä fel. Det är Urin som ska skickas.
        //returnIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    //Hantera klick på textviews i griden
    //Denna ska skicka tillbaka uri:n till första aktiviteten så bilden kan visas i fullskärm.
    AdapterView.OnItemClickListener myOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            System.out.println("Nu klickade du på en bild.");
            //Fånga uri:n till bilden och skicka tillbaka den till första aktiviteten som ett meddelande i en intent med koden 1.

            Cursor cursor = mySimpleCursorAdapter.getCursor();
            cursor.moveToPosition(position);//Få cursorn att peka på den bild användaren tryckt på

            int thColumnIndex = cursor.getColumnIndex(thumb_DATA);//Gör om detta så att vi får bilden!
            String thumbPath = cursor.getString(thColumnIndex);//Get the contactPhotoUri to the thumbnails data
            System.out.println("Pathen till bilden är: "+ thumbPath);

            //int int_ID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));//Sätt att den ska kolla i ID-kolumnen tror jag. Ej: Hämta dess ID. Var? I cursorn eller på external content?
            sendResult(thumbPath);


            //Gamla koden kallar en metod som visar thumbnailen och ett toastmeddelande.
            //Använd en liknande kod för att visa fullscreen i första aktiviteten.
            //getThumbnail(int_ID);//Hämta dess thumbnail
        }
    };

    //Hämta thumbnailen till den titel användaren tryckt på
    private Bitmap getThumbnail(int id){
/*
        final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final Uri thumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        final String thumb_DATA = MediaStore.Images.Thumbnails.DATA;
        final String thumb_image_ID = MediaStore.Images.Thumbnails.IMAGE_ID;
*/
        String[] thumbColumns = {thumb_DATA, thumb_image_ID};

        CursorLoader thumbCursorLoader = new CursorLoader(
                this,
                thumbUri,//Pathen till thumbnails på external content
                thumbColumns,
                thumb_image_ID + "=" + id,
                null, null);

        Cursor thumbCursor = thumbCursorLoader.loadInBackground();//Fecth the thumbnails to the thumb cursor

        Bitmap thumbBitmap = null;
        if(thumbCursor.moveToFirst()){
            int thColumnIndex = thumbCursor.getColumnIndex(thumb_DATA);
            String thumbPath = thumbCursor.getString(thColumnIndex);//Get the contactPhotoUri to the thumbnails data
            Toast.makeText(getApplicationContext(), thumbPath, Toast.LENGTH_LONG).show();//En toast som visar thumbpathen

            thumbBitmap = BitmapFactory.decodeFile(thumbPath);//BitmapFactory takes the thumbs contactPhotoUri and decode it to an image

            //A dialogue to display the thumbnail
            AlertDialog.Builder thumbDialog = new AlertDialog.Builder(GridViewActivity.this);
            ImageView thumbView = new ImageView(GridViewActivity.this);
            thumbView.setImageBitmap(thumbBitmap);
            LinearLayout layout = new LinearLayout(GridViewActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(thumbView);
            thumbDialog.setView(layout);
            thumbDialog.show();
        }
        else{
            Toast.makeText(getApplicationContext(), "No thumbnail!", Toast.LENGTH_LONG).show();
        }


        return thumbBitmap;
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


            //Kolla in detta så du fattar vad som händer
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
