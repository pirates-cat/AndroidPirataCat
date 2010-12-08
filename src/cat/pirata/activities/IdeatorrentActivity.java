package cat.pirata.activities;

import org.json.JSONException;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import cat.pirata.R;
import cat.pirata.utils.DbHelper;
import cat.pirata.utils.RSS;


public class IdeatorrentActivity extends TabActivity {
	
	private RSS rss;
	private Handler hr;
	private DbHelper db;
	private TabHost tabHost;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent i;
		View tabIndicator;

		i = new Intent().setClass(this, IdeaWaiting.class);
		tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_idea, getTabWidget(), false);
		((TextView) tabIndicator.findViewById(R.id.title)).setText("Espera");
		spec = tabHost.newTabSpec("Sala d'espera").setIndicator(tabIndicator).setContent(i);
		tabHost.addTab(spec);
		
		i = new Intent().setClass(this, IdeaVoting.class);
		tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_idea, getTabWidget(), false);
		((TextView) tabIndicator.findViewById(R.id.title)).setText("Populars");
		spec = tabHost.newTabSpec("Més populars").setIndicator(tabIndicator).setContent(i);
		tabHost.addTab(spec);
		
		i = new Intent().setClass(this, IdeaDeveloping.class);
		tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_idea, getTabWidget(), false);
		((TextView) tabIndicator.findViewById(R.id.title)).setText("Fent");
		spec = tabHost.newTabSpec("En desenvolupament").setIndicator(tabIndicator).setContent(i);
		tabHost.addTab(spec);
		
		i = new Intent().setClass(this, IdeaDone.class);
		tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_idea, getTabWidget(), false);
		((TextView) tabIndicator.findViewById(R.id.title)).setText("Fetes");
		spec = tabHost.newTabSpec("Portades a terme").setIndicator(tabIndicator).setContent(i);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(2);
		
		
		db = new DbHelper(getBaseContext());
		rss = new RSS(db);
		hr = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				ProgressBar pb = (ProgressBar) getParent().findViewById(R.id.progressbar);
				pb.setVisibility(View.INVISIBLE);
				Log.d("", "--IDEA-DONE--");
				tabHost.invalidate();
			}
		};
	}
	

	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("IDEA", "onResume");

		Thread background = new Thread(new Runnable() {
			public void run() {
				try {
					rss.ideaUpdate();
				} catch (IllegalStateException e) {
					// race condition !!
				} catch (JSONException e) {
					e.printStackTrace();
				} finally {
					hr.sendMessage(hr.obtainMessage());
				}
			}
		});
		background.start();
		ProgressBar pb = (ProgressBar) getParent().findViewById(R.id.progressbar);
		pb.setVisibility(View.VISIBLE);
	}
	
	
	@Override
	protected void onDestroy() {
		Log.d("IDEA", "onDestroy");
		super.onDestroy();
		db.close();
	}
}
