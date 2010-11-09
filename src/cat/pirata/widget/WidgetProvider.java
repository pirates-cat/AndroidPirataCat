package cat.pirata.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;
import cat.pirata.R;


public class WidgetProvider extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		updateWidget(context);
		//context.startService(new Intent(context, MyBatteryReceiver.class));
	}

	public void updateWidget(Context context){
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.widget);
		updateViews.setTextViewText(R.id.level, String.valueOf(context.hashCode()));

		ComponentName myComponentName = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myComponentName, updateViews);
	}

}
