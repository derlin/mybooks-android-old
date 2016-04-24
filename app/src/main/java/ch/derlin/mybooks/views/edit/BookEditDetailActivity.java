package ch.derlin.mybooks.views.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.views.BookListActivity;
import ch.derlin.mybooks.views.IFab;
import ch.derlin.mybooks.views.details.BookDetailFragment;

/**
 * An activity representing a single Book detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BookListActivity}.
 */
public class BookEditDetailActivity extends AppCompatActivity implements IFab{

    private FloatingActionButton fab;


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_book_detail );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.detail_toolbar );
        setSupportActionBar( toolbar );

        fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setBackgroundDrawable( getResources().getDrawable( android.R.drawable.ic_menu_save, getTheme() ) );

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
            arguments.putParcelable( BookDetailFragment.ARG_BOOK, getIntent().getParcelableExtra( BookDetailFragment
                    .ARG_BOOK ) );
            BookEditDetailFragment fragment = new BookEditDetailFragment();
            fragment.setArguments( arguments );
            getSupportFragmentManager().beginTransaction().add( R.id.book_detail_container, fragment ).commit();
        }
    }


    @Override
    public FloatingActionButton getIFab(){
        return fab;
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item ){
        int id = item.getItemId();
        if( id == android.R.id.home ){
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            setResult( Activity.RESULT_CANCELED );
            finish();
            return true;
        }
        return super.onOptionsItemSelected( item );
    }
}
