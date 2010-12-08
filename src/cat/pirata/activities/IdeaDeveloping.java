package cat.pirata.activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cat.pirata.R;
import cat.pirata.utils.DbHelper;

public class IdeaDeveloping extends Activity {
	
	private DbHelper db;
	private LinearLayout ll;
	private boolean isAuth = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.idea);
		ll = (LinearLayout) findViewById(R.id.ll);
		
		db = new DbHelper(getBaseContext());
	}

	@Override
	protected void onResume() {
		super.onResume();
		createList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}
	
	
	public void voteUp (View view) {
		if (!isAuth) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		cleanUpEqualDown(view);
		((ImageButton) view).setImageResource(R.drawable.idea_up_color);
	}
	
	public void voteEqual (View view) {
		if (!isAuth) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		cleanUpEqualDown(view);
		((ImageButton) view).setImageResource(R.drawable.idea_equal_color);
	}
	
	public void voteDown (View view) {
		if (!isAuth) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		cleanUpEqualDown(view);
		((ImageButton) view).setImageResource(R.drawable.idea_down_color);
	}
	
	private void cleanUpEqualDown (View view) {
		View child = getLayoutInflater().inflate(R.layout.idea_row_solucio, (ViewGroup) view.getParent());
		((ImageButton) child.findViewById(R.id.icon_up)).setImageResource(R.drawable.idea_up_grey);
		((ImageButton) child.findViewById(R.id.icon_eq)).setImageResource(R.drawable.idea_equal_grey);
		((ImageButton) child.findViewById(R.id.icon_down)).setImageResource(R.drawable.idea_down_grey);
	}
	
	
	private void createList() {
		ll.removeAllViews();
		Cursor crProp = db.getPropostes("developing");
		
		if (crProp.moveToFirst()) {
			do {
				addViewProposta(crProp.getInt(crProp.getColumnIndex("pid")),
						crProp.getInt(crProp.getColumnIndex("pubDate")),
						crProp.getString(crProp.getColumnIndex("title")),
						crProp.getString(crProp.getColumnIndex("description")));
				
				Cursor crSol = db.getSolucions(crProp.getInt(crProp.getColumnIndex("pid")));
				
				if (!crSol.moveToFirst()) {
					crSol.close();
					continue;
				}
				
				do {
					addViewSolucio(crSol.getInt(crSol.getColumnIndex("pid")),
							crSol.getInt(crSol.getColumnIndex("sid")),
							crSol.getInt(crSol.getColumnIndex("votes")),
							crSol.getString(crSol.getColumnIndex("title")),
							crSol.getString(crSol.getColumnIndex("description")));
				} while (crSol.moveToNext());
				crSol.close();
			} while (crProp.moveToNext());
		}
		crProp.close();
	}

	private void addViewSolucio(int pid, int sid, int votes, String title, String description) {
		
		View child = getLayoutInflater().inflate(R.layout.idea_row_solucio_novote, null);
		TextView tv;
		
		tv = (TextView) child.findViewById(R.id.pid);
		tv.setText(String.valueOf(pid));

		tv = (TextView) child.findViewById(R.id.sid);
		tv.setText(String.valueOf(sid));
		
		tv = (TextView) child.findViewById(R.id.votes);
		tv.setText(String.valueOf(votes));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(String.valueOf(title));
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(String.valueOf(description));
		
		ll.addView(child);
	}

	private void addViewProposta(int pid, int pubDate, String title, String description) {

		View child = getLayoutInflater().inflate(R.layout.idea_row_proposta, null);
		TextView tv;
		
		tv = (TextView) child.findViewById(R.id.pid);
		tv.setText(String.valueOf(pid));
		
		tv = (TextView) child.findViewById(R.id.pubDate);
		tv.setText(String.format("%s %s", day(pubDate), calMonth(month(pubDate))));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(String.valueOf(title));
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(String.valueOf(description));
		
		ll.addView(child);
	}
	
	private int day (int what) {
		what -= 1244160000; // 40years
		for (int i = 24*3600; what >= i; what -= i);
		return what % 30;
	}
	
	private int month (int what) {
		what -= 1244160000; // 40years
		for (int i = 30*24*3600; what >= i; what -= i);
		return what % 12;
	}
	private String calMonth(int i) {
		String[] day = new String[] {
				"Gen","Feb","Mar","Abr","Mai","Jun","Jul","Ago","Set","Oct","Nov","Dec"};
		return day[i];
	}
}
