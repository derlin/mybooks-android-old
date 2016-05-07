package ch.derlin.mybooks.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

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
public class BaseDboxService extends Service{

    public static final String DBOX_APP_KEY = "213bpo6j2q90un0";
    public static final String DBOX_APP_SECRET = "b1tijzm481l9eta";

    // In the class declaration section:
    protected DropboxAPI<AndroidAuthSession> mDBApi;

    private final IBinder myBinder = new BTBinder();

    /** Binder for this service * */
    public class BTBinder extends Binder{
        /**
         * @return a reference to the bound service
         */
        public BaseDboxService getService(){
            return BaseDboxService.this;
        }
    }//end class


    @Override
    public IBinder onBind( Intent arg0 ){
        return myBinder;
    }


    //-------------------------------------------------------------


    @Override
    public int onStartCommand( Intent intent, int flags, int startId ){
        return super.onStartCommand( intent, flags, startId );
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
    public boolean startAuth( Context callingActivity ){
        // And later in some initialization function:
        AndroidAuthSession session = getSession( getApplicationContext() );
        mDBApi = new DropboxAPI<>( session );

        if( !session.isLinked() ){
            session.startOAuth2Authentication( callingActivity );
            return false;
        }
        return true;
    }


    /**
     * finish the authentication process. Must be called in the
     * onResume method of the caller. See {@link ch.derlin.mybooks.views.StartActivity}
     * for an example.
     */
    public void finishAuth(){

        if( mDBApi.getSession().authenticationSuccessful() ){
            try{
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String oauthToken = mDBApi.getSession().getOAuth2AccessToken();
                storeSessionTokens( getApplicationContext(), oauthToken );

            }catch( IllegalStateException e ){
                Log.i( "DbAuthLog", "Error authenticating", e );
            }
        }
    }


    // ----------------------------------------------------


    private AndroidAuthSession getSession( Context appContext ){
        String token = getOauthToken( appContext );

        if( token == null ){
            return new AndroidAuthSession( getAppKeyPair() );
        }

        return new AndroidAuthSession( getAppKeyPair(), token );
    }


    private AppKeyPair getAppKeyPair(){
        AppKeyPair appKeys = new AppKeyPair( DBOX_APP_KEY, DBOX_APP_SECRET );
        return appKeys;
    }


    private void storeSessionTokens( Context appContext, String token ){
        PreferenceManager.getDefaultSharedPreferences( appContext ).edit() //
                .putString( "oauth.token", token ) //
                .commit();
    }


    private String getOauthToken( Context appContext ){
        return PreferenceManager.getDefaultSharedPreferences( appContext ) //
                .getString( "oauth.token", null );
    }
}
