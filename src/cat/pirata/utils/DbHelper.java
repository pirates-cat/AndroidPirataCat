package cat.pirata.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cat.pirata.R;

public class DbHelper {

	private static final String DATABASE_NAME = "PIRATACAT";
	private static final int DATABASE_VERSION = 4;

	private SQLiteDatabase db;


	// -PUBLIC-

	public DbHelper(Context context) {
		db = (new DatabaseHelper(context)).getWritableDatabase();
	}

	public void close() {
		db.close();
	}

	public Cursor getLastNews(int numlastnews) {
		String ids = "SELECT id FROM rss WHERE enabled=1";
		String sql = "SELECT id, lastAccess, body, followUrl FROM row WHERE id IN ("+ids+") ORDER BY lastAccess DESC LIMIT "+numlastnews;
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}

	public Cursor getRssEnabled() {
		String sql = "SELECT id,name,url,icon,enabled FROM rss WHERE enabled=1 LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}

	public Cursor getRssAll() {
		String sql = "SELECT id,name,url,icon,enabled FROM rss LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}


	public void updateFieldFromRSS(int id, String field, int enabled ) {
		String sql = "UPDATE rss SET " + field + "=" + enabled + " WHERE id=" + id;
		db.execSQL(sql);
	}


	public Long getLastStr(int id) {
		String sql = "SELECT lastAccess FROM row WHERE id=" + id + " ORDER BY lastAccess DESC LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		Long lastAccess =  (cr.moveToFirst()) ? cr.getLong(cr.getColumnIndex("lastAccess")) : 0L;
		cr.close();
		return lastAccess;
	}

	public void updateAllFieldsFromRow(int id, Long lastAccess, String body, String followUrl) {
		String sql = "INSERT INTO row (id, lastAccess, body, followUrl) VALUES ("+id+", "+lastAccess+", ?,?)";
		db.execSQL(sql, new String[]{ body, followUrl });
	}

	public void clearOldNews(long timeInMillis) {
		String sql = "DELETE FROM row WHERE lastAccess<" + timeInMillis;
		db.execSQL(sql);
	}

	public void resetAllData() {
		String sql = "";
		db.execSQL(sql);
	}

	public int getIcon(int id) {
		String sql = "SELECT icon FROM rss WHERE id="+id+" AND enabled=1 LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		int value =  (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("icon")) : -1;
		cr.close();
		return value;
	}
	
	public boolean isFirstTime() {
		String sql = "SELECT value FROM config WHERE key='FirstTime'";
		Cursor cr = db.rawQuery(sql, null);
		int value = (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("value")) : -1;
		cr.close();
		if (value==1) {
			sql = "UPDATE config SET value=0 WHERE key='FirstTime'";
			db.execSQL(sql);
			return true;
		}
		return false;
	}

	// -MENUS-


	
	// -WIDGET-
	
	public Cursor getLastRow() {
		String sql = "SELECT id,lastAccess,body,followUrl FROM row ORDER BY lastAccess DESC LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();		
		return cr;
	}

	// -CLASS-

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("DB", "onCreate");
			String[] sqlBlock = new String[] {
					"CREATE TABLE config (key TEXT, value INT)",
					"INSERT INTO config (key, value) VALUES ('FirstTime', 1)",
					"CREATE TABLE rss (id INT, name TEXT, url TEXT, icon INT, enabled INT)",
					"INSERT INTO rss (id, name, url, icon, enabled) VALUES (0, 'Bloc Pirata', 'http://pirata.cat/bloc/?feed=rss2',"+ R.drawable.ic_info_bloc +", 1)",
					"INSERT INTO rss (id, name, url, icon, enabled) VALUES (1, 'YouTube', 'http://gdata.youtube.com/feeds/base/users/PiratesdeCatalunyaTV/uploads?alt=rss&v=2&orderby=published',"+ R.drawable.ic_info_youtube +", 1)",
					"INSERT INTO rss (id, name, url, icon, enabled) VALUES (2, 'Flickr', 'http://api.flickr.com/services/feeds/groups_pool.gne?id=1529563@N23&lang=es-es&format=rss_200',"+ R.drawable.ic_info_flickr +", 1)",
					"CREATE TABLE row (id INT, lastAccess INTEGER, body TEXT, followUrl TEXT)",
					"CREATE TABLE idea (id INT)",
					"INSERT INTO idea (id) VALUES (0)",
			};

			for (String sql : sqlBlock) { db.execSQL(sql); }
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("DB", "onUpgrade");
			String[] sqlBlock = new String[] {
					"DROP TABLE IF EXISTS config",
					"DROP TABLE IF EXISTS rss",
					"DROP TABLE IF EXISTS row",
					"DROP TABLE IF EXISTS idea"
			};

			for (String sql : sqlBlock) { db.execSQL(sql); }
			onCreate(db);
		}
	}
}
