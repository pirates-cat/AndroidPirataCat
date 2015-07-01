package cat.pirata.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

	private AlertDialog dialog;
	
	@SuppressLint("NewApi")
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
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    // Get the layout inflater
	    LayoutInflater inflater = getLayoutInflater();
	    final View alertView = inflater.inflate(R.layout.idea_dialog_loguejar, null);

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(alertView)
	    // Add action buttons
	           .setPositiveButton(R.string.menu_connectar, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // sign in the user ...
	   				EditText etu = (EditText) alertView.findViewById(R.id.user);
					EditText etp = (EditText) alertView.findViewById(R.id.pass);
					
					CtrlDb.getInstance().setUserPass(etu.getText().toString(), etp.getText().toString());
					if (CtrlNet.getInstance().tryAuth()) {
						Toast.makeText(getBaseContext(), "Fet! :-)", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getBaseContext(), "Error! :-(", Toast.LENGTH_LONG).show();
					}
	               }
	           })
	           .setNegativeButton("Cancela", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   // Android cancela el diàleg quan cliquem qualsevol botó
	               }
	           })
	           .setTitle("Dades personals");

		dialog = builder.create();
		dialog.show();
	}

	private void desloguejarDialog() {
		// TODO preguntar si quiere borrar datos bd
		CtrlDb.getInstance().setToken("");
		CtrlDb.getInstance().setUserPass("","");
		Toast.makeText(getBaseContext(), "Usuari, contrasenya i sessió esborrats!", Toast.LENGTH_LONG).show();
	}
}
