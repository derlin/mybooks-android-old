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
 * An activity representing a single Book detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 */
public class EditActivity extends AppCompatActivity implements EditFragment.EditFragmentHolder{

    private View.OnClickListener mListener;


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

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if( savedInstanceState == null ){
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
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
