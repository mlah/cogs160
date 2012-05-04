package edu.ucsd.cogs160;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GestureMathStartActivity extends Activity implements OnClickListener {

    private int studentId;
    
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
    
    /** Called when returning to this activity. */
    @Override
    protected void onResume() {
        super.onResume();
        initializeStudentId();
    }
    
    private void initializeStudentId() {
        if (studentIdTextView == null) {
            //TODO: generate error?
            Log.e("error!", "somehow we have come to initializeStudentId() without having called onCreate()");
        }
        //generate random student id
        Random r = new Random();
        studentId = r.nextInt(999999999)+1; //9 digit, no zero
        studentIdTextView.setText("Student ID: " + studentId);        
    }

    public void onClick(View v) {
        if (v.getId() == R.id.startButton) {
            Intent intent = new Intent(this, GestureMathProblemFlowActivity.class);
            intent.putExtra("studentId", studentId);
            startActivity(intent);
        }
        
        if (v.getId() == R.id.optionsButton) { 
            Intent intent = new Intent(this, GestureMathOptionsActivity.class);
            startActivity(intent);
        }
    }

}
