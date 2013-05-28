package com.facebook.android.fbreader;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class FBReaderApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "xHPceMV0YZ4wxJe6x8HjeJuL9kdw9N52tcBuCee2", 
				"NT0PhDS8L5OZk5Dd2U6rh0lEbjngUYHQhN9GH0jJ");


		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		// Optionally enable public read access.
		// defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
	}
	
}
