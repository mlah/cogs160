package edu.ucsd.cogs160;

import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GestureMathProblemFlowActivity extends Activity implements OnTouchListener {
    private TextView statusText;
    private Button leftNum1;
    private Button leftNum2;
    private Button leftNum3;
    private Button rightNum;
    private EditText inputField;
    private TextView dbDebugText;  //TODO: this is temporary for debugging the db
    private Button nextButton;
    
    private Resources res;
    private int BACKGROUND_COLOR;
    private int HINT_COLOR;
    private int CONFIRM_COLOR;
    
    private GestureMathDataOpenHelper dbHelper;
    private SQLiteDatabase db;
    
    private int downStatus;
    private List<Problem> problemList;
    private int currentProblemIndex;
    private int maxProblemIndex;
    private int currentProblemId;
    private String currentSolution;
    private int studentId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //initialize db
        dbHelper = new GestureMathDataOpenHelper(this.getApplicationContext());
        db = dbHelper.getWritableDatabase();
                
        //get random student id
        studentId = getIntent().getIntExtra("studentId", -1);
        
        //initialize problem list
        res = getResources();
        problemList = initializeProblemList();
        currentProblemIndex = 0;
        maxProblemIndex = problemList.size()-1;

        //initialize colors
        BACKGROUND_COLOR = color.darker_gray;
        HINT_COLOR = res.getColor(R.color.DodgerBlue);
        CONFIRM_COLOR = res.getColor(R.color.CadetBlue);

        statusText = (TextView)findViewById(R.id.textView);
        statusText.setText("Student ID: " + String.valueOf(studentId));
        downStatus = 0;
        
        dbDebugText = (TextView)findViewById(R.id.dbDebug);

        //set up buttons
        leftNum1 = (Button)findViewById(R.id.leftNum1);
        leftNum1.setBackgroundColor(BACKGROUND_COLOR);
        leftNum1.setOnTouchListener(this);

        leftNum2 = (Button)findViewById(R.id.leftNum2);
        leftNum2.setBackgroundColor(BACKGROUND_COLOR);
        leftNum2.setOnTouchListener(this);
        
        leftNum3 = (Button)findViewById(R.id.leftNum3);
        leftNum3.setBackgroundColor(BACKGROUND_COLOR);
        
        rightNum = (Button)findViewById(R.id.rightNum);
        rightNum.setBackgroundColor(BACKGROUND_COLOR);
        
        nextButton = (Button)findViewById(R.id.nextButton);
        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setOnTouchListener(this);
        
        //handle user input. also demonstrates inline function definition
        inputField = (EditText)findViewById(R.id.inputField);
        inputField.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                
                inputField.setCursorVisible(true);
                
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String answer = ((EditText)v).getText().toString();
                    if (answer.equals("")) {
                        //if blank, do nothing
                    } else {
                        int didGesture = 0;
                        if (downStatus == 2)
                            didGesture = 1;  //TODO: move this to a global variable and set in the touch handler?  instead of piping through downStatus
                        
                        if (answer.equals(currentSolution)) {
                            //right answer
                            dbHelper.addSolvedProblem(db, studentId, currentProblemId, didGesture, 1);
                        } else {
                            //wrong answer
                            dbHelper.addSolvedProblem(db, studentId, currentProblemId, didGesture, 0);
                        }
                        //show next screen button, disable input, change input field color to confirmed
                        statusText.setText("next screen");
                        nextButton.setVisibility(View.VISIBLE);
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(inputField.getWindowToken(), 0);
                        inputField.setCursorVisible(false);
                        inputField.setFocusableInTouchMode(false);
                        inputField.setBackgroundColor(CONFIRM_COLOR);
                        
                        //debug db solved_problem table
                        dbDebugText.setText(dbHelper.getSolvedProblems(db));
                    }
                    return true;
                }
                return false;
            }
        });
        
        //set values for current problem
        initializeProblem(problemList.get(currentProblemIndex));
    }
    
    /** Called when the activity is finished by calling finish()
     *  Do cleanup or final write to db here */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    
    
    public List<Problem> initializeProblemList() {
        
        ArrayList<Problem> pl = new ArrayList<Problem>();
        
        String[] problemsStrings = {"problem1", "problem2", "problem3"};
        for (String s : problemsStrings) {
            TypedArray ta = res.obtainTypedArray(res.getIdentifier(s, "array", "edu.ucsd.cogs160"));
            pl.add(new Problem(ta.getInt(0, -1),
                                     ta.getString(1),
                                     ta.getInt(2, -1),
                                     ta.getInt(3, -1),
                                     ta.getInt(4, -1),
                                     ta.getInt(5, -1),
                                     ta.getInt(6, -1),
                                     ta.getString(7),
                                     ta.getInt(8, -1)));
            ta.recycle();
        }
        
        return pl;
    }
    
    
    
    public void initializeProblem(Problem problem) {
        currentProblemId = problem.problem_id;
        
        //TODO: handle moving the input field, and rearranging the addends
        leftNum1.setText(String.valueOf(problem.left1));
        leftNum2.setText(String.valueOf(problem.left2));
        leftNum3.setText(String.valueOf(problem.left3));
        
        rightNum.setText(String.valueOf(problem.right));
        
        currentSolution = String.valueOf(problem.solution);
    }
    
    
    
    public void nextProblem() {
        //advance to next problem
        currentProblemIndex++;
        
        //TODO: out of problems.  what next?
        if (currentProblemIndex > maxProblemIndex) {
            //if pressed twice, delete rows from db
            if (currentProblemIndex == maxProblemIndex+1) {
                dbHelper.deleteAllSolvedProblems(db);
                statusText.setText("solved problems deleted from db");
                dbDebugText.setText(dbHelper.getSolvedProblems(db));
                return;
            }
            //if pressed thrice, destroy activity, return to start screen
            if (currentProblemIndex == maxProblemIndex+2) {
                finish();
            }
            statusText.setText("no more problems");
            return;
        }
        
        //get and set up next problem
        initializeProblem(problemList.get(currentProblemIndex));
        
        //restore UI elements to original state, clear input, etc
        nextButton.setVisibility(View.INVISIBLE);
        inputField.setText("");
        inputField.setFocusableInTouchMode(true);
        inputField.setCursorVisible(false);
        unHighlightAll();
        downStatus = 0;
    }
    
    
    public void unHighlightAll() {
        leftNum1.setBackgroundColor(BACKGROUND_COLOR);
        leftNum2.setBackgroundColor(BACKGROUND_COLOR);
        leftNum3.setBackgroundColor(BACKGROUND_COLOR);
        inputField.setBackgroundColor(BACKGROUND_COLOR);
    }
    
    
    //handle all touch events
    public boolean onTouch(View v, MotionEvent e) {
        //    	myText.setText(String.valueOf(e.getActionMasked()));

        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {     
            if (v.getId() == R.id.nextButton) { /* do nothing */ return true; }
            
            switch (downStatus) {
            case 0:
                Log.i("info", "down 0");
                if (v.getId() == R.id.leftNum1) {
                    leftNum1.setBackgroundColor(HINT_COLOR);
                    leftNum2.setBackgroundColor(HINT_COLOR);
                } else { //leftNum2
                    leftNum2.setBackgroundColor(HINT_COLOR);
                    leftNum1.setBackgroundColor(HINT_COLOR);                    
                }
                downStatus = 1;
                statusText.setText(String.valueOf(downStatus));
                break;
            case 1:
                Log.i("info", "down 1");
                leftNum1.setBackgroundColor(CONFIRM_COLOR);
                leftNum2.setBackgroundColor(CONFIRM_COLOR);
                inputField.setBackgroundColor(HINT_COLOR);
                downStatus = 2;
                statusText.setText(String.valueOf(downStatus));
                break;
            default:
                Log.i("info", "down default");
                break;
            }
        }

        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            //if next button pressed, move to the next screen
            if (v.getId() == R.id.nextButton) {
                nextProblem();
                return true;
            }
            
            switch (downStatus) {
            case 2:
                Log.i("info", "up 2");
                //do nothing
            case 1:
                if (downStatus != 2) {  //i guess this still gets triggered even if we touch two correctly
                    leftNum1.setBackgroundColor(BACKGROUND_COLOR);
                    leftNum2.setBackgroundColor(BACKGROUND_COLOR);
                    downStatus = 0;
                    statusText.setText(String.valueOf(downStatus));
                }
                break;
            default:
                break;
            }
        }

        return true;
    }
    
    
    
    //disable back button
    @Override
    public void onBackPressed() {
        // do nothing
    }
}