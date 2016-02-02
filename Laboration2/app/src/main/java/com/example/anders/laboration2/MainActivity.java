package com.example.anders.laboration2;



import android.app.LoaderManager;
//import android.content.CursorLoader;
import android.content.Intent;
//import android.database.Cursor;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
//import android.os.Environment;
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
/*import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;*/
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
import java.util.List;


/* This assignment is almost done. This is a copy of the lab2 from users/anders/androidprojectsomething.../laboration2
                    This copy is made for finishing the project starting 2015-10-28


        Button1:    Sends us to a GridActivity that presents the photos from external content in a gridview
                    Select a photo in the gridview to get back to mainactivity. The photo is displayed using the
                    PropImageView.
        Button2:    Sends an intent to an inbuilt contact picking activity. The contact should return in the onActivityForResult.
                    I believe that the data argument holds the intent there and data.getData() gives us the Uri to the contact picked.

        Left to do:... Some stuff.

                    The class ImageAdapter is not in use. There is an adapter built into GridViewActivity that handles the grid and the images.

 */




public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Button galleryButton;
    Button tagButton;
    Button showTagsButton;
   // String uri;
    static final int PICK_THUMBNAIL_REQUEST = 1;
    private static final int PICK_CONTACT_REQUEST = 2;

    PropImageView fullscreenImg;
    Bitmap bitmap;
    byte[] photoByteArray;

    //Data structures
    private String contactPhotoUri;//Uri to a specific contact?
    private String contactName;//This might just be instead of LOOKUP_ID. This should maybe not be used then.
    private String filename = "contact_tags.txt";// For storing tags
    private HashMap<String, String> contactTags; //Temporary memory for storing new tags
    private HashMap<String, Uri> contactUrisFromName; //Key = Name, Value = Contact uri. Maybe skip this, go for LOOKUP_ID instead.
    private String contactsTagString;// All tags stored in a string as a temporary memory.
    //The contactsTagString is for writing to file. Maybe something more.

    //Tag list
    private ListView listview;
    private List<String> taggedContacts;
    private ArrayAdapter<String> arrayAdapter;

    private String nameUriPairs;
    //private HashMap<String, String> bajs;//Temporary for storing.. stuff I forgot. For the last dialog? Contacts info?
    private HashMap<String, String> storeContactsUriFromName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //fullScreenImage = (ImageView) findViewById(R.id.full_screen_image);
        fullscreenImg = (PropImageView) findViewById(R.id.fullscreen_view);
        galleryButton = (Button) findViewById(R.id.gallery_button);
        tagButton = (Button) findViewById(R.id.tag_button);
        showTagsButton = (Button) findViewById(R.id.showtags_button);

        taggedContacts = new ArrayList<String>();
        //taggedContacts.add("Abel Snabelsson");
        listview = (ListView) findViewById(R.id.tags_list);
        //listview.setOnItemClickListener(this);//Made MainActivity implement OnItemClickListener

        System.out.println("Start the app!");
        loadFile();
        createListView();
    }


    //This button starts an activity that sends us to the GridViewActivity and waits for a result
    public void clickButton1(View view){
        //Start the gridView activity
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }


    //This button starts the contact picking activity.
    public void clickButton2(View view){
        //ContactsContract..
        //ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        //ContactsContract.Contacts.CONTENT_URI
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); //Tidigare argument nr 2: Uri.parse("content://contacts"
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);//Show only contacts with phone numbers!
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);//What result do I get here? Name? Number? Contact?
    }


    public void clickButton3(View view){
        //Display the name of the selected uri, contactPhotoUri should contain it.
        System.out.println("You clicked button 3.");
        if(contactTags.containsKey(contactPhotoUri.toString())){
            String name = contactTags.get(contactPhotoUri.toString());
            System.out.println("The name string is: " + name);
            Toast.makeText(getBaseContext(), name, Toast.LENGTH_LONG).show();
            updateTaggedContacts(name);
        }
        else Toast.makeText(getBaseContext(), "No tag yet.", Toast.LENGTH_LONG).show();
    }

    private void updateTaggedContacts(String name){
        //String name format: ,name,name,name....etc Might need to trim the first ","
        System.out.println("Trying to update the listview with tags. The name string is: "+name);
        String[] names = name.split(",");
        taggedContacts.clear();
        for(String s : names){
            taggedContacts.add(s);
        }
        arrayAdapter.notifyDataSetChanged();

    }

    private void createListView(){
        System.out.println("Creating the listview and set an adapter to it.");
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, taggedContacts);
        listview.setAdapter(arrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getBaseContext(), "You clicked: "+taggedContacts.get(position), Toast.LENGTH_LONG).show();
                //showDialog(taggedContacts.get(position));
                createDialog(taggedContacts.get(position));
            }
        });
    }

    private void showDialog(String name){
        //The uri will lead to the data we want to display.
        if(storeContactsUriFromName.containsKey(name)){
            Toast.makeText(getBaseContext(), "Uri is"+storeContactsUriFromName.get(name), Toast.LENGTH_LONG).show();
        }
        else Toast.makeText(getBaseContext(), "The storeContactsUriFromName doesn't contain the uri.", Toast.LENGTH_LONG).show();
    }

    private void createDialog(String name){
        /*  Call this method from the Contacts ListViews onClick-method. Add the uri as an argument.
            1. Create a listadapter
            2. Make a query for all info about a selected contact. Could go as an argument.
            3. Put all the information about the contact in an ArrayList
            4. Link the ArrayList to the ListAdapter(For dynamic purpose)
            5. Create the dialog and show it with the ListAdapter as source
         */
        //Query all information. Always retrieve Data._ID if you're binding the result Cursor to a ListView; otherwise, the binding won't work.





        ArrayList<String> information = new ArrayList<String>();
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /*This method is to take the result from GridViewActivity and display the image in fullscreen.
     */
    /*final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    final Uri thumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    final String thumb_DATA = MediaStore.Images.Thumbnails.DATA;
    final String thumb_image_ID = MediaStore.Images.Thumbnails.IMAGE_ID;*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_THUMBNAIL_REQUEST && resultCode == RESULT_OK && data != null){ //&& data.getData() != null) {
        System.out.println("Print the data that returns to MainActivity after choosing an image./n====================================================/n"+data.toString());

            contactPhotoUri = data.getStringExtra("result");//Get the contactPhotoUri. Might be done with data.getData() instead. GetPath is the way described in the assignment!
            bitmap = BitmapFactory.decodeFile(contactPhotoUri);//Get the image

            //Make a bytearray of the bitmap for sending it to the contact
            // Source at: http://stackoverflow.com/questions/4989182/converting-java-bitmap-to-byte-array
            int size = bitmap.getRowBytes() * bitmap.getHeight();
            ByteBuffer b = ByteBuffer.allocate(size);
            bitmap.copyPixelsToBuffer(b);
            b.rewind();
            photoByteArray = new byte[size];//Remove this if the other code in the else if statement below works .... <--Really?

            //Read from the buffer and fill the bytearray bit by bit (length should be -1?)
           try{
                b.get(photoByteArray, 0, photoByteArray.length);
            }catch (BufferUnderflowException e){
               e.printStackTrace();
               System.out.println("Hejhejehej");
            }


            //fullScreenImage.setImageBitmap(bitmap);
            fullscreenImg.setImageBitmap(bitmap);
            tagButton.setEnabled(true);
            showTagsButton.setEnabled(true);
        }


        /*  The following code is for receiving a contact from the contacts picker.
                1. We want to tag the contact with this contacts name.
                2. Before tagging, we need to look for duplicates.
                3. We also need to store the LOOKUP_URI so that the contact can be displayed when clicking on the tag.


            Solution 1:     Skip the name and just keep a dynamic list of URI:s to the tagged contacts. When the "show tags" button is pressed, all names are placed in a temporary
                            hashmap with the names as keys and LOOK_UP_IDs as values. When a name is pressed, make a search for the contacts details using LOOK_UP_ID.

         */
        else if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {

            //First test to get the info of the picked contact: 2015-10-28
            System.out.println("The data.getData back from contactspicker is of type:" + data.getData().getPath());
            Uri contactUri = data.getData();

            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            //This is what i tried yesterday.
            //Test code for querying the contacts email using its id
            // get the contact id from the Uri
            String id = contactUri.getLastPathSegment();//This line gets the contacts identifier!
            Toast.makeText(getBaseContext(),"Contacts id is: "+id, Toast.LENGTH_LONG).show();


            //Have probably made a test with email here:

            // query for everything email.
           cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                    new String[]{id}, null);
            cursor.moveToFirst();
            String columns[] = cursor.getColumnNames();
            for (String c : columns) {
                int index = cursor.getColumnIndex(c);
                //Log.v("DEBUG_TAG", "Column: " + c + " == ["//This is crashing the app.
                        //+ cursor.getString(index) + "]");
            }


            //I beleive, as of 2015-12-16, that contactName is never assigned to anything and therefore make up the null values among the tags.
            if(!storeContactsUriFromName.containsKey(contactName)){//Nullpointer at storeContactsUriFromName. Might be contactName = null? contactName is never assigned to anything! I beleive.
                storeContactsUriFromName.put(contactName, contactUri.toString());
                nameUriPairs += ","+contactName+","+contactUri;
            }

            if(!isDuplicate(contactPhotoUri.toString(), contactName)){
                writeToFile();
            }

        }

        taggedContacts.clear();
        arrayAdapter.notifyDataSetChanged();
    }


    private boolean isDuplicate(String uri, String name){
        boolean response = false;
        if(contactsTagString.contains(uri)){
            String namesToExamine = contactTags.get(uri);
            System.out.println("Checks for duplicate. String with names are: " + namesToExamine+" and the name to check for is: "+name);
            //namesToExamine = namesToExamine.substring(1);//Don't know if this is needed.
            String[] taggedInImage = namesToExamine.split(",");//Nullpointer 2015-11-04
            for(String s : taggedInImage){
                if(s.equals(name)){
                    response = true;
                }
            }
        }

        return response;
    }

    private void writeToFile(){

        //First put the tag in the temporary memory
        if(contactTags.containsKey(contactPhotoUri.toString())){
            String namesTaggedInThisPhoto = contactTags.get(contactPhotoUri);
            namesTaggedInThisPhoto += ","+contactName;//Is contact name null from the start??? This might be the source of null among tags.
            contactName = namesTaggedInThisPhoto; //This is for adding several names to one photo.
        }


        contactTags.put(contactPhotoUri.toString(), contactName);//This might be the source of the nulls among the tags?
        contactsTagString += ":"+contactPhotoUri+":"+contactName;

        try{
           //Then save to textfile
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fileout);
            writer.write(contactsTagString+"//¤"+nameUriPairs);
            writer.close();

            //Toast.makeText(getBaseContext(), "Tag saved in file.", Toast.LENGTH_LONG).show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }



    //Might need a check if the file exists before loading it.
    private void loadFile(){
        final int READ_BLOCK_SIZE = 100;//Check what this is for
        contactTags = new HashMap<String, String>();
        storeContactsUriFromName = new HashMap<String, String>();//For holding uri:s to images on the external storage. Maybe weak if images are moved...
        nameUriPairs = "";
        contactsTagString = "";

        //Problem to solve: If this is first time using the app, create an empty file. But only the first time.
        // 1. Tried with throwing a FileNotFoundException and create an empty file under the catch.

        try{
            FileInputStream filein = openFileInput(filename);
            InputStreamReader reader = new InputStreamReader(filein);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];

            String readFileString = "";

            //contactsTagString = "";
            int charRead;

            while((charRead = reader.read(inputBuffer)) > 0){
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                readFileString += readstring;
            }
            reader.close();
            Toast.makeText(getBaseContext(), "The file was loaded. File string is: "+readFileString, Toast.LENGTH_LONG).show();
            System.out.println("The file was loaded. ReadFile string is: "+readFileString);

            if(readFileString.length()>0){//Check if there was anything stored in the file
                String[] splitInTwo = readFileString.split("//¤");//This split is not working! Might be now after changing to //¤ from just ¤
                contactsTagString = splitInTwo[0];
                contactsTagString = contactsTagString.substring(1);
                String[] allTags = contactsTagString.split(":");//These are now all tags for all pictures. If I remember correct.

                if(allTags.length>1){
                    for(int i  = 1; i < allTags.length;i = i+2){
                        String uri = allTags[i-1];
                        String name = allTags[i];
                        contactTags.put(uri, name);//Map all uris to a specific name... Maybe the "name"-value can consist of several names? Don't remember
                    }
                }

                System.out.println("splitInTwo has length: "+splitInTwo.length);
                //System.out.println("SplitInTwoPosition1 = "+splitInTwo[1]);//This throws out of bounds
                System.out.println("SplitInTwo[1] = "+splitInTwo[1]);
                if(splitInTwo.length>1){
                    nameUriPairs = splitInTwo[1];
                    Toast.makeText(getBaseContext(), "The file was loaded. nameUriPairs string is: "+nameUriPairs, Toast.LENGTH_LONG).show();
                    nameUriPairs = nameUriPairs.substring(1);
                    String[] allNameUriPairs = nameUriPairs.split(",");
                    if(allNameUriPairs.length>1){
                        for(int i = 1;i < allNameUriPairs.length;i = i + 2){
                            String name = allNameUriPairs[i-1];
                            String uri = allNameUriPairs[i];
                            storeContactsUriFromName.put(name, uri);//I guess this one is for getting the uri to a contact based on its name. Should use LOOKUP_ID instead.
                        }
                    }
                }
                /*else {
                    nameUriPairs = "";
                }*/
            }




        } catch(FileNotFoundException e1){
            //Create the file
            try{
                FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
                OutputStreamWriter writer = new OutputStreamWriter(fileout);
                writer.write("");
                writer.close();
                //contactsTagString = "";
                //nameUriPairs = "";//Doubly initialized
                System.out.println("The file wasn't found but created in the catch.");
            } catch(IOException e2){
                e2.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
