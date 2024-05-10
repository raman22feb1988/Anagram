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
    HashMap<String, String> dictionary;

    int mode = 0;
    String ultimate = "";

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
        b9 = findViewById(R.id.button30);

        db = new sqliteDB(MainActivity.this);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        boolean prepared = pref.getBoolean("prepared", false);

        if(prepared) {
            getWordLength();
        } else {
            Toast.makeText(MainActivity.this, "Please give some time to prepare database of dictionary words only when opening this for the first time", Toast.LENGTH_LONG).show();
            db.prepareScore();
            prepareDictionary();
        }

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWordLength();
            }
        });
    }

    public void prepareDictionary()
    {
        dictionary = new HashMap<>();

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
            String definition = entry.getValue();
            StringBuilder front = new StringBuilder();
            StringBuilder back = new StringBuilder();
            for(char letter = 'A'; letter <= 'Z'; letter++)
            {
                if(dictionary.containsKey(word + letter))
                {
                    front.append(letter);
                }
                if(dictionary.containsKey(letter + word))
                {
                    back.append(letter);
                }
            }
            boolean q = db.insertWord(word, word.length(), anagram, definition, probability(word), new String(front), new String(back), "");
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppData", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("prepared", true);
        editor.commit();

        getWordLength();
    }

    public void getWordLength()
    {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View yourCustomView = inflater.inflate(R.layout.input, null);

        EditText e1 = yourCustomView.findViewById(R.id.edittext1);

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
                            cumulativeTime(begin, delay, replies);
                            start();
                        }
                    }
                }).create();
        dialog.show();
    }

    public void start()
    {
        anagrams = db.getAllAnagrams(letters);
        words = anagrams.size();
        score = db.getScore(letters);
        counter = db.getCounter(letters);
        number = db.getNumber(letters);

        nextWord();
    }

    public void nextWord()
    {
        long begin = System.currentTimeMillis();
        ArrayList<String> jumbles = new ArrayList<>();
        ArrayList<String> replies = new ArrayList<>();
        ArrayList<Integer> totals = new ArrayList<>();
        HashMap<String, Integer> grid = new HashMap<>();
        for(int idx = (50 * counter); idx < Math.min((50 * counter) + 50, words); idx++)
        {
            String jumble = anagrams.get(idx);
            jumbles.add(jumble);
        }

        HashMap<String, ArrayList<String>> answers = db.getUnsolvedAnswers(jumbles);
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
        }

        double delay = replies.size() == 0 ? 0 : db.getTime(replies.get(0));

        b1.setEnabled(true);
        b2.setEnabled(true);
        b4.setEnabled(true);
        b6.setEnabled(true);
        b7.setEnabled(true);
        b8.setEnabled(true);
        b9.setEnabled(true);

        t1.setText("Page " + (counter + 1) + " out of " + (((words - 1) / 50) + 1));
        t4.setText("Score: " + score + "/" + number);
        t5.setText("");
        e2.setText("");

        customadapter cusadapter = new customadapter(MainActivity.this, R.layout.cell, jumbles, totals);
        g1.setAdapter(cusadapter);

        g1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mode = 2;
                ultimate = jumbles.get(i);

                String solved = db.getSolvedAnswers(ultimate);
                t5.setText(Html.fromHtml(solved));
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
                    time += delay;
                    ArrayList<String> guesses = new ArrayList<>();
                    guesses.add(guess);
                    db.updateTime(guesses, time, 1);
                    ArrayList<String> hook = db.getDefinition(guess);
                    String meaning = hook.get(0);
                    String front = hook.get(1);
                    String back = hook.get(2);
                    String amount = "<b><small>" + back + "</small> " + guess + " <small>" + front + "</small></b> " + meaning;
                    t5.setText(Html.fromHtml(amount));
                    replies.remove(guess);
                    score++;
                    db.updateScore(letters, score);
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
                db.updateCounter(letters, counter);
                cumulativeTime(begin, delay, replies);
                nextWord();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = 0;
                ultimate = "";

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
                db.updateCounter(letters, counter);
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

                    Button b10 = yourCustomView.findViewById(R.id.button14);
                    Button b11 = yourCustomView.findViewById(R.id.button15);
                    Button b12 = yourCustomView.findViewById(R.id.button16);
                    Button b13 = yourCustomView.findViewById(R.id.button17);
                    Button b14 = yourCustomView.findViewById(R.id.button18);
                    Button b20 = yourCustomView.findViewById(R.id.button32);
                    Button b22 = yourCustomView.findViewById(R.id.button35);

                    b10.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Known");
                        }
                    });

                    b11.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Unknown");
                        }
                    });

                    b12.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Benjamin");
                        }
                    });

                    b13.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Prefix");
                        }
                    });

                    b14.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Suffix");
                        }
                    });

                    b20.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Plural");
                        }
                    });

                    b22.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            e5.setText("Learnt");
                        }
                    });

                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Set label for " + guess)
                            .setView(yourCustomView)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String label = (e5.getText()).toString();

                                    long stop = System.currentTimeMillis();
                                    double time = stop - begin;
                                    time /= 1000;
                                    time += delay;
                                    ArrayList<String> guesses = new ArrayList<>();
                                    guesses.add(guess);
                                    db.updateLabel(guesses, time, 1, label);
                                    ArrayList<String> hook = db.getDefinition(guess);
                                    String meaning = hook.get(0);
                                    String front = hook.get(1);
                                    String back = hook.get(2);

                                    String colour;

                                    switch(label)
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

                                    String amount = "<font color=\"" + colour + "\"><b><small>" + back + "</small> " + guess + " <small>" + front + "</small></b> " + meaning + " <b>" + label + "</b></font>";
                                    t5.setText(Html.fromHtml(amount));
                                    replies.remove(guess);
                                    score++;
                                    db.updateScore(letters, score);
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

                Button b15 = yourCustomView.findViewById(R.id.button19);
                Button b16 = yourCustomView.findViewById(R.id.button20);
                Button b17 = yourCustomView.findViewById(R.id.button21);
                Button b18 = yourCustomView.findViewById(R.id.button22);
                Button b19 = yourCustomView.findViewById(R.id.button23);
                Button b21 = yourCustomView.findViewById(R.id.button33);
                Button b23 = yourCustomView.findViewById(R.id.button36);

                b15.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Known");
                    }
                });

                b16.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Unknown");
                    }
                });

                b17.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Benjamin");
                    }
                });

                b18.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Prefix");
                    }
                });

                b19.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Suffix");
                    }
                });

                b21.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Plural");
                    }
                });

                b23.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        e4.setText("Learnt");
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

                                if(mode == 1)
                                {
                                    if(line.equals(ultimate))
                                    {
                                        ArrayList<String> hook = db.getDefinition(line);
                                        String meaning = hook.get(0);
                                        String front = hook.get(1);
                                        String back = hook.get(2);

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

                                        String amount = "<font color=\"" + colour + "\"><b><small>" + back + "</small> " + line + " <small>" + front + "</small></b> " + meaning + " <b>" + category + "</b></font>";
                                        t5.setText(Html.fromHtml(amount));
                                    }
                                }
                                else if(mode == 2)
                                {
                                    char last[] = line.toCharArray();
                                    Arrays.sort(last);
                                    String order = new String(last);

                                    if(order.equals(ultimate))
                                    {
                                        String solved = db.getSolvedAnswers(order);
                                        t5.setText(Html.fromHtml(solved));
                                    }
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

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Go to page")
                        .setView(yourCustomView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String pages = (e1.getText()).toString();
                                int page = pages.length() == 0 ? 0 : Integer.parseInt(pages);
                                int maximum = (((words - 1) / 50) + 1);
                                if(page < 1 || page > maximum)
                                {
                                    Toast.makeText(MainActivity.this, "Enter a value between 1 and " + maximum, Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    counter = page - 1;
                                    db.updateCounter(letters, counter);
                                    cumulativeTime(begin, delay, replies);
                                    nextWord();
                                }
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    public void cumulativeTime(long begin, double delay, ArrayList<String> replies)
    {
        long stop = System.currentTimeMillis();
        double time = stop - begin;
        time /= 1000;
        time += delay;
        db.updateTime(replies, time, 0);
    }

    public class customadapter extends ArrayAdapter<String>
    {
        Context con;
        int _resource;
        List<String> lival1;
        List<Integer> lival2;

        public customadapter(Context context, int resource, List<String> li1, List<Integer> li2) {
            super(context, resource, li1);
            // TODO Auto-generated constructor stub
            con = context;
            _resource = resource;
            lival1 = li1;
            lival2 = li2;
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

            t2.setText(lival);
            t3.setText(Integer.toString(li));

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
        int frequency[] = new int[]{9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1};
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
}