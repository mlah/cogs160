package edu.ucsd.cogs160;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;

public class GestureMathOptionsActivity extends Activity implements OnClickListener {
    
    private Switch option1;
    private Switch option2;
    private Switch option3;
    
    private Button clearDbButton;
    private Button saveButton;
    
    private GestureMathDataOpenHelper dbHelper;
    private SQLiteDatabase db;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        //initialize db connection
        dbHelper = new GestureMathDataOpenHelper(this.getApplicationContext());
        db = dbHelper.getWritableDatabase();
        
        //TODO: get options
        //TODO: set status of switches to correct states
        
        clearDbButton = (Button)findViewById(R.id.clearDbButton);
        clearDbButton.setOnClickListener(this);
        
        option1 = (Switch)findViewById(R.id.switch1);
        option2 = (Switch)findViewById(R.id.switch2);
        option3 = (Switch)findViewById(R.id.switch3);
        //option1.setChecked(true);
        
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
        

    }

    public void onClick(View v) {
        if (v.getId() == R.id.clearDbButton) {
            dbHelper.deleteAllSolvedProblems(db);
        }
        
        if (v.getId() == R.id.saveButton) {
            //TODO: save status of options to db
            //TODO: close db connection
            //option1.isChecked();
            
            //return to start screen
            finish();
        }
    }
}
