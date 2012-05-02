package edu.ucsd.cogs160;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class GestureMathDataOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "gesture_math";
    private static final String SOLVED_PROBLEMS_TABLE_NAME = "solved_problems";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_PROBLEM_ID = "problem_id";
    private static final String KEY_DID_GESTURE = "did_gesture";
    private static final String KEY_CORRECT = "correct";
    private static final String SOLVED_PROBLEMS_TABLE_CREATE =
                "CREATE TABLE " + SOLVED_PROBLEMS_TABLE_NAME + " (" +
                KEY_STUDENT_ID + " INT, " +
                KEY_PROBLEM_ID + " INT, " + 
                KEY_DID_GESTURE + " INT, " + 
                KEY_CORRECT + " INT);";
    private static final String INSERT_SOLVED_PROBLEM =
            "INSERT INTO " + SOLVED_PROBLEMS_TABLE_NAME + " (student_id, problem_id, did_gesture, correct) " + 
                    " VALUES (?, ?, ?, ?);";

    GestureMathDataOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SOLVED_PROBLEMS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
    
    public void addSolvedProblem(SQLiteDatabase db, int student_id, int problem_id, int did_gesture, int correct) {
        SQLiteStatement stmt = db.compileStatement(INSERT_SOLVED_PROBLEM);
        stmt.bindLong(1, student_id);
        stmt.bindLong(2, problem_id);
        stmt.bindLong(3, did_gesture);
        stmt.bindLong(4, correct);
        if (stmt.executeInsert() == -1) {
            //problem!
            Log.e("db insert error", "error inserting solved problem row");
        }
    }
    
    public String getSolvedProblems(SQLiteDatabase db) {
        String output = "student_id  problem_id  did_gesture  correct\n";
        Cursor c = db.query(SOLVED_PROBLEMS_TABLE_NAME, null, null, null, null, null, null);
        while (c.moveToNext()) {
            output += String.valueOf(c.getInt(0)) + "  " + 
                      String.valueOf(c.getInt(1)) + "  " + 
                      String.valueOf(c.getInt(2)) + "  " + 
                      String.valueOf(c.getInt(3)) + "\n";
        }
        return output;
    }
    
    public void deleteAllSolvedProblems(SQLiteDatabase db) {
        db.delete(SOLVED_PROBLEMS_TABLE_NAME, null, null);
    }

}
