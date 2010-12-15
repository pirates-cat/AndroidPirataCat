package cat.pirata.widget;

import java.util.Calendar;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import cat.pirata.activities.PartitPirata;
import cat.pirata.activities.R;
import cat.pirata.utils.DbHelper;


public class WidgetProvider extends AppWidgetProvider {

	private DbHelper db;
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		db = new DbHelper(context);
// -- no auto-updates
//		RSS rss = new RSS(db);
//		rss.refreshLastNews();
		updateWidget(context);
		db.close();
	}

	public void updateWidget(Context context){
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		Cursor cr = db.getLastRow();
		String followUrl = context.getResources().getString(R.string.sourcecode);
		if (cr.moveToFirst()) {
			int id = cr.getInt(cr.getColumnIndex("id"));
			int icon = db.getIcon(id);
			long lastAccess = cr.getLong(cr.getColumnIndex("lastAccess"));
			String body = cr.getString(cr.getColumnIndex("body"));
			followUrl = cr.getString(cr.getColumnIndex("followUrl"));
			
			updateViews.setImageViewResource(R.id.widget_icon, icon);
			updateViews.setTextViewText(R.id.widget_text1, body);
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date( lastAccess ));
			updateViews.setTextViewText(R.id.widget_text2, String.format("%d %s", cal.get(Calendar.DATE), calMonth(cal.get(Calendar.MONTH))));
			updateViews.setTextViewText(R.id.widget_text3, followUrl);
		}
		cr.close();	
		
        Intent intent = new Intent(context, PartitPirata.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        
// -- should?
//		Uri uri = Uri.parse(followUrl);
//		intent = new Intent(Intent.ACTION_VIEW, uri);
//		pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//		updateViews.setOnClickPendingIntent(R.id.widget_llayout, pendingIntent);
     

		ComponentName myComponentName = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myComponentName, updateViews);
	}
	
	private String calMonth(int i) {
		String[] day = new String[] {
				"Gen","Feb","Mar","Abr","Mai","Jun","Jul","Ago","Set","Oct","Nov","Dec"};
		return day[i];
	}
}
