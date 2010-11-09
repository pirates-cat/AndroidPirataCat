package cat.pirata.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


public class CalendariActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// "https://www.google.com/calendar/embed?title=Calendari%20d%27events%20del%20Partit%20Pirata&showTitle=0&showPrint=0&showTabs=0&showCalendars=0&showTz=0&height=600&wkst=2&hl=ca&bgcolor=%23ffffff&src=998h7jra4pnmlorrm8vg7hhaho%40group.calendar.google.com&color=%235229A3&ctz=Europe%2FMadrid";
		String url = "https://www.google.com/calendar/hosted/dario.im/embed?showTitle=0&showNav=0&showPrint=0&showCalendars=0&showTz=0&mode=AGENDA&height=300&wkst=2&bgcolor=%23FFFFFF&src=998h7jra4pnmlorrm8vg7hhaho%40group.calendar.google.com&color=%23182C57&ctz=Europe%2FMadrid";
		WebView webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);

		webview.loadUrl(url);
		setContentView(webview);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}