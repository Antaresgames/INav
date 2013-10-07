package cz.muni.fi.sandbox;

import android.app.Application;


public class NavigationSandboxApplication extends Application {

    @Override
    public void onCreate() {

        //PreferenceManager.setDefaultValues(this, R.xml.default_values, false);
    }

    @Override
    public void onTerminate() {
    }
}
