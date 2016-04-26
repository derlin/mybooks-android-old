package ch.derlin.mybooks.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import ch.derlin.mybooks.books.Book;

import static ch.derlin.mybooks.service.DboxConstants.*;

/**
 * Context:
 *
 * @author Lucy Linder
 *         Date 20.04.16.
 */
public class DboxBroadcastReceiver extends BroadcastReceiver{
    private static final IntentFilter INTENT_FILTER = new IntentFilter( DBXS_INTENT_FILTER );

    // ----------------------------------------------------


    @Override
    public void onReceive( Context context, Intent intent ){
        String evtType = intent.getStringExtra( DBXS_EXTRA_EVT_KEY );

        switch( evtType ){
            case DBXS_EVT_BOOKS_CHANGED:
                onBooksChanged( intent.getStringExtra( DBXS_EXTRA_REV_KEY ) );
                break;
            case DBXS_EVT_UPLOAD_OK:
                onUploadOk();
                break;
            case DBXS_EVT_BOOKS_ON_SYNC:
                onBooksUnchanged();
                break;
            case DBXS_EVT_ERROR:
                onError( intent.getStringExtra( DBXS_EXTRA_MSG_KEY ) );
                break;
            case DBXS_EVT_BOOK_DELETED:
                onBookDeleted( ( Book ) intent.getParcelableExtra( DBXS_EXTRA_BOOK_KEY ) );
                break;
        }
    }


    /**
     * Register this receiver to the local broadcast manager to start receiving events.
     *
     * @param context the context
     */
    public void registerSelf( Context context ){
        LocalBroadcastManager.getInstance( context ).registerReceiver( this, INTENT_FILTER );
    }


    /**
     * Unregister this receiver from the local broadcast manager to stop receiving events.
     *
     * @param context the context
     */
    public void unregisterSelf( Context context ){
        LocalBroadcastManager.getInstance( context ).unregisterReceiver( this );
    }

    // ----------------------------------------------------


    protected void onBooksChanged( String rev ){
        // pass
    }

    protected void onBooksUnchanged(){
        // pass
    }


    protected void onError( String msg ){
        // pass
    }


    protected void onUploadOk(){
        // pass
    }


    public void onBookDeleted( Book book ){

    }

}
