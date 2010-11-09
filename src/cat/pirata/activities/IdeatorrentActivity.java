package cat.pirata.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class IdeatorrentActivity extends Activity {
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("En breu...");
        setContentView(textview);
    }
}
