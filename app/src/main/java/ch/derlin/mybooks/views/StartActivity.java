package ch.derlin.mybooks.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.service.DboxService;

public class StartActivity extends AppCompatActivity{


    private DboxService mDboxService;

    // ----------------------------------------------------

    private ServiceConnection mServiceConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected( ComponentName name, IBinder service ){
            mDboxService = ( ( DboxService.DbxBinder ) service ).getService();
            onServiceBound();
        }


        @Override
        public void onServiceDisconnected( ComponentName name ){
            mDboxService = null;
        }
    };

    // ----------------------------------------------------


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_start );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
    }


    @Override
    protected void onStart(){
        super.onStart();
        startService( new Intent( getApplicationContext(), DboxService.class ) );
        bindService( new Intent( getApplicationContext(), DboxService.class ),  //
                mServiceConnection, Context.BIND_AUTO_CREATE );
    }


    @Override
    protected void onDestroy(){
        unbindService( mServiceConnection );
        super.onDestroy();
    }


    @Override
    protected void onPause(){
        super.onPause();
    }


    @Override
    protected void onResume(){
        super.onResume();
        if( mDboxService != null ){
            mDboxService.finishAuth();
            startApp();
        }
    }

    // ----------------------------------------------------


    private void onServiceBound(){
        if( mDboxService.startAuth( this ) ){
            // returns true only if already authenticated
            startApp();
        }// else, a dbx activity will be launched --> see the on resume
    }


    private void startApp(){
        Intent intent = new Intent( this, BookListActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME );
        startActivity( intent );
        this.finish();
    }


}
