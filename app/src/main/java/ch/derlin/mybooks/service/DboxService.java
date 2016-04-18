package ch.derlin.mybooks.service;

import android.util.Log;
import ch.derlin.mybooks.books.Book;
import com.dropbox.client2.DropboxAPI;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.Map;

/**
 * Context:
 *
 * @author Lucy Linder
 *         Date 18.04.16.
 */
public class DboxService extends BaseDboxService{

    private static DboxService INSTANCE;
    private static final String BOOKS_FILE_PATH = "/mybooks.json";
    private String lastRev;


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

    // ----------------------------------------------------


    public Map<String, Book> getBooks(){
        try{
            File file = File.createTempFile( "mybooks", "json" );
            FileOutputStream outputStream = new FileOutputStream( file );
            DropboxAPI.DropboxFileInfo info = mDBApi.getFile( BOOKS_FILE_PATH, null, outputStream, null );
            lastRev = info.getMetadata().rev;
            Log.i( "DbExampleLog", "The uploaded file's rev is: " + lastRev );
            return parseBooksFile( file );
        }catch( Exception e ){
            Log.e( "LA", e.toString() );
        }

        return null;
    }


    private Map<String, Book> parseBooksFile( File file ){

        try{
            Map<String, Book> map = new GsonBuilder().create().fromJson( new FileReader( file ), new
                    TypeToken<Map<String, Book>>(){}.getType() );
            Log.d( "lj", "" + map );
            return map;

        }catch( IOException e ){
            e.printStackTrace();
        }

        return null;
    }

}
