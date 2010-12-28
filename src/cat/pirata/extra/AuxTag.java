package cat.pirata.extra;

import android.view.View;

public class AuxTag {
	public AuxTag (View view, int rsid) {
		this.view = view;
		this.rsid = rsid;
	}
	public View view;
	public int rsid;
}