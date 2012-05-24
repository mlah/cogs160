package edu.ucsd.cogs160;

import java.util.ArrayList;
import java.util.List;

import android.R.color;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class GestureMathProblemFlowActivity extends Activity 
                                            implements OnTouchListener, OnKeyListener, OnClickListener, AnimatorListener {
    private TextView statusText;
    private Button leftNum1;
    private Button leftNum2;
    private Button leftNum3;
    private Button rightNum;
    private EditText inputField;
    private TextView dbDebugText;  //TODO: this is temporary for debugging the db
    private Button nextButton;
    private Button repeatButton;

    private TextView plusLeft1;
    private TextView plusLeft2;
    private TextView equals;
    private TextView plusRight;
    
    private TextView instructionText;
    private MediaPlayer mediaPlayer = null;
    private boolean touchActive = false;
    
    private ImageView twoFingerView;
    private ImageView oneFingerView;
    private ObjectAnimator twoFadeInAnimator;
    private ObjectAnimator twoFadeOutAnimator;
    private ObjectAnimator oneFadeInAnimator;
    private ObjectAnimator oneFadeOutAnimator;
    
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
    
    private enum ProblemMode {
        PRETEST, PRETRAINING, TRAINING, POSTTEST
    }
    
    //these variables completely define the current state of the application
    private ProblemMode currentMode;
    private int currentScreen;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //initialize db
        dbHelper = new GestureMathDataOpenHelper(this.getApplicationContext());
        db = dbHelper.getWritableDatabase();
                
        //get random student id from start screen
        studentId = getIntent().getIntExtra("studentId", -1);

        //initialize colors
        res = getResources();
        BACKGROUND_COLOR = color.darker_gray;
        HINT_COLOR = res.getColor(R.color.DodgerBlue);
        CONFIRM_COLOR = res.getColor(R.color.CadetBlue);

        //debug text
        statusText = (TextView)findViewById(R.id.debugText);
        statusText.setText("Student ID: " + String.valueOf(studentId));
        downStatus = 0;
        
        //set up buttons
        leftNum1 = (Button)findViewById(R.id.leftNum1);
        leftNum1.setBackgroundColor(BACKGROUND_COLOR);
        leftNum1.setOnTouchListener(this);

        leftNum2 = (Button)findViewById(R.id.leftNum2);
        leftNum2.setBackgroundColor(BACKGROUND_COLOR);
        leftNum2.setOnTouchListener(this);
        
        leftNum3 = (Button)findViewById(R.id.leftNum3);
        leftNum3.setBackgroundColor(BACKGROUND_COLOR);
        leftNum3.setOnTouchListener(this);
        
        rightNum = (Button)findViewById(R.id.rightNum);
        rightNum.setBackgroundColor(BACKGROUND_COLOR);
        rightNum.setOnTouchListener(this);
        
        nextButton = (Button)findViewById(R.id.nextButton);
        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setOnClickListener(this);

        repeatButton = (Button)findViewById(R.id.repeatButton);
        repeatButton.setVisibility(View.INVISIBLE);
        repeatButton.setOnClickListener(this);
        
        //set up hands and animations
        twoFingerView = (ImageView)findViewById(R.id.twoFingerView);
        twoFingerView.bringToFront();
        oneFingerView = (ImageView)findViewById(R.id.oneFingerView);
        oneFingerView.bringToFront();
        
        twoFadeInAnimator = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.anim.fade_in);
        //twoFadeInAnimator.setStartDelay(1000);
        twoFadeInAnimator.setTarget(twoFingerView);
        twoFadeInAnimator.addListener(this);
        twoFadeOutAnimator = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.anim.fade_out);
        //twoFadeOutAnimator.setStartDelay(2500);
        twoFadeOutAnimator.setTarget(twoFingerView);
        twoFadeOutAnimator.addListener(this);
        
        oneFadeInAnimator = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.anim.fade_in);
        //oneFadeInAnimator.setStartDelay(3500);
        oneFadeInAnimator.setTarget(oneFingerView);
        oneFadeInAnimator.addListener(this);
        oneFadeOutAnimator = (ObjectAnimator)AnimatorInflater.loadAnimator(this, R.anim.fade_out);
        //oneFadeOutAnimator.setStartDelay(4000);
        oneFadeOutAnimator.setTarget(oneFingerView);
        oneFadeOutAnimator.addListener(this);
        
        
        //set up equation text
        plusLeft1 = (TextView)findViewById(R.id.plusLeft1);
        plusLeft2 = (TextView)findViewById(R.id.plusLeft2);
        equals = (TextView)findViewById(R.id.equals);
        plusRight = (TextView)findViewById(R.id.plusRight);
        
        //set up input field
        inputField = (EditText)findViewById(R.id.inputField);
        inputField.setOnKeyListener(this);
        
        //set up instruction text
        instructionText = (TextView)findViewById(R.id.instructionText);
        
        //initialize to first screen of pretraining
        initializePretraining();
    }
    
    
    
    /** Called when the activity is finished by calling finish()
     *  Do cleanup or final write to db here */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
    
    
    
    public void initializeForCurrentState() {
        switch (currentMode) {
        case PRETRAINING:
            initializePretraining();
            break;
        case TRAINING:
            initializeTraining();
            break;
        }
    }
    
    
    
    public void initializePretraining() {
        currentMode = ProblemMode.PRETRAINING;
        currentScreen = 0;
        touchActive = false;
        
        //initialize problem list
        problemList = initializeProblemList(currentMode);
        currentProblemIndex = 0;
        maxProblemIndex = problemList.size()-1;
               
        //hide problem
        hideProblem();
        
        //intro text, show next button
        instructionText.setText(res.getString(R.string.pretraining_intro));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pretraining_0);
        mediaPlayer.start();
        nextButton.setVisibility(View.VISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
    }
    
    
    
    public void pretrainingInstructionScreen() {
        currentScreen = 1;
        initializeProblem(problemList.get(currentProblemIndex));
        showProblem();
        touchActive = false;
        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        unHighlightAll();
        toggleTouch(false);
        
        instructionText.setText(res.getString(R.string.pretraining_instr1));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pretraining_1);
        startMediaPlayer();
        
        //animate hands
        twoFadeInAnimator.start();  //animations chain through listener
    }
    
    
    
    public void pretrainingTrialScreen() {
        currentScreen = 2;
        showProblem();
        touchActive = true;
        
        unHighlightAll();
        toggleTouch(true);
        inputField.setOnTouchListener(this); //enable touch
        inputField.setKeyListener(null);     //disable EditText input

        instructionText.setText(res.getString(R.string.pretraining_instr2));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pretraining_2);
        mediaPlayer.start();
        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //restore UI elements to original state, clear input, etc
        inputField.setText("");
        inputField.setFocusableInTouchMode(true);
        inputField.setCursorVisible(false);
        unHighlightAll();
        downStatus = 0;
    }
    
    
    
    public void initializeTraining() {
        currentMode = ProblemMode.TRAINING;
        currentScreen = 0;
        
        //initialize problem list
        problemList = initializeProblemList(currentMode);
        currentProblemIndex = 0;
        maxProblemIndex = problemList.size()-1;
    }

    
    
    public List<Problem> initializeProblemList(ProblemMode pm) {
        
        ArrayList<Problem> pl = new ArrayList<Problem>();
        
        if (pm == ProblemMode.PRETRAINING) {
            String[] problemsStrings = {"pretraining1", "pretraining2", "pretraining3"};
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
            
        } else if (pm == ProblemMode.TRAINING) {
            String[] problemsStrings = {"training1", "training2", "training3"};
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
    
    
    
    public void toggleTouch(boolean b) {
        leftNum1.setEnabled(b);
        leftNum2.setEnabled(b);
        leftNum3.setEnabled(b);
        inputField.setEnabled(b);
        rightNum.setEnabled(b);
        leftNum1.setTextColor(Color.WHITE);
        leftNum2.setTextColor(Color.WHITE);
        leftNum3.setTextColor(Color.WHITE);
        inputField.setTextColor(Color.WHITE);
        rightNum.setTextColor(Color.WHITE);
    }
    
    
    
    public void startMediaPlayer() {
        if (mediaPlayer != null) { 
            mediaPlayer.start();
        }
    }
    
    
    
    public void releaseMediaPlayer() {
        //clean up mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    
    
    //when the Next button is pressed, move to the next problem (or action)
    public void nextScreen() {
        
        //if first problem, show problem 
        if (currentScreen == 0) {
            pretrainingInstructionScreen();
        } else if (currentScreen == 1) {
            pretrainingTrialScreen();
        } else if (currentScreen == 2) {
            //advance to next problem
            currentProblemIndex++;
            
            //out of problems, if pretraining, move to training. if training, finish
            if (currentProblemIndex > maxProblemIndex) {
                if (currentMode == ProblemMode.PRETRAINING) {
                    initializeTraining();
                    return;
                } else if (currentMode == ProblemMode.TRAINING) {
                    finish();                
                }
            }
            
            //play instructions for next problem
            pretrainingInstructionScreen();
        }
    }
    
    
    
    public void repeatScreen() {
        //based on current screen, repeat speech or reset touch
        if (currentScreen == 0) {
            startMediaPlayer();
        } else if (currentScreen == 1) {
            pretrainingInstructionScreen();
        } else if (currentScreen == 2) {
            pretrainingInstructionScreen();
        }
    }
    
    
    
    public void showProblem() {
        leftNum1.setVisibility(View.VISIBLE);
        plusLeft1.setVisibility(View.VISIBLE);
        leftNum2.setVisibility(View.VISIBLE);
        plusLeft2.setVisibility(View.VISIBLE);
        leftNum3.setVisibility(View.VISIBLE);
        equals.setVisibility(View.VISIBLE);
        inputField.setVisibility(View.VISIBLE);
        plusRight.setVisibility(View.VISIBLE);
        rightNum.setVisibility(View.VISIBLE);        
    }
    
    public void hideProblem() {
        leftNum1.setVisibility(View.INVISIBLE);
        plusLeft1.setVisibility(View.INVISIBLE);
        leftNum2.setVisibility(View.INVISIBLE);
        plusLeft2.setVisibility(View.INVISIBLE);
        leftNum3.setVisibility(View.INVISIBLE);
        equals.setVisibility(View.INVISIBLE);
        inputField.setVisibility(View.INVISIBLE);
        plusRight.setVisibility(View.INVISIBLE);
        rightNum.setVisibility(View.INVISIBLE);
    }
    
    
    
    public void unHighlightAll() {
        leftNum1.setBackgroundColor(BACKGROUND_COLOR);
        leftNum2.setBackgroundColor(BACKGROUND_COLOR);
        leftNum3.setBackgroundColor(BACKGROUND_COLOR);
        inputField.setBackgroundColor(BACKGROUND_COLOR);
    }
    
    
    
    //handle all touch events
    public boolean onTouch(View v, MotionEvent e) {
        //only if touch is active
        if (touchActive) {
            //handle actions on all buttons
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {     
                
                switch (downStatus) {
                case 0:
                    Log.i("info", "down 0");
                    if (v.getId() == R.id.leftNum1) {
//                        leftNum1.setBackgroundColor(HINT_COLOR);
//                        leftNum2.setBackgroundColor(HINT_COLOR);
                    } else { //leftNum2
//                        leftNum2.setBackgroundColor(HINT_COLOR);
//                        leftNum1.setBackgroundColor(HINT_COLOR);                    
                    }
                    downStatus = 1;
                    statusText.setText(String.valueOf(downStatus));
                    break;
                case 1:
                    Log.i("info", "down 1");
                    leftNum1.setBackgroundColor(CONFIRM_COLOR);
                    leftNum2.setBackgroundColor(CONFIRM_COLOR);
                    //inputField.setBackgroundColor(HINT_COLOR);
                    downStatus = 2;
                    statusText.setText(String.valueOf(downStatus));
                    break;
                case 2:
                    if (v.getId() == R.id.inputField) {
                        inputField.setBackgroundColor(CONFIRM_COLOR);
                        nextButton.setVisibility(View.VISIBLE);
                    }
                default:
                    Log.i("info", "down default");
                    break;
                }
            }
    
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                
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
        }
        return true;
    }
    
    
    
    //handle all key (keyboard) events
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        
        inputField.setCursorVisible(true);
        
        // If the event is a key-down event on the "enter" button
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            // Perform action on key press
            String answer = ((EditText)v).getText().toString();
            if (answer.equals("")) {
                //if blank, do nothing
            } else {
                //if answer entered, lock in answer and show next button
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
    
    
    
    //handle Next and Repeat button clicks
    public void onClick(View v) {        
        //if Next button is pressed, do appropriate action
        if (v.getId() == R.id.nextButton) {
            nextScreen();
        } else if (v.getId() == R.id.repeatButton) {
            repeatScreen();
        }
    }
    
    
    
    public void onAnimationEnd(Animator a) {
        
        if (a.equals(twoFadeInAnimator)) {
            leftNum1.setBackgroundColor(CONFIRM_COLOR);
            leftNum2.setBackgroundColor(CONFIRM_COLOR);
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            twoFadeOutAnimator.start();
        }
        
        if (a.equals(twoFadeOutAnimator)) {
            oneFadeInAnimator.start();
        }
        
        if (a.equals(oneFadeInAnimator)) {
            inputField.setBackgroundColor(CONFIRM_COLOR);
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            oneFadeOutAnimator.start();
        }
        
        if (a.equals(oneFadeOutAnimator)) {
            nextButton.setVisibility(View.VISIBLE);
        }
    }
    
    
    
    //disable back button
    @Override
    public void onBackPressed() {
        // do nothing
    }

    //unused required AnimatorListener methods
    public void onAnimationRepeat(Animation animation) {
        // TODO Auto-generated method stub
    }
    public void onAnimationStart(Animation animation) {
        // TODO Auto-generated method stub
    }
    public void onAnimationCancel(Animator animation) {
        // TODO Auto-generated method stub
        
    }
    public void onAnimationRepeat(Animator animation) {
        // TODO Auto-generated method stub
        
    }
    public void onAnimationStart(Animator animation) {
        // TODO Auto-generated method stub
        
    }
}