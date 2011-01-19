package cat.pirata.extra;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CtrlJson {
	
	private static CtrlJson INSTANCE = null;
	
	public static CtrlJson getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CtrlJson();
		}
		return INSTANCE;
	}

	public SortedSet<StrIdea> getIdeas(Integer intStatus) {
		String status = String.valueOf(intStatus);
		SortedSet<StrIdea> ssbIdeas = new TreeSet<StrIdea>(new MyComparator_ideas_id());
		
		try {
			String content = CtrlFile.getInstance().readFile("json");
			JSONObject json = new JSONObject(content);
			
			if (json.has(status)) {
				json = new JSONObject(json.getString(status));
				JSONArray ideas = json.toJSONArray(json.names());
				
				for (int i=0; i < ideas.length(); i++) {
					
					StrIdea idea = new StrIdea();
					
					idea.status = ideas.getJSONObject(i).getInt("st");
					idea.id = ideas.getJSONObject(i).getInt("id");
					idea.pubDate = ideas.getJSONObject(i).getString("dt");
					idea.title = ideas.getJSONObject(i).getString("tt");
					idea.description = ideas.getJSONObject(i).getString("ds");
					idea.ssbSolution = new TreeSet<StrSolution>(new MyComparator_solutions_id());
					
					JSONObject tjson = new JSONObject(ideas.getJSONObject(i).getString("s"));
					JSONArray solutions = tjson.toJSONArray(tjson.names());
					
					for (int j=0; j < solutions.length(); j++) {
						if (!(status.equals("3") && solutions.getJSONObject(j).getInt("sl")!=1)) {
							
							StrSolution solution = new StrSolution();
							
							solution.status = solutions.getJSONObject(j).getInt("st");
							solution.id = solutions.getJSONObject(j).getInt("id");
							solution.sid = solutions.getJSONObject(j).getInt("sid");
							solution.title = solutions.getJSONObject(j).getString("tt");
							solution.description = solutions.getJSONObject(j).getString("ds");
							solution.votes = solutions.getJSONObject(j).getInt("vt");
							solution.sl = solutions.getJSONObject(j).getInt("sl");
							
							idea.ssbSolution.add(solution);
						}
					}
					ssbIdeas.add(idea);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ssbIdeas;
	}
	
	public SortedSet<StrRss> getRSS() {
		SortedSet<StrRss> ssRss = new TreeSet<StrRss>(new MyComparator_pubDate());
		try {
			String content = CtrlFile.getInstance().readFile("rss");
			JSONObject json = new JSONObject(content);
			JSONArray entry = json.toJSONArray(json.names());
				
			for (int i=0; i < entry.length(); i++) {
				StrRss rss = new StrRss();
				rss.id = entry.getJSONObject(i).getInt("id");
				rss.link = entry.getJSONObject(i).getString("link");
				rss.pubDate = entry.getJSONObject(i).getString("pubDate");
				rss.title = entry.getJSONObject(i).getString("title");
				ssRss.add(rss);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ssRss;
	}
	
	
	public SortedSet<StrComment> parseComments(String jsonStr) {
		SortedSet<StrComment> ssComment = new TreeSet<StrComment>(new MyComparator_pubDate_inv());
		try {
			JSONArray entry = new JSONArray(jsonStr);
			for (int i=0; i < entry.length(); i++) {
				StrComment comment = new StrComment();
				comment.author = entry.getJSONObject(i).getString("author");
				comment.pubDate = entry.getJSONObject(i).getString("pubDate");
				comment.description = entry.getJSONObject(i).getString("description");
				ssComment.add(comment);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ssComment;
	}
	
	// ----- PRIVATE
	// rss
	private class MyComparator_pubDate implements Comparator<StrRss> {
		public int compare(StrRss a, StrRss b) {
			String aStr = a.pubDate;
			String bStr = b.pubDate;
			return bStr.compareTo(aStr);
		}
	}
	
	// comments
	private class MyComparator_pubDate_inv implements Comparator<StrComment> {
		public int compare(StrComment a, StrComment b) {
			String aStr = a.pubDate;
			String bStr = b.pubDate;
			return aStr.compareTo(bStr);
		}
	}
	
	// ideas
	private class MyComparator_ideas_id implements Comparator<StrIdea> {
		public int compare(StrIdea a, StrIdea b) {
			Integer aa, bb;
			aa = Integer.valueOf(a.id);
			bb = Integer.valueOf(b.id);
			return bb.compareTo(aa);
		}
	}
	
	// solutions
	private class MyComparator_solutions_id implements Comparator<StrSolution> {
		public int compare(StrSolution a, StrSolution b) {
			Integer aa, bb;
			aa = Integer.valueOf(a.sid);
			bb = Integer.valueOf(b.sid);
			return aa.compareTo(bb);
		}
	}
}

