package cat.pirata.extra;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cat.pirata.activities.R;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "PIRATACAT";
	private static final int DATABASE_VERSION = 6;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("DB", "onCreate");
		String[] sqlBlock = new String[] {
				"CREATE TABLE config (key TEXT, value TEXT)",
				"INSERT INTO config (key, value) VALUES ('FirstTime', '1')",
				"INSERT INTO config (key, value) VALUES ('LastUpdate', '1')",
				"INSERT INTO config (key, value) VALUES ('MyToken', '')",
				"INSERT INTO config (key, value) VALUES ('Username', '')",
				"INSERT INTO config (key, value) VALUES ('Password', '')",
				
				"CREATE TABLE rss (id INTEGER, icon INTEGER)",
				"INSERT INTO rss (id, icon) VALUES (0, "+ R.drawable.ic_info_bloc +")",
				"INSERT INTO rss (id, icon) VALUES (1, "+ R.drawable.ic_info_youtube +")",
				"INSERT INTO rss (id, icon) VALUES (2, "+ R.drawable.ic_info_flickr +")",
				"INSERT INTO rss (id, icon) VALUES (3, "+ R.drawable.ic_info_ppinternational +")",
				
				"CREATE TABLE solutions (id INTEGER PRIMARY KEY, voted INTEGER DEFAULT NULL)"
		};
	
		for (String sql : sqlBlock) { db.execSQL(sql); }
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DB", "onUpgrade");
		String[] sqlBlock = new String[] {
			"DROP TABLE IF EXISTS config",
			"DROP TABLE IF EXISTS rss",
			"DROP TABLE IF EXISTS solutions"
		};

		for (String sql : sqlBlock) { db.execSQL(sql); }
		onCreate(db);
	}
}
