package com.example.anders.laboration2;



import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;





public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Button galleryButton;
    Button tagButton;
    Button showTagsButton;

    static final int PICK_THUMBNAIL_REQUEST = 1;
    private static final int PICK_CONTACT_REQUEST = 2;

    //For fullscreen image
    PropImageView fullscreenImg;
    Bitmap bitmap;
    byte[] photoByteArray;

    //Data structures and storage
    private String currentPhotoUri;//Uri to the photo currently selected
    private String filename = "contact_tags.txt";// For storing tags
    private String contactsTagString;//A string representation of contactsTags for storing to file - "uri:name,ID,name,ID:uri:name,ID" etc.
    //For storing photo uri:s and link them to the names and id:s of the contacts tagged in them
    //Key: Photo uri - Value: "name,ID,name,ID" etc
    private ConcurrentSkipListMap<String, String> contactsTags;
    private HashMap<String, ContactObject> contactObjectsTaggedInCurrentPhoto;

    private ContactLoaderClass contactLoader;

    //Tags list
    private ListView listview;
    private List<String> taggedInThisPhoto; //This is a list for storing names tagged in the selected photo
    private ArrayAdapter<String> arrayAdapter;

    private ArrayAdapter<String> detailsAdapter;
    private AlertDialog.Builder dialogBuilder;

    private final String splitString = "splitstring";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fullscreenImg = (PropImageView) findViewById(R.id.fullscreen_view);
        galleryButton = (Button) findViewById(R.id.gallery_button);
        tagButton = (Button) findViewById(R.id.tag_button);
        showTagsButton = (Button) findViewById(R.id.showtags_button);

        contactsTags = new ConcurrentSkipListMap<String, String>();
        taggedInThisPhoto = new ArrayList<>();
        listview = (ListView) findViewById(R.id.tags_list);

        detailsAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);

        contactLoader = new ContactLoaderClass(this);
        contactObjectsTaggedInCurrentPhoto = new HashMap<>();

        loadFile();
        createListView();
        createDetailsList();
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
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {}
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}



    //This button starts an activity that sends us to the GridViewActivity and waits for a result
    public void clickGalleryButton(View view){
        //Start the gridView activity
        contactObjectsTaggedInCurrentPhoto.clear();
        taggedInThisPhoto.clear();
        arrayAdapter.notifyDataSetChanged();
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }


    //This button starts the contact picking activity.
    public void clickTagButton(View view){
        contactObjectsTaggedInCurrentPhoto.clear();
        taggedInThisPhoto.clear();
        arrayAdapter.notifyDataSetChanged();

        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    //This button runs the method for showing the phototags in a ListView
    public void clickShowTags(View view){
        loadTagsFromString();
    }

    //Loads photo tags from a string of contact id:s associated with the photo and puts the tags in a listview
    private void loadTagsFromString(){
        if(contactsTags.containsKey(currentPhotoUri)){
            //The photo has id tags
            String ids = contactsTags.get(currentPhotoUri);//Get all ids for contacts tagged in this photo
            String[] idArray = ids.split(",");
            for(String id : idArray){
                ContactObject contact = contactLoader.getContact(id);
                String name = contact.getName();
                contactObjectsTaggedInCurrentPhoto.put(name, contact);
                taggedInThisPhoto.add(name);
            }
            arrayAdapter.notifyDataSetChanged();

        }
        else{
            Toast.makeText(getBaseContext(), "No tags yet", Toast.LENGTH_LONG).show();
        }
    }


    //Creates the listview for the photo tags
    private void createListView(){
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, taggedInThisPhoto);
        listview.setAdapter(arrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactObject contact = contactObjectsTaggedInCurrentPhoto.get(taggedInThisPhoto.get(position));//Get the contact object from the clicked name
                getDetails(contact);
            }
        });
    }

    //Create a dialog for the contacts details
    private void createDetailsList(){

        dialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setAdapter(detailsAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }


    private void getDetails(ContactObject contact){
        String name = contact.getName();
        ArrayList<String> phoneNumbers = contact.getPhoneNumbers();
        ArrayList<String> emails = contact.getEmails();

        detailsAdapter.clear();

        /*if (name != null) {
            detailsAdapter.add(name);
        }*/
        if (phoneNumbers.size() > 0) {
            for(String s : phoneNumbers){
                detailsAdapter.add(s);
            }
        }
        if (emails.size() > 0) {
            for(String s : emails){
                detailsAdapter.add(s);
            }
        }

        showDetails(name);
    }


    private void showDetails(String name){
        String contactName = "Details for: Contact name missing";
        if(name != null){
            contactName = "Details for: " + name;
        }
        dialogBuilder.setTitle(contactName);
        dialogBuilder.show();
    }


    //This method receives the results when the user picks a photo or a contact
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_THUMBNAIL_REQUEST && resultCode == RESULT_OK && data != null){

            currentPhotoUri = data.getStringExtra("result");//Get the currentPhotoUri.

            bitmap = BitmapFactory.decodeFile(currentPhotoUri);//Get the image

            //Make a bytearray of the bitmap for sending it to the contact
            int size = bitmap.getRowBytes() * bitmap.getHeight();
            ByteBuffer b = ByteBuffer.allocate(size);
            bitmap.copyPixelsToBuffer(b);
            b.rewind();
            photoByteArray = new byte[size];

            //Read from the buffer and fill the byte array bit by bit
           try{
                b.get(photoByteArray, 0, photoByteArray.length);
            }catch (BufferUnderflowException e){
               e.printStackTrace();
            }

            fullscreenImg.setImageBitmap(bitmap);
            tagButton.setEnabled(true);
            showTagsButton.setEnabled(true);
        }



        else if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {//A picked contact is returned
            Uri contactUri = data.getData();

            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();

            int IDColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
            String id = cursor.getString(IDColumn);
            cursor.close();

            if(!isDuplicate(currentPhotoUri.toString(), id)){
                addNewTag(id, currentPhotoUri);
                updateTagsString();
                writeToFile();
            }
        }
    }


    //For avoiding duplicate tags in photos
    private boolean isDuplicate(String uri, String id){
        if(contactsTags.containsKey(uri)){
            String tagsToExamine = contactsTags.get(uri);
            String[] taggedInImage = tagsToExamine.split(",");
            for(String s : taggedInImage){
                if(s.equals(id)){
                    return true;
                }
            }
        }
        return false;
    }

    private void addNewTag(String tag, String uri){
        String idsInThisPhoto = tag; //This is used if the photo has no existing tags since before
        if(contactsTags.containsKey(uri)){
            idsInThisPhoto = contactsTags.get(uri);
            idsInThisPhoto += ","+tag;
            contactsTags.remove(uri);
        }
        contactsTags.put(uri, idsInThisPhoto);
    }

    //This method updates the contactsTagString that keeps all photo - tags relationships.
    private void updateTagsString(){
        String key;
        String value;
        contactsTagString = "";
        Iterator<String> iterator = contactsTags.keySet().iterator();
        while(iterator.hasNext()){
            key = iterator.next();//key = photoUri
            value = contactsTags.get(key);//value = id,id,id etc
            contactsTagString += splitString + key + splitString +value;//Use splitstring intead of : since : is used in the uri
        }
    }


    //Writes the contactsTagString to file
    private void writeToFile(){
        try{
           //Then save to textfile
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fileout);
            writer.write(contactsTagString);
            writer.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }






    /* This method loads the photo - tags relationships into the contactsTagString from file.
        Improvements can be made that only loads tags of contacts that are not removed from the device.
        Just check all tags id:s against the id:s held in the allContacts SkipList of ContactLoaderClass.
     */
    private void loadFile(){
        final int READ_BLOCK_SIZE = 100;
        contactsTagString = "";

        try{
            FileInputStream filein = openFileInput(filename);
            InputStreamReader reader = new InputStreamReader(filein);
            char[] inputBuffer = new char[READ_BLOCK_SIZE];

            int charRead;
            while((charRead = reader.read(inputBuffer)) > 0){
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                contactsTagString += readstring;
            }
            reader.close();

            if(contactsTagString.length()>0){//Check if there was anything stored in the file
                contactsTagString = contactsTagString.substring(11);//Remove the first splitstring
                String[] allTags = contactsTagString.split(splitString);//Split the string. Even index are Uri:s, odd index are id:s for contact tags


                if(allTags.length>1){
                    for(int i  = 1; i < allTags.length;i = i+2){
                        String uri = allTags[i-1];
                        String ids = allTags[i];
                        contactsTags.put(uri, ids);//Map all photo uri:s to the IDs of contacts tagged in them
                    }
                }
            }

        } catch(FileNotFoundException e1){
            //Create the file
            try{
                FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(fileout);
                writer.write("");
                writer.close();
            } catch(IOException e2){
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
