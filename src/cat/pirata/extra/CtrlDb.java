package cat.pirata.extra;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CtrlDb {
	
	private static CtrlDb INSTANCE = null;
	private SQLiteDatabase db = null;
	
	public static CtrlDb getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CtrlDb();
		}
		return INSTANCE;
	}

	public void setContext(Context context) {
		db = (new DatabaseHelper(context)).getWritableDatabase();
	}

	public void close() {
		db.close();
	}
	
	// ----------------------------------------------------------

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

	public int getIcon(Integer id) {
		String sql = "SELECT icon FROM rss WHERE id="+id+" LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		int value =  (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("icon")) : -1;
		cr.close();
		return value;
	}

	public int getVoted(Integer id) {
		String sql = "SELECT voted FROM solutions WHERE id="+id+" LIMIT 1";
		Cursor cr = db.rawQuery(sql, null);
		int value = (cr.moveToFirst()) ? cr.getInt(cr.getColumnIndex("voted")) : -2;
		cr.close();
		return value;
	}

	public void setVoted(Integer id, int vote) {
		String sql;
		if (getVoted(id) != -2) {
			sql = "UPDATE solutions SET voted='"+vote+"' WHERE id="+id;
		} else {
			sql = "INSERT INTO solutions (id, voted) VALUES ("+id+","+vote+")";
		}
		db.execSQL(sql);
	}
	
	public String getToken() {
		String sql = "SELECT value FROM config WHERE key='MyToken'";
		Cursor cr = db.rawQuery(sql, null);
		String value = (cr.moveToFirst()) ? cr.getString(cr.getColumnIndex("value")).trim() : null;
		cr.close();
		return value;
	}

	public void setToken(String token) {
		String sql = "UPDATE config SET value='"+token+"' WHERE key='MyToken'";
		db.execSQL(sql);
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
	
	public void setUserPass(String user, String pass) {
		// It SHOULD work with the AccountManager, not with BD!
		String[] sqlBlock = new String[] {
				"UPDATE config SET value='"+user+"' WHERE key='Username'",
				"UPDATE config SET value='"+pass+"' WHERE key='Password'"
		};
		for (String sql : sqlBlock) { db.execSQL(sql); }
	}

	public boolean startUpdate() {
		String sql = "SELECT value FROM config WHERE key='LastUpdate'";
		Cursor cr = db.rawQuery(sql, null);
		Long value = (cr.moveToFirst()) ? cr.getLong(cr.getColumnIndex("value")) : 0L;
		cr.close();
		Calendar calOld = Calendar.getInstance();
		Calendar calNow = Calendar.getInstance();
		calOld.setTimeInMillis(value);
		calNow.setTimeInMillis(System.currentTimeMillis());
		calOld.roll(Calendar.MINUTE, 3);
		calOld.get(Calendar.MINUTE);
		return calOld.before(calNow);
	}

	public void endUpdate() {
		String sql = "UPDATE config SET value='"+System.currentTimeMillis()+"' WHERE key='LastUpdate'";
		db.execSQL(sql);
	}
}

