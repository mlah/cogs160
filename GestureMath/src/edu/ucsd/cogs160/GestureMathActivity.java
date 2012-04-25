package edu.ucsd.cogs160;

import android.R.color;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class GestureMathActivity extends Activity implements OnTouchListener {
    private TextView myText;
    private Button button1;
    private Button button2;
    private int numDown;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        myText = (TextView)findViewById(R.id.textView);
        numDown = 0;

        button1 = (Button)findViewById(R.id.button1);
        button1.setBackgroundColor(color.darker_gray);
        button1.setOnTouchListener(this);

        button2 = (Button)findViewById(R.id.button2);
        button2.setBackgroundColor(color.darker_gray);
        button2.setOnTouchListener(this);
    }

    public boolean onTouch(View v, MotionEvent e) {
        //    	myText.setText(String.valueOf(e.getActionMasked()));

        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            ((Button)v).setBackgroundColor(Color.RED);
            
            switch (numDown) {
            case 0:
                numDown = 1;
                myText.setText(String.valueOf(numDown));
                break;
            case 1:
                numDown = 2;
                myText.setText(String.valueOf(numDown));
                button1.setBackgroundColor(Color.GREEN);
                button2.setBackgroundColor(Color.GREEN);
                break;
            default:
                break;
            }
        }

        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            ((Button)v).setBackgroundColor(color.darker_gray);
            
            switch (numDown) {
            case 2:
                numDown = 1;
                myText.setText(String.valueOf(numDown));
                if (v.getId() == R.id.button1)
                    button2.setBackgroundColor(Color.RED);
                else
                    button1.setBackgroundColor(Color.RED);  
                break;
            case 1:
                numDown = 0;
                myText.setText(String.valueOf(numDown));                
                break;
            default:
                break;
            }
        }

        return true;
    }
}