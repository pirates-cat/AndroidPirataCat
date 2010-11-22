package cat.pirata.activities;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import cat.pirata.R;
import cat.pirata.utils.DbHelper;
import cat.pirata.utils.RSS;


public class InfoActivity extends ListActivity {

	public static final int numLastNews = 50;

	private RSS rss;
	private Handler hr;
	private DbHelper db;
	private Cursor cr;

	private Dialog dialog;


	// -FLOW-

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("", "onCreate");
		db = new DbHelper(getBaseContext());
		rss = new RSS(db);
		cr = rss.getLastNews(numLastNews);
		hr = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				setListAdapter(new BlocAdapter());
				ProgressBar pb = (ProgressBar) getParent().findViewById(R.id.progressbar);
				pb.setVisibility(View.INVISIBLE);
				Log.d("", "--FIN--");
			}
		};
		setListAdapter(new BlocAdapter());
		getListView().setOnItemClickListener(new openUrlListener());
		firstTimeDialog();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d("", "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("", "onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("", "onResume");

		Thread background = new Thread(new Runnable() {
			public void run() {
				try {
					rss.refreshLastNews();
					cr.requery();
					rss.clearOldNews();
					hr.sendMessage(hr.obtainMessage());
				} catch (IllegalStateException e) {
					// race condition !!
					return;
				}
			}
		});
		background.start();
		ProgressBar pb = (ProgressBar) getParent().findViewById(R.id.progressbar);
		pb.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("", "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("", "onStop");
	}

	@Override
	protected void onDestroy() {
		Log.d("", "onDestroy");
		super.onDestroy();
		cr.close();
		db.close();
	}

	// -MENUS-

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.informacio, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.config:
			configDialog();
			return true;
		case R.id.quit:
			quitDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void firstTimeDialog() {
		if (db.isFirstTime()) {
			dialog = new Dialog(getParent());
			dialog.setContentView(R.layout.info_dialog_firsttime);
			dialog.setTitle(R.string.quisom);
			Button button = (Button) dialog.findViewById(R.id.entrar);
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) { dialog.cancel(); }
			});
			dialog.show();
		}
	}

	private void configDialog() {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.info_dialog_config);
		dialog.setTitle("@string/selectrss");

		Cursor cur = db.getRssAll();
		do {
			int id = cur.getInt(cur.getColumnIndex("id"));
			String name = cur.getString(cur.getColumnIndex("name"));
			int enabled = cur.getInt(cur.getColumnIndex("enabled"));

			RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio);
			CheckBox cb = new CheckBox(this);
			cb.setId(id);
			cb.setText(name);
			if (enabled==1) cb.setChecked(true);
			rg.addView(cb);

		} while (cur.moveToNext());
		cur.close();

		Button button = (Button) dialog.findViewById(R.id.load);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
				RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio);
				for (int i = 0; i<rg.getChildCount(); i++) {
					CheckBox cb = (CheckBox) rg.getChildAt(i);
					int enabled = (cb.isChecked()) ? 1 : 0;
					db.updateFieldFromRSS(cb.getId(), "enabled", enabled);
				}
				cr.requery();
				getListView().invalidateViews();
			}
		});

		dialog.show();
	}

	private void quitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("@string/segursortir")
		.setCancelable(false)
		.setPositiveButton("@string/si", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//db.resetAllData();
				InfoActivity.this.finish();
			}
		})
		.setNegativeButton("@string/no", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	// -CLASS-

	class openUrlListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
			cr.moveToPosition(position);
			String url = cr.getString(cr.getColumnIndex("followUrl"));
			if (url.startsWith("http")) {
				Uri uri = Uri.parse(url);
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		}
	}


	class BlocAdapter extends ArrayAdapter<String> {
		BlocAdapter() { super(InfoActivity.this, R.layout.info_row); }

		@Override
		public int getCount() { return cr.getCount(); }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View row = getLayoutInflater().inflate(R.layout.info_row, parent, false);
			cr.moveToPosition(position);

			ImageView iv = (ImageView)row.findViewById(R.id.icon);
			int icon = db.getIcon( cr.getInt(cr.getColumnIndex("id")) );
			if (icon == -1) { icon = R.drawable.icon_error; }
			iv.setImageResource( icon );

			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date( cr.getLong(cr.getColumnIndex("lastAccess")) ));

			TextView tv1 = ((TextView)row.findViewById(R.id.date));
			tv1.setText(String.format("%d %s", cal.get(Calendar.DATE), calMonth(cal.get(Calendar.MONTH))));

			TextView tv2 = ((TextView)row.findViewById(R.id.text));
			tv2.setText(cr.getString(cr.getColumnIndex("body")));

			return row;
		}

		private String calMonth(int i) {
			String[] day = new String[] {
					"Gen","Feb","Mar","Abr","Mai","Jun","Jul","Ago","Set","Oct","Nov","Dec"};
			return day[i];
		}
	}
}