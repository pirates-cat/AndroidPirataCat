package cat.pirata.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cat.pirata.R;

public class DbHelper {

	private static final String DATABASE_NAME = "PIRATACAT";
	private static final int DATABASE_VERSION = 20;

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
		String sql = "SELECT id,name,lastAccess,url,icon,enabled FROM rss WHERE enabled=1 LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}

	public Cursor getRssAll() {
		String sql = "SELECT id,name,lastAccess,url,icon,enabled FROM rss LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}


	public void updateFieldFromRSS(int id, String field, int enabled ) {
		String sql = "UPDATE rss SET " + field + "=" + enabled + " WHERE id=" + id;
		db.execSQL(sql);
	}
	
	public void updateFieldFromRSS(int id, String field, long lastAccess ) {
		String sql = "UPDATE rss SET " + field + "=" + lastAccess + " WHERE id=" + id;
		db.execSQL(sql);
	}


	public Long getLastStr(int id) {
		// WARNING: ORDER BY what? DESC -> damn flickr
		String sql = "SELECT lastAccess FROM rss WHERE id=" + id + " LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		Long lastAccess = (cr.moveToFirst()) ? cr.getLong(cr.getColumnIndex("lastAccess")) : 0L;
		cr.close();
		return lastAccess;
	}

	public void insertRow(int id, Long lastAccess, String body, String followUrl) {
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
	
	
	public void ideaUpdate(String str) throws JSONException {
		JSONObject json = new JSONObject(str);
		//Log.d("JSON", json.toString(4));
		
		if (json.has("solucions")) {
			JSONArray solucions = new JSONArray(json.getString("solucions"));
			for (int i = 0; i < solucions.length(); i++) {
				String sql = "INSERT INTO solucions (pid, sid, title, description, votes) VALUES (" +
				solucions.getJSONObject(i).getString("pid") + ", " +
				solucions.getJSONObject(i).getString("sid") + ", '" +
				solucions.getJSONObject(i).getString("title").replace("'", "`") + "', '" +
				solucions.getJSONObject(i).getString("description").replace("'", "`") + "', " +
				solucions.getJSONObject(i).getString("votes") + ")";

				Log.d("sql", sql);
				db.execSQL(sql);
			}
		}
		
		if (json.has("propostes")) {
			JSONArray propostes = new JSONArray(json.getString("propostes"));
			for (int i = 0; i < propostes.length(); i++) {
				String sql = "INSERT INTO propostes (status, pid, pubDate, title, description) VALUES ('" +
				propostes.getJSONObject(i).getString("status") + "', " +
				propostes.getJSONObject(i).getString("pid") + ", " +
				propostes.getJSONObject(i).getString("pubDate") + ", '" +
				propostes.getJSONObject(i).getString("title").replace("'", "`") + "', '" +
				propostes.getJSONObject(i).getString("description").replace("'", "`") + "')";

				Log.d("sql", sql);
				db.execSQL(sql);
			}
		}
	}
	
	public int getIdeaLastUpdate() {
		String sql = "SELECT value FROM config WHERE key='LastUpdateIdea'";
		Cursor cr = db.rawQuery(sql, null);
		int value =  (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("value")) : -1;
		cr.close();
		return value;
	}

	public void setIdeaLastUpdate(int time) {
		String sql = "UPDATE config SET value="+String.valueOf(time)+" WHERE key='LastUpdateIdea'";
		db.execSQL(sql);
	}
	
	
	public Cursor getPropostes(String status) {
		String sql = "SELECT * FROM propostes WHERE status='"+status+"' ORDER BY pubDate DESC LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}
	
	public Cursor getSolucions(Integer pid) {
		String sql = "SELECT * FROM solucions WHERE pid='"+String.valueOf(pid)+"' ORDER BY sid DESC LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}

	// -MENUS-


	public void deleteRSS(int id) {
		// we will have at least the bloc pirata :)
		if (id!=0) {
			String sql = "DELETE FROM rss WHERE id=" + id;
			db.execSQL(sql);
		}
	}
	
	public void insertRSS(String nom, String url, int icon) {
		String sql = "SELECT value FROM config WHERE key='ID'";
		Cursor cr = db.rawQuery(sql, null);
		int id = (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("value")) : -1;
		cr.close();
		if (id!=-1) {
			sql = "UPDATE config SET value="+(id+1)+" WHERE key='ID'";
			db.execSQL(sql);
		} else { return; }
		
		int idIcon = 0;
		switch (icon) {
			case 0:	idIcon = R.drawable.ic_ic_vermell; break;
			case 1:	idIcon = R.drawable.ic_ic_groc; break;
			case 2:	idIcon = R.drawable.ic_ic_verd; break;
			case 3:	idIcon = R.drawable.ic_ic_star; break;
		}
		
		sql = "INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES ("+id+", ?, 0, ?, "+idIcon+", 1)";
		db.execSQL(sql, new String[]{ nom, url });
	}
	
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
					"INSERT INTO config (key, value) VALUES ('LastUpdateIdea', 1)",
					"INSERT INTO config (key, value) VALUES ('ID', 4)",
					"CREATE TABLE rss (id INT, name TEXT, lastAccess INTEGER, url TEXT, icon INT, enabled INT)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (0, 'Bloc Pirata', 0, 'http://pirata.cat/bloc/?feed=rss2',"+ R.drawable.ic_info_bloc +", 1)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (1, 'YouTube', 0, 'http://gdata.youtube.com/feeds/base/users/PiratesdeCatalunyaTV/uploads?alt=rss&v=2&orderby=published',"+ R.drawable.ic_info_youtube +", 1)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (2, 'Flickr', 0, 'http://api.flickr.com/services/feeds/groups_pool.gne?id=1529563@N23&lang=es-es&format=rss_200',"+ R.drawable.ic_info_flickr +", 1)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (3, 'PPInternational', 0, 'http://www.pp-international.net/rss.xml',"+ R.drawable.ic_info_ppinternational +", 1)",					
					"CREATE TABLE row (id INT, lastAccess INTEGER, body TEXT, followUrl TEXT)",
					
					"CREATE TABLE comentaris (pid INT, cid INT, pubDate INT, author TEXT, description TEXT)",
					"CREATE TABLE propostes (status TEXT, pid INT, pubDate INT, title TEXT, description TEXT)",
					"CREATE TABLE solucions (pid INT, sid INT, title TEXT, description TEXT, votes INT, voted INT DEFAULT NULL)"
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
					"DROP TABLE IF EXISTS comentaris",
					"DROP TABLE IF EXISTS propostes",
					"DROP TABLE IF EXISTS solucions"
			};

			for (String sql : sqlBlock) { db.execSQL(sql); }
			onCreate(db);
		}
	}
}
