package cat.pirata.extra;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;
import android.widget.ProgressBar;


public class CtrlNet {

	private static CtrlNet INSTANCE = null;
	
	public static CtrlNet getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CtrlNet();
		}
		return INSTANCE;
	}
	
	public void update(ProgressBar pb) throws Exception {
		if (CtrlDb.getInstance().startUpdate()) {
			String url, body;
			url = "http://192.168.1.2/proxy.php?up="+System.currentTimeMillis();
			body = downloadBody(url);
			CtrlFile.getInstance().saveFile("json", body);
			pb.setProgress(50);
			
			url = "http://192.168.1.2/proxy.php?rss="+System.currentTimeMillis();
			body = downloadBody(url);
			CtrlFile.getInstance().saveFile("rss", body);
			pb.setProgress(100);
			CtrlDb.getInstance().endUpdate();
		}
	}
	
	public String getOnlineComment(Integer id) {
		return downloadBody("http://192.168.1.2/proxy.php?id=" + String.valueOf(id));
	}
	
	
	// url = "http://m.pirata.cat/request/postTest.php";
	
	public void sendNewComment(String data, Integer iid) {
		String post = "commennt_text="+data+"&_comment_submitted=true";
		String url = "https://xifrat.pirata.cat/ideatorrent/idea/"+String.valueOf(iid);
		String ret = doPost(url, post);
		Log.d("-PERFECT-", ret);
	}
	
	public String voteSolution(int rsid, int vote) {
		String post = "";
		String url = "https://xifrat.pirata.cat/ideatorrent/ajaxvote/"+rsid+"/"+vote;
		String ret = doPost(url, post);
		Log.d("-PERFECT-", ret);
		return ret;
	}

	public boolean tryAuth() {
		String user = CtrlDb.getInstance().getUser();
		String pass = CtrlDb.getInstance().getPass();
		if (user.equals("") || pass.equals("")) { return false; }

		String post = "name="+user+"&pass="+pass+"&op=Entra&form_id=user_login_block";
		String url = "https://xifrat.pirata.cat/ideatorrent?destination=ideatorrent";
		String myToken = doPost(url, post);
		Log.d("-PERFECT-", myToken);

		if (myToken.equals("")) { return false; }
		
		CtrlDb.getInstance().setToken(myToken);
		return true;
	}
	
	
	// ----- PRIVATE
	
    private String doPost(String urlString, String content) {
    	Log.d("urlString", urlString);
    	String body = "DefaultError";
    	Map<String, List<String>> mapHeader = null;
    	String token = CtrlDb.getInstance().getToken();
    	
    	try {
	    	trustEveryone();
	        URL url = new URL(urlString);
	        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	        con.setConnectTimeout(10000);
	        con.setRequestMethod("POST");
	        con.setRequestProperty("referer", urlString);
	        if (!token.equals("")) {
	        	con.setRequestProperty("cookie", token);
	        }
	        con.setDoOutput(true);
	        con.setDoInput(true);
	        con.connect();
	        
	        OutputStream out = con.getOutputStream();
	        byte[] buff = content.getBytes("UTF8");
	        out.write(buff); out.flush(); out.close();
	        
	        mapHeader = con.getHeaderFields();
//	        for (Entry<String, List<String>> y : mapHeader.entrySet()) {
//	        	Log.d("HEADER", y.getKey()+": "+y.getValue().toString());
//	        }
	        
	        // If Comment == I do not care
	        InputStream is = con.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 655350);
			ByteArrayBuffer baf = new ByteArrayBuffer(655350);
			int current = 0;
			while((current = bis.read()) != -1) {
				baf.append((byte)current);
			}
			body = new String(baf.toByteArray());
//			Log.d("body", body);
    	} catch (IOException e) {
    		e.printStackTrace();
    		return "";
    	}
    	
		if (content.equals("")) {
			return (body.equals("AJAXOK")) ? "OK" : "ERROR";
		} else if (body.indexOf("Ho sentim, el nom d'usuari o la contrasenya no s") == -1) {
			if (mapHeader.containsKey("set-cookie"))
				return mapHeader.get("set-cookie").get(mapHeader.get("set-cookie").size()-1).substring(0,69);
			else
				return token;
		} else {
			return "";
		}
    }

	private String downloadBody(String urlString) {
    	Log.d("downloadBody", urlString);
		try {
			URL myURL = new URL(urlString);
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
	
	// http://stackoverflow.com/questions/1217141/self-signed-ssl-acceptance-android
	private void trustEveryone() {
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
				public boolean verify(String hostname, SSLSession session) {
					return true;
			}});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[]{
				new X509TrustManager(){
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
					public X509Certificate[] getAcceptedIssuers() {	return new X509Certificate[0]; }
				}
			}, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}
}
