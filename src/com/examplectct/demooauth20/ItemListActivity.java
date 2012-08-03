package com.examplectct.demooauth20;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.auth.InvalidCredentialsException;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ctctlabs.ctctwsjavalib.CTCTConnection;
import com.ctctlabs.ctctwsjavalib.Contact;
import com.ctctlabs.ctctwsjavalib.ModelObject;

public class ItemListActivity extends FragmentActivity
        implements ItemListFragment.Callbacks {

    private static final String TAG				= ItemListActivity.class.getSimpleName();
    private static final String AUTHORIZE_PATH	= "https://oauth2.constantcontact.com/oauth2/oauth/siteowner/authorize";
    private static final String CLIENT_ID		= ""; // ** enter your own Constant Contact developer API Key here **
    private static final String REDIRECT_URI	= "https://localhost";
    private static final String SHPREF_KEY_ACCESS_TOKEN
    											= "Access_Token";
    
    static final String NAME_KEY				= "Name";
    static final String EMAILADDRESS_KEY		= "EmailAddress";
    
    static ArrayList<HashMap<String,String>> contactHashmaps;
    
    private boolean mTwoPane;
	private WebView webview;
	private Fragment fragment;
	private View fragmentView;
	private String accessToken;
	private String userName;

    private class MyWebservicesAsyncTask extends AsyncTask<String, Void, Boolean> {
    	@Override
		protected Boolean doInBackground(String... params) {
			String accessToken = params[0];
			
			CTCTConnection conn = new CTCTConnection();
			try {
				// authenticate with access_token obtained
				userName = conn.authenticateOAuth2(accessToken);
				
				// api call to get Contacts for username list id 1
				if (userName != null) {
					String link = "/ws/customers/"+userName+"/lists/1";
					
					ArrayList<ModelObject> contacts = conn.getContactListMembers(link).getLoadedEntries();
					
					// load cache for display in list
					contactHashmaps = new ArrayList< HashMap<String, String> >();
					HashMap<String, String> hmap;
					for (ModelObject mo: contacts) {
						String name = (String) ((Contact) mo).getAttribute(NAME_KEY);
						String email   = (String) ((Contact) mo).getAttribute(EMAILADDRESS_KEY);
						hmap = new HashMap<String, String>();
						hmap.put(NAME_KEY, name);
						hmap.put(EMAILADDRESS_KEY, email);
						contactHashmaps.add(hmap);
					}
					
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (InvalidCredentialsException e) {
				e.printStackTrace();
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// called in UI thread
			if (result) {
				mShowContacts();
			}
			super.onPostExecute(result);
		}
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_item_list);
        webview = (WebView) findViewById(R.id.item_webview);
        fragment = ((Fragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_list));
        fragmentView = fragment.getView();
        
        // check whether access token already saved 
        accessToken = getPreferences(Context.MODE_PRIVATE).getString(SHPREF_KEY_ACCESS_TOKEN, null);
        if (accessToken == null) {
        	
        	// need to get access token with OAuth2.0
            fragmentView.setVisibility(View.GONE);
            webview.setVisibility(View.VISIBLE);
            // set up webview for OAuth2 login
            webview.setWebViewClient(new WebViewClient() {
            	@Override
            	public boolean shouldOverrideUrlLoading(WebView view, String url) {
            		//Log.d(TAG, "** in shouldOverrideUrlLoading(), url is: " + url);
            		if ( url.startsWith(REDIRECT_URI) ) {
            			
            			// extract OAuth2 access_token appended in url
            			if ( url.indexOf("access_token=") != -1 ) {
            				accessToken = mExtractToken(url);

            				// store in default SharedPreferences
            				Editor e = getPreferences(Context.MODE_PRIVATE).edit();
            				e.putString(SHPREF_KEY_ACCESS_TOKEN, accessToken);
            				e.commit();
            				
            				// spawn worker thread to do api calls to get list of contacts to display
            				new MyWebservicesAsyncTask().execute(accessToken);
            			}

            			// don't go to redirectUri
            			return true;
            		}

            		// load the webpage from url (login and grant access)
            		return super.shouldOverrideUrlLoading(view, url); // return false; 
            	}
            });
            
            // do OAuth2 login
            String authorizationUri = mReturnAuthorizationRequestUri();
            webview.loadUrl(authorizationUri);
            
        } else {
        	
        	// have access token, so spawn worker thread to do api calls to get list of contacts to display
        	new MyWebservicesAsyncTask().execute(accessToken);
        }
    }

    private String mExtractToken(String url) {
    	// url has format https://localhost/#access_token=<tokenstring>&token_type=Bearer&expires_in=315359999
    	String[] sArray = url.split("access_token=");
    	return (sArray[1].split("&token_type=Bearer"))[0];
    }
    
    private void mShowContacts() {
    	webview.setVisibility(View.GONE);
    	
    	ItemListFragment fragment = ((ItemListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_list));
    	if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            fragment.setActivateOnItemClick(true);
        }  
    	
    	fragment.doSetListAdapter();
    	
    	
    	fragmentView.setVisibility(View.VISIBLE);
    }
    
    private String mReturnAuthorizationRequestUri() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(AUTHORIZE_PATH);
    	sb.append("?response_type=token");
    	sb.append("&client_id="+CLIENT_ID);
    	sb.append("&redirect_uri="+REDIRECT_URI);
    	return sb.toString();
    }
    
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

}
