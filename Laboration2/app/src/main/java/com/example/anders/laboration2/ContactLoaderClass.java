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

/* A class for
    1. Loading the information of all contacts into ContactObjects
    2. Providing MainActivity with these ContactObjects when it searches for contacts with specific IDs
 */


public class ContactLoaderClass {

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


                Cursor phoneCursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if(phoneCursor.getCount() > 0){

                    while(phoneCursor.moveToNext()){

                        String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumbers.add(phone);
                    }
                }
                phoneCursor.close();



            //Make a query for the phone numbers
            context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id},                                           //Using the contacts id for lookup
                    null);



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
