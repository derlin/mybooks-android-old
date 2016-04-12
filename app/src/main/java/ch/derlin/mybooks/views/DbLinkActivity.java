package ch.derlin.mybooks.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.dropbox.DboxConfig;
import com.dropbox.sync.android.DbxAccountManager;

public class DbLinkActivity extends AppCompatActivity{

    static final int REQUEST_LINK_TO_DBX = 0;  // This value is up to you


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );

        DbxAccountManager dboxManager = DboxConfig.getAccountManager( this );
        if( dboxManager.getLinkedAccount() == null ){
            dboxManager.startLink( this, REQUEST_LINK_TO_DBX );
        }else{
            startApp();
        }

        setContentView( R.layout.activity_start );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
    }


    private void startApp(){
        Intent intent = new Intent( this, BookListActivity.class );
        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME );
        startActivity( intent );
        this.finish();
    }


    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data ){
        if( requestCode == REQUEST_LINK_TO_DBX ){
            if( resultCode == Activity.RESULT_OK ){
                startApp();
            }else{
                Toast.makeText( this, "error linking dropbox account", Toast.LENGTH_LONG ).show();
            }
        }else{
            super.onActivityResult( requestCode, resultCode, data );
        }
    }

}
