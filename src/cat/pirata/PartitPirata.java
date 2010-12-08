package cat.pirata;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import cat.pirata.activities.CalendariActivity;
import cat.pirata.activities.IdeatorrentActivity;
import cat.pirata.activities.InfoActivity;

public class PartitPirata extends TabActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent i;

		i = new Intent().setClass(this, InfoActivity.class);
		spec = tabHost.newTabSpec("informacio").setIndicator("Informació", res.getDrawable(R.layout.ic_tab_info)).setContent(i);
		tabHost.addTab(spec);

		i = new Intent().setClass(this, CalendariActivity.class);
		spec = tabHost.newTabSpec("calendari").setIndicator("Calendari", res.getDrawable(R.layout.ic_tab_calendari)).setContent(i);
		tabHost.addTab(spec);

		i = new Intent().setClass(this, IdeatorrentActivity.class);
		spec = tabHost.newTabSpec("ideatorrent").setIndicator("IdeaTorrent", res.getDrawable(R.layout.ic_tab_ideatorrent)).setContent(i);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(2);
	}
}