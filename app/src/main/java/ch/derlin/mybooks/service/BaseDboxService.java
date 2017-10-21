package ch.derlin.mybooks.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;

import ch.derlin.mybooks.R;

/**
 * A basic dropbox service using the V2 API.
 * How to use:
 * - change the APP_KEY and APP_SECRET
 * - in your main activity, initialise the service by calling
 * {@ref startAuth}. If it returns true, then the linking has
 * already been done. If not, the method will launch the OAuth
 * flow. In this case, you need to call {@ref finishAuth} in the
 * activity onResume method. see {@link ch.derlin.mybooks.views.StartActivity}
 * for an example.
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class BaseDboxService extends Service {

    protected DbxClientV2 dbxClientV2;

    private final IBinder myBinder = new BTBinder();

    /**
     * Binder for this service *
     */
    public class BTBinder extends Binder {
        /**
         * @return a reference to the bound service
         */
        public BaseDboxService getService() {
            return BaseDboxService.this;
        }
    }//end class


    @Override
    public IBinder onBind(Intent arg0) {
        return myBinder;
    }


    //-------------------------------------------------------------


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    // ----------------------------------------------------


    /**
     * start the authentication process. If the user is already
     * logged in, the method returns immediately. If not,
     * a dropbox activity is launched and the caller will need
     * to call {@ref finishAuth} in its onResume.
     *
     * @param callingActivity the calling activity
     * @return true (immediate) if already authentified, won't
     * return but will launch the OAuth process otherwise.
     */
    public boolean startAuth(Context callingActivity) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            Auth.startOAuth2Authentication(this, getString(R.string.app_key));
            return false;
        } else {
            initializeClient(accessToken);
            return true;
        }

    }


    /**
     * finish the authentication process. Must be called in the
     * onResume method of the caller. See {@link ch.derlin.mybooks.views.StartActivity}
     * for an example.
     */
    public void finishAuth() {

        String accessToken = Auth.getOAuth2Token(); //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            storeAccessToken(accessToken);
            initializeClient(accessToken);
        } else {
            Log.i("DbAuthLog", "Error authenticating");
        }
    }


    // ----------------------------------------------------


    private void initializeClient(String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("MyBooks/1.0").build();
        dbxClientV2 = new DbxClientV2(config, accessToken);
    }


    private void storeAccessToken(String accessToken) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        prefs.edit().putString(getString(R.string.prefs_access_token), accessToken).apply();
    }


    private String getAccessToken() {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        return prefs.getString(getString(R.string.prefs_access_token), null);
    }
}
