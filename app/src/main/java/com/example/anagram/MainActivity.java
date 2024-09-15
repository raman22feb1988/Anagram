package com.example.anagram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    sqliteDB db;
    int letters = 0;
    String label = "*";
    HashMap<String, String> dictionary;
    HashMap<String, Integer> anagramsList;

    int mode = 0;
    String ultimate = "";
    boolean summary = false;

    TextView t1;
    GridView g1;
    TextView t4;
    TextView t5;
    EditText e2;
    Button b1;
    Button b2;
    Button b3;
    Button b4;
    Button b5;
    Button b6;
    Button b7;
    Button b8;
    Button b9;
    Button b10;
    Button b11;
    Button b12;
    Button b13;
    Button b14;
    Button b15;
    Button b16;
    Button b17;
    Button b18;

    ArrayList<String> anagrams;
    int words;
    int score;
    int counter;
    int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = findViewById(R.id.textview1);
        g1 = findViewById(R.id.gridview1);
        t4 = findViewById(R.id.textview4);
        t5 = findViewById(R.id.textview5);
        e2 = findViewById(R.id.edittext2);
        b1 = findViewById(R.id.button1);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        b5 = findViewById(R.id.button5);
        b6 = findViewById(R.id.button10);
        b7 = findViewById(R.id.button11);
        b8 = findViewById(R.id.button12);
        b9 = findViewById(R.id.button15);
        b10 = findViewById(R.id.button17);
        b11 = findViewById(R.id.button19);
        b12 = findViewById(R.id.button20);
        b13 = findViewById(R.id.button21);
        b14 = findViewById(R.id.button22);
        b15 = findViewById(R.id.button23);
        b16 = findViewById(R.id.button24);
        b17 = findViewById(R.id.button25);
        b18 = findViewById(R.id.button26);

        db = new sqliteDB(MainActivity.this);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        boolean prepared = pref.getBoolean("prepared", false);

        if(prepared) {
            getWordLength();
        } else {
            Toast.makeText(MainActivity.this, "Please give 1 hour to prepare database of dictionary words. Only when opening this mobile app for the first time.", Toast.LENGTH_LONG).show();
            db.prepareScore();
            prepareDictionary();
        }

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MainActivity.this, Report.class);
                startActivity(intent1);
                finish();
            }
        });

        b11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                e2.setText("");
            }
        });

        b12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.query, null);

                TextView t6 = yourCustomView.findViewById(R.id.textview14);
                t6.setText(db.getSchema());

                EditText e6 = yourCustomView.findViewById(R.id.edittext8);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enter your SQL query")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String sqlQuery = (e6.getText()).toString();
                                db.myQuery(sqlQuery, MainActivity.this);

                                int real = letters == 1 ? db.getCustomScore(label) : db.getCustomCounter(letters);
                                if (real != score) {
                                    score = real;
                                    db.updateScore(letters, real, label);
                                }

                                anagrams = letters == 1 ? db.getCustomQuiz(label, MainActivity.this) : db.getAllAnagrams(letters);
                                words = anagrams.size();

                                counter = db.getCounter(letters, label);
                                number = letters == 1 ? db.getCustomNumber(label) : db.getNumber(letters);

                                int peak = (words - 1) / 50;
                                if (counter > peak && words > 0) {
                                    counter = peak;
                                    db.updateCounter(letters, label, counter);
                                }

                                nextWord();
                            }
                        }).create();
                dialog.show();
            }
        });

        b13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.query, null);

                TextView t7 = yourCustomView.findViewById(R.id.textview14);
                t7.setText(db.getSchema());

                EditText e7 = yourCustomView.findViewById(R.id.edittext8);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("SELECT DISTINCT(anagram) FROM words WHERE")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String customQuery = (e7.getText()).toString();
                                ArrayList<String> resultSet = db.getCustomQuiz(customQuery, MainActivity.this);

                                if(resultSet != null) {
                                    label = customQuery;
                                    letters = 1;

                                    anagrams = resultSet;
                                    words = anagrams.size();
                                    score = db.getCustomScore(label);
                                    number = db.getCustomNumber(label);

                                    int exists = db.existLabel(letters, label);

                                    if (exists == 0) {
                                        counter = 0;
                                        db.insertLabel(letters, score, label);
                                    } else {
                                        counter = db.getCounter(letters, label);
                                    }

                                    int highest = (words - 1) / 50;
                                    if (counter > highest && words > 0) {
                                        counter = highest;
                                        db.updateCounter(letters, label, counter);
                                    }

                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });

        b14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.exportDB(MainActivity.this);
            }
        });

        b15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.importDB(MainActivity.this);
            }
        });

        b16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.exportLabels(MainActivity.this);
            }
        });

        b17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.importLabels(MainActivity.this);
            }
        });

        b18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String labelColours = db.getLabelColours();
                db.messageBox("Label colours", labelColours, MainActivity.this);
            }
        });
    }

    public void prepareDictionary()
    {
        dictionary = new HashMap<>();
        anagramsList = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("CSW2021.txt"), "UTF-8"));
            while(true)
            {
                String s = reader.readLine();
                if(s == null)
                {
                    break;
                }
                else
                {
                    String t[] = s.split("=");
                    dictionary.put(t[0], t[1]);

                    char jumbled[] = t[0].toCharArray();
                    Arrays.sort(jumbled);
                    String solution = new String(jumbled);

                    if(anagramsList.containsKey(solution))
                    {
                        anagramsList.put(solution, anagramsList.get(solution) + 1);
                    }
                    else
                    {
                        anagramsList.put(solution, 1);
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareDatabase();
    }

    public void prepareDatabase()
    {
        Iterator<Map.Entry<String, String>> itr = dictionary.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String word = entry.getKey();
            char c[] = word.toCharArray();
            Arrays.sort(c);
            String anagram = new String(c);
            int solutions = anagramsList.get(anagram);
            String definition = entry.getValue();
            StringBuilder back = new StringBuilder();
            StringBuilder front = new StringBuilder();
            for(char letter = 'A'; letter <= 'Z'; letter++)
            {
                if(dictionary.containsKey(word + letter))
                {
                    back.append(letter);
                }
                if(dictionary.containsKey(letter + word))
                {
                    front.append(letter);
                }
            }
            boolean q = db.insertWord(word, word.length(), anagram, definition, probability(word), new String(back), new String(front), "", solutions);
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("prepared", true);
        editor.commit();

        setPageNumbers();
        insertColours();
        getWordLength();
    }

    public void insertColours()
    {
        db.insertColour("Known", "#008000");
        db.insertColour("Unknown", "#FF0000");
        db.insertColour("Compound", "#FF00FF");
        db.insertColour("Prefix", "#8000FF");
        db.insertColour("Suffix", "#0000FF");
        db.insertColour("Plural", "#808080");
        db.insertColour("Guessable", "#FF8000");
        db.insertColour("Past", "#0080FF");
        db.insertColour("Learnt", "#B97A57");
        db.insertColour("New", "#C0C000");
        db.insertColour("", "#000000");
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String alphabet = (e1.getText()).toString();
                        letters = alphabet.length() == 0 ? 0 : Integer.parseInt(alphabet);
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(MainActivity.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
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

    public void wordLength(long begin, double delay, ArrayList<String> replies)
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);
        e1.setHint("Enter a value between 2 and 15");

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Word length")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String alphabets = (e1.getText()).toString();
                        letters = alphabets.length() == 0 ? 0 : Integer.parseInt(alphabets);
                        if(letters < 2 || letters > 15)
                        {
                            Toast.makeText(MainActivity.this, "Enter a value between 2 and 15", Toast.LENGTH_LONG).show();
                            wordLength(begin, delay, replies);
                        }
                        else
                        {
                            mode = 0;
                            ultimate = "";

                            cumulativeTime(begin, delay, replies);
                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void setPageNumbers()
    {
        for(int lengths = 2; lengths <= 15; lengths++)
        {
            ArrayList<String> anagramList = db.getAllAnagrams(lengths);
            db.updatePageNumbers(anagramList);
        }
    }

    public void start()
    {
        label = "*";
        anagrams = db.getAllAnagrams(letters);
        words = anagrams.size();
        score = db.getScore(letters, label);

        int actual = db.getCustomCounter(letters);
        if(actual != score)
        {
            score = actual;
            db.updateScore(letters, actual, label);
        }

        counter = db.getCounter(letters, label);
        number = db.getNumber(letters);

        int high = (words - 1) / 50;
        if(counter > high && words > 0)
        {
            counter = high;
            db.updateCounter(letters, label, counter);
        }

        nextWord();
    }

    public void nextWord()
    {
        long begin = System.currentTimeMillis();
        ArrayList<String> jumbles = new ArrayList<>();
        ArrayList<String> replies = new ArrayList<>();
        ArrayList<Integer> totals = new ArrayList<>();
        ArrayList<Integer> amounts = new ArrayList<>();
        HashMap<String, Integer> grid = new HashMap<>();
        for(int idx = (50 * counter); idx < Math.min((50 * counter) + 50, words); idx++)
        {
            String jumble = anagrams.get(idx);
            jumbles.add(jumble);
        }

        HashMap<String, ArrayList<String>> answers = db.getUnsolvedAnswers(jumbles);
        HashMap<String, Integer> allList = db.getAllAnswers(jumbles);
        for(int total = 0; total < jumbles.size(); total++)
        {
            String answer = jumbles.get(total);
            if(answers.containsKey(answer)) {
                ArrayList<String> answersList = answers.get(answer);
                replies.addAll(answersList);
                totals.add(answersList.size());
                for (String answerList : answersList) {
                    grid.put(answerList, total);
                }
            }
            else {
                totals.add(0);
            }
            amounts.add(allList.get(answer));
        }

        double delay = replies.size() == 0 ? 0 : db.getTime(replies.get(0));

        b1.setEnabled(true);
        b2.setEnabled(true);
        b4.setEnabled(true);
        b6.setEnabled(true);
        b7.setEnabled(true);
        b8.setEnabled(true);
        b9.setEnabled(true);
        b10.setEnabled(true);

        t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 50) + 1));
        t4.setText("Score: " + score + "/" + number);
        t5.setText("");
        summary = false;
        e2.setText("");

        customadapter cusadapter = new customadapter(MainActivity.this, R.layout.cell, jumbles, totals, amounts);
        g1.setAdapter(cusadapter);

        g1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mode = 2;
                ultimate = jumbles.get(i);

                String solved = db.getSolvedAnswers(ultimate);
                t5.setText(Html.fromHtml(solved));
                summary = false;
            }
        });

        g1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String unsolved = jumbles.get(i);
                String unsolvedAnswers = db.getUnsolvedWords(unsolved);

                db.messageBox("Unsolved answers", unsolvedAnswers, MainActivity.this);
                return true;
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guess = (((e2.getText()).toString()).trim()).toUpperCase();
                if(replies.contains(guess))
                {
                    mode = 1;
                    ultimate = guess;

                    long stop = System.currentTimeMillis();
                    double time = stop - begin;
                    time /= 1000;
                    time += (letters == 1 ? db.getTime(guess) : delay);
                    ArrayList<String> guesses = new ArrayList<>();
                    guesses.add(guess);
                    db.updateTime(guesses, time, 1);
                    ArrayList<String> hook = db.getDefinition(guess);
                    String meaning = hook.get(0);
                    String back = hook.get(1);
                    String front = hook.get(2);
                    HashMap<String, String> colourList = db.getColours();
                    String coloursList = db.getLabel(guess);
                    String amount;

                    if (colourList.containsKey(coloursList) || colourList.containsKey("")) {
                        String coloured = colourList.containsKey(coloursList) ? colourList.get(coloursList) : colourList.get("");
                        amount = "<font color=\"" + coloured + "\"><b><small>" + front + "</small> " + guess + " <small>" + back + "</small></b> " + meaning + " <b>" + (coloursList.length() == 0 ? "(No Label)" : coloursList) + "</b></font>";
                    } else {
                        amount = "<b><small>" + front + "</small> " + guess + " <small>" + back + "</small></b> " + meaning + " <b>" + (coloursList.length() == 0 ? "(No Label)" : coloursList) + "</b>";
                    }

                    t5.setText(Html.fromHtml(amount));
                    summary = false;
                    replies.remove(guess);
                    score++;
                    db.updateScore(letters, score, label);
                    int index = grid.get(guess);
                    totals.set(index, totals.get(index) - 1);
                    g1.invalidateViews();
                    t4.setText("Score: " + score + "/" + number);
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 0;
                ultimate = "";

                if(counter == (words - 1) / 50) {
                    counter = 0;
                }
                else {
                    counter++;
                }
                db.updateCounter(letters, label, counter);
                cumulativeTime(begin, delay, replies);
                nextWord();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wordLength(begin, delay, replies);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 0;
                ultimate = "";

                if(counter == 0) {
                    counter = (words - 1) / 50;
                }
                else {
                    counter--;
                }
                db.updateCounter(letters, label, counter);
                cumulativeTime(begin, delay, replies);
                nextWord();
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cumulativeTime(begin, delay, replies);
                Intent intent1 = new Intent(MainActivity.this, Report.class);
                startActivity(intent1);
                finish();
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cumulativeTime(begin, delay, replies);
                finish();
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String guess = (((e2.getText()).toString()).trim()).toUpperCase();
                if(replies.contains(guess))
                {
                    mode = 1;
                    ultimate = guess;

                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    final View yourCustomView = inflater.inflate(R.layout.output, null);

                    EditText e5 = yourCustomView.findViewById(R.id.edittext5);

                    Spinner s2 = yourCustomView.findViewById(R.id.spinner3);
                    ArrayList<String> labelsList = db.getAllLabels();

                    ArrayAdapter<String> comboBoxAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, labelsList);
                    comboBoxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    s2.setAdapter(comboBoxAdapter);

                    s2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            e5.setText(labelsList.get(i));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });

                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Set label for " + guess)
                            .setView(yourCustomView)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String cardbox = (e5.getText()).toString();

                                    long stop = System.currentTimeMillis();
                                    double time = stop - begin;
                                    time /= 1000;
                                    time += (letters == 1 ? db.getTime(guess) : delay);;
                                    ArrayList<String> guesses = new ArrayList<>();
                                    guesses.add(guess);
                                    db.updateLabel(guesses, time, 1, cardbox);
                                    ArrayList<String> hook = db.getDefinition(guess);
                                    String meaning = hook.get(0);
                                    String back = hook.get(1);
                                    String front = hook.get(2);

                                    HashMap<String, String> colours = db.getColours();
                                    String amount;

                                    if(colours.containsKey(cardbox) || colours.containsKey("")) {
                                        String colour = colours.containsKey(cardbox) ? colours.get(cardbox) : colours.get("");
                                        amount = "<font color=\"" + colour + "\"><b><small>" + front + "</small> " + guess + " <small>" + back + "</small></b> " + meaning + " <b>" + (cardbox.length() == 0 ? "(No Label)" : cardbox) + "</b></font>";
                                    } else {
                                        amount = "<b><small>" + front + "</small> " + guess + " <small>" + back + "</small></b> " + meaning + " <b>" + (cardbox.length() == 0 ? "(No Label)" : cardbox) + "</b>";
                                    }

                                    t5.setText(Html.fromHtml(amount));
                                    summary = false;
                                    replies.remove(guess);
                                    score++;
                                    db.updateScore(letters, score, label);
                                    int index = grid.get(guess);
                                    totals.set(index, totals.get(index) - 1);
                                    g1.invalidateViews();
                                    t4.setText("Score: " + score + "/" + number);
                                }
                            }).create();
                    dialog.show();
                }
            }
        });

        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.label, null);

                EditText e3 = yourCustomView.findViewById(R.id.edittext3);
                EditText e4 = yourCustomView.findViewById(R.id.edittext4);

                Spinner s1 = yourCustomView.findViewById(R.id.spinner2);
                ArrayList<String> labelList = db.getAllLabels();

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, labelList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s1.setAdapter(spinnerAdapter);

                s1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        e4.setText(labelList.get(i));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Change label")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String line = (((e3.getText()).toString()).trim()).toUpperCase();
                                String category = (e4.getText()).toString();
                                db.updateWord(line, category);

                                if (mode == 1) {
                                    if (line.equals(ultimate)) {
                                        ArrayList<String> hook = db.getDefinition(line);
                                        String meaning = hook.get(0);
                                        String back = hook.get(1);
                                        String front = hook.get(2);

                                        HashMap<String, String> colours = db.getColours();
                                        String colour = colours.containsKey(category) ? colours.get(category) : colours.get("");

                                        String amount = "<font color=\"" + colour + "\"><b><small>" + front + "</small> " + line + " <small>" + back + "</small></b> " + meaning + " <b>" + category + "</b></font>";
                                        t5.setText(Html.fromHtml(amount));
                                        summary = false;
                                    }
                                } else if (mode == 2) {
                                    char last[] = line.toCharArray();
                                    Arrays.sort(last);
                                    String order = new String(last);

                                    if (order.equals(ultimate)) {
                                        String solved = db.getSolvedAnswers(order);
                                        t5.setText(Html.fromHtml(solved));
                                        summary = false;
                                    }
                                }

                                if(summary == true)
                                {
                                    revise(jumbles);
                                }
                            }
                        }).create();
                dialog.show();
            }
        });

        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 0;
                ultimate = "";

                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.input, null);

                EditText e1 = yourCustomView.findViewById(R.id.edittext1);
                int maximum = ((words - 1) / 50) + 1;
                e1.setHint("Enter a value between 1 and " + maximum);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e1.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                if(page < 1 || page > maximum)
                                {
                                    Toast.makeText(MainActivity.this, "Enter a value between 1 and " + maximum, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    counter = page - 1;
                                    db.updateCounter(letters, label, counter);
                                    cumulativeTime(begin, delay, replies);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });

        b10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summary = true;
                revise(jumbles);
            }
        });

        b12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.query, null);

                TextView t6 = yourCustomView.findViewById(R.id.textview14);
                t6.setText(db.getSchema());

                EditText e6 = yourCustomView.findViewById(R.id.edittext8);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enter your SQL query")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String sqlQuery = (e6.getText()).toString();
                                db.myQuery(sqlQuery, MainActivity.this);

                                int real = letters == 1 ? db.getCustomScore(label) : db.getCustomCounter(letters);
                                if (real != score) {
                                    score = real;
                                    db.updateScore(letters, real, label);
                                }

                                anagrams = letters == 1 ? db.getCustomQuiz(label, MainActivity.this) : db.getAllAnagrams(letters);
                                words = anagrams.size();

                                counter = db.getCounter(letters, label);
                                number = letters == 1 ? db.getCustomNumber(label) : db.getNumber(letters);

                                int peak = (words - 1) / 50;
                                if (counter > peak && words > 0) {
                                    counter = peak;
                                    db.updateCounter(letters, label, counter);
                                }

                                cumulativeTime(begin, delay, replies);
                                nextWord();
                            }
                        }).create();
                dialog.show();
            }
        });

        b13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View yourCustomView = inflater.inflate(R.layout.query, null);

                TextView t7 = yourCustomView.findViewById(R.id.textview14);
                t7.setText(db.getSchema());

                EditText e7 = yourCustomView.findViewById(R.id.edittext8);

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("SELECT DISTINCT(anagram) FROM words WHERE")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String customQuery = (e7.getText()).toString();
                                ArrayList<String> resultSet = db.getCustomQuiz(customQuery, MainActivity.this);

                                if(resultSet != null) {
                                    label = customQuery;
                                    letters = 1;

                                    anagrams = resultSet;
                                    words = anagrams.size();
                                    score = db.getCustomScore(label);
                                    number = db.getCustomNumber(label);

                                    int exists = db.existLabel(letters, label);

                                    if (exists == 0) {
                                        counter = 0;
                                        db.insertLabel(letters, score, label);
                                    } else {
                                        counter = db.getCounter(letters, label);
                                    }

                                    int highest = (words - 1) / 50;
                                    if (counter > highest && words > 0) {
                                        counter = highest;
                                        db.updateCounter(letters, label, counter);
                                    }

                                    cumulativeTime(begin, delay, replies);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    public void revise(ArrayList<String> jumbles)
    {
        String revision = db.getSummary(jumbles);
        t5.setText(Html.fromHtml(revision));
    }

    public void cumulativeTime(long begin, double delay, ArrayList<String> replies)
    {
        long stop = System.currentTimeMillis();
        double time = stop - begin;
        time /= 1000;
        if(letters == 1) {
            db.updateCustomTime(replies, time);
        } else {
            time += delay;
            db.updateTime(replies, time, 0);
        }
    }

    public class customadapter extends ArrayAdapter<String>
    {
        Context con;
        int _resource;
        List<String> lival1;
        List<Integer> lival2;
        List<Integer> lival3;

        public customadapter(Context context, int resource, List<String> li1, List<Integer> li2, List<Integer> li3) {
            super(context, resource, li1);
            // TODO Auto-generated constructor stub
            con = context;
            _resource = resource;
            lival1 = li1;
            lival2 = li2;
            lival3 = li3;
        }

        @Override
        public View getView(int position, View v, ViewGroup vg)
        {
            View vi = null;
            LayoutInflater linflate = (LayoutInflater)(MainActivity.this).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = linflate.inflate(_resource, null);

            TextView t2 = vi.findViewById(R.id.textview2);
            TextView t3 = vi.findViewById(R.id.textview3);

            String lival = lival1.get(position);
            int li = lival2.get(position);
            int livalue = lival3.get(position);

            t2.setText(lival);
            t3.setText(superscript(li) + "/" + subscript(livalue));

            if(li == 0)
            {
                t2.setBackgroundColor(Color.GREEN);
                t3.setBackgroundColor(Color.GREEN);
            }

            return vi;
        }
    }

    public double probability(String st)
    {
        int frequency[] = new int[] {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
        int count = 100;
        double chance = 1;
        for(int j = 0; j < st.length(); j++)
        {
            char ch = st.charAt(j);
            int ord = ((int) ch) - 65;
            chance *= frequency[ord];
            chance /= count;
            if(frequency[ord] > 0) {
                frequency[ord]--;
            }
            count--;
        }
        return chance;
    }

    public String superscript(int value) {
        char characters[] = "⁰¹²³⁴⁵⁶⁷⁸⁹".toCharArray();
        String chars = Integer.toString(value);
        char character[] = chars.toCharArray();
        char sub[] = new char[chars.length()];
        for(int values = 0; values < chars.length(); values++)
        {
            sub[values] = characters[character[values] - 48];
        }
        return new String(sub);
    }

    public String subscript(int value) {
        char characters[] = "₀₁₂₃₄₅₆₇₈₉".toCharArray();
        String chars = Integer.toString(value);
        char character[] = chars.toCharArray();
        char sub[] = new char[chars.length()];
        for(int values = 0; values < chars.length(); values++)
        {
            sub[values] = characters[character[values] - 48];
        }
        return new String(sub);
    }
}