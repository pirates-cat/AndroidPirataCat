package cat.pirata.ideatorrent;

import java.util.Calendar;
import java.util.SortedSet;

import android.app.Activity;
import android.app.Dialog;
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
import cat.pirata.activities.R;
import cat.pirata.extra.AuxTag;
import cat.pirata.extra.CtrlDb;
import cat.pirata.extra.CtrlJson;
import cat.pirata.extra.CtrlNet;

public class IdeaTorrent extends Activity {

	Integer STATUS = -99;
	Integer LAYOUTSOLUTION = R.layout.idea_row_solucio;
	
	// override
	protected Integer getSTATUS(){ return STATUS; }
	protected Integer getLAYOUTSOLUTION() { return LAYOUTSOLUTION; }

	
	private LinearLayout ll;
	private Dialog dialog;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("createIdea", String.valueOf(getSTATUS()));
		super.onCreate(savedInstanceState);
		setContentView(R.layout.linearlayout);
		ll = (LinearLayout) findViewById(R.id.Root);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ll.removeAllViews();
		createList();
	}


	// VOTING
	public void voteUp (View v) {
		if (CtrlDb.getInstance().getToken().equals("")) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		AuxTag aux = (AuxTag) v.getTag();
		
		if (CtrlNet.getInstance().voteSolution(aux.rsid, 1).equals("ERROR")) {
			Toast.makeText(getBaseContext(), "Error procesant el vot! Refresca!", Toast.LENGTH_SHORT).show();
			return;
		} else {
			Toast.makeText(getBaseContext(), "Vot confirmat!", Toast.LENGTH_SHORT).show();
		}
		
		ImageButton ib;
		ib = (ImageButton) aux.view.findViewById(R.id.icon_up);
		ib.setImageResource(R.drawable.idea_up_color);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_eq);
		ib.setImageResource(R.drawable.idea_equal_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_down);
		ib.setImageResource(R.drawable.idea_down_grey);

		CtrlDb.getInstance().setVoted(aux.rsid, 1);
	}
	
	public void voteEqual (View v) {
		if (CtrlDb.getInstance().getToken().equals("")) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		AuxTag aux = (AuxTag) v.getTag();
		
		if (CtrlNet.getInstance().voteSolution(aux.rsid, 0).equals("ERROR")) {
			Toast.makeText(getBaseContext(), "Error procesant el vot! Refresca!", Toast.LENGTH_SHORT).show();
			return;
		} else {
			Toast.makeText(getBaseContext(), "Vot confirmat!", Toast.LENGTH_SHORT).show();
		}
		
		ImageButton ib;
		ib = (ImageButton) aux.view.findViewById(R.id.icon_up);
		ib.setImageResource(R.drawable.idea_up_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_eq);
		ib.setImageResource(R.drawable.idea_equal_color);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_down);
		ib.setImageResource(R.drawable.idea_down_grey);

		CtrlDb.getInstance().setVoted(aux.rsid, 0);
	}
	
	public void voteDown (View v) {
		if (CtrlDb.getInstance().getToken().equals("")) {
			Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
			return;
		}
		AuxTag aux = (AuxTag) v.getTag();
		
		if (CtrlNet.getInstance().voteSolution(aux.rsid, -1).equals("ERROR")) {
			Toast.makeText(getBaseContext(), "Error procesant el vot! Refresca!", Toast.LENGTH_SHORT).show();
			return;
		} else {
			Toast.makeText(getBaseContext(), "Vot confirmat!", Toast.LENGTH_SHORT).show();
		}
		
		ImageButton ib;
		ib = (ImageButton) aux.view.findViewById(R.id.icon_up);
		ib.setImageResource(R.drawable.idea_up_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_eq);
		ib.setImageResource(R.drawable.idea_equal_grey);
		ib = (ImageButton) aux.view.findViewById(R.id.icon_down);
		ib.setImageResource(R.drawable.idea_down_color);

		CtrlDb.getInstance().setVoted(aux.rsid, -1);
	}
	// */
	
	
	
	private void createList() {
		SortedSet<Bundle> v = CtrlJson.getInstance().getIdeas(getSTATUS());
		
		for (Bundle b : v) {
			//Log.d("idea", b.toString());
			addViewIdea(b);
			Bundle s = b.getBundle("s");
			for (String solBundle : s.keySet()) {
				//Log.d("sol", s.getBundle(solBundle).toString());
				addViewSolution( s.getBundle(solBundle));
			}
		}
	}

	
	private void addViewSolution(Bundle b) {
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
		
		View child = getLayoutInflater().inflate(getLAYOUTSOLUTION(), null);

		child.setClickable(true);
		child.setOnClickListener(openClose);
		
		TextView tv;
		ImageButton ib;
		AuxTag aux = new AuxTag(child, Integer.valueOf(b.getString("sid")));
		
		if (getLAYOUTSOLUTION() == R.layout.idea_row_solucio) {
			int opt = CtrlDb.getInstance().getVoted(b.getString("sid"));
			
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
		tv.setText(b.getString("vt"));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(b.getString("tt"));
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(b.getString("ds"));
		
		ll.addView(child);
	}
	
	
	private void addViewIdea(Bundle b) {

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
			public boolean onLongClick(View view) {
				dialog = new Dialog(getParent());
				dialog.setTitle("Comentaris");
				dialog.setContentView(R.layout.idea_dialog_comments);
				LinearLayout root = (LinearLayout) dialog.findViewById(R.id.root);
				
				String jsonStr = CtrlNet.getInstance().getOnlineComment( Integer.valueOf((String) view.getTag()) );
				SortedSet<Bundle> ss = CtrlJson.getInstance().parseComments(jsonStr);
				
				for (Bundle b : ss) {
					View child = getLayoutInflater().inflate(R.layout.idea_dialog_comment, null);
					
					TextView tv;
					tv = (TextView) child.findViewById(R.id.author);
					tv.setText(b.getString("author"));

					tv = (TextView) child.findViewById(R.id.pubDate);
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis( Long.valueOf( b.getString("pubDate").concat("000") ) );
					tv.setText(String.format("%02d %s", cal.get(Calendar.DATE), calMonth(cal.get(Calendar.MONTH))));

					tv = (TextView) child.findViewById(R.id.description);
					tv.setText(b.getString("description"));
					
					root.addView(child);
				}
				
				Button button;
				
				button = (Button) dialog.findViewById(R.id.send);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (CtrlDb.getInstance().getToken().equals("")) {
							Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}
						TextView tv = (TextView) dialog.findViewById(R.id.nouComentari);
						CtrlNet.getInstance().sendNewComment(String.valueOf(tv.getText()), (Integer)v.getTag());
						dialog.cancel();
					}
				});
				button.setTag(view.getTag());
				
				button = (Button) dialog.findViewById(R.id.cancel);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) { dialog.cancel(); }
				});
				dialog.show();
				return true;
			}
		};
		
		View child = getLayoutInflater().inflate(R.layout.idea_row_proposta, null);
		child.setTag(b.getString("id"));
		child.setClickable(true);
		child.setLongClickable(true);
		child.setOnClickListener(openClose);
		child.setOnLongClickListener(openDialog);
		
		TextView tv;
		
		tv = (TextView) child.findViewById(R.id.pubDate);
		String dt = b.getString("dt");
		tv.setText(dt.substring(8,10)+" "+calMonth(Integer.valueOf(dt.substring(5,7))-1));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(String.valueOf(b.getString("tt")));
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(String.valueOf(b.getString("ds")));
		
		ll.addView(child);
	}
	
	private String calMonth(int i) {
		String[] day = new String[] {
				"Gen","Feb","Mar","Abr","Mai","Jun","Jul","Ago","Set","Oct","Nov","Dec"};
		return day[i];
	}
}
