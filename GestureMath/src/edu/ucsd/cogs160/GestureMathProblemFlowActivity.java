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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.DigitsKeyListener;
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
                                            implements OnTouchListener, OnKeyListener, OnClickListener, 
                                                       AnimatorListener, OnCompletionListener {
    private TextView statusText;
    private Button leftNum1;
    private Button leftNum2;
    private Button leftNum3;
    private Button rightNum;
    private EditText inputField;
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
    private TransitionDrawable highlightTransition1;
    private TransitionDrawable highlightTransition2;
    private TransitionDrawable highlightTransition3;
    
    private Resources res;
    private int BACKGROUND_COLOR;
    private int HINT_COLOR;
    private int CONFIRM_COLOR;
    
    private GestureMathDataOpenHelper dbHelper;
    private SQLiteDatabase db;
    
    private int gestureCondition;
    //TODO: this enum doesn't seem to work, as putting GestureConditionCode.GESTURE.getIndex() into the case
    // statement causes an error (case statements must hold a constant value)
//    private enum GestureConditionCode {
//        GESTURE (0), HIGHLIGHT (1), NON_GESTURE (2);
//        private final int index;
//        private GestureConditionCode (int index) {
//            this.index = index;
//        }
//        public int index() { return index; } 
//    }
    
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
        
        //get options
        gestureCondition = dbHelper.getOptions(db);
                
        //get random student id from start screen
        studentId = getIntent().getIntExtra("studentId", -1);

        //initialize colors
        res = getResources();
        BACKGROUND_COLOR = color.darker_gray;
        HINT_COLOR = res.getColor(R.color.DodgerBlue); //TODO: unused?
        CONFIRM_COLOR = res.getColor(R.color.holo_blue_dark);

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
        
        //set up equation text
        plusLeft1 = (TextView)findViewById(R.id.plusLeft1);
        plusLeft2 = (TextView)findViewById(R.id.plusLeft2);
        equals = (TextView)findViewById(R.id.equals);
        plusRight = (TextView)findViewById(R.id.plusRight);
        
        //set up input field
        inputField = (EditText)findViewById(R.id.inputField);
        //inputField.setBackgroundColor(BACKGROUND_COLOR);
        inputField.setBackgroundResource(R.drawable.input_underline);
        inputField.setOnKeyListener(this);
        
        //set up navigation buttons
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
        
        //set up highlight animations
        //it seems like each View needs its own TransitionDrawable object
        ColorDrawable layers[] = new ColorDrawable[2]; 
        layers[0] = new ColorDrawable(BACKGROUND_COLOR); 
        layers[1] = new ColorDrawable(CONFIRM_COLOR); 
        highlightTransition1 = new TransitionDrawable(layers);
        highlightTransition1.setCrossFadeEnabled(true);
        highlightTransition2 = new TransitionDrawable(layers);
        highlightTransition2.setCrossFadeEnabled(true);
        highlightTransition3 = new TransitionDrawable(layers);
        highlightTransition3.setCrossFadeEnabled(true);
//        highlightTransition.setId(0, 0);
//        highlightTransition.setId(1, 1);
//        highlightTransition.setDrawableByLayerId(0, highlightTransition.getDrawable(1)); 
//        highlightTransition.setDrawableByLayerId(1, new ColorDrawable(CONFIRM_COLOR));
        
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
        GestureMathDataOpenHelper.backupDb();
    }
    
    
    public void initializeProblemList(ProblemMode pm) {
        
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
            //alternate experimenter and student problems (total must be even)
            String[] problemsStrings = {"training_exp1", "training_stu1", 
                                        "training_exp2", "training_stu2", 
                                        "training_exp3", "training_stu3", 
                                        "training_exp4", "training_stu4", 
                                        "training_exp5", "training_stu5", 
                                        "training_exp6", "training_stu6" };
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
        
        currentProblemIndex = 0;
        maxProblemIndex = pl.size()-1;
        problemList = pl;
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
    
    
    
    //PRETRAINING 0
    public void initializePretraining() {
        currentMode = ProblemMode.PRETRAINING;
        currentScreen = 0;

        initializeProblemList(currentMode);
        
        //set up problem state
        hideProblem();
        touchActive = false;
        
        nextButton.setVisibility(View.VISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //set up text and play voice
        switch (gestureCondition) {
        case 0: //gesture
            instructionText.setText(res.getString(R.string.pretraining_intro));
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pretraining_0);
            mediaPlayer.start();
            break;
        case 1: //highlight
            //same text and speech as non-gesture
        case 2: //non-gesture
            instructionText.setText(res.getString(R.string.pretraining_intro_ng));
            releaseMediaPlayer();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pretraining_0_ng);
            mediaPlayer.start();
            break;
        }
    }
    
    
    //PRETRAINING 1
    public void pretrainingInstructionScreen() {
        currentScreen = 1;
        
        initializeProblem(problemList.get(currentProblemIndex));
        
        //set up problem state
        showProblem();
        unHighlightAll();
        touchActive = false;
        toggleTouch(false);
        
        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //set up text and play voice
        instructionText.setText(res.getString(R.string.pretraining_instr1));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pretraining_1);
        mediaPlayer.setOnCompletionListener(this);
        startMediaPlayer();
        
        switch (gestureCondition) {
        case 0: //gesture
            //animate hands
            twoFadeInAnimator.start();  //animations chain through listener
            break;
        case 1: //highlight
            //animate highlight
            animateHighlight();
            break;
        case 2: //non-gesture
            break;
        }
    }
    
    
    //PRETRAINING 2
    public void pretrainingTrialScreen(int text, int voice) {
        currentScreen = 2;
        
        initializeProblem(problemList.get(currentProblemIndex));
        
        //set up problem state
        showProblem();
        unHighlightAll();
        touchActive = true;
        toggleTouch(true);
        inputField.setOnTouchListener(this); //enable touch
        inputField.setKeyListener(null);     //disable EditText input

        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //set up text and play voice
        instructionText.setText(res.getString(text));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), voice);
        if (gestureCondition == 1 || gestureCondition == 2) { //highlight and non-gesture
            //Next button appears after speech completes, because no gesture is done
            mediaPlayer.setOnCompletionListener(this);
        }
        startMediaPlayer();
        
        //restore UI elements to original state, clear input, etc
        inputField.setText("");
        inputField.setFocusableInTouchMode(true);
        inputField.setCursorVisible(false);
        unHighlightAll();
        downStatus = 0;
        
        if (gestureCondition == 1 || gestureCondition == 2) { //highlight and non-gesture
            toggleTouch(false);
            touchActive = false;
        }
    }
    
    
    //TRAINING 0
    public void initializeTraining() {
        currentMode = ProblemMode.TRAINING;
        currentScreen = 0;
        
        initializeProblemList(currentMode);
        
        //set up problem state
        hideProblem();
        touchActive = false;
        
        nextButton.setVisibility(View.VISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //set up text and play voice
        instructionText.setText(res.getString(R.string.training_intro));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.training_0);
        mediaPlayer.start();

    }
    
    
    //TRAINING 1
    public void trainingSolutionScreen() {
        currentScreen = 1;
        
        initializeProblem(problemList.get(currentProblemIndex));
        
        //set up problem state
        showProblem();
        inputField.setText(String.valueOf(problemList.get(currentProblemIndex).solution)); //show problem solution
        unHighlightAll();
        touchActive = false;
        toggleTouch(false);
        
        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //set up text and play voice
        //TODO: is adding currentProblemIndex here okay? are the values in R.java guaranteed to be sequential?
        instructionText.setText(res.getString(R.string.training_instr_base) + "\n" +
                                res.getString(R.string.training_instr_1 + currentProblemIndex/2) + "\n" +
                                res.getString(R.string.training_instr_post));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.training_1_1 + currentProblemIndex/2);
        mediaPlayer.setOnCompletionListener(this);
        startMediaPlayer();
    }
    
    
    //TRAINING 2
    public void trainingGesturePreScreen() {
        //interaction is the same as pretrainingTrialScreen, 
        // but make sure to set the currentScreen correctly for training
        switch (gestureCondition) {
        case 0: //gesture
            pretrainingTrialScreen(R.string.training_gesture, R.raw.training_2);
            break;
        case 1: //highlight
            //same text and speech as non-gesture
        case 2: //non-gesture
            pretrainingTrialScreen(R.string.training_no_gesture, R.raw.training_2_ng);
            break;
        }
        currentScreen = 2;
    }
    
    
    //TRAINING 2A (5)
    public void trainingGesturePreInstructionScreen() {
        //interaction is the same as pretrainingInstructionScreen, 
        // but make sure to set the currentScreen correctly for training
        pretrainingInstructionScreen();
        currentScreen = 5;
    }
    
    
    //TRAINING 3
    public void trainingAnswerScreen() {
        currentScreen = 3;
        
        //set up problem state
        showProblem();
        unHighlightAll();
        //touchActive = true;
        toggleTouch(true);
        inputField.setOnTouchListener(null);                 //disable touch
        inputField.setKeyListener(new DigitsKeyListener());  //enable EditText input
        inputField.setText("");
        inputField.setFocusableInTouchMode(true);
        inputField.setCursorVisible(false);
        
        nextButton.setVisibility(View.INVISIBLE);
        repeatButton.setVisibility(View.VISIBLE);
        
        //set up text and play voice
        instructionText.setText(res.getString(R.string.training_enter_answer));
        releaseMediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.training_3);
        mediaPlayer.start();
    }
    
    
    //TRAINING 4
    public void trainingGesturePostScreen() {
        //interaction is the same as pretrainingTrialScreen, 
        // but make sure to set the currentScreen correctly for training
        // slightly different text
        switch (gestureCondition) {
        case 0: //gesture
            pretrainingTrialScreen(R.string.training_repeat_gesture, R.raw.training_4);
            break;
        case 1: //highlight
            //same text and speech as non-gesture
        case 2: //non-gesture
            pretrainingTrialScreen(R.string.training_repeat_no_gesture, R.raw.training_4_ng);
            break;
        }
        currentScreen = 4;   
    }
    
    
    //TRAINING 4A (6)
    public void trainingGesturePostInstructionScreen() {
        //interaction is the same as pretrainingInstructionScreen, 
        // but make sure to set the currentScreen correctly for training
        pretrainingInstructionScreen();
        currentScreen = 6;
    }
    
    
    
    //DONE WITH EXPERIMENT
    public void doneScreen() {
        currentScreen = -1;
        hideProblem();
        
        instructionText.setText(res.getString(R.string.done_message));
        
        nextButton.setText("Done");
        nextButton.setVisibility(View.VISIBLE);
        repeatButton.setVisibility(View.INVISIBLE);        
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
    
    
    public void animateHighlight() {
        Handler handler = new Handler(); 
        handler.postDelayed(new Runnable() { 
             public void run() {
                 leftNum1.setBackgroundDrawable(highlightTransition1);
                 leftNum2.setBackgroundDrawable(highlightTransition2);
                 highlightTransition1.startTransition(1000);
                 highlightTransition2.startTransition(1000);
             } 
        }, 1000);        

        handler.postDelayed(new Runnable() { 
             public void run() {
                 inputField.setBackgroundDrawable(highlightTransition3);
                 highlightTransition3.startTransition(1000);
             } 
        }, 4000);
    }
    
    
    //when the Next button is pressed, move to the next problem (or action)
    public void nextScreen() {
        
        if (currentMode == ProblemMode.PRETRAINING) {
            switch (currentScreen) {
            case 0:
                //if first problem, show problem 
                pretrainingInstructionScreen();
                break;
            case 1:
                switch (gestureCondition) {
                case 0: //gesture
                    pretrainingTrialScreen(R.string.pretraining_instr2, R.raw.pretraining_2);
                    break;
                case 1: //highlight
                    //same text and speech as non-gesture
                case 2: //non-gesture
                    pretrainingTrialScreen(R.string.pretraining_instr2_ng, R.raw.pretraining_2_ng);
                    break;
                }
                break;
            case 2:                
                if (currentProblemIndex >= maxProblemIndex) {
                    //out of problems, if pretraining, move to training
                    initializeTraining();
                    return;
                }
                
                //advance to next problem
                currentProblemIndex++;
                
                //play instructions for next problem
                pretrainingInstructionScreen();
                break;
            }
        } else if (currentMode == ProblemMode.TRAINING) {
            switch (currentScreen) {
            case 0:
                //if first problem, show problem 
                trainingSolutionScreen();
                break;
            case 1:
                currentProblemIndex++;  //move from experimenter problem to student problem
                trainingGesturePreScreen();
                break;
            case 2:
                trainingAnswerScreen();
                break;
            case 3:
                trainingGesturePostScreen();
                break;
            case 4:
                //out of problems, finish
                if (currentProblemIndex >= maxProblemIndex) {
                    doneScreen();
                    return;
                }
                
                //advance to next problem
                currentProblemIndex++;
                
                //play instructions for next problem
                trainingSolutionScreen();
                break;
            case 5:
                trainingGesturePreScreen();
                break;
            case 6:
                trainingGesturePostScreen();
                break;
            case -1:
                nextButton.setText("Next >");
                finish();
                break;
            }
        }
    }
    
    
    
    public void repeatScreen() {
        //based on current screen, repeat speech or reset touch
        if (currentMode == ProblemMode.PRETRAINING) {
            switch (currentScreen) {
            case 0:
                startMediaPlayer();
                break;
            case 1:
                pretrainingInstructionScreen();
                break;
            case 2:
                pretrainingInstructionScreen();
                break;
            }
        } else if (currentMode == ProblemMode.TRAINING) {
            switch (currentScreen) {
            case 0:
                startMediaPlayer();
                break;
            case 1:
                trainingSolutionScreen();
                break;
            case 2:
                trainingGesturePreInstructionScreen();
                break;
            case 3:
                trainingAnswerScreen();
                break;
            case 4:
                trainingGesturePostInstructionScreen();
                break;
            case 5:
                trainingGesturePreInstructionScreen();
                break;
            case 6:
                trainingGesturePostInstructionScreen();
                break;
            }
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
        
        leftNum1.setTextColor(Color.WHITE);
        leftNum2.setTextColor(Color.WHITE);
        leftNum3.setTextColor(Color.WHITE);
        inputField.setTextColor(Color.WHITE);
        rightNum.setTextColor(Color.WHITE);
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
        //inputField.setBackgroundColor(BACKGROUND_COLOR);
        inputField.setBackgroundResource(R.drawable.input_underline);
    }
    
    
    
    //handle all touch events
    public boolean onTouch(View v, MotionEvent e) {
        //only if touch is active
        if (touchActive) {
            //handle actions on all buttons
            if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {     
                
                switch (downStatus) {
                case 0:
                    Log.i("down", String.valueOf(downStatus));
                    if (v.getId() == R.id.leftNum1 || v.getId() == R.id.leftNum2) {
                        downStatus = 1;
                    }
                    break;
                case 1:
                    Log.i("down", String.valueOf(downStatus));
                    if (v.getId() == R.id.leftNum1 || v.getId() == R.id.leftNum2) {
                        leftNum1.setBackgroundColor(CONFIRM_COLOR);
                        leftNum2.setBackgroundColor(CONFIRM_COLOR);
                        //inputField.setBackgroundColor(HINT_COLOR);
                        downStatus = 2;
                    }
                    break;
                case 2:
                    Log.i("down", String.valueOf(downStatus));
                    if (v.getId() == R.id.inputField) {
                        inputField.setBackgroundColor(CONFIRM_COLOR);
                        nextButton.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    Log.i("info", "down default");
                    break;
                }
            }
    
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                
                switch (downStatus) {
                case 2:
                    Log.i("up", String.valueOf(downStatus));
                    //do nothing
                    break;
                case 1:
                    Log.i("up", String.valueOf(downStatus));
                    leftNum1.setBackgroundColor(BACKGROUND_COLOR);
                    leftNum2.setBackgroundColor(BACKGROUND_COLOR);
                    downStatus = 0;
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
                if (answer.equals(currentSolution)) {
                    //right answer
                    dbHelper.addSolvedProblem(db, studentId, gestureCondition, currentProblemId, 1);
                } else {
                    //wrong answer
                    dbHelper.addSolvedProblem(db, studentId, gestureCondition, currentProblemId, 0);
                }
                //show next screen button, disable input, change input field color to confirmed
                nextButton.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputField.getWindowToken(), 0);
                inputField.setCursorVisible(false);
                inputField.setFocusableInTouchMode(false);
                //inputField.setBackgroundColor(CONFIRM_COLOR);
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
        
        //chaining hands animations
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
            //do nothing
        }
    }
    
    
    
    public void onCompletion(MediaPlayer mp) {
        nextButton.setVisibility(View.VISIBLE);
//        if (currentMode == ProblemMode.TRAINING) {
//            if (currentScreen == 1) {
//                nextButton.setVisibility(View.VISIBLE);
//            }
//        }
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