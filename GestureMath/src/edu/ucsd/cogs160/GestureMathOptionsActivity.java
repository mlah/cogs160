package edu.ucsd.cogs160;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

public class GestureMathOptionsActivity extends Activity implements OnClickListener {
    
    private RadioGroup gestureCondtionRadioGroup;
    private int[] radioIdIndex = {R.id.radioGesture, R.id.radioHighlight, R.id.radioNonGesture};
    
    private Button clearDbButton;
    private Button saveButton;
    
    private TextView dbDebugView;
    
    private GestureMathDataOpenHelper dbHelper;
    private SQLiteDatabase db;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);
        
        //TODO: this is a hack to delete the database (useful when changing the schema)
        //this.deleteDatabase("gesture_math");

        //initialize db connection
        dbHelper = new GestureMathDataOpenHelper(this.getApplicationContext());
        db = dbHelper.getWritableDatabase();
        
        clearDbButton = (Button)findViewById(R.id.clearDbButton);
        clearDbButton.setOnClickListener(this);
        
        //get options state from db
        gestureCondtionRadioGroup = (RadioGroup)findViewById(R.id.radioGestureCondition);
        int option = dbHelper.getOptions(db);
        gestureCondtionRadioGroup.check(radioIdIndex[option]);
        
        dbDebugView = (TextView)findViewById(R.id.dbDebugView);
        dbDebugView.setText(dbHelper.getSolvedProblems(db));
        
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);
    }
    
    
    
    /** Called when the activity is finished by calling finish()
     *  Do cleanup or final write to db here */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        GestureMathDataOpenHelper.backupDb();
    }

    
    
    public void onClick(View v) {
        if (v.getId() == R.id.clearDbButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to clear the database?")
                   .setCancelable(false)
                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dbHelper.deleteAllSolvedProblems(db);
                           dbDebugView.setText(dbHelper.getSolvedProblems(db));
                       }
                   })
                   .setNegativeButton("No", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.cancel();
                       }
                   });
            AlertDialog alert = builder.create();
            alert.show();
        }
        
        if (v.getId() == R.id.saveButton) {
            //save options state to db
            int checked = gestureCondtionRadioGroup.getCheckedRadioButtonId();
            View child = gestureCondtionRadioGroup.findViewById(checked);
            int index = gestureCondtionRadioGroup.indexOfChild(child);
            dbHelper.updateOptions(db, index);
            
            //return to start screen
            finish();
        }
    }
}
