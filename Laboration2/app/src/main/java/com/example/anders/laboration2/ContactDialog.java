package com.example.anders.laboration2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Anders on 2016-02-07.
 */

/* Suggested solution for dynamic list within the dialog:
    1. Get everything to work from within this class like this:
        1. Keep the ListView, ArrayAdapter and everything here.
        2. Initiate everything.
        3. Show the List in the dialog
        4. Make the onClick change it
        5. If it works, make a public method here for updating the list from MainActivity.

 */


public class ContactDialog extends DialogFragment {

    private List<String> list;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;

    /* In onCreate:
    initialize essential components of the fragment that you want to retain when
    the fragment is paused or stopped, then resumed.
     */


    /* In onCreateView:
    This is for regular fragments. Maybe the onCreateDialog is the same as onCreateView for a regular fragment.
    The system calls this when it's time to draw the fragment UI for the first time. Returns a view object, the root of the UI.

     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Dialog builder class for dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        String[] hej;
        hej = new String[]{"sune", "mangs"};

        builder.setTitle("Contact details")
                .setItems(hej, null)
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User cancelled the dialog.
            }
        /*}).setAdapter(MainActivity.detailsAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {//Might skip this and just have a ListView in the dialog_fragment xml
                //Do nothing
            }*/
        });

        return builder.create();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
