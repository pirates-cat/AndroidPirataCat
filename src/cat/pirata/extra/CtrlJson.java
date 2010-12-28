package cat.pirata.extra;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class CtrlJson {
	
	private static CtrlJson INSTANCE = null;
	
	public static CtrlJson getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CtrlJson();
		}
		return INSTANCE;
	}

	public SortedSet<Bundle> getIdeas(Integer intStatus) {
		String status = String.valueOf(intStatus);
		SortedSet<Bundle> v = new TreeSet<Bundle>(new MyComparator_id());
		
		try {
			String content = CtrlFile.getInstance().readFile("json");
			JSONObject json = new JSONObject(content);
			
			if (json.has(status)) {
				json = new JSONObject(json.getString(status));
				JSONArray ideas = json.toJSONArray(json.names());
				
				for (int i=0; i < ideas.length(); i++) {
					Bundle b = new Bundle();
					b.putString("st", ideas.getJSONObject(i).getString("st"));
					b.putString("id", ideas.getJSONObject(i).getString("id"));
					b.putString("dt", ideas.getJSONObject(i).getString("dt"));
					b.putString("tt", ideas.getJSONObject(i).getString("tt"));
					b.putString("ds", ideas.getJSONObject(i).getString("ds"));
					
					Bundle s = new Bundle();
					
					JSONObject tjson = new JSONObject(ideas.getJSONObject(i).getString("s"));
					JSONArray solutions = tjson.toJSONArray(tjson.names());
					
					for (int j=0; j < solutions.length(); j++) {
						if (!(status.equals("3") && solutions.getJSONObject(j).getInt("sl")!=1)) {
							Bundle bb = new Bundle();
							bb.putString("st", solutions.getJSONObject(j).getString("st"));
							bb.putString("id", solutions.getJSONObject(j).getString("id"));
							bb.putString("sid", solutions.getJSONObject(j).getString("sid"));
							bb.putString("tt", solutions.getJSONObject(j).getString("tt"));
							bb.putString("ds", solutions.getJSONObject(j).getString("ds"));
							bb.putString("vt", solutions.getJSONObject(j).getString("vt"));
							bb.putString("sl", solutions.getJSONObject(j).getString("sl"));
							s.putBundle(solutions.getJSONObject(j).getString("sid"), bb);
						}
					}
					b.putBundle("s", s);
					v.add(b);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return v;
	}
	
	public SortedSet<Bundle> getRSS() {
		SortedSet<Bundle> v = new TreeSet<Bundle>(new MyComparator_pubDate());
		try {
			String content = CtrlFile.getInstance().readFile("rss");
			JSONObject json = new JSONObject(content);
			JSONArray entry = json.toJSONArray(json.names());
				
			for (int i=0; i < entry.length(); i++) {
				Bundle b = new Bundle();
				b.putString("id", entry.getJSONObject(i).getString("id"));
				b.putString("link", entry.getJSONObject(i).getString("link"));
				b.putString("pubDate", entry.getJSONObject(i).getString("pubDate"));
				b.putString("title", entry.getJSONObject(i).getString("title"));
				v.add(b);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return v;
	}
	
	
	public SortedSet<Bundle> parseComments(String jsonStr) {
		SortedSet<Bundle> v = new TreeSet<Bundle>(new MyComparator_pubDate());
		try {
			JSONArray entry = new JSONArray(jsonStr);
			for (int i=0; i < entry.length(); i++) {
				Bundle b = new Bundle();
				b.putString("author", entry.getJSONObject(i).getString("author"));
				b.putString("pubDate", entry.getJSONObject(i).getString("pubDate"));
				b.putString("description", entry.getJSONObject(i).getString("description"));
				v.add(b);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return v;
	}
	
	// ----- PRIVATE
	private class MyComparator_pubDate implements Comparator<Bundle> {
		public int compare(Bundle a, Bundle b) {
			String aStr = a.getString("pubDate");
			String bStr = b.getString("pubDate");
			return bStr.compareTo(aStr);
		}
	}
	
	private class MyComparator_id implements Comparator<Bundle> {
		public int compare(Bundle a, Bundle b) {
			Integer aa, bb;
			if (a.containsKey("sid")) {
				aa = Integer.valueOf(a.getString("sid"));
				bb = Integer.valueOf(b.getString("sid"));
			} else {
				aa = Integer.valueOf(a.getString("id"));
				bb = Integer.valueOf(b.getString("id"));
			}
			return aa.compareTo(bb);
		}
	}
}

