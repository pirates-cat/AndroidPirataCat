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
import cat.pirata.extra.StrAuxTag;
import cat.pirata.extra.CtrlDb;
import cat.pirata.extra.CtrlJson;
import cat.pirata.extra.CtrlNet;
import cat.pirata.extra.StrComment;
import cat.pirata.extra.StrIdea;
import cat.pirata.extra.StrSolution;
import cat.pirata.extra.Utils;

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

	private void createList() {
		SortedSet<StrIdea> ssIdea = CtrlJson.getInstance().getIdeas(getSTATUS());
		
		for (StrIdea idea : ssIdea) {
			addViewIdea(idea);
			for (StrSolution solution : idea.ssbSolution) {
				addViewSolution(solution);
			}
		}
	}

	
	private void addViewSolution(StrSolution solution) {
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
		StrAuxTag aux = new StrAuxTag(child, solution.sid);
		
		if (getLAYOUTSOLUTION() == R.layout.idea_row_solucio) {
			int opt = CtrlDb.getInstance().getVoted( solution.sid );
			
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
		tv.setText(String.valueOf(solution.votes));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(solution.title);
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(solution.description);
		
		ll.addView(child);
	}
	
	
	private void addViewIdea(StrIdea idea) {

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
				
				String jsonStr = CtrlNet.getInstance().getOnlineComment( (Integer)view.getTag() );
				SortedSet<StrComment> ssComment = CtrlJson.getInstance().parseComments(jsonStr);
				
				for (StrComment comment : ssComment) {
					View child = getLayoutInflater().inflate(R.layout.idea_dialog_comment, null);
					
					TextView tv;
					tv = (TextView) child.findViewById(R.id.author);
					tv.setText(comment.author);

					tv = (TextView) child.findViewById(R.id.pubDate);
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis( Long.valueOf( (comment.pubDate).concat("000") ) );
					tv.setText(String.format("%02d %s", cal.get(Calendar.DATE), Utils.calMonth(cal.get(Calendar.MONTH))));

					tv = (TextView) child.findViewById(R.id.description);
					tv.setText(comment.description);
					
					root.addView(child);
				}
				
				Button button;
				
				button = (Button) dialog.findViewById(R.id.send);
				button.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (CtrlDb.getInstance().getToken().equals("")) {
							Toast.makeText(getBaseContext(), "Autentifica't al menu de sota!", Toast.LENGTH_SHORT).show();
							dialog.cancel();
							return;
						}
						TextView tv = (TextView) dialog.findViewById(R.id.nouComentari);
						try {
							String text = tv.getText().toString();
							Integer iid = (Integer) v.getTag();
							CtrlNet.getInstance().sendNewComment(text, iid);
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(getBaseContext(), "Error desconegut! :(", Toast.LENGTH_SHORT).show();
						}
						Toast.makeText(getBaseContext(), "Enviat amb èxit! :)", Toast.LENGTH_SHORT).show();
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
		child.setTag(idea.id);
		child.setClickable(true);
		child.setLongClickable(true);
		child.setOnClickListener(openClose);
		child.setOnLongClickListener(openDialog);
		
		TextView tv;
		
		tv = (TextView) child.findViewById(R.id.pubDate);
		String dt = idea.pubDate;
		tv.setText(dt.substring(8,10)+" "+Utils.calMonth(Integer.valueOf(dt.substring(5,7))-1));
		
		tv = (TextView) child.findViewById(R.id.title);
		tv.setText(idea.title);
		
		tv = (TextView) child.findViewById(R.id.description);
		tv.setText(idea.description);
		
		ll.addView(child);
	}
}
