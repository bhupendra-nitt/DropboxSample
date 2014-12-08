package com.example.dropboxloginsample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AppKeyPair;

public class MainActivity extends Activity {
	private static final String APP_KEY = "YOUR_APP_KEY";
	private static final String APP_SECRET = "YOUR_APP_SECRET";

	private DropboxAPI<AndroidAuthSession> mDBApi;
	private AppKeyPair mAppKeys;
	private AndroidAuthSession mSession;
	private ListView mDirectoryList;
	private ArrayAdapter<String> mDirectoryAdapter;
	private List<String> mDir;
	private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        mSession = new AndroidAuthSession(mAppKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(mSession);
        mDBApi.getSession().startOAuth2Authentication(MainActivity.this);

        mDirectoryList = (ListView) findViewById(R.id.directory_list);
    }

	@Override
	protected void onResume() {
		super.onResume();
		if (mDBApi.getSession().authenticationSuccessful()) {
			try {
				mDBApi.getSession().finishAuthentication();
		        new SyncDirectories().execute();
			} catch (IllegalStateException e) {
				Log.d("DropboxDemo", "Error Authenticating...");
			}
		}
	}

	private class SyncDirectories extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDir = new ArrayList<String>();
			mDialog = ProgressDialog.show(MainActivity.this, "",
					"Loading...");
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				Entry dirent = mDBApi.metadata("", 25000, null, true, null);
				for (Entry ent : dirent.contents) {
					if (ent.isDir) {
						mDir.add(ent.fileName());
					}
				}
			} catch (DropboxUnlinkedException e) {
				Log.d("DropboxDemo", "An Exception Occurred..");
			} catch (DropboxPartialFileException e) {
				Log.d("DropboxDemo", "An Exception Occurred..");
			} catch (DropboxServerException e) {
				if (e.error == DropboxServerException._304_NOT_MODIFIED) {
				} else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
				} else if (e.error == DropboxServerException._403_FORBIDDEN) {
				} else if (e.error == DropboxServerException._404_NOT_FOUND) {
				} else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
				} else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
				} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
				} else {
				}
				Log.d("DropboxDemo", e.body.error);
			} catch (DropboxException e) {
				Log.d("DropboxDemo", "An Exception Occurred..");
			}
			return false;
		}

		@Override
		public void onPostExecute(Boolean result) {
			mDirectoryAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_list_item_1, mDir);
			mDirectoryList.setAdapter(mDirectoryAdapter);
			mDialog.dismiss();
		}
	}
}
