package cat.pirata.ideatorrent;

import cat.pirata.activities.R;


public class IdeaVoting extends IdeaTorrent {
	private static Integer STATUS = -1;
	Integer LAYOUTSOLUTION = R.layout.idea_row_solucio;

	// override
	protected Integer getSTATUS(){ return STATUS; }
	protected Integer getLAYOUTSOLUTION() { return LAYOUTSOLUTION; }
}