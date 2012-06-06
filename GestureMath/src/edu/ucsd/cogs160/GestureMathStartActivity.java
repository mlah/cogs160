package edu.ucsd.cogs160;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * GestureMathStartActivity
 * 
 * This activity handles the Start screen, which is the first screen when application
 *  starts (appropriately)
 * 
 * @author mlah
 *
 */
public class GestureMathStartActivity extends Activity implements OnClickListener {

    private int studentId;

    //interface elements
    private TextView studentIdTextView;
    private Button startButton;
    private Button optionsButton;
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
                
        studentIdTextView = (TextView)findViewById(R.id.studentIdTextView);
        initializeStudentId();
        
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
        
        optionsButton = (Button)findViewById(R.id.optionsButton); 
        optionsButton.setOnClickListener(this);
    }
    
    
    
    /** 
     * Called when returning to this activity.
     * This occurs when the experiment concludes and we call finish() on GestureMathProblemFlowActivity  
     */
    @Override
    protected void onResume() {
        super.onResume();
        initializeStudentId();
    }
    
    
    
    /**
     * Initialize the unique student ID for this run of the experiment
     */
    private void initializeStudentId() {
        if (studentIdTextView == null) {
            //TODO: generate error?
            Log.e("error!", "somehow we have come to initializeStudentId() without having called onCreate()");
        }
        //generate random student id
        Random r = new Random();
        //make sure student ID is unique (not in db already)
        GestureMathDataOpenHelper dbHelper = new GestureMathDataOpenHelper(this.getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        do {
            studentId = r.nextInt(99999)+1; //5 digit, no zero
        } while (studentId < 10000 || dbHelper.doesStudentIdExist(db, studentId));
        studentIdTextView.setText("Student ID: " + studentId);
        db.close();
    }

    
    
    /**
     * Respond to button presses (clicks)
     */
    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            Intent intent = new Intent(this, GestureMathProblemFlowActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        } else if (v.getId() == R.id.optionsButton) { 
            Intent intent = new Intent(this, GestureMathOptionsActivity.class);
            startActivity(intent);
        }
    }

}
