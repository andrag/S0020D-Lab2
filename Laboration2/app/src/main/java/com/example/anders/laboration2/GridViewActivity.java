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

//Exempel fr�n http://android-er.blogspot.se/2012/11/list-mediastoreimagesthumbnails-in.html


/*
H�mtar samtliga bilder fr�n external content och organiserar i en gridview.
Skickar tillbaka bildens contactPhotoUri till mainactivity som putextra med namnet "result".
Har en inbyggd adapter-klass f�r att hantera bilderna i gridview.
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
        String[] from = {MediaStore.MediaColumns.TITLE};//F�r att h�mta titlar sen.
        int[] to = {android.R.id.text1};//Gridviewen kommer best� av textviews av denna typen

        //Ladda alla filer fr�n external content. Tror "media.EXTERNAL_STORAGE_URI" inneb�r det.
        CursorLoader cursorLoader = new CursorLoader(
                this,
                sourceUri, //Pathen till bilderna p� external content. Eller typ alla filer kanske. Media!
                null, null, null,
                MediaStore.Audio.Media.TITLE);//Sortera efter titel.

        Cursor cursor = cursorLoader.loadInBackground();//Ladda in external content i cursorn.

        //Skapa en adapter som populerar gridviewen med bilder fr�n cursorn
        mySimpleCursorAdapter = new MyAdapter(
                this,
                android.R.layout.simple_list_item_1, //Layouten f�r griden
                cursor,//Cursorn som h�ller datat vi vill visa i griden
                from,//Vi h�mtar titlarna fr�n kolumner i tabellen
                to,//Varje titel hamnar i en egen TextView som bygger upp griden
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);//Aviserar om datat fr�n cursorn �ndras. Ska ej beh�vas tror jag.

        myGridView.setAdapter(mySimpleCursorAdapter);
        myGridView.setOnItemClickListener(myOnItemClickListener);
    }

    private void sendResult(String path){
        //Cursor cursor = mySimpleCursorAdapter.getCursor();
       // cursor.moveToPosition(position);//F� cursorn att peka p� den bild anv�ndaren tryckt p�

        //int int_ID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));//H�mta dess ID

        Intent returnIntent = new Intent(this, MainActivity.class);
        returnIntent.putExtra("result", path);//ID:et � fel. Det �r Urin som ska skickas.
        //returnIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    //Hantera klick p� textviews i griden
    //Denna ska skicka tillbaka uri:n till f�rsta aktiviteten s� bilden kan visas i fullsk�rm.
    AdapterView.OnItemClickListener myOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            System.out.println("Nu klickade du p� en bild.");
            //F�nga uri:n till bilden och skicka tillbaka den till f�rsta aktiviteten som ett meddelande i en intent med koden 1.

            Cursor cursor = mySimpleCursorAdapter.getCursor();
            cursor.moveToPosition(position);//F� cursorn att peka p� den bild anv�ndaren tryckt p�

            int thColumnIndex = cursor.getColumnIndex(thumb_DATA);//G�r om detta s� att vi f�r bilden!
            String thumbPath = cursor.getString(thColumnIndex);//Get the contactPhotoUri to the thumbnails data
            System.out.println("Pathen till bilden �r: "+ thumbPath);

            //int int_ID = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));//S�tt att den ska kolla i ID-kolumnen tror jag. Ej: H�mta dess ID. Var? I cursorn eller p� external content?
            sendResult(thumbPath);


            //Gamla koden kallar en metod som visar thumbnailen och ett toastmeddelande.
            //Anv�nd en liknande kod f�r att visa fullscreen i f�rsta aktiviteten.
            //getThumbnail(int_ID);//H�mta dess thumbnail
        }
    };

    //H�mta thumbnailen till den titel anv�ndaren tryckt p�
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
                thumbUri,//Pathen till thumbnails p� external content
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


            //Kolla in detta s� du fattar vad som h�nder
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
