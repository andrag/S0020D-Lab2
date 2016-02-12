package com.example.anders.laboration2;

import java.util.ArrayList;

/**
 * Created by Anders on 2016-02-12.
 */
public class ContactObject {


    private String id, name;
    private ArrayList<String> emails;
    private ArrayList<String> phoneNumbers;

    public ContactObject(String id, String name, ArrayList<String> emails, ArrayList<String> phoneNumbers){
        this.id = id;
        this.name = name;
        this.emails = emails;
        this.phoneNumbers = phoneNumbers;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public ArrayList<String> getEmails(){
        return emails;
    }

    public ArrayList<String> getPhoneNumbers(){
        return phoneNumbers;
    }

    //For testing
    public String printContact(){
        System.out.println("Contact id: "+id+" Contact name: "+name+" Emails: "+emails.toString()+" Phones: "+phoneNumbers.toString());
        return "Contact id: "+id+" Contact name: "+name+" Emails: "+emails.toString()+" Phones: "+phoneNumbers.toString();
    }
}
