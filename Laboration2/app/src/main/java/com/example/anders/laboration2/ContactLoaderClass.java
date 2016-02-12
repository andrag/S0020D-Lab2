package com.example.anders.laboration2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by Anders on 2016-02-12.
 */
public class ContactLoaderClass {//implements LoaderManager.LoaderCallbacks<Cursor> {

    private ConcurrentSkipListMap<String, ContactObject> allContacts;
    Context context;

    public ContactLoaderClass(Context mContext){
        this.context = mContext;
        allContacts = new ConcurrentSkipListMap<>();
        loadAllContacts();
        printAllContacts();
    }

    //For testing
    public void printAllContacts(){
        Iterator<ContactObject> iterator = allContacts.values().iterator();
        while(iterator.hasNext()){
            iterator.next().printContact();
        }

    }

    public ContactObject getContact(String id){
        return allContacts.get(id);
    }

/*
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }*/

    public void loadAllContacts(){

        //First search for all contacts with Contact.CONTENT_URI
        Cursor contactsCursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
                );

        //Iterate all rows/contacts of the resulting cursor
        if(contactsCursor.getCount() > 0) {
            while(contactsCursor.moveToNext()){

            ArrayList<String> emails = new ArrayList<>();
            ArrayList<String> phoneNumbers = new ArrayList<>();

            String id = contactsCursor.getString(contactsCursor.getColumnIndex(BaseColumns._ID)); //The unique ID of a row. Cursor index -1!?
            String contactName = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            //Make a query for the phone numbers
            context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id},                                           //Using the contacts id for lookup
                    null);

            //Skip the phone number for now


            Cursor emailCursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                    null,
                    null);

            while(emailCursor.moveToNext()){
                String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));//Check if this is right.
                emails.add(email);
            }
            emailCursor.close();

            ContactObject contact = new ContactObject(id, contactName, emails, phoneNumbers);
            allContacts.put(contact.getId(), contact);
        }
        }

        contactsCursor.close();

    }


}
