package ch.derlin.mybooks.service;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ch.derlin.mybooks.books.Book;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListRevisionsResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.*;

import static ch.derlin.mybooks.service.DboxConstants.*;

/**
 * A specialized dropbox service using the CORE API V2 to store
 * books in a json file in dropbox.
 * It can be used as a singleton, but for this ensure that the
 * service is started with startService and not bindService.
 * <p/>
 * To keep in sync with the service, use a {@link DboxBroadcastReceiver}
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class DboxService extends BaseDboxService{

    private static final String BOOKS_FILE_PATH = "/mybooks.json";

    protected LocalBroadcastManager mBroadcastManager;
    private Gson mGson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, Book> mBooks;
    private String mLatestRev;
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


    /**
     * @return the book list, or null if not loaded yet.
     */
    public List<Book> getBooks(){
        return mBooks != null ?  //
                new ArrayList<>( mBooks.values() ) : null;
    }


    /**
     * @return the list of titles (normalized) or null if
     * the books are not loaded.
     */
    public List<String> getTitles(){
        return mBooks != null ?  //
                new ArrayList<>( mBooks.keySet() ) : null;
    }


    /**
     * @param title the book title
     * @return the book, or null if the title does not match any.
     */
    public Book getBook( String title ){
        return mBooks != null ?  //
                mBooks.get( Book.normalizeKey( title ) ) : null;
    }


    /**
     * @return the last revision of the dbx file
     */
    public String getLatestRev(){
        return mLatestRev;
    }

    // ----------------------------------------------------


    /**
     * save the modification of a books to dropbox.
     * Note: the saving process is asynchronous: to know
     * if it succeeded, use a {@link DboxBroadcastReceiver}.
     *
     * @param oldTitle original title
     * @param book     new values
     * @return false upon error (book not found)
     */
    public synchronized boolean editBook( String oldTitle, Book book ){
        String oldKey = Book.normalizeKey( oldTitle );
        if( !mBooks.containsKey( oldKey ) ) return false;

        Map<String, Book> updated = new TreeMap<>( mBooks );

        if( oldTitle.compareTo( book.title ) != 0 ){
            updated.remove( oldKey );
        }
        updated.put( book.getNormalizedKey(), book );
        startUpload( updated );
        return true;
    }


    /**
     * add a book.
     * Note: the saving process is asynchronous: to know
     * if it succeeded, use a {@link DboxBroadcastReceiver}.
     *
     * @param book the new book
     */
    public synchronized void addBook( Book book ){
        Map<String, Book> updated = new TreeMap<>( mBooks );
        updated.put( book.getNormalizedKey(), book );
        startUpload( updated );
    }


    /**
     * delete a book.
     * Note: the saving process is asynchronous: to know
     * if it succeeded, use a {@link DboxBroadcastReceiver}.
     *
     * @param title the book title
     * @return false upon error (book not found)
     */
    public synchronized boolean deleteBook( String title ){
        String key = Book.normalizeKey( title );
        if( !mBooks.containsKey( key ) ) return false;
        Map<String, Book> updated = new TreeMap<>( mBooks );
        Book delBook = updated.remove( key );
        startUpload( updated, delBook );
        return true;
    }


    private synchronized void setBooks( Map<String, Book> updated ){
        mBooks = updated;
    }

    // ----------------------------------------------------


    @Override
    public boolean startAuth( Context callingActivity ){
        boolean isAuth = super.startAuth( callingActivity );
        if( isAuth ){
            // already linked, automatically load books
            startFetchBooks();
        }
        return isAuth;
    }


    @Override
    public void finishAuth(){
        super.finishAuth();
        startFetchBooks();
    }


    public boolean startFetchBooks(){

        if( mGetFileThread == null ){
            mGetFileThread = new Thread( new RunnableGetFile() );
            mGetFileThread.start();
            return true;
        }
        return false;
    }


    public boolean startUpload( Map<String, Book> updated ){
        return startUpload( updated, null );
    }


    public boolean startUpload( Map<String, Book> updated, Book delBook ){
        if( mUpdateFileThread == null ){
            mUpdateFileThread = new Thread( new RunnableUpdate( updated, delBook ) );
            mUpdateFileThread.start();
            return true;
        }
        return false;
    }

    // ----------------------------------------------------

    /*
     * A background job to upload the changes to Dropbox.
     */
    private class RunnableUpdate implements Runnable{

        Map<String, Book> updatedBooks;
        Book delBook;


        public RunnableUpdate( Map<String, Book> updatedBooks, Book delBook ){
            this.updatedBooks = updatedBooks;
            this.delBook = delBook;
        }


        public void run(){

            File file = null;

            try{

                file = File.createTempFile( "mybooks", "json" );

                try( FileOutputStream out = new FileOutputStream( file ) ){
                    String json = mGson.toJson( updatedBooks );
                    out.write( json.getBytes() );
                    FileMetadata fileMetadata = dbxClientV2.files()
                            .uploadBuilder(BOOKS_FILE_PATH)
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(new FileInputStream(file));

                    mLatestRev = fileMetadata.getRev();
                    setBooks( updatedBooks );
                    notifyBooksChanged();

                    if( delBook != null ){
                        notifyBookDeleted( delBook );
                    }else{
                        notifyUploadOk();
                    }
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

    /*
     * A background job to get the file.
     */
    private class RunnableGetFile implements Runnable{

        public void run(){

            File file = null;

            try{
                ListRevisionsResult revisions = dbxClientV2.files().listRevisions(BOOKS_FILE_PATH);
                // TODO: no revision -> create file !
                String rev = revisions.getEntries().get(0).getRev();


                if( mLatestRev == null || !mLatestRev.equals( rev ) ){
                    // there is actually a change
                    file = File.createTempFile( "mybooks", "json" );
                    FileOutputStream outputStream = new FileOutputStream( file );
                    dbxClientV2.files().download(BOOKS_FILE_PATH).download(outputStream);
                    mBooks = mGson.fromJson( new FileReader( file ), new TypeToken<Map<String, Book>>(){}.getType() );
                    mLatestRev = rev;
                    if( mBooks != null ){
                        notifyBooksChanged();
                    }else{
                        notifyBooksOnSync();
                    }
                }else{
                    notifyBooksOnSync();
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


    protected void notifyBookDeleted( Book book ){
        // add an extra to the broadcast
        Intent i = getIntent( DBXS_EVT_BOOK_DELETED );
        i.putExtra( DBXS_EXTRA_BOOK_KEY, book );
        mBroadcastManager.sendBroadcast( i );
    }


    protected void notifyUploadOk(){
        Intent i = getIntent( DBXS_EVT_UPLOAD_OK );
        mBroadcastManager.sendBroadcast( i );
    }


    protected void notifyBooksOnSync(){
        Intent i = getIntent( DBXS_EVT_BOOKS_ON_SYNC );
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
