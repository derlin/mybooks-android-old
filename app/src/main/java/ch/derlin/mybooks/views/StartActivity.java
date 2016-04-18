package ch.derlin.mybooks.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import ch.derlin.mybooks.service.DboxService;

public class StartActivity extends AppCompatActivity{

    // In the class declaration section:
    //    private DropboxAPI<AndroidAuthSession> mDBApi;


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        waitForService();

        //        setContentView( R.layout.activity_start );
        //        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        //        setSupportActionBar( toolbar );
    }


    @Override
    protected void onStart(){
        super.onStart();
        startService( new Intent( getApplicationContext(), DboxService.class ) );
    }


    private void waitForService(){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground( Void... params ){

                // wait for the service to start
                while( DboxService.getInstance() == null ) ;
                // if not authenticated yet, start the process
                if( DboxService.getInstance().startAuth( StartActivity.this ) ){
                    // returns true only if already authenticated.
                    startApp();
                }

                return null;

            }
        }.execute();
    }


    @Override
    protected void onResume(){
        super.onResume();
        if( DboxService.getInstance() != null ){
            DboxService.getInstance().finishAuth();
            startApp();
        }
    }


    private void startApp(){
        Intent intent = new Intent( this, BookListActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME );
        startActivity( intent );
        this.finish();
    }


}
