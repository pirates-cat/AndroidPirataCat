package cat.pirata.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;

import android.database.Cursor;
import android.text.Html;
import android.util.Log;

public class RSS {

	public static final int numMonthsOfOldNews = -1;
	
	private DbHelper db;

	
	// -PUBLIC-

	public RSS(DbHelper db) {
		this.db = db;
	}

	public Cursor getLastNews(int numlastnews) {
		return db.getLastNews(numlastnews);
	}

	public void refreshLastNews() {
		Cursor cr = db.getRssEnabled();
		do {
			String url = cr.getString(cr.getColumnIndex("url"));
			int id = cr.getInt(cr.getColumnIndex("id"));
			String rss = downloadRSS(url);
			if (rss != null) {
				Log.d("<DOWNLOAD>", url);
				Long lastTime = db.getLastStr(id);
				int numDownload = parseRSS(id, rss, lastTime);
				Log.d("</DOWNLOAD>", String.valueOf(numDownload));
			}
		} while (cr.moveToNext());
		cr.close();
	}

	public void clearOldNews() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date( System.currentTimeMillis() ));
		cal.roll(Calendar.MONTH, numMonthsOfOldNews);
		db.clearOldNews( cal.getTimeInMillis() );
	}
	
	public void ideaUpdate() throws JSONException {
		int time = db.getIdeaLastUpdate();
		Log.d("1TIME", String.valueOf(time));
		
		String str = downloadRSS("http://192.168.1.5/export.php?time=" + String.valueOf(time));
		db.ideaUpdate(str);
		time = Integer.valueOf(String.format("%s", System.currentTimeMillis()).substring(0,10));

		Log.d("2TIME", String.valueOf(time));
		db.setIdeaLastUpdate(time);
	}
	
	



	// -PRIVATE-

	private int parseRSS(int id, String rss, Long lastTime) {
		int ret = 0;
		int start, end = 0;
		int nextBlock = rss.indexOf("<item>", 0);
		String[] title = new String[] { "title", "link", "pubDate" };
		String[] value = new String[title.length];

		while (nextBlock != -1) {
			for (int i = 0; i < title.length; i++) {
				start = rss.indexOf(title[i], nextBlock) + title[i].length() + 1;
				end = rss.indexOf(title[i], start)-2;
				value[i] = rss.subSequence(start, end).toString();
			}
			Long pubDate = strDateToLong(value[2]);
			String StrpubDate = String.valueOf(pubDate).trim();
			String StrlastTime = String.valueOf(lastTime).trim();
//			Log.d("<lastTime>", String.valueOf(StrpubDate));
//			Log.d("<lastTime>", String.valueOf(StrlastTime));
			if (StrpubDate.compareTo(StrlastTime)==0) {
				break;
			}
			if (ret==0) {
				db.updateFieldFromRSS(id, "lastAccess", pubDate);
			}
			db.insertRow(id, pubDate, Html.fromHtml(value[0]).toString(), value[1]);
			nextBlock = rss.indexOf("<item>", nextBlock+1);
			ret++;
		}
		return ret;
	}

	public String downloadRSS(String url) {
		try {
			URL myURL = new URL(url);
			URLConnection ucon = myURL.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 65535);
			ByteArrayBuffer baf = new ByteArrayBuffer(65535);
			int current = 0;
			while((current = bis.read()) != -1) {
				baf.append((byte)current);
			}
			return new String(baf.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return new String();
		}
	}

	private long strDateToLong(String lastBuildDateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		Date d;
		try {
			d = sdf.parse(lastBuildDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
		return d.getTime();
	}
}
