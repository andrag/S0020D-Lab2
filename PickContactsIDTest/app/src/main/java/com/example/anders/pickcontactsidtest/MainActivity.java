package com.example.anders.pickcontactsidtest;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

//import java.net.URI;

/*  TODO:

    1. Create a listView
    2. Get the lookup_key or uri, or _ID for a contact from the contacts picker intent
    3. Make sure the names come from a string formed something like: name;lookup_key;name;lookup_key
    4. Make a temporary HashMap that stores the names as keys and lookup_keys as values
    5. Populate the list with names from the string
    6. Implement onItemClick
        6.1 Get the lookup_key from the HashMap using the name
        6.2 Search for all data of the contact
        6.3 Present it in a dialogue

        Check on the above

    7. Integrate this in the final application
        7.1 Change all the storing of tags to store in the contactTags SkipList like this:
                photoUri1:name,lookup_key,name,lookup_key:photoUri2:name,lookup_key,name,lookupkey and so on.


 */


public class MainActivity extends ActionBarActivity {

    private Button tagButton;
    private Button showTagsButton;
    private String newTag;
    private ConcurrentSkipListMap<String, String> contactTags;
    private String currentPhotoUri = "dummyURI"; //This variable name should be used instead of contactPhotoUri in the main app
    private HashMap<String, String> namesAndIDsInThisPhoto;


    private List<String> taggedInThisPhoto;
    private ListView listView;
    private String dummyNames = "Urban";
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tagButton = (Button) findViewById(R.id.contacts_button);
        showTagsButton = (Button) findViewById(R.id.showtags_button);
        taggedInThisPhoto = new ArrayList<>(); //This used to be taggedContacts. Change the name in the main app as well.
        contactTags = new ConcurrentSkipListMap<String, String>();
        namesAndIDsInThisPhoto = new HashMap<String, String>();

        dummyLoadFile();

        listView = (ListView) findViewById(R.id.tags_list);
        createListView();
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
        // automatically handle clicks on the Home/Up tagButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickTagContacts(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);//Show only contacts with phone numbers
        startActivityForResult(pickContactIntent, 1);
    }

    //Do the listView stuff
    public void clickShowTags(View view) {
        //Get the contact names tagged in this "photo"
        if(contactTags.containsKey(currentPhotoUri)){
            String namesAndIDsTagged = contactTags.get(currentPhotoUri);
            String[] splitNamesAndIDs = namesAndIDsTagged.split(",");
            taggedInThisPhoto.clear();
            //Might need to get rid of some substring in the beginning depending on how the string is made in the main app
            if(splitNamesAndIDs.length>1){
                for(int i = 1;i < splitNamesAndIDs.length;i+=2){
                    namesAndIDsInThisPhoto.put(splitNamesAndIDs[i - 1], splitNamesAndIDs[i]);//Guess there is no object at i+1
                    taggedInThisPhoto.add(splitNamesAndIDs[i-1]);
                }
                arrayAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data!= null){
            Uri contactUri = data.getData();

            System.out.print("The URI to the contact is: " + contactUri);
            //Toast.makeText(getBaseContext(), "The URI to the contact is: " + contactUri, Toast.LENGTH_SHORT).show();

            //Get name and _ID of the contact picked
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);//Can I specify this to search for name and _ID?
            cursor.moveToFirst();
            int nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int IDColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
            String name = cursor.getString(nameColumn);
            String ID = cursor.getString(IDColumn);

            newTag = name + "," + ID;

            addNewTag(newTag, currentPhotoUri);

            Toast.makeText(getBaseContext(), newTag, Toast.LENGTH_LONG).show();

        }
    }

    //This is for adding a new tag to the contactTag skiplist
    private void addNewTag(String tag, String uri){
        Toast.makeText(getBaseContext(), "Adding the new tag to the skiplist.", Toast.LENGTH_LONG).show();
        String tagsOfPhoto = tag;
        if(contactTags.containsKey(uri)){
            tagsOfPhoto = contactTags.get(uri);
            tagsOfPhoto +="," + tag;//Add the new tag
            contactTags.remove(uri);
        }
        contactTags.put(uri, tagsOfPhoto);
        //This should also be written to file in the real application.
    }

    private void createListView(){
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, taggedInThisPhoto);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                createDialog(taggedInThisPhoto.get(position));
            }
        });
    }


    //Put another parameter here. Searchable.
    private void createDialog(String name){
            Toast.makeText(getBaseContext(), "No dialogue yet.", Toast.LENGTH_LONG);
    }

    private void dummyLoadFile(){
        //Fake that we load contacts and tags into the SkipList

    }


}
