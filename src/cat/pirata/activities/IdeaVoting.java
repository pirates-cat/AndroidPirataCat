package cat.pirata.activities;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cat.pirata.utils.AuxTag;
import cat.pirata.utils.DbHelper;
import cat.pirata.utils.Net;

public class IdeaVoting extends Activity {
	
	private static String NAME = "voting";
	private static int LAYOUTSOLUTION = R.layout.idea_row_solucio;
	
	private DbHelper db;
	private Net net;
	private LinearLayout ll;
	private Dialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.idea);
		ll = (LinearLayout) findViewById(R.id.ll);
		
		db = new DbHelper(getBaseContext());
		net = new Net(db);
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
	
	// VOTING
	public void voteUp (View v) {
		if (!net.isAuth()) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		AuxTag aux = (AuxTag) v.getTag();
		ImageButton ib;
		ib = (ImageButton) aux.view.findViewById(R.id.icon_up);
		ib.setImageResource(R.drawable.idea_up_color);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_eq);
		ib.setImageResource(R.drawable.idea_equal_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_down);
		ib.setImageResource(R.drawable.idea_down_grey);
		if (net.vote(aux.rsid, 1).equals("ERROR")) {
			Toast.makeText(getBaseContext(), "Error procesant el vot! Refresca!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getBaseContext(), "Vot confirmat!", Toast.LENGTH_SHORT).show();
		}
		db.setVoted(aux.rsid, 1);
	}
	
	public void voteEqual (View v) {
		if (!net.isAuth()) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		AuxTag aux = (AuxTag) v.getTag();
		ImageButton ib;
		ib = (ImageButton) aux.view.findViewById(R.id.icon_up);
		ib.setImageResource(R.drawable.idea_up_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_eq);
		ib.setImageResource(R.drawable.idea_equal_color);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_down);
		ib.setImageResource(R.drawable.idea_down_grey);
		if (net.vote(aux.rsid, 0).equals("ERROR")) {
			Toast.makeText(getBaseContext(), "Error procesant el vot! Refresca!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getBaseContext(), "Vot confirmat!", Toast.LENGTH_SHORT).show();
		}
		db.setVoted(aux.rsid, 0);
	}
	
	public void voteDown (View v) {
		if (!net.isAuth()) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		AuxTag aux = (AuxTag) v.getTag();
		ImageButton ib;
		ib = (ImageButton) aux.view.findViewById(R.id.icon_up);
		ib.setImageResource(R.drawable.idea_up_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_eq);
		ib.setImageResource(R.drawable.idea_equal_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_down);
		ib.setImageResource(R.drawable.idea_down_color);
		if (net.vote(aux.rsid, -1).equals("ERROR")) {
			Toast.makeText(getBaseContext(), "Error procesant el vot! Refresca!", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getBaseContext(), "Vot confirmat!", Toast.LENGTH_SHORT).show();
		}
		db.setVoted(aux.rsid, -1);
	}
	//
	
	private void createList() {
		ll.removeAllViews();
		Cursor crProp = db.getPropostes(NAME);
		
		if (crProp.moveToFirst()) {
			do {
				addViewIdea(crProp.getInt(crProp.getColumnIndex("iid")),
						crProp.getInt(crProp.getColumnIndex("pubDate")),
						crProp.getString(crProp.getColumnIndex("title")),
						crProp.getString(crProp.getColumnIndex("description")));
				
				Cursor crSol = db.getSolucions(crProp.getInt(crProp.getColumnIndex("iid")));
				
				if (!crSol.moveToFirst()) {
					crSol.close();
					continue;
				}
				
				do {
					addViewSolution(crSol.getInt(crSol.getColumnIndex("iid")),
							crSol.getInt(crSol.getColumnIndex("sid")),
							crSol.getInt(crSol.getColumnIndex("rsid")),
							crSol.getInt(crSol.getColumnIndex("votes")),
							crSol.getString(crSol.getColumnIndex("title")),
							crSol.getString(crSol.getColumnIndex("description")));
				} while (crSol.moveToNext());
				crSol.close();
			} while (crProp.moveToNext());
		}
		crProp.close();
		ll.invalidate();
	}

	
	private void addViewSolution(int iid, int sid, int rsid, int votes, String title, String description) {
		OnClickListener openClose = new OnClickListener() {
		    public void onClick(View v) {
		    	TextView tv = (TextView) v.findViewById(R.id.description);
		    	LinearLayout lrl = (LinearLayout) v.findViewById(R.id.baseDesc);
		    	if (lrl.getChildCount()==0) {
		    		View child = getLayoutInflater().inflate(R.layout.tv_description, lrl);
		    		TextView tu = (TextView) child.findViewById(R.id.description);
		    		tu.setText(tv.getText());
		    	} else {
		    		lrl.removeAllViews();
		    	}
		    	ll.invalidate();
		    }
		};
		
		View child = getLayoutInflater().inflate(LAYOUTSOLUTION, null);

		child.setClickable(true);
		child.setOnClickListener(openClose);
		
		TextView tv;
		ImageButton ib;
		AuxTag aux = new AuxTag();
		aux.view = child;
		aux.rsid = rsid;
		
		if (LAYOUTSOLUTION == R.layout.idea_row_solucio) {
			int opt = db.getVoted(rsid);
			
			ib = (ImageButton) child.findViewById(R.id.icon_up);
			ib.setTag(aux);
			if (opt==1) { ib.setImageResource(R.drawable.idea_up_color); }
			
			ib = (ImageButton) child.findViewById(R.id.icon_eq);
			ib.setTag(aux);
			if (opt==0) { ib.setImageResource(R.drawable.idea_equal_color); }
			
			ib = (ImageButton) child.findViewById(R.id.icon_down);
			ib.setTag(aux);
			if (opt==-1) { ib.setImageResource(R.drawable.idea_down_color); }
		}
		
		tv = (TextView) child.findViewById(R.id.votes);
		tv.setText(String.valueOf(votes));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(String.valueOf(title));
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(String.valueOf(description));
		
		ll.addView(child);
	}
	

	private void addViewIdea(int iid, int pubDate, String title, String description) {

		OnClickListener openClose = new OnClickListener() {
		    public void onClick(View v) {
		    	TextView tv = (TextView) v.findViewById(R.id.description);
		    	LinearLayout lrl = (LinearLayout) v.findViewById(R.id.baseDesc);
		    	if (lrl.getChildCount()==0) {
		    		View child = getLayoutInflater().inflate(R.layout.tv_description, lrl);
		    		TextView tu = (TextView) child.findViewById(R.id.description);
		    		tu.setText(tv.getText());
		    	} else {
		    		lrl.removeAllViews();
		    	}
		    	ll.invalidate();
		    }
		};
		

		OnLongClickListener openDialog = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				dialog = new Dialog(getParent());
				dialog.setTitle("Comentaris");
				dialog.setContentView(R.layout.idea_dialog_comments);
				
				String strJson = net.getOnlineComment( (Integer)v.getTag() );
				Log.d("json", strJson );
				try {
					JSONObject json = new JSONObject(strJson);
					if (json.has("comments")) {
						JSONArray comments = new JSONArray(json.getString("comments"));
						LinearLayout root = (LinearLayout) dialog.findViewById(R.id.root);
						
						for (int i = 0; i < comments.length(); i++) {
							View child = getLayoutInflater().inflate(R.layout.idea_dialog_comment, null);
							
							TextView tv;
							String str;
							
							tv = (TextView) child.findViewById(R.id.author);
							str = comments.getJSONObject(i).getString("author");
							tv.setText(str);

							tv = (TextView) child.findViewById(R.id.pubDate);
							Calendar cal = Calendar.getInstance();
							str = comments.getJSONObject(i).getString("pubDate").concat("000");
							cal.setTime( new Date(Long.valueOf( str )) );
							tv.setText(String.format("%02d %s", cal.get(Calendar.DATE), calMonth(cal.get(Calendar.MONTH))));

							tv = (TextView) child.findViewById(R.id.description);
							str = comments.getJSONObject(i).getString("description");
							tv.setText(str);
							
							root.addView(child);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				Button button;
				
				button = (Button) dialog.findViewById(R.id.send);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (!net.isAuth()) {
							Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}
						TextView tv = (TextView) dialog.findViewById(R.id.nouComentari);
						net.sendNewComment(String.valueOf(tv.getText()), (Integer)v.getTag());
						dialog.cancel();
					}
				});
				button.setTag(v.getTag());
				
				button = (Button) dialog.findViewById(R.id.cancel);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) { dialog.cancel(); }
				});
				dialog.show();
				return true;
			}
		};
		
		View child = getLayoutInflater().inflate(R.layout.idea_row_proposta, null);
		child.setTag(iid);
		child.setClickable(true);
		child.setLongClickable(true);
		child.setOnClickListener(openClose);
		child.setOnLongClickListener(openDialog);
		
		TextView tv;
		
		tv = (TextView) child.findViewById(R.id.pubDate);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime( new Date(Long.valueOf(String.valueOf(pubDate).concat("000"))) );
		tv.setText(String.format("%02d %s", cal.get(Calendar.DATE), calMonth(cal.get(Calendar.MONTH))));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(String.valueOf(title));
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(String.valueOf(description));
		
		ll.addView(child);
	}
	
	private String calMonth(int i) {
		String[] day = new String[] {
				"Gen","Feb","Mar","Abr","Mai","Jun","Jul","Ago","Set","Oct","Nov","Dec"};
		return day[i];
	}
}
