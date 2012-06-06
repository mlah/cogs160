package edu.ucsd.cogs160;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

/**
 * GestureMathDataOpenHelper
 * 
 * This class extends SQLiteOpenHelper and handles connecting to the database
 *  and executing queries
 * 
 * @author mlah
 *
 */
public class GestureMathDataOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "gesture_math";
    
    private static final String SOLVED_PROBLEMS_TABLE_NAME = "solved_problems";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_GESTURE_CONDITION = "gesture_condition";
    private static final String KEY_PROBLEM_ID = "problem_id";
    private static final String KEY_CORRECT = "correct";
    private static final String KEY_STUDENT_ANSWER = "student_answer";
    
    private static final String OPTIONS_TABLE_NAME = "options";
    private static final String KEY_GESTURE_CONDITION_OPTION = "gesture_condition_option";
    
    private static final String SOLVED_PROBLEMS_TABLE_CREATE =
            "CREATE TABLE " + SOLVED_PROBLEMS_TABLE_NAME + " (" +
                    KEY_STUDENT_ID + " INT, " +
                    KEY_GESTURE_CONDITION + " INT, " +
                    KEY_PROBLEM_ID + " INT, " + 
                    KEY_CORRECT + " INT, " + 
                    KEY_STUDENT_ANSWER + " INT);";
    
    private static final String OPTIONS_TABLE_CREATE =
            "CREATE TABLE " + OPTIONS_TABLE_NAME + " (" +
                    KEY_GESTURE_CONDITION_OPTION + " INT);";
    
    private static final String INSERT_SOLVED_PROBLEM =
            "INSERT INTO " + SOLVED_PROBLEMS_TABLE_NAME + 
                    " (" + KEY_STUDENT_ID + ", " + KEY_GESTURE_CONDITION + ", " + 
                           KEY_PROBLEM_ID + ", " + KEY_CORRECT + ", " + 
                           KEY_STUDENT_ANSWER + ") " + 
                    " VALUES (?, ?, ?, ?, ?);";

    private static final String INSERT_OPTIONS =
            "INSERT INTO " + OPTIONS_TABLE_NAME + " (" + KEY_GESTURE_CONDITION_OPTION + ") " + 
                    " VALUES (?);";

    private static final String UPDATE_OPTIONS =
            "UPDATE " + OPTIONS_TABLE_NAME + " SET " + KEY_GESTURE_CONDITION_OPTION + " = ?;";

    
    /** Constructor */
    GestureMathDataOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    
    
    /**
     * This method is only called if the database does not exist 
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SOLVED_PROBLEMS_TABLE_CREATE);
        db.execSQL(OPTIONS_TABLE_CREATE);
        insertOptions(db, 0);  //default to gesture condition
    }

    
    
    /** Required method */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
    
    
    
    /**
     * Add a row to the solved_problem table 
     */
    public void addSolvedProblem(SQLiteDatabase db, int student_id, int gesture_condition, 
                                                    int problem_id, int correct, int student_answer) {

        SQLiteStatement stmt = db.compileStatement(INSERT_SOLVED_PROBLEM);
        stmt.bindLong(1, student_id);
        stmt.bindLong(2, gesture_condition);
        stmt.bindLong(3, problem_id);
        stmt.bindLong(4, correct);
        stmt.bindLong(5, student_answer);
        if (stmt.executeInsert() == -1) {
            //problem!
            Log.e("db insert error", "error inserting solved problem row");
        }
    }
    
    
    
    /**
     * Get all rows from the solved_problems table 
     * 
     * @param db the db to use
     * @return
     */
    public String getSolvedProblems(SQLiteDatabase db) {
        String output = "student_id\tgesture_condition\tproblem_id\tcorrect\tstudent_answer\n";
        //this query is "select * from solved_problems"
        Cursor c = db.query(SOLVED_PROBLEMS_TABLE_NAME, null, null, null, null, null, null);
        while (c.moveToNext()) {
            output += String.valueOf(c.getInt(0)) + "\t\t\t" + 
                      String.valueOf(c.getInt(1)) + "\t\t\t\t\t\t\t\t" + 
                      String.valueOf(c.getInt(2)) + "\t\t\t\t" + 
                      String.valueOf(c.getInt(3)) + "\t\t\t" + 
                      String.valueOf(c.getInt(4)) + "\n";
        }
        return output;
    }

    
    
    /**
     * Insert a row into the options table.  Right now the options table only has 
     *  one column and one row, so this method should only ever be called once
     *  
     * @param db the db to use
     * @param gesture_condition the value to initialize the gesture_condition option
     */
    public void insertOptions(SQLiteDatabase db, int gesture_condition) {
        SQLiteStatement stmt = db.compileStatement(INSERT_OPTIONS);
        stmt.bindLong(1, gesture_condition);
        if (stmt.executeInsert() == -1) {
            //problem!
            Log.e("db insert error", "error inserting options");
        }
    }
    
    
    
    /**
     * Update the gesture_condition option 
     * 
     * @param db the db to use
     * @param gesture_condition the value to update the gesture_condition with
     */
    public void updateOptions(SQLiteDatabase db, int gesture_condition) {
        SQLiteStatement stmt = db.compileStatement(UPDATE_OPTIONS);
        stmt.bindLong(1, gesture_condition);
        if (stmt.executeUpdateDelete() == -1) {
            //problem!
            Log.e("db insert error", "error updating options");
        }
    }
    
    
    
    /**
     * Get the options from the db
     * For now we only have one option so return a single int
     * @param db the db to use
     * @return
     */
    public int getOptions(SQLiteDatabase db) {
        //this query is "select * from options"
        Cursor c = db.query(OPTIONS_TABLE_NAME, null, null, null, null, null, null);
        c.moveToNext();
        return c.getInt(0);
    }
    
    
    
    /**
     * Delete all the rows in the solved_problems db
     * 
     * @param db the db to use
     */
    public void deleteAllSolvedProblems(SQLiteDatabase db) {
        db.delete(SOLVED_PROBLEMS_TABLE_NAME, null, null);
    }
    
    
    
    /**
     * Check if a student_id exists in the solved_problems table, indicating
     *  the student_id is a repeat and should not be used
     * 
     * @param db the db to use
     * @param student_id the student_id to check if exists in db
     * @return
     */
    public boolean doesStudentIdExist(SQLiteDatabase db, int student_id) {
        //this query is "select student_id from solved_problems where student_id=[param student_id]"
        Cursor c = db.query(SOLVED_PROBLEMS_TABLE_NAME, 
                            new String[] {KEY_STUDENT_ID}, 
                            KEY_STUDENT_ID + "=" + String.valueOf(student_id), 
                            null, null, null, null);
        if (c.moveToNext()) {
            //if any results, then student ID has already been used 
            return true;
        }
        return false;
    }
    
    
    
    /**
     * Backup the SQLite database to external storage, in this case a microSD card
     */
    public static void backupDb() {
        Log.i("db backup", "start");
        try {
            //TODO: this directory is a hack for the ASUS Transformer TF-101.
            // Environment.getExternalStorageDirectory() reports the SD card path as "/mnt/sdcard", but the correct directory is "/Removable/MicroSD"
            // using the File Manager on the TF-101 shows the correct microSD path.  hopefully this is also the case with other devices
            //File sd = Environment.getExternalStorageDirectory();
            File sd = new File("/Removable/MicroSD");
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "edu.ucsd.cogs160" + "//databases//" + DATABASE_NAME;
                String backupDBPath = DATABASE_NAME + ".sqlite";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();

                    long bytes = dst.transferFrom(src, 0, src.size());

                    src.close();
                    dst.close();
                                        
                    Log.i("db backup", "number of bytes " + String.valueOf(bytes));
                    Log.i("db backup", "backup saved at " + backupDB.toString());
                    
                } else {
                    Log.e("db backup", "db file does not exist " + currentDB.toString());
                }              
            } else {
                Log.e("db backup", "cannot write to location " + sd.toString());                
            }
        } catch (Exception e) {
            // exception
            Log.e("db backup", "database backup failed " + e.getMessage());
        }
    }

}
