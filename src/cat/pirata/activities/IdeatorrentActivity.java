package cat.pirata.activities;

import org.json.JSONException;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import cat.pirata.utils.DbHelper;
import cat.pirata.utils.Net;


public class IdeatorrentActivity extends TabActivity {
	
	private Net net;
	private Handler hr;
	private DbHelper db;
	private TabHost tabHost;
	private Dialog dialog;
	
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
		
		
		db = new DbHelper(getBaseContext());
		net = new Net(db);
		hr = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				ProgressBar pb = (ProgressBar) getParent().findViewById(R.id.progressbar);
				pb.setVisibility(View.INVISIBLE);
				tabHost.setCurrentTab(1);
				Log.d("", "--IDEA-DONE--");
			}
		};
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("IDEA", "onResume");
		ProgressBar pb = (ProgressBar) getParent().findViewById(R.id.progressbar);
		pb.setVisibility(View.VISIBLE);
		Thread background = new Thread(new Runnable() {
			public void run() {
				try {
					net.ideaUpdate();
					net.tryAuth();
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
	}
	
	@Override
	protected void onDestroy() {
		Log.d("IDEA", "onDestroy");
		super.onDestroy();
		db.setToken("");
		db.close();
	}
	
	
	// -- MENU
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ideatorrent, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refrescar:
			refrescarDialog();
			return true;
		case R.id.loguejar:
			loguejarDialog();
			return true;
		case R.id.desloguejar:
			desloguejarDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void refrescarDialog() {
		db.setToken("");
		if (net.tryAuth()) {
			Toast.makeText(getBaseContext(), ":-)", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getBaseContext(), ":-(", Toast.LENGTH_LONG).show();
		}
		tabHost.refreshDrawableState();
	}

	private void loguejarDialog() {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.idea_dialog_loguejar);
		dialog.setTitle("Dades personals");
		
		Button button;
		button = (Button) dialog.findViewById(R.id.ok);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText etu = (EditText) dialog.findViewById(R.id.user);
				EditText etp = (EditText) dialog.findViewById(R.id.pass);
				
				db.setUserPass(etu.getText().toString(), etp.getText().toString());
				if (net.tryAuth()) {
					Toast.makeText(getBaseContext(), ":-)", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getBaseContext(), ":-(", Toast.LENGTH_LONG).show();
				}
				dialog.cancel();
			}
		});

		button = (Button) dialog.findViewById(R.id.cancel);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		
		dialog.show();
	}

	private void desloguejarDialog() {
		// TODO preguntar si quiere borrar datos bd
		db.setToken("");
		db.setUserPass("","");
		Toast.makeText(getBaseContext(), "Usuari, contrasenya i sessió esborrats!", Toast.LENGTH_LONG).show();
	}
}