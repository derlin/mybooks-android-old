package ch.derlin.mybooks.service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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

    private static final String BOOKS_FILE_PATH = "/mybooks.json";
    protected LocalBroadcastManager mBroadcastManager;

    private Gson mGson = new GsonBuilder().create();
    private Map<String, Book> mBooks;
    private String mLatestRev;
    private boolean mFileOpened = false;
    private Thread mGetFileThread = null, mUpdateFileThread = null;

    // ----------------------------------------------------

    /** Binder for this service * */
    public class DbxBinder extends Binder{
        /**
         * @return a reference to the bound service
         */
        public DboxService getService(){
            return DboxService.this;
        }
    }//end class

    private DbxBinder myBinder = new DbxBinder();


    @Override
    public IBinder onBind( Intent arg0 ){
        return myBinder;
    }
    // ----------------------------------------------------

    private static DboxService INSTANCE;


    public static DboxService getInstance(){
        return INSTANCE;
    }


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


    @Override
    public int onStartCommand( Intent intent, int flags, int startId ){
        mBroadcastManager = LocalBroadcastManager.getInstance( this );
        return super.onStartCommand( intent, flags, startId );
    }


    // ----------------------------------------------------


    public boolean isFileOpened(){
        return mFileOpened;
    }


    public List<Book> getBooks(){
        return mFileOpened && mBooks != null ?  //
                new ArrayList<>( mBooks.values() ) : null;
    }


    public List<String> getTitles(){
        return mFileOpened && mBooks != null ?  //
                new ArrayList<>( mBooks.keySet() ) : null;
    }


    public Book getBook( String title ){
        return mFileOpened && mBooks != null ?  //
                mBooks.get( title ) : null;
    }


    public String getLatestRev(){
        return mLatestRev;
    }


    public boolean editBook( String oldTitle, Book book ){
        if( !mBooks.containsKey( Book.normalizeKey( oldTitle ) ) ) return false;

        if( oldTitle.compareTo( book.title ) != 0 ){
            mBooks.remove( oldTitle );
        }
        mBooks.put( book.getNormalizedKey(), book );
        startUpload();
        return true;
    }


    public void addBook( Book book ){
        mBooks.put( book.getNormalizedKey(), book );
        startUpload();
    }


    public boolean deleteBook( String title ){
        String key = Book.normalizeKey( title );
        if( !mBooks.containsKey( key ) ) return false;
        mBooks.remove( key );
        startUpload();
        return true;
    }

    // ----------------------------------------------------


    public boolean openFile(){
        if( mGetFileThread == null ){
            mGetFileThread = new Thread( new RunnableGetFile() );
            mGetFileThread.start();
            return true;
        }
        return false;
    }


    public void closeFile(){
        if( mGetFileThread != null ){
            mGetFileThread.interrupt();
            mGetFileThread = null;
        }
        if( mUpdateFileThread != null ){
            mUpdateFileThread.interrupt();
            mUpdateFileThread = null;
        }

        mFileOpened = false;
    }


    public boolean startUpload(){
        if( mUpdateFileThread == null ){
            mUpdateFileThread = new Thread( new RunnableUpdate() );
            mUpdateFileThread.start();
            return true;
        }
        return false;
    }

    // ----------------------------------------------------


    private class RunnableUpdate implements Runnable{
        public void run(){

            File file = null;

            try{

                file = File.createTempFile( "mybooks", "json" );

                try( FileOutputStream out = new FileOutputStream( file ) ){
                    String json = mGson.toJson( mBooks );
                    out.write( json.getBytes() );
                    DropboxAPI.Entry entry = mDBApi.putFileOverwrite( BOOKS_FILE_PATH, new FileInputStream( file ),
                            file.length(), null );

                    mLatestRev = entry.rev;
                    notifyBooksChanged();
                    notifyUploadOk();
                }

            }catch( Exception e ){
                Log.e( getClass().getName(), e.toString() );
                notifyError( String.format( "error uploading file (%s)", e.getMessage() ) );

            }finally{
                if( file != null ) file.delete();
                mUpdateFileThread = null;
            }

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

                if( mLatestRev == null || mLatestRev.equals( rev ) ){
                    // there is actually a change
                    mBooks = mGson.fromJson( new FileReader( file ), new TypeToken<Map<String, Book>>(){}.getType() );
                    if( mBooks != null ){
                        mFileOpened = true;
                        notifyBooksChanged();
                    }
                }

            }catch( Exception e ){
                Log.e( getClass().getName(), e.toString() );
                notifyError( String.format( "Could not get file (%s)", e.getMessage() ) );

            }finally{
                if( file != null ) file.delete();
                mGetFileThread = null;

            }

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
