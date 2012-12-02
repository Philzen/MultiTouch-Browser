package com.changeit.mtbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;
import com.changeit.wmpolyfill.WebClient;
import com.changeit.wmpolyfill.helper.Alert;

public class MultitouchBrowser extends Activity
{

    WebView webview;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	webview = new WebView(this);
	WebClient wmp = new WebClient(webview);

	// Hide the status bar at the top
	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	// Adds Progress bar Support
	getWindow().requestFeature(Window.FEATURE_PROGRESS);
	// Makes Progress bar Visible
	getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

	final Activity MyActivity = this;
	WebChromeClient wcc = new WebChromeClient()
	{
	    @Override
	    public void onProgressChanged(WebView view, int progress)
	    {
		//Make the bar disappear after URL is loaded, and changes string to Loading...
		MyActivity.setTitle("Loading " + view.getUrl() + " ... ");
		MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded

		// Return the app name after finish loading
		if (progress == 100) {
		    MyActivity.setTitle(R.string.app_name);
		}
	    }

	    @Override
	    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback)
	    {
		callback.invoke(origin, true, false);
	    }

	    @Override	// enable alert javascript, will generate native Android alert
	    public boolean onJsAlert(WebView view, String url, String message, JsResult result)
	    {
		Alert alert = new Alert(view);
		alert.show(message + ", Javascript Result [" + result.toString() + "];");
		return true;
	    }
	};

	webview.setWebChromeClient(wcc);
	webview.setWebViewClient(wmp);
	webview.loadUrl("http://maps.google.de");
    }

    /**
     * Getting the back button to work
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
	if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
	    webview.goBack();
	    return true;
	}
	return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	menu.add(2, 100, Menu.FIRST + 0, "Open Streetmap");
	menu.add(2, 101, Menu.FIRST + 1, "Google Maps").setIcon(R.drawable.googlemaps);
	menu.add(2, 102, Menu.FIRST + 2, "Terrestris");
	menu.add(1, 1, Menu.FIRST + 3, "Add");
	menu.add(1, 0, Menu.FIRST + 4, "Go to URL...");

	menu.add(2, 6, Menu.CATEGORY_ALTERNATIVE + 0, "Settings");
	menu.add(2, 7, Menu.CATEGORY_ALTERNATIVE + 1, "Manage Bookmarks");
	menu.add(2, 8, Menu.CATEGORY_ALTERNATIVE + 2, "Item8");

	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	String[] urls = new String[8];
	urls[0] = "http://openlayers.org/dev/examples/mobile.html";
	urls[1] = "http://maps.google.de";
	urls[2] = "http://ows.terrestris.de/webgis-client/index.html";
	
	Alert alert = new Alert(webview);
	if (item.getGroupId() == 2) {
	    alert.show("Loading " + urls[item.getItemId()-100]);
	    webview.loadUrl(urls[item.getItemId()-100]);
	} else {
	    alert.show("you clicked on item " + item.getTitle());
	}
	
	return super.onOptionsItemSelected(item);
    }
}
