package edu.ucsd.cogs160;

import android.R.color;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GestureMathActivity extends Activity implements OnTouchListener {
    private TextView statusText;
    private Button leftNum1;
    private Button leftNum2;
    private Button leftNum3;
    private Button rightNum;
    private EditText inputField;
    private int downStatus;
    
    private static int BACKGROUND_COLOR;
    private static int HINT_COLOR;
    private static int CONFIRM_COLOR;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //define colors
        Resources res = getResources();
        BACKGROUND_COLOR = color.darker_gray;
        HINT_COLOR = res.getColor(R.color.GreenYellow);
        CONFIRM_COLOR = res.getColor(R.color.ForestGreen);

        statusText = (TextView)findViewById(R.id.textView);
        downStatus = 0;

        leftNum1 = (Button)findViewById(R.id.leftNum1);
        leftNum1.setBackgroundColor(BACKGROUND_COLOR);
        leftNum1.setOnTouchListener(this);
        //Log.i("info", String.valueOf(leftNum1.getAlpha()));

        leftNum2 = (Button)findViewById(R.id.leftNum2);
        leftNum2.setBackgroundColor(BACKGROUND_COLOR);
        leftNum2.setOnTouchListener(this);
        
        leftNum3 = (Button)findViewById(R.id.leftNum3);
        leftNum3.setBackgroundColor(BACKGROUND_COLOR);
        //leftNum3.setOnTouchListener(this);
        
        rightNum = (Button)findViewById(R.id.rightNum);
        rightNum.setBackgroundColor(BACKGROUND_COLOR);
        
        inputField = (EditText)findViewById(R.id.inputField);
        inputField.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String answer = ((EditText)v).getText().toString();
                    if (answer.equals("5")) {
                        statusText.setText("Good job!");
                    } else if (answer.equals("")) {
                        //if blank, do nothing
                    } else {
                        statusText.setText("Nice try");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public boolean onTouch(View v, MotionEvent e) {
        //    	myText.setText(String.valueOf(e.getActionMasked()));

        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {     
            
            switch (downStatus) {
            case 0:
                Log.i("info", "down 0");
                if (v.getId() == R.id.leftNum1) {
                    leftNum1.setBackgroundColor(CONFIRM_COLOR);
                    leftNum2.setBackgroundColor(HINT_COLOR);
                } else { //leftNum2
                    leftNum2.setBackgroundColor(CONFIRM_COLOR);
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
            
            switch (downStatus) {
            case 2:
                Log.i("info", "down 0");
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
}