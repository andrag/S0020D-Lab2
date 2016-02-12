package com.example.anders.dialogtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {

    Button showDialog;
    Button changeData;
    ArrayAdapter<String> arrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1);

        arrayAdapter.add("Hardik");
        arrayAdapter.add("Archit");
        arrayAdapter.add("Jignesh");
        arrayAdapter.add("Umang");
        arrayAdapter.add("Gatti");


        showDialog = (Button) findViewById(R.id.button);
        changeData = (Button) findViewById(R.id.button2);

        showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
                //builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Select One Name:-");




                builderSingle.setNegativeButton(
                        "cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builderSingle.setAdapter(
                        arrayAdapter,
                        //Null this or just close dialog.
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                /*String strName = arrayAdapter.getItem(which);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                        MainActivity.this);
                                builderInner.setMessage(strName);
                                builderInner.setTitle("Your Selected Item is");
                                builderInner.setPositiveButton(
                                        "Ok",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builderInner.show();*/
                            }
                        });
                builderSingle.show();
            }
        });

        changeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayAdapter.clear();
                arrayAdapter.add("Listobjet 1");
                arrayAdapter.add("Listobjekt2 osv..");
            }
        });
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
}
