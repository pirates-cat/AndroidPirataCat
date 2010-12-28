package cat.pirata.activities;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import cat.pirata.extra.CtrlDb;
import cat.pirata.extra.CtrlNet;
import cat.pirata.ideatorrent.IdeaDeveloping;
import cat.pirata.ideatorrent.IdeaDone;
import cat.pirata.ideatorrent.IdeaVoting;
import cat.pirata.ideatorrent.IdeaWaiting;

public class Participacio extends TabActivity {

	private Dialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;

		Class<?>[] classes = new Class<?>[] {
			IdeaWaiting.class,
			IdeaVoting.class,
			IdeaDeveloping.class,
			IdeaDone.class
		};
		
		String[] classesStr = new String[] {
			"Espera",
			"Votant",
			"Fent",
			"Fetes"
		};
		
		for (int i = 0; i < classes.length; i++) {
			Intent intent = new Intent().setClass(this, classes[i]);
			View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab, getTabWidget(), false);
			((TextView)tabIndicator.findViewById(R.id.title)).setText(classesStr[i]);
			spec = tabHost.newTabSpec(classesStr[i]).setIndicator(tabIndicator).setContent(intent);
			tabHost.addTab(spec);
		}
		
		tabHost.setCurrentTab(1);
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
		CtrlDb.getInstance().setToken("");
		if (CtrlNet.getInstance().tryAuth()) {
			Toast.makeText(getBaseContext(), "Fet! :-)", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getBaseContext(), "Error! :-(", Toast.LENGTH_LONG).show();
		}
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
				
				CtrlDb.getInstance().setUserPass(etu.getText().toString(), etp.getText().toString());
				if (CtrlNet.getInstance().tryAuth()) {
					Toast.makeText(getBaseContext(), "Fet! :-)", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getBaseContext(), "Error! :-(", Toast.LENGTH_LONG).show();
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
		CtrlDb.getInstance().setToken("");
		CtrlDb.getInstance().setUserPass("","");
		Toast.makeText(getBaseContext(), "Usuari, contrasenya i sessió esborrats!", Toast.LENGTH_LONG).show();
	}
}
