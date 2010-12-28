package cat.pirata.activities;

import java.util.Calendar;
import java.util.SortedSet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cat.pirata.extra.CtrlDb;
import cat.pirata.extra.CtrlJson;

public class Informacio extends Activity {
	
	private LinearLayout ll;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("", "onCreate");
		setContentView(R.layout.linearlayout);
		ll = (LinearLayout) findViewById(R.id.Root);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("", "onResume");
		
		ll.removeAllViews();
		SortedSet<Bundle> ss = CtrlJson.getInstance().getRSS();

		for (Bundle b : ss) {
			View child = getLayoutInflater().inflate(R.layout.info_row, null, false);
			child.setTag(b.getString("link"));

			ImageView iv = (ImageView) child.findViewById(R.id.icon);
			int icon = CtrlDb.getInstance().getIcon( b.getString("id") );
			if (icon == -1) { icon = R.drawable.icon_error; }
			iv.setImageResource( icon );
			
			TextView tv;
			
			tv = (TextView) child.findViewById(R.id.date);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(Long.valueOf(b.getString("pubDate").concat("000")));
			tv.setText(String.format("%02d %s", cal.get(Calendar.DATE), calMonth(cal.get(Calendar.MONTH))));

			tv = (TextView) child.findViewById(R.id.text);
			tv.setText( b.getString("title") );
			
			child.setClickable(true);
			child.setOnClickListener(new openUrlListener());
			
			ll.addView(child);
		}
	}	

	private String calMonth(int i) {
		String[] day = new String[] {
				"Gen","Feb","Mar","Abr","Mai","Jun","Jul","Ago","Set","Oct","Nov","Dec"};
		return day[i];
	}
	
	class openUrlListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			view.setBackgroundColor(Color.YELLOW);
			String link = (String) view.getTag();
			if (link.startsWith("http")) {
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		}
	}
}
