package cat.pirata.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cat.pirata.activities.R;

public class DbHelper {

	private static final String DATABASE_NAME = "PIRATACAT";
	private static final int DATABASE_VERSION = 32;

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
		// Log.d("JSON", json.toString(4));
		
		if (json.has("ideas")) {
			JSONArray ideas = new JSONArray(json.getString("ideas"));
			for (int i = 0; i < ideas.length(); i++) {
				String sql = "INSERT INTO ideas (status, iid, pubDate, title, description) VALUES ('" +
					ideas.getJSONObject(i).getString("status") + "', " +
					ideas.getJSONObject(i).getString("iid") + ", " +
					ideas.getJSONObject(i).getString("pubDate") + ", '" +
					ideas.getJSONObject(i).getString("title").replace("'", "`") + "', '" +
					ideas.getJSONObject(i).getString("description").replace("'", "`") + "')";

				// Log.d("sql(i)", sql);
				db.execSQL(sql);
			}
		}
		
		if (json.has("solutions")) {
			JSONArray solutions = new JSONArray(json.getString("solutions"));
			for (int i = 0; i < solutions.length(); i++) {
				String sql = "INSERT INTO solutions (iid, sid, rsid, title, description, votes) VALUES (" +
					solutions.getJSONObject(i).getString("iid") + ", " +
					solutions.getJSONObject(i).getString("sid") + ", " +
					solutions.getJSONObject(i).getString("rsid") + ", '" +
					solutions.getJSONObject(i).getString("title").replace("'", "`") + "', '" +
					solutions.getJSONObject(i).getString("description").replace("'", "`") + "', " +
					solutions.getJSONObject(i).getString("votes") + ")";

				// Log.d("sql(s)", sql);
				db.execSQL(sql);
			}
		}
		
		if (json.has("votes")) {
			JSONArray votes = new JSONArray(json.getString("votes"));
			for (int i = 0; i < votes.length(); i++) {
				String sql = "UPDATE solutions " +
					"SET votes=" + votes.getJSONObject(i).getString("votes") +
					" WHERE rsid=" + votes.getJSONObject(i).getString("rsid");

				// Log.d("sql(v)", sql);
				db.execSQL(sql);
			}
		}
	}
	
	public int getVoted(int rsid) {
		String sql = "SELECT voted FROM solutions WHERE rsid="+rsid+" LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		int value =  (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("voted")) : -1;
		cr.close();
		return value;
	}

	public void setVoted(int rsid, int vote) {
		String sql = "UPDATE solutions SET voted='"+vote+"' WHERE rsid="+rsid;
		db.execSQL(sql);
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
		String sql = "SELECT * FROM ideas WHERE status='"+status+"' ORDER BY pubDate DESC LIMIT 100";
		Cursor cr = db.rawQuery(sql, null);
		cr.moveToFirst();
		return cr;
	}
	
	public Cursor getSolucions(Integer iid) {
		String sql = "SELECT * FROM solutions WHERE iid='" + String.valueOf(iid) + "' ORDER BY sid ASC LIMIT 100";
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
	
	public void setUserPass(String user, String pass) {
		// It SHOULD work with the AccountManager, not with BD!
		String[] sqlBlock = new String[] {
				"UPDATE config SET value='"+user+"' WHERE key='Username'",
				"UPDATE config SET value='"+pass+"' WHERE key='Password'"
		};
		for (String sql : sqlBlock) { db.execSQL(sql); }
	}
	
	public String getUser() {
		String sql = "SELECT value FROM config WHERE key='Username'";
		Cursor cr = db.rawQuery(sql, null);
		String value =  (cr.moveToFirst()) ? cr.getString(cr.getColumnIndex("value")) : null;
		cr.close();
		return value;
	}
	
	public String getPass() {
		String sql = "SELECT value FROM config WHERE key='Password'";
		Cursor cr = db.rawQuery(sql, null);
		String value =  (cr.moveToFirst()) ? cr.getString(cr.getColumnIndex("value")) : null;
		cr.close();
		return value;
	}
	
	public void setToken(String token) {
		String sql = "UPDATE config SET value='"+token+"' WHERE key='MyToken'";
		db.execSQL(sql);
	}

	public String getToken() {
		String sql = "SELECT value FROM config WHERE key='MyToken'";
		Cursor cr = db.rawQuery(sql, null);
		String value = (cr.moveToFirst()) ? cr.getString(cr.getColumnIndex("value")).trim() : null;
		cr.close();
		return value;
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
					"CREATE TABLE config (key TEXT, value TEXT)",
					"INSERT INTO config (key, value) VALUES ('FirstTime', '1')",
					"INSERT INTO config (key, value) VALUES ('LastUpdateIdea', '1')",
					"INSERT INTO config (key, value) VALUES ('ID', '4')",
					"INSERT INTO config (key, value) VALUES ('MyToken', '')",
					"INSERT INTO config (key, value) VALUES ('Username', '')",
					"INSERT INTO config (key, value) VALUES ('Password', '')",
					"CREATE TABLE rss (id INT, name TEXT, lastAccess INTEGER, url TEXT, icon INT, enabled INT)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (0, 'Bloc Pirata', 0, 'http://pirata.cat/bloc/?feed=rss2',"+ R.drawable.ic_info_bloc +", 1)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (1, 'YouTube', 0, 'http://gdata.youtube.com/feeds/base/users/PiratesdeCatalunyaTV/uploads?alt=rss&v=2&orderby=published',"+ R.drawable.ic_info_youtube +", 1)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (2, 'Flickr', 0, 'http://api.flickr.com/services/feeds/groups_pool.gne?id=1529563@N23&lang=es-es&format=rss_200',"+ R.drawable.ic_info_flickr +", 1)",
					"INSERT INTO rss (id, name, lastAccess, url, icon, enabled) VALUES (3, 'PPInternational', 0, 'http://www.pp-international.net/rss.xml',"+ R.drawable.ic_info_ppinternational +", 1)",					
					"CREATE TABLE row (id INT, lastAccess INTEGER, body TEXT, followUrl TEXT)",
					
					"CREATE TABLE ideas (status TEXT, iid INT, pubDate INT, title TEXT, description TEXT)",
					"CREATE TABLE solutions (iid INT, sid INT, rsid INT, title TEXT, description TEXT, votes INT, voted INT DEFAULT 15)"
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
					"DROP TABLE IF EXISTS solucions",
					"DROP TABLE IF EXISTS comments",
					"DROP TABLE IF EXISTS ideas",
					"DROP TABLE IF EXISTS solutions"
			};

			for (String sql : sqlBlock) { db.execSQL(sql); }
			onCreate(db);
		}
	}
}

