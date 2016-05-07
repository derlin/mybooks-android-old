package ch.derlin.mybooks.views.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.books.Book;
import ch.derlin.mybooks.views.MainActivity;

/**
 * An activity representing an add/edit screen.
 * The edit feature is handled by this activity only on narrow width devices.
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class EditActivity extends AppCompatActivity implements EditFragment.EditFragmentHolder{

    private View.OnClickListener mListener; // the save callback of the editFragment


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );

        setContentView( R.layout.activity_edit );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.detail_toolbar );
        setSupportActionBar( toolbar );

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ){
            actionBar.setDisplayHomeAsUpEnabled( true );
        }

        if( savedInstanceState == null ){
            // create the detail fragment
            Bundle arguments = new Bundle();
            String bookTitle = getIntent().getStringExtra( MainActivity.ARG_BOOK_TITLE );
            setTitle( bookTitle == null ? "New Book" : bookTitle );
            arguments.putString( MainActivity.ARG_BOOK_TITLE, bookTitle );
            EditFragment fragment = new EditFragment();
            fragment.setArguments( arguments );
            getSupportFragmentManager().beginTransaction().add( R.id.book_edit_container, fragment ).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ){
        getMenuInflater().inflate( R.menu.toolbar_menu_edit, menu );
        return true;
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item ){
        int id = item.getItemId();
        switch( id ){
            case R.id.action_save:
                if( mListener != null ){
                    mListener.onClick( null );
                }
                return true;

            case android.R.id.home:
                setResult( Activity.RESULT_CANCELED );
                finish();
                return true;
        }
        return super.onOptionsItemSelected( item );
    }


    @Override
    public void attachSaveListener( View.OnClickListener listener ){
        mListener = listener;
    }


    @Override
    public void done( Book book, boolean actionDone ){
        setResult( Activity.RESULT_OK );
        finish();
    }
}
