package com.example.anagram;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class sqliteDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CSW2021.db";
    public Context con;

    public sqliteDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
        // TODO Auto-generated constructor stub
        con = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table words(word text, length integer, anagram text, definition text, probability real, time real, solved integer, back text, front text, label text, page integer)"
        );
        db.execSQL(
                "create table scores(length integer, score integer, counter integer, page integer, label text)"
        );
        db.execSQL(
                "create table colours(label text, colour text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS words");
        db.execSQL("DROP TABLE IF EXISTS scores");
        db.execSQL("DROP TABLE IF EXISTS colours");
        onCreate(db);
    }

    public void myQuery(String sqlQuery, Context activity) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(sqlQuery);
        }
        catch(SQLiteException e) {
            alertBox("Error", e.toString(), activity);
        }
    }

    public String getLabelColours()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT label, colour FROM colours", null);

        String labelColours = "<b>";
        int line = 1;

        if (cursor.moveToFirst()) {
            do {
                String label = cursor.getString(0);
                String colour = cursor.getString(1);

                if(line == 1) {
                    labelColours += ("<font color=\"" + colour + "\">" + line + ". " + (label.length() == 0 ? "(Default)" : label) + ": " + colour + "</font>");
                } else {
                    labelColours += ("<br><font color=\"" + colour + "\">" + line + ". " + (label.length() == 0 ? "(Default)" : label) + ": " + colour + "</font>");
                }
                line++;
            } while (cursor.moveToNext());
        }
        labelColours += "</b>";
        return labelColours;
    }

    public ArrayList<String> getAllLabels()
    {
        ArrayList<String> labelList = new ArrayList<>();
        labelList.add("");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(label) FROM words ORDER BY label", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                if(data.length() > 0) {
                    labelList.add(data);
                }
            } while(cursor.moveToNext());
        }
        return labelList;
    }

    public ArrayList<String> getTableNames()
    {
        ArrayList<String> tableList = new ArrayList<>();
        int idx = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                if(idx > 0) {
                    tableList.add(data);
                }
                idx++;
            } while(cursor.moveToNext());
        }
        return tableList;
    }

    public String getSchema()
    {
        String schema = new String();
        ArrayList<String> tablesList = getTableNames();

        SQLiteDatabase db = this.getReadableDatabase();
        for(String tableName : tablesList)
        {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            String columnList[] = cursor.getColumnNames();
            schema += (schema.length() == 0 ? tableName + "\n" + Arrays.toString(columnList) : "\n" + tableName + "\n" + Arrays.toString(columnList));
        }
        return schema;
    }

    public void exportDB(Context situation)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        ArrayList<String> tables = getTableNames();
        for(String table : tables)
        {
            File file = new File(exportDir, "Android/data/com.example.anagram/files/" + table + ".csv");
            try
            {
                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                SQLiteDatabase db = this.getReadableDatabase();
                Cursor curCSV = db.rawQuery("SELECT * FROM " + table,null);
                String columnsList[] = curCSV.getColumnNames();
                csvWrite.writeNext(columnsList);
                while(curCSV.moveToNext())
                {
                    String arrStr[] = new String[columnsList.length];
                    for(int index = 0; index < columnsList.length; index++)
                    {
                        arrStr[index] = curCSV.getString(index);
                    }
                    csvWrite.writeNext(arrStr);
                }
                csvWrite.close();
                curCSV.close();
                alertBox("Export CSV", "Export CSV complete.", situation);
            }
            catch(Exception sqlEx)
            {
                alertBox("Export CSV", sqlEx.toString(), situation);
            }
        }
    }

    public void importDB(Context situation)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String path = "Android/data/com.example.anagram/files/words.csv";
        String database = "words";

        LayoutInflater inflater = LayoutInflater.from(situation);
        final View yourCustomView = inflater.inflate(R.layout.path, null);

        TextView t4 = yourCustomView.findViewById(R.id.textview22);
        EditText e2 = yourCustomView.findViewById(R.id.edittext10);
        EditText e3 = yourCustomView.findViewById(R.id.edittext11);

        t4.setText(exportDir.toString() + "/");
        e2.setText(path);
        e3.setText(database);

        AlertDialog dialog = new AlertDialog.Builder(situation)
                .setTitle("File name")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String databaseName = (e3.getText()).toString();
                        ArrayList<String> databases = getTableNames();
                        if(databases.contains(databaseName))
                        {
                            File file = new File(exportDir, (e2.getText()).toString());
                            try
                            {
                                CSVReader csvRead = new CSVReader(new FileReader(file));
                                try {
                                    String columns[] = csvRead.readNext();
                                    String nextLine[] = csvRead.readNext();
                                    do {
                                        ContentValues contentValues = new ContentValues();
                                        for(int column = 0; column < columns.length; column++) {
                                            contentValues.put(columns[column], nextLine[column]);
                                        }
                                        db.insert(databaseName, null, contentValues);
                                        nextLine = csvRead.readNext();
                                    } while (nextLine != null);
                                    csvRead.close();
                                    alertBox("Import CSV", "Import CSV complete.", situation);
                                }
                                catch(IOException e)
                                {
                                    alertBox("Import CSV", e.toString(), situation);
                                }
                            }
                            catch(FileNotFoundException e)
                            {
                                alertBox("Import CSV", e.toString(), situation);
                            }
                        }
                        else
                        {
                            alertBox("Import CSV", "Table not found. Create a new table with the name '" + databaseName + "' first.", situation);
                        }
                    }
                }).create();
        dialog.show();
    }

    public void exportLabels(Context situation)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "Android/data/com.example.anagram/files/labels.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT word, label FROM words WHERE label != \"\"",null);
            String columnsList[] = curCSV.getColumnNames();
            csvWrite.writeNext(columnsList);
            while(curCSV.moveToNext())
            {
                String arrStr[] = new String[columnsList.length];
                for(int index = 0; index < columnsList.length; index++)
                {
                    arrStr[index] = curCSV.getString(index);
                }
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            alertBox("Export labels", "Export labels complete.", situation);
        }
        catch(Exception sqlEx)
        {
            alertBox("Export labels", sqlEx.toString(), situation);
        }
    }

    public void importLabels(Context situation)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        String path = "Android/data/com.example.anagram/files/labels.csv";

        LayoutInflater inflater = LayoutInflater.from(situation);
        final View yourCustomView = inflater.inflate(R.layout.message, null);

        TextView t3 = yourCustomView.findViewById(R.id.textview21);
        EditText e1 = yourCustomView.findViewById(R.id.edittext9);

        t3.setText(exportDir.toString() + "/");
        e1.setText(path);

        AlertDialog dialog = new AlertDialog.Builder(situation)
                .setTitle("File name")
                .setView(yourCustomView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File file = new File(exportDir, (e1.getText()).toString());
                        try
                        {
                            CSVReader csvRead = new CSVReader(new FileReader(file));
                            try {
                                String columns[] = csvRead.readNext();
                                String nextLine[] = csvRead.readNext();
                                do {
                                    updateWord(nextLine[0], nextLine[1]);
                                    nextLine = csvRead.readNext();
                                } while (nextLine != null);
                                csvRead.close();
                                alertBox("Import labels", "Import labels complete.", situation);
                            }
                            catch(IOException e)
                            {
                                alertBox("Import labels", e.toString(), situation);
                            }
                        }
                        catch(FileNotFoundException e)
                        {
                            alertBox("Import labels", e.toString(), situation);
                        }
                    }
                }).create();
        dialog.show();
    }

    public boolean prepareScore()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        for(int i = 2; i <= 15; i++) {
            contentValues.put("length", i);
            contentValues.put("score", 0);
            contentValues.put("counter", 0);
            contentValues.put("page", 0);
            contentValues.put("label", "*");

            db.insert("scores", null, contentValues);
        }
        return true;
    }

    public boolean insertWord(String word, int length, String anagram, String definition, double probability, String back, String front, String label)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("word", word);
        contentValues.put("length", length);
        contentValues.put("anagram", anagram);
        contentValues.put("definition", definition);
        contentValues.put("probability", probability);
        contentValues.put("time", 0);
        contentValues.put("solved", 0);
        contentValues.put("back", back);
        contentValues.put("front", front);
        contentValues.put("label", label);
        contentValues.put("page", 0);

        db.insert("words", null, contentValues);
        return true;
    }

    public boolean insertLabel(int letters, int score, String label)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("length", letters);
        contentValues.put("score", score);
        contentValues.put("counter", 0);
        contentValues.put("page", 0);
        contentValues.put("label", label);

        db.insert("scores", null, contentValues);
        return true;
    }

    public boolean insertColour(String label, String colour)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("label", label);
        contentValues.put("colour", colour);

        db.insert("colours", null, contentValues);
        return true;
    }

    public int getScore(int letters, String label)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT score FROM scores WHERE length = " + letters + " AND label = \"" + label + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int getCustomScore(String customQuery)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(word) FROM words WHERE solved = 1 AND anagram IN (SELECT DISTINCT(anagram) FROM words WHERE " + customQuery + ")", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int getCustomCounter(int letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(word) FROM words WHERE solved = 1 AND length = " + letters, null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int getCustomNumber(String customQuery)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(word) FROM words WHERE anagram IN (SELECT DISTINCT(anagram) FROM words WHERE " + customQuery + ")", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int getCounter(int letters, String label)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT counter FROM scores WHERE length = " + letters + " AND label = \"" + label + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public ArrayList<String> getAllAnagrams(int letters)
    {
        ArrayList<String> anagramList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT(anagram) FROM words WHERE length = " + letters + " ORDER BY probability DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);

                anagramList.add(data);
            } while (cursor.moveToNext());
        }
        return anagramList;
    }

    public ArrayList<String> getCustomQuiz(String customQuery, Context activity)
    {
        try {
            ArrayList<String> anagramList = new ArrayList<>();

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT DISTINCT(anagram) FROM words WHERE " + customQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    String data = cursor.getString(0);

                    anagramList.add(data);
                } while (cursor.moveToNext());
            }
            return anagramList;
        }
        catch(SQLiteException e) {
            alertBox("Error", e.toString(), activity);
            return null;
        }
    }

    public ArrayList<String> getSolvedWords(int letters)
    {
        ArrayList<String> wordList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word, definition, time, back, front, label, page FROM words WHERE length = " + letters + " AND solved = 1 ORDER BY time DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String definition = cursor.getString(1);
                String time = cursor.getString(2);
                String back = cursor.getString(3);
                String front = cursor.getString(4);
                String label = cursor.getString(5);
                String page = cursor.getString(6);

                wordList.add("<b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition + " (" + time + " seconds) <b>" + label + "</b>, Page " + page);
            } while (cursor.moveToNext());
        }
        return wordList;
    }

    public ArrayList<String> getLabelledWords(int letters, String label)
    {
        ArrayList<String> wordList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word, definition, time, back, front, page FROM words WHERE length = " + letters + " AND solved = 1 AND label = \"" + label + "\" ORDER BY time DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String definition = cursor.getString(1);
                String time = cursor.getString(2);
                String back = cursor.getString(3);
                String front = cursor.getString(4);
                String page = cursor.getString(5);

                wordList.add("<b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition + " (" + time + " seconds) <b>" + label + "</b>, Page " + page);
            } while (cursor.moveToNext());
        }
        return wordList;
    }

    public ArrayList<String> getSqlQuery(String query, Context activity)
    {
        try {
            ArrayList<String> wordList = new ArrayList<>();

            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT word, definition, time, back, front, label, page FROM words WHERE solved = 1 AND " + query + " ORDER BY time DESC", null);

            if (cursor.moveToFirst()) {
                do {
                    String data = cursor.getString(0);
                    String definition = cursor.getString(1);
                    String time = cursor.getString(2);
                    String back = cursor.getString(3);
                    String front = cursor.getString(4);
                    String label = cursor.getString(5);
                    String page = cursor.getString(6);

                    wordList.add("<b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition + " (" + time + " seconds) <b>" + label + "</b>, Page " + page);
                } while (cursor.moveToNext());
            }
            return wordList;
        }
        catch(SQLiteException e) {
            alertBox("Error", e.toString(), activity);
            return null;
        }
    }

    public HashMap<String, ArrayList<String>> getUnsolvedAnswers(ArrayList<String> jumbles)
    {
        HashMap<String, ArrayList<String>> answerList = new HashMap<>();

        String jumble = (((jumbles.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT anagram, word FROM words WHERE anagram IN " + jumble + " AND solved = 0", null);

        if (cursor.moveToFirst()) {
            do {
                String anagram = cursor.getString(0);
                String word = cursor.getString(1);

                if(answerList.containsKey(anagram))
                {
                    (answerList.get(anagram)).add(word);
                }
                else
                {
                    ArrayList<String> answersList = new ArrayList<>();
                    answersList.add(word);
                    answerList.put(anagram, answersList);
                }
            } while (cursor.moveToNext());
        }
        return answerList;
    }

    public HashMap<String, Integer> getAllAnswers(ArrayList<String> jumbles)
    {
        HashMap<String, Integer> allList = new HashMap<>();

        String jumble = (((jumbles.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT anagram, COUNT(word) FROM words WHERE anagram IN " + jumble + " GROUP BY anagram", null);

        if (cursor.moveToFirst()) {
            do {
                String anagram = cursor.getString(0);
                String word = cursor.getString(1);

                allList.put(anagram, Integer.parseInt(word));
            } while (cursor.moveToNext());
        }
        return allList;
    }

    public HashMap<String, String> getColours()
    {
        HashMap<String, String> colourList = new HashMap<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT label, colour FROM colours", null);

        if (cursor.moveToFirst()) {
            do {
                String label = cursor.getString(0);
                String colour = cursor.getString(1);

                colourList.put(label, colour);
            } while (cursor.moveToNext());
        }
        return colourList;
    }

    public String getSolvedAnswers(String jumble)
    {
        String solved = new String();
        int total = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word, definition, back, front, label FROM words WHERE anagram = \"" + jumble + "\" AND solved = 1 ORDER BY time", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String definition = cursor.getString(1);
                String back = cursor.getString(2);
                String front = cursor.getString(3);
                String label = cursor.getString(4);

                HashMap<String, String> colours = getColours();
                String colour = colours.containsKey(label) ? colours.get(label) : colours.get("");

                if(total == 1) {
                    solved += ("<font color=\"" + colour + "\">" + total + ". <b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition + " <b>" + label + "</b></font>");
                }
                else {
                    solved += ("<br><font color=\"" + colour + "\">" + total + ". <b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition + " <b>" + label + "</b></font>");
                }

                total++;
            } while (cursor.moveToNext());
        }
        return solved;
    }

    public String getUnsolvedWords(String unsolved)
    {
        String unsolvedAnswers = new String();
        int total = 1;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT word, definition, back, front FROM words WHERE anagram = \"" + unsolved + "\" AND solved = 0", null);

        if (cursor.moveToFirst()) {
            do {
                String data = cursor.getString(0);
                String definition = cursor.getString(1);
                String back = cursor.getString(2);
                String front = cursor.getString(3);

                if(total == 1) {
                    unsolvedAnswers += (total + ". <b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition);
                }
                else {
                    unsolvedAnswers += ("<br>" + total + ". <b><small>" + front + "</small> " + data + " <small>" + back + "</small></b> " + definition);
                }

                total++;
            } while (cursor.moveToNext());
        }
        return unsolvedAnswers;
    }

    public ArrayList<String> getDefinition(String guess)
    {
        ArrayList<String> hookList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT definition, back, front FROM words WHERE word = \"" + guess + "\"", null);

        String meaning = null;
        String back = null;
        String front = null;

        if (cursor.moveToFirst()) {
            do {
                meaning = cursor.getString(0);
                back = cursor.getString(1);
                front = cursor.getString(2);
            } while (cursor.moveToNext());
        }

        hookList.add(meaning);
        hookList.add(back);
        hookList.add(front);

        return hookList;
    }

    public int getPage(int letters, String label)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT page FROM scores WHERE length = " + letters + " AND label = \"" + label + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int updateScore(int letters, int score, String label) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("score", score);

        return db.update("scores", values, "length = ? AND label = ?",
                new String[] {Integer.toString(letters), label});
    }

    public int updateCounter(int letters, String label, int counter) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("counter", counter);

        return db.update("scores", values, "length = ? AND label = ?",
                new String[] {Integer.toString(letters), label});
    }

    public int updatePage(int letters, int counter, String label) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("page", counter);

        return db.update("scores", values, "length = ? AND label = ?",
                new String[] {Integer.toString(letters), label});
    }

    public int updateTime(ArrayList<String> guesses, double time, int solved) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("solved", solved);

        String guess = (((guesses.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");
        return db.update("words", values, "word IN " + guess,
                new String[] {});
    }

    public void updateCustomTime(ArrayList<String> guesses, double time) {
        SQLiteDatabase db = getWritableDatabase();
        String guess = (((guesses.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");
        db.execSQL("UPDATE words SET time = time + " + time + " WHERE word IN " + guess);
    }

    public int updateLabel(ArrayList<String> guesses, double time, int solved, String cardbox) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("solved", solved);
        values.put("label", cardbox);

        String guess = (((guesses.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");
        return db.update("words", values, "word IN " + guess,
                new String[] {});
    }

    public int updateWord(String line, String category) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("label", category);

        return db.update("words", values, "word = ?",
                new String[] {line});
    }

    public int updatePageNumbers(ArrayList<String> anagramList) {
        SQLiteDatabase db = getWritableDatabase();

        int wordLength = anagramList.size();
        int pages = (((wordLength - 1) / 50) + 1);

        int success = 1;

        for(int pageNumber = 0; pageNumber < pages; pageNumber++) {
            List<String> anagramArray = anagramList.subList(pageNumber * 50, Math.min((pageNumber + 1) * 50, wordLength));
            String anagramString = (((anagramArray.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");

            ContentValues values = new ContentValues();
            values.put("page", pageNumber + 1);

            success &= db.update("words", values, "anagram IN " + anagramString,
                    new String[] {});
        }

        return success;
    }

    public double getTime(String guess)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT time FROM words WHERE word = \"" + guess + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Double.parseDouble(data);
    }

    public int getNumber(int letters)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(word) FROM words WHERE length = " + letters, null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public int existLabel(int letters, String label)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(score) FROM scores WHERE length = " + letters + " AND label = \"" + label + "\"", null);

        String data = null;

        if (cursor.moveToFirst()) {
            do {
                data = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return Integer.parseInt(data);
    }

    public String getSummary(ArrayList<String> guesses)
    {
        String guess = (((guesses.toString()).replace("[", "(\"")).replace("]", "\")")).replace(", ", "\", \"");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT front, word, back, label FROM words WHERE anagram IN " + guess + " AND NOT label = \"\"", null);

        HashMap<String, ArrayList<String>> h = new HashMap<>();

        if (cursor.moveToFirst()) {
            do {
                String front = cursor.getString(0);
                String word = cursor.getString(1);
                String back = cursor.getString(2);
                String label = cursor.getString(3);

                String data = "<small>" + front + "</small> " + word + " <small>" + back + "</small>";

                if(h.containsKey(label)) {
                    (h.get(label)).add(data);
                }
                else {
                    h.put(label, new ArrayList<>());
                    (h.get(label)).add(data);
                }
            } while (cursor.moveToNext());
        }

        String revision = "";
        int serial = 0;

        for(Map.Entry<String, ArrayList<String>> entry : h.entrySet())
        {
            String key = entry.getKey();
            String value = (h.get(key)).toString();
            int l = value.length();
            String aerolith = value.substring(1, l - 1);

            HashMap<String, String> colours = getColours();
            String colour = colours.containsKey(key) ? colours.get(key) : colours.get("");

            if(serial == 0) {
                revision += ("<font color=\"" + colour + "\"><b>" + key + ": " + aerolith + "</b></font>");
            }
            else {
                revision += ("<br><font color=\"" + colour + "\"><b>" + key + ": " + aerolith + "</b></font>");
            }

            serial++;
        }

        return revision;
    }

    public void alertBox(String title, String message, Context location)
    {
        LayoutInflater inflater = LayoutInflater.from(location);
        final View yourCustomView = inflater.inflate(R.layout.display, null);

        TextView t1 = yourCustomView.findViewById(R.id.textview13);
        t1.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(location)
            .setTitle(title)
            .setView(yourCustomView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create();
        dialog.show();
    }

    public void messageBox(String title, String message, Context location)
    {
        LayoutInflater inflater = LayoutInflater.from(location);
        final View yourCustomView = inflater.inflate(R.layout.display, null);

        TextView t2 = yourCustomView.findViewById(R.id.textview13);
        t2.setText(Html.fromHtml(message));

        AlertDialog dialog = new AlertDialog.Builder(location)
            .setTitle(title)
            .setView(yourCustomView)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).create();
        dialog.show();
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
}