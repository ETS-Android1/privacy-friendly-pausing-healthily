package orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DBHandler extends SQLiteOpenHelper {

    private Context mContext;
    private SQLiteDatabase dataBase;
    private static final String DATABASE_NAME = "exercises.sqlite";
    private static final String DATABASE_PATH = "/data/data/orgprivacy_friendly_apps.secuso.privacyfriendlybreakreminder/databases/";
    private static final int DATABASE_VERSION = 1;

    private static String DEVICE_LANGUAGE = "";
    public static final String EXERCISES_ID = "id";
    public static final String EXERCISES_IMAGE_ID = "imageID";
    public static final String EXERCISES_SECTION = "section";
    public static final String EXERCISES_DESCRIPTION = "description";
    public static final String EXERCISES_EXECUTION = "execution";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;


        DEVICE_LANGUAGE = Locale.getDefault().getLanguage();
        System.out.println("Current Language: " + DEVICE_LANGUAGE);
        //If Database exists open
        if (checkDataBase()) {
            openDataBase();
        } else {
            try {
                this.getReadableDatabase();
                copyDataBase();
                this.close();
                openDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public List<Exercise> getExercises() {
        Exercise exercise;
        List<Exercise> exerciseList = new ArrayList<>();
        dataBase = this.getReadableDatabase();
        Cursor res = dataBase.rawQuery("SELECT * FROM EXERCISES_" + DEVICE_LANGUAGE, null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            exercise = new Exercise(res.getInt(0), res.getString(1), res.getString(2), res.getInt(3), res.getString(4));
            exerciseList.add(exercise);
            res.moveToNext();
        }

        res.close();
        return exerciseList;
    }


    public List<Exercise> getExercisesFromSection(String section) {
        Exercise exercise;
        List<Exercise> exerciseList = new ArrayList<>();
        dataBase = this.getReadableDatabase();
        if (DEVICE_LANGUAGE.equals("de"))
            section = "nacken";

        Cursor res = dataBase.rawQuery("SELECT * FROM EXERCISES_" + DEVICE_LANGUAGE + " WHERE " + EXERCISES_SECTION + " LIKE " + "\"%" + section + "%\"", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            exercise = new Exercise(res.getInt(0), res.getString(1), res.getString(2), res.getInt(3), res.getString(4));
            exerciseList.add(exercise);
            res.moveToNext();
        }

        res.close();
        return exerciseList;
    }


    private boolean checkDataBase() {
        dataBase = null;
        boolean exist = false;
        try {
            String dbPath = DATABASE_PATH + DATABASE_NAME;
            dataBase = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.v("db log", "Database doesn't exist!!");
        }

        if (dataBase != null) {
            exist = true;
            closeDataBase();
        }
        return exist;
    }

    public void openDataBase() throws SQLException {
        String dbPath = DATABASE_PATH + DATABASE_NAME;
        dataBase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDataBase() {
        if (dataBase != null)
            dataBase.close();
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = mContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
}
