package ch.derlin.mybooks.dropbox;

/**
 * Context:
 *
 * @author Lucy Linder
 *         Date 12.04.16.
 */
import android.content.Context;
import com.dropbox.sync.android.DbxAccountManager;

public final class DboxConfig {
    private DboxConfig() {
    }


    public static final String DBOX_APP_KEY = "213bpo6j2q90un0";
    private static final String DBOX_APP_SECRET = "b1tijzm481l9eta";


    public static DbxAccountManager getAccountManager( Context context ) {
        return DbxAccountManager.getInstance( context.getApplicationContext(), DBOX_APP_KEY, DBOX_APP_SECRET );
    }
}
