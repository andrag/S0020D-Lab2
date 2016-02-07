package com.example.anders.laboration2;



import android.app.LoaderManager;
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

    PropImageView fullscreenImg;
    Bitmap bitmap;
    byte[] photoByteArray;

    //Data structures
    private String currentPhotoUri;//Uri to the photo currently selected
    private String contactPicked;//This might just be instead of LOOKUP_ID. This should maybe not be used then.
    private String namesTaggedInThisPhoto;//A string of all names and IDs of contacts tagged in the selected photo
    private String filename = "contact_tags.txt";// For storing tags
    private String newTag;

    //For storing photo uri:s and link them to the names and id:s of the contacts tagged in them
    //Key: Photo uri - Value: "name,ID,name,ID" etc
    private ConcurrentSkipListMap<String, String> contactsTags;

    private String contactsTagString;//A string representation of contactsTags for storing to file - "uri:name,ID,name,ID:uri:name,ID" etc.
    private final String splitString = "splitstring";


    private HashMap<String, String> namesAndIDsTaggedInCurrentPhoto;//Try change from ID string to uri

    //Tag list
    private ListView listview;
    private List<String> taggedInThisPhoto; //This is a list for storing names tagged in the selected photo
    private ArrayAdapter<String> arrayAdapter;






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
        namesAndIDsTaggedInCurrentPhoto = new HashMap<>();

        listview = (ListView) findViewById(R.id.tags_list);

        System.out.println("Start the app!");
        loadFile();
        createListView();
    }


    //This button starts an activity that sends us to the GridViewActivity and waits for a result
    public void clickGalleryButton(View view){
        //Start the gridView activity
        namesAndIDsTaggedInCurrentPhoto.clear();
        taggedInThisPhoto.clear();
        arrayAdapter.notifyDataSetChanged();
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivityForResult(intent, PICK_THUMBNAIL_REQUEST);
    }


    //This button starts the contact picking activity.
    public void clickTagButton(View view){
        namesAndIDsTaggedInCurrentPhoto.clear();
        taggedInThisPhoto.clear();
        arrayAdapter.notifyDataSetChanged();

        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI); //Tidigare argument nr 2: Uri.parse("content://contacts"
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);//Show only contacts with phone numbers!
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);//What result do I get here? Name? Number? Contact?
    }


    public void clickShowTags(View view){
        loadTagsFromString();
    }

    private void loadTagsFromString(){
        if(contactsTags.containsKey(currentPhotoUri)){
            //Syntax of the names/ID-string: ,name;ID,name;ID,name;ID
            String namesAndIds = contactsTags.get(currentPhotoUri);
            namesAndIds.substring(1);//Trim the first ",". What happens when we only have one tag?
            String[] namesAndIDsArray = namesAndIds.split(","); //This gives name;ID name;ID in the array
            
            taggedInThisPhoto.clear();
            if(namesAndIDsArray.length>0){//This check might be unnecessary
                for(String s : namesAndIDsArray){
                    String[] splitNameAndID = s.split(";");
                    String name = splitNameAndID[0];
                    String ID = splitNameAndID[1];//IndexOutOfBounds

                    namesAndIDsTaggedInCurrentPhoto.put(name, ID);
                    taggedInThisPhoto.add(name);//This currently adds the wrong stuff. Like a lot of ,,,,
                }
                arrayAdapter.notifyDataSetChanged();
            }

        } else{
            Toast.makeText(getBaseContext(), "This photo has no tags yet.", Toast.LENGTH_LONG).show();
        }
    }



    //This used to get called from showTagsOnClick before we had IDs involved. 2016-02-04
    private void updateTaggedContacts(String name){
        //String name format: ,name,name,name....etc Might need to trim the first ","
        System.out.println("Trying to update the listview with tags. The name string is: "+name);
        String[] names = name.split(",");//This throws a nullpointer exception when no file exists to read from from the start!
        taggedInThisPhoto.clear();
        for(String s : names){
            taggedInThisPhoto.add(s);
        }
        arrayAdapter.notifyDataSetChanged();

    }

    private void createListView(){
        System.out.println("Creating the listview and set an adapter to it.");
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, taggedInThisPhoto);
        listview.setAdapter(arrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contactID = namesAndIDsTaggedInCurrentPhoto.get(taggedInThisPhoto.get(position));
                Toast.makeText(getBaseContext(), "You clicked: "+ contactID , Toast.LENGTH_LONG).show();

                /*String name = null;
                String number = null;
                String email = null;*/

                Cursor cursor = getContentResolver().query(Uri.parse(contactID), null, null, null, null);//Change to uri later
                cursor.moveToFirst();//Move to first row?
                int nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);//Select the name nameColumn
                int numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int emailColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME);


                //Null-secure these things.
                String name = cursor.getString(nameColumn);
                String number = cursor.getString(numberColumn);
                String email = cursor.getString(emailColumn);

                cursor.close();

                Toast.makeText(getBaseContext(), name + number + email, Toast.LENGTH_LONG).show();


                //showDialog(taggedInThisPhoto.get(position));
                //createDialog(taggedInThisPhoto.get(position));


                //Change the selections to get more details
               /* Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,//new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        null, //ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                            //ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                            //ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                        new String[]{contactID},
                        null);
                String contactNumber = "It did not work :P";
                String email = null;

                if(cursor.moveToFirst()){
                    contactNumber = cursor.getString((cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    //email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.))
                }

                cursor.close();

                Toast.makeText(getBaseContext(), contactNumber, Toast.LENGTH_LONG).show();
*/

            }


        });
    }

    private void showDialog(String name){

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

            currentPhotoUri = data.getStringExtra("result");//Get the currentPhotoUri. Might be done with data.getData() instead. GetPath is the way described in the assignment!

            bitmap = BitmapFactory.decodeFile(currentPhotoUri);//Get the image

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
            }

            fullscreenImg.setImageBitmap(bitmap);
            tagButton.setEnabled(true);
            showTagsButton.setEnabled(true);
        }



        else if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            //Here, a photo is to be tagged if the contact is not already tagged in the photo

            Uri contactUri = data.getData();

            //The null as second and third arg is inefficient. Returns all columns resp all rows. You get the whole table.
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();//Move to first row?
            int nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);//Select the name nameColumn
            int IDColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
            String name = cursor.getString(nameColumn);
            String ID = cursor.getString(IDColumn);

            cursor.close();

            //newTag = name+";"+ID;
            newTag = name+";"+ contactUri;

            //contactPicked = cursor.getString(nameColumn);//Maybe remove the contactPicked variable?

            if(!isDuplicate(currentPhotoUri.toString(), newTag)){
                addNewTag(newTag, currentPhotoUri);//Include write to file in this method
                updateTagsString();
                writeToFile();
            }

        }
    }


    //Can this be done by checking contactsTags HashMap instead of contactsTagString? Made a try :)
    private boolean isDuplicate(String uri, String nameAndID){
        boolean response = false;
        if(contactsTags.containsKey(uri)){
            String tagsToExamine = contactsTags.get(uri);
            String[] taggedInImage = tagsToExamine.split(",");//This makes it necessary to have yet another split symbol: uri:name;ID;name;ID:uri:
            for(String s : taggedInImage){
                if(s.equals(nameAndID)){
                    return true;
                }
            }
        }
        return false;
    }

    private void addNewTag(String tag, String uri){
        String tagAndIDsOfThisPhoto = tag; //Only if the photo has no existing tags since before
        if(contactsTags.containsKey(uri)){
            tagAndIDsOfThisPhoto = contactsTags.get(uri);
            tagAndIDsOfThisPhoto += ","+tag;
            contactsTags.remove(uri);
        }
        //tagAndIDsOfThisPhoto = tagAndIDsOfThisPhoto;
        contactsTags.put(uri, tagAndIDsOfThisPhoto);

    }

    //This method updates the string to write to the file after a tag is made
    private void updateTagsString(){
        System.out.println("Updating contactsTagString with the new iteration method.");
        String key;
        String value;
        contactsTagString = "";
        Iterator<String> iterator = contactsTags.keySet().iterator();
        while(iterator.hasNext()){
            key = iterator.next();
            value = contactsTags.get(key);//Will this cause synch errors since trying to access while iterating??? Probably work since its only reading.
            contactsTagString += splitString + key + splitString +value;
        }
    }



    private void writeToFile(){

        try{
           //Then save to textfile
            FileOutputStream fileout = openFileOutput(filename, MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fileout);
            writer.write(contactsTagString);//+"//¤"+nameUriPairs);
            writer.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }






    //Might need a check if the file exists before loading it.
    private void loadFile(){
        final int READ_BLOCK_SIZE = 100;//Check what this is for
        //contactsTags = new ConcurrentSkipListMap<String, String>();

        contactsTagString = "";

        //Problem to solve: If this is first time using the app, create an empty file. But only the first time.
        // 1. Tried with throwing a FileNotFoundException and create an empty file under the catch.

        try{
            FileInputStream filein = openFileInput(filename);
            InputStreamReader reader = new InputStreamReader(filein);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];

            contactsTagString = "";

            int charRead;

            while((charRead = reader.read(inputBuffer)) > 0){
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                contactsTagString += readstring;
            }
            reader.close();

            if(contactsTagString.length()>0){//Check if there was anything stored in the file

                contactsTagString = contactsTagString.substring(9);
                String[] allTags = contactsTagString.split(splitString);//Split the string. Even index are Uri:s, odd index are name;ID,name;ID etc

                if(allTags.length>1){
                    for(int i  = 1; i < allTags.length;i = i+2){
                        String uri = allTags[i-1];
                        String nameAndIDs = allTags[i];
                        contactsTags.put(uri, nameAndIDs);//Map all photo uri:s to the names and IDs of contacts tagged in them
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
                //contactsTagString = ""; //This might need to be un-uncommented :P
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
