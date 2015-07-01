package cat.pirata.activities;


import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import cat.pirata.extra.CtrlDb;
import cat.pirata.extra.CtrlFile;
import cat.pirata.extra.CtrlJson;
import cat.pirata.extra.CtrlNet;

public class PartitPirata extends TabActivity {

	private ProgressBar pb;
	private Handler hr;
	private TabHost tabHost;
	private Dialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		CtrlDb.getInstance().setContext(getBaseContext());
		CtrlFile.getInstance().setContext(getBaseContext());
		CtrlNet.getInstance();
		CtrlJson.getInstance();
		
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.Parent);
		rl.setBackgroundResource(R.drawable.background);
		
		pb = (ProgressBar) findViewById(R.id.ProgressBar);
		hr = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				pb.setVisibility(View.INVISIBLE);
				// fast refresh
				int tab = tabHost.getCurrentTab();
				if (tab == 0) {
					tabHost.setCurrentTab(1);
					tabHost.setCurrentTab(tab);
				} else {
					tabHost.setCurrentTab(0);
					tabHost.setCurrentTab(tab);
				}
				Log.d("PARTIT", "--UPDATE--");
			}
		};
		
		tabHost = getTabHost();
		TabHost.TabSpec spec;

		Class<?>[] classes = new Class<?>[] {
			Participacio.class,
			Informacio.class,
			Calendari.class
		};
		
		String[] classesStr = new String[] {
			getString(R.string.Participacio),
			getString(R.string.Informacio),
			getString(R.string.Calendari)
		};
		
		for (int i = 0; i < classes.length; i++) {
			Intent intent = new Intent().setClass(this, classes[i]);
			View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab, getTabWidget(), false);
			((TextView)tabIndicator.findViewById(R.id.title)).setText(classesStr[i]);
			spec = tabHost.newTabSpec(classesStr[i]).setIndicator(tabIndicator).setContent(intent);
			tabHost.addTab(spec);
		}
		
		firstTimeDialog();
	}

	@Override
	protected void onResume() {
		Log.d("PARTIT", "onResume");
		super.onResume();
		update();
	}

	@Override
	protected void onDestroy() {
		Log.d("PARTIT", "onDestroy");
		CtrlDb.getInstance().close();
		super.onDestroy();
	}
	
	
	// ----- PRIVATE
	
	private void update() {
		pb.setVisibility(View.VISIBLE);
		pb.setProgress(0);
		
		Thread background = new Thread(new Runnable() {
			public void run() {
				try {
					CtrlNet.getInstance().update(pb);
					pb.setProgress(100);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					hr.sendMessage(hr.obtainMessage());
				}
			}
		});
		background.start();
	}
	
	private void firstTimeDialog() {
		if (CtrlDb.getInstance().isFirstTime()) {
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.info_dialog_firsttime);
			dialog.setTitle(R.string.quisom);
			Button button = (Button) dialog.findViewById(R.id.entrar);
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) { dialog.cancel(); }
			});
			dialog.show();
		}
	}
}