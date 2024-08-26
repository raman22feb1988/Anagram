package com.example.anagram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class Report extends AppCompatActivity {
    sqliteDB db;
    int letters = 0;
    String label = "*";

    TextView t1;
    TextView t2;
    Button b1;
    Button b2;
    Button b3;
    Button b4;
    Button b5;
    Button b6;
    Button b7;
    Button b8;

    ArrayList<String> anagrams;
    int words;
    int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);

        t1 = findViewById(R.id.textview6);
        t2 = findViewById(R.id.textview7);
        b1 = findViewById(R.id.button6);
        b2 = findViewById(R.id.button7);
        b3 = findViewById(R.id.button8);
        b4 = findViewById(R.id.button9);
        b5 = findViewById(R.id.button13);
        b6 = findViewById(R.id.button14);
        b7 = findViewById(R.id.button16);
        b8 = findViewById(R.id.button18);

        db = new sqliteDB(Report.this);

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        getWordLength();
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(Report.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(Report.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String alphabet = (e1.getText()).toString();
                        letters = alphabet.length() == 0 ? 0 : Integer.parseInt(alphabet);
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(Report.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
                            getWordLength();
                        }
                        else
                        {
                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void start()
    {
        label = "*";
        anagrams = db.getSolvedWords(letters);
        words = anagrams.size();
        counter = db.getPage(letters, label);

        nextWord();
    }

    public void begin()
    {
        anagrams = db.getLabelledWords(letters, label);
        words = anagrams.size();
        counter = db.getPage(letters, label);

        nextWord();
    }

    public void nextWord()
    {
        b1.setEnabled(true);
        b2.setEnabled(true);
        b7.setEnabled(true);
        b8.setEnabled(true);

        if(words > 0) {
            t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 100) + 1));
        }
        else {
            t1.setText("Page " + (counter + 1) + " out of 1");
        }
        t2.setText("");

        for(int i = 0; i < 100; i++)
        {
            int position = (counter * 100) + i;
            if(position >= words)
            {
                break;
            }
            String jumble = anagrams.get(position);
            int open = jumble.lastIndexOf("<b>");
            int close = jumble.lastIndexOf("</b>");

            String category = jumble.substring(open + 3, close);
            HashMap<String, String> colours = db.getColours();
            String colour = colours.containsKey(category) ? colours.get(category) : colours.get("");

            if(i == 0)
            {
                t2.setText("<font color=\"" + colour + "\">" + (position + 1) + ". " + jumble + "</font>");
            }
            else
            {
                t2.setText(t2.getText() + "<br><font color=\"" + colour + "\">" + (position + 1) + ". " + jumble + "</font>");
            }
        }

        t2.setText(Html.fromHtml((t2.getText()).toString()));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(words > 100) {
                    counter--;
                    if (counter < 0) {
                        counter = (words - 1) / 100;
                    }
                    db.updatePage(letters, counter, label);
                    nextWord();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(words > 100) {
                    counter++;
                    if (counter == ((words - 1) / 100) + 1) {
                        counter = 0;
                    }
                    db.updatePage(letters, counter, label);
                    nextWord();
                }
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(Report.this, MainActivity.class);
                startActivity(intent2);
                finish();
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Report.this);
                final View yourCustomView = inflater.inflate(R.layout.filter, null);

                EditText e6 = yourCustomView.findViewById(R.id.edittext6);
                EditText e7 = yourCustomView.findViewById(R.id.edittext7);

                Spinner s1 = yourCustomView.findViewById(R.id.spinner1);
                ArrayList<String> labelList = db.getAllLabels();

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(Report.this, android.R.layout.simple_spinner_item, labelList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s1.setAdapter(spinnerAdapter);

                s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        e6.setText(labelList.get(i));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

                AlertDialog dialog = new AlertDialog.Builder(Report.this)
                        .setTitle("Filter by label")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                label = (e6.getText()).toString();
                                String alphabets = (e7.getText()).toString();
                                letters = alphabets.length() == 0 ? 0 : Integer.parseInt(alphabets);

                                int exist = db.existLabel(letters, label);

                                if(exist == 0)
                                {
                                    db.insertLabel(letters, 0, label);
                                    begin();
                                }
                                else
                                {
                                    if(label.equals("*"))
                                    {
                                        start();
                                    }
                                    else
                                    {
                                        begin();
                                    }
                                }
                            }
                        }).create();
                dialog.show();
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Report.this);
                final View yourCustomView = inflater.inflate(R.layout.input, null);

                EditText e1 = yourCustomView.findViewById(R.id.edittext1);
                int maximum = (((words - 1) / 100) + 1);
                e1.setHint("Enter a value between 1 and " + maximum);

                AlertDialog dialog = new AlertDialog.Builder(Report.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e1.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                if(page < 1 || page > maximum)
                                {
                                    Toast.makeText(Report.this, "Enter a value between 1 and " + maximum, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    counter = page - 1;
                                    db.updatePage(letters, counter, label);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });

        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(Report.this);
                final View yourCustomView = inflater.inflate(R.layout.query, null);

                TextView t3 = yourCustomView.findViewById(R.id.textview14);
                t3.setText(db.getSchema());

                EditText e2 = yourCustomView.findViewById(R.id.edittext8);

                AlertDialog dialog = new AlertDialog.Builder(Report.this)
                        .setTitle("SELECT front, word, back, definition, time, label FROM words WHERE")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String customQuery = (e2.getText()).toString();
                                ArrayList<String> resultSet = db.getSqlQuery(customQuery, Report.this);

                                if(resultSet != null) {
                                    label = customQuery;
                                    letters = 0;

                                    anagrams = resultSet;
                                    words = anagrams.size();

                                    int exists = db.existLabel(letters, label);

                                    if (exists == 0) {
                                        counter = 0;
                                        db.insertLabel(letters, 0, label);
                                    } else {
                                        counter = db.getPage(letters, label);
                                    }

                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }
}