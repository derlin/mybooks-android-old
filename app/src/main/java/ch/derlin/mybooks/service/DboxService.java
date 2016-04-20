package ch.derlin.mybooks.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.derlin.mybooks.books.Book;
import com.dropbox.client2.DropboxAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.derlin.mybooks.service.DboxConstants.*;

/**
 * Context:
 *
 * @author Lucy Linder
 *         Date 18.04.16.
 */
public class DboxService extends BaseDboxService{

    private static DboxService INSTANCE;
    private static final String BOOKS_FILE_PATH = "/mybooks.json";
    protected LocalBroadcastManager mBroadcastManager;

    private Gson mGson = new GsonBuilder().create();
    private Map<String, Book> mBooks;
    private String mLatestRev;

    // ----------------------------------------------------


    @Override
    public void onCreate(){
        super.onCreate();
        INSTANCE = this;
    }


    @Override
    public void onDestroy(){
        INSTANCE = null;
        super.onDestroy();
    }


    public static DboxService getInstance(){
        return INSTANCE;
    }


    @Override
    public int onStartCommand( Intent intent, int flags, int startId ){
        mBroadcastManager = LocalBroadcastManager.getInstance( this );
        return super.onStartCommand( intent, flags, startId );
    }


    // ----------------------------------------------------


    public List<Book> getBooks(){
        return new ArrayList<>( mBooks.values() );
    }


    public List<String> getTitles(){
        return new ArrayList<>( mBooks.keySet() );
    }


    public Book getBook( String title ){
        return mBooks.get( title );
    }


    public String getLatestRev(){
        return mLatestRev;
    }


    public void startDownload(){
        new Thread( new RunnableGetFile() ).start();
    }


    public void startUpload(){
        new Thread( new RunnableUpdate() ).start();
    }

    // ----------------------------------------------------


    private class RunnableUpdate implements Runnable{
        public void run(){
            File file = null;

            try{
                File.createTempFile( "mybooks", "json" );

                try( FileOutputStream out = new FileOutputStream( file ) ){
                    String json = mGson.toJson( mBooks );
                    out.write( json.getBytes() );
                    DropboxAPI.Entry entry = mDBApi.putFile( BOOKS_FILE_PATH, new FileInputStream( file ), file
                            .length(), null, null );

                    mLatestRev = entry.rev;
                    notifyBooksChanged();
                    notifyUploadOk();
                }

            }catch( Exception e ){
                Log.e( getClass().getName(), e.toString() );
                notifyError( String.format( "error uploading file (%s)", e.getMessage() ) );
            }

            if( file != null ) file.delete();
        }
    }


    private class RunnableGetFile implements Runnable{

        public void run(){
            File file = null;

            try{

                file = File.createTempFile( "mybooks", "json" );
                FileOutputStream outputStream = new FileOutputStream( file );
                DropboxAPI.DropboxFileInfo info = mDBApi.getFile( BOOKS_FILE_PATH, null, outputStream, null );
                String rev = info.getMetadata().rev;

                if( mLatestRev != null && mLatestRev.equals( rev ) ){
                    // no change
                    file.delete();
                    return;
                }

                mBooks = mGson.fromJson( new FileReader( file ), new TypeToken<Map<String, Book>>(){}.getType() );

                notifyBooksChanged();

            }catch( Exception e ){
                Log.e( getClass().getName(), e.toString() );
                notifyError( String.format( "Could not get file (%s)", e.getMessage() ) );
            }

            if( file != null ) file.delete();
        }
    }

    // ----------------------------------------------------


    protected void notifyBooksChanged(){
        // add an extra to the broadcast
        Intent i = getIntent( DBXS_EVT_BOOKS_CHANGED );
        i.putExtra( DBXS_EXTRA_REV_KEY, mLatestRev );
        mBroadcastManager.sendBroadcast( i );
    }


    protected void notifyUploadOk(){
        Intent i = getIntent( DBXS_EVT_UPLOAD_OK );
        mBroadcastManager.sendBroadcast( i );
    }


    protected void notifyError( String error ){
        Intent i = getIntent( DBXS_EVT_ERROR );
        i.putExtra( DBXS_EXTRA_MSG_KEY, error );
        mBroadcastManager.sendBroadcast( i );
    }


    protected Intent getIntent( String evtType ){
        Intent i = new Intent( DBXS_INTENT_FILTER );
        i.putExtra( DBXS_EXTRA_EVT_KEY, evtType );
        return i;
    }

    // ----------------------------------------------------


}
