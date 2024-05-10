package com.example.anagram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
        b6 = findViewById(R.id.button29);
        b7 = findViewById(R.id.button31);

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

        AlertDialog dialog = new AlertDialog.Builder(Report.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        label = "*";
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
            String colour;

            switch(category)
            {
                case "Known": colour = "#008000";
                    break;
                case "Unknown": colour = "#FF0000";
                    break;
                case "Benjamin": colour = "#FF00FF";
                    break;
                case "Prefix": colour = "#8000FF";
                    break;
                case "Suffix": colour = "#0000FF";
                    break;
                case "Plural": colour = "#FF8000";
                    break;
                default: colour = "#000000";
            }

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

                Button b8 = yourCustomView.findViewById(R.id.button24);
                Button b9 = yourCustomView.findViewById(R.id.button25);
                Button b10 = yourCustomView.findViewById(R.id.button26);
                Button b11 = yourCustomView.findViewById(R.id.button27);
                Button b12 = yourCustomView.findViewById(R.id.button28);
                Button b13 = yourCustomView.findViewById(R.id.button34);
                Button b14 = yourCustomView.findViewById(R.id.button37);

                b8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Known");
                    }
                });

                b9.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Unknown");
                    }
                });

                b10.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Benjamin");
                    }
                });

                b11.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Prefix");
                    }
                });

                b12.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Suffix");
                    }
                });

                b13.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Plural");
                    }
                });

                b14.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e6.setText("Learnt");
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
                                    db.insertLabel(letters, label);
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

                AlertDialog dialog = new AlertDialog.Builder(Report.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e1.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                int maximum = (((words - 1) / 100) + 1);
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
    }
}