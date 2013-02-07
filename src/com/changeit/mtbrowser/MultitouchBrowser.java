package com.changeit.mtbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.changeit.wmpolyfill.WebClient;
import com.changeit.wmpolyfill.helper.Alert;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultitouchBrowser extends Activity
{

	private EditText urlTextInput;
	private LinearLayout AddressBar;
	private Menu menu;
	WebView webview;
	Boolean webviewVisible;
	protected WebClient webviewMultitouchPolyfill;
	protected WebChromeClient wcc;
	protected FrameLayout webViewPlaceholder;
	private boolean stateIsLoading = false;
	final String[] urls;
	final String[] urlNames;

	public MultitouchBrowser()
	{
		urls = new String[]{
			null,
			"http://openlayers.org/dev/examples/mobile.html",
			"http://maps.google.de",
			"http://ows.terrestris.de/webgis-client/index.html",
			"http://help.arcgis.com/en/webapi/javascript/arcgis/samples/mobile_simplemap/index.html",
			null,
			"http://space.remvst.com/",
			"https://developer.mozilla.org/de/demos/detail/kite-flying/launch",
			null,
			"http://seb.ly/demos/JSTouchController/TouchControl.html",
			"http://paulirish.com/demo/multi",
			"http://spark.attrakt.se/"
		};
		urlNames = new String[]{
			"Mapping Applications:",
			"		Open Streetmap",
			"		Google Maps",
			"		Terrestris",
			"		ArcGis Mobile Example",
			"Games:",
			"		Space Fight",
			"		Kite Flying",
			"Multitouch Visualisation:",
			"		Asteroids Controller",
			"		Fingerpainting",
			"		Multitouch Sparks"
//	    "Leaflet Mobile Demo",
//	    "Modest Maps",
//	    "VisualMobility.tk (Leaflet)"
		};
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		//To change body of generated methods, choose Tools | Templates.
	}

	public void showUrlBar(boolean focus)
	{
		AddressBar.setVisibility(View.VISIBLE);
		if (focus) {
			AddressBar.requestFocus();
		}
	}

	public void hideUrlBar()
	{
		AddressBar.setVisibility(View.GONE);
	}

	public void toggleAddressBar(boolean focus)
	{
		if (AddressBar.getVisibility() == View.VISIBLE) {
			hideUrlBar();
		} else {
			showUrlBar(focus);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getY() < 100) {
			showUrlBar(false);
		} else {
			hideUrlBar();
		}

		return super.onTouchEvent(event);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Hide the status bar at the top
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// Adds Progress bar Support
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		// Makes Progress bar Visible
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

		setContentView(R.layout.main);
		initUI();
		showLinkList();
	}

	protected void initUI()
	{
		AddressBar = ((LinearLayout) findViewById(R.id.UrlInputWrapper));
		webViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder));
		if (webview == null) {
			final Activity MyActivity = this;
			urlTextInput = (EditText) findViewById(R.id.UrlInput);
			wcc = new WebChromeClient()
			{
				@Override
				public void onProgressChanged(WebView view, int progress)
				{
					// Return the app name after finish loading
					if (stateIsLoading == false) {
						//Make the bar disappear after URL is loaded, and changes string to Loading...
						MyActivity.setTitle("Loading ... ");
						showUrlBar(false);
						stateIsLoading = true;
					} else if (progress == 100) {
						MyActivity.setTitle(view.getTitle());
						hideUrlBar();
						stateIsLoading = false;
					} else if (progress > 10 && urlTextInput.hasFocus() == false) {
						urlTextInput.setText(view.getUrl());
					}
					MyActivity.setProgress(progress * 100); //Make the bar disappear after URL is loaded
				}

				@Override
				public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback)
				{
					showConfirmGeolocation(origin, callback);
				}

				@Override	// enable alert javascript, will generate native Android alert
				public boolean onJsAlert(WebView view, String url, String message, JsResult result)
				{
					Alert alert = new Alert(view);
					alert.show(message);
					result.confirm();
					return true;
				}

				@Override
				public void onConsoleMessage(String message, int lineNumber, String sourceID)
				{
					// TODO Auto-generated method stub
					Log.v("console", "invoked: onConsoleMessage() - " + sourceID + ":"
							+ lineNumber + " - " + message);
					super.onConsoleMessage(message, lineNumber, sourceID);
				}

				@Override
				public boolean onConsoleMessage(ConsoleMessage cm)
				{
					Log.v("console", cm.message() + " -- From line "
							+ cm.lineNumber() + " of "
							+ cm.sourceId());
					return true;
				}
			};

			webview = new WebView(this);
			webview.getSettings().setNavDump(true);
			// remove white invisible scrollbar which otherwise generated white bar on the right side
			webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

			String TAG = "HTML 5";
			WebSettings ws = webview.getSettings();
			ws.setAllowFileAccess(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
				try {
					Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
					m1.invoke(ws, Boolean.TRUE);

					Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
					m2.invoke(ws, Boolean.TRUE);

					Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
					m3.invoke(ws, "/data/data/" + getPackageName() + "/databases/");

					// Set cache size to 8 mb by default. should be more than enough
					Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{Long.TYPE});
					m4.invoke(ws, 1024 * 1024 * 8);

					Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{String.class});
					m5.invoke(ws, "/data/data/" + getPackageName() + "/cache/");

					Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{Boolean.TYPE});
					m6.invoke(ws, Boolean.TRUE);

					Log.d(getPackageName(), "Enabled HTML5-Features");
				} catch (NoSuchMethodException e) {
					Log.e(getPackageName(), "Reflection fail", e);
				} catch (InvocationTargetException e) {
					Log.e(getPackageName(), "Reflection fail", e);
				} catch (IllegalAccessException e) {
					Log.e(getPackageName(), "Reflection fail", e);
				}

				webviewMultitouchPolyfill = new WebClient(webview);
				webviewMultitouchPolyfill.setPolyfillAllTouches(false);
				webview.setWebChromeClient(wcc);
			}

			webViewPlaceholder.addView(webview);
			initUrlBox();
		}
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
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (webview.canGoBack() && webview.isShown()) {
				webview.goBack();
				return true;
			}
			showExitDialog();
			return false;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			toggleAddressBar(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.menu = menu;
		menu.add(1, 4, Menu.FIRST + 0, "Multitouch").setIcon(android.R.drawable.checkbox_on_background).setChecked(true);
		menu.add(1, 3, Menu.FIRST + 1, "Polyfill all").setIcon(android.R.drawable.checkbox_off_background).setChecked(false);
		menu.add(1, 2, Menu.FIRST + 2, "Cool Links").setIcon(android.R.drawable.ic_menu_mapmode);
//	menu.add(1, 1, Menu.FIRST + 3, "Add").setEnabled(false);
//	menu.add(1, 0, Menu.FIRST + 4, "Go to URL...");
//	menu.add(1, 5, Menu.FIRST + 5, "Preferences");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Alert alert = new Alert(webview);
		if (item.getGroupId() == 2) {
			this.loadUrl(urls[item.getItemId() - 100]);
		} else {
			if (item.getItemId() == 0) {
				showUrlBar(true);
			} else if (item.getItemId() == 2) {
				showLinkList();
			} else if (item.getItemId() == 3) {
				webviewMultitouchPolyfill.setPolyfillAllTouches(!item.isChecked());
				toggleMenuCheckbox(item);
			} else if (item.getItemId() == 4) {
				if (item.isChecked()) {
					menu.findItem(3).setEnabled(false);
					webviewMultitouchPolyfill.setEnabled(false);
				} else {
					menu.findItem(3).setEnabled(true);
					webviewMultitouchPolyfill.setEnabled(true);
				}
				toggleMenuCheckbox(item);
			} else {
				alert.show("you clicked on item " + item.getTitle());
			}
		}

		return super.onOptionsItemSelected(item);
	}

	protected boolean toggleMenuCheckbox(MenuItem item)
	{
		if (item.isChecked()) {
			item.setIcon(android.R.drawable.checkbox_off_background);
			item.setChecked(false);
			return false;
		} else {
			item.setIcon(android.R.drawable.checkbox_on_background);
			item.setChecked(true);
			return true;
		}
	}

	protected void loadUrl(String url)
	{
		urlTextInput.setText(url, TextView.BufferType.NORMAL);
		webview.loadUrl(url);
		Log.d(getPackageName(), "loadUrl " + url);
	}

	protected void showExitDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("You want to proceed and close the Multitouch Browser ?")
				.setTitle("Close");
		final Activity MyActivity = this;
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				MyActivity.finish();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	protected void showConfirmGeolocation(final String origin, final GeolocationPermissions.Callback callback)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Allow " + webview.getUrl() + " to access you position ?")
				.setTitle("Geolocation");
		builder.setPositiveButton("Locate Me", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				callback.invoke(origin, true, false);
			}
		});
		builder.setNegativeButton("Deny", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public void showLinkList()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Cool Multitouch Links")
				.setItems(urlNames, null);
		final AlertDialog d = builder.create();
		d.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog)
			{

				ListView b = d.getListView();
				b.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						// TODO Do something

						//Dismiss once everything is OK.
						// d.dismiss();
						if (urls[position] != null) {
							d.dismiss();
							loadUrl(urls[position]);
						}

					}
				});
			}
		});
		d.show();
	}

	public void initUrlBox()
	{
		urlTextInput.setOnKeyListener(new View.OnKeyListener()
		{
			public boolean onKey(View arg0, int keyCode, KeyEvent arg2)
			{
				if ((keyCode == KeyEvent.KEYCODE_ENTER)) {
					InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					in.hideSoftInputFromWindow(urlTextInput.getApplicationWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					loadUrl(getSafeUrl(urlTextInput.getText().toString()));
					return true;
				}
				return false;
			}
		});
	}

	private String getSafeUrl(String url)
	{
		if (!url.startsWith("http")) {
			while (url.startsWith("/") || url.startsWith(":")) {
				url = url.substring(1);
			}
			url = "http://" + url;
		}
		return url;
	}
}
