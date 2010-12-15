package cat.pirata.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;

import android.database.Cursor;
import android.text.Html;
import android.util.Log;

public class Net {

	public static final int numMonthsOfOldNews = -2;
	
	private DbHelper db;

	
	// -PUBLIC-

	public Net(DbHelper db) {
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
			String rss = downloadBody(url);
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
		Log.d("(1)TIME", String.valueOf(time));
		
		String str = downloadBody("http://m.pirata.cat/request/ideatorrentControl.php?time=" + String.valueOf(time));
		// Log.d("query", (new JSONObject(str)).toString(2));
		db.ideaUpdate(str);
		time = Integer.valueOf(String.format("%s", System.currentTimeMillis()).substring(0,10));

		Log.d("(2)TIME", String.valueOf(time));
		db.setIdeaLastUpdate(time);
	}

	public void setUserPass(String user, String pass) {
		db.setUserPass(user, pass);
	}
	
	public boolean isAuth() {
		String myToken = db.getToken();
		if (myToken.equals("")) return false;
		return true;
	}

	public boolean tryAuth() {
		String user = db.getUser();
		String pass = db.getPass();
		Log.d("user", user);
		Log.d("pass", pass);
		if (user.equals("") || pass.equals("")) { return false; }

		String myToken = getNewToken(user, pass);
		if (myToken.equals("")) { return false; }
		
		db.setToken(myToken);
		return true;
	}

	public String getOnlineComment(int iid) {
		return downloadBody("http://m.pirata.cat/request/ideatorrentControl.php?id=" + String.valueOf(iid));
	}
	

	public String vote(int rsid, int vote) {
		String post = "";
		String url = "https://xifrat.pirata.cat/ideatorrent/ajaxvote/"+rsid+"/"+vote;
		// url = "http://m.pirata.cat/request/postTest.php";
		
		String ret = "";
		try {
			ret = doPost(url, post);
			Log.d("-VOTE-", ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void sendNewComment(String data, Integer iid) {		
		String post = "commennt_text="+data+"&_comment_submitted=true";
		String url = "https://xifrat.pirata.cat/ideatorrent/idea/"+String.valueOf(iid);
		// url = "http://m.pirata.cat/request/postTest.php";
		
		String ret = "";
		try {
			ret = doPost(url, post);
			Log.d("-PERFECT-", ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// -PRIVATE-

	private String getNewToken(String user, String pass) {
		String post = "name="+user+"&pass="+pass+"&op=Entra&form_id=user_login_block";
		String url = "https://xifrat.pirata.cat/ideatorrent?destination=ideatorrent";
		// url = "http://m.pirata.cat/request/postTest.php";
		
		String ret = "";
		try {
			ret = doPost(url, post);
			Log.d("-PERFECT-", ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
    private String doPost(String urlString, String content) throws IOException {
    	trustEveryone();
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setConnectTimeout(10000);
        con.setRequestMethod("POST");
        con.setRequestProperty("referer", urlString);
        String cookie = db.getToken();
        if (!cookie.equals("")) {
        	con.setRequestProperty("cookie", cookie);
        }
        con.setDoOutput(true);
        con.setDoInput(true);
        con.connect();
        
        OutputStream out = con.getOutputStream();
        byte[] buff = content.getBytes("UTF8");
        out.write(buff); out.flush(); out.close();
        
        Map<String, List<String>> mapHeader = con.getHeaderFields();
        for (Entry<String, List<String>> y : mapHeader.entrySet()) {
        	Log.d("HEADER", y.getKey()+": "+y.getValue().toString());
        }
        
        // If Comment == I do not care
        InputStream is = con.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is, 655350);
		ByteArrayBuffer baf = new ByteArrayBuffer(655350);
		int current = 0;
		while((current = bis.read()) != -1) {
			baf.append((byte)current);
		}
		String body = new String(baf.toByteArray());
		Log.d("body", body);
		
		if (content.equals("")) {
			if (body.equals("AJAXOK"))
				return "OK";
			else
				return "ERROR";
		} else if (body.indexOf("Ho sentim, el nom d'usuari o la contrasenya no s") == -1) {
			if (mapHeader.containsKey("set-cookie"))
				return mapHeader.get("set-cookie").get(mapHeader.get("set-cookie").size()-1).substring(0,69);
			else
				return db.getToken();
		} else {
			return "";
		}
    }

	private String downloadBody(String url) {
		try {
			URL myURL = new URL(url);
			URLConnection ucon = myURL.openConnection();
			ucon.setConnectTimeout(10000);
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
	
    // http://stackoverflow.com/questions/1217141/self-signed-ssl-acceptance-android
   private void trustEveryone() {
    	try {
    		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
        			public boolean verify(String hostname, SSLSession session) {
        				return true;
        			}});
    		SSLContext context = SSLContext.getInstance("TLS");
    		context.init(null, new X509TrustManager[]{new X509TrustManager(){
    			public void checkClientTrusted(X509Certificate[] chain,
    					String authType) throws CertificateException {}
    			public void checkServerTrusted(X509Certificate[] chain,
    					String authType) throws CertificateException {}
    			public X509Certificate[] getAcceptedIssuers() {
    				return new X509Certificate[0];
    			}}}, new SecureRandom());
    		HttpsURLConnection.setDefaultSSLSocketFactory(
    				context.getSocketFactory());
    	} catch (Exception e) { // should never happen
    		e.printStackTrace();
    	}
    }
}
