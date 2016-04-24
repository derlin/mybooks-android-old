package ch.derlin.mybooks.views.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.views.BookListActivity;
import ch.derlin.mybooks.views.edit.BookEditDetailActivity;

/**
 * An activity representing a single Book detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BookListActivity}.
 */
public class BookDetailActivity extends AppCompatActivity{

    private String mBookTitle;


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_book_detail );
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.detail_toolbar );
        setSupportActionBar( toolbar );

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setImageDrawable( getResources().getDrawable( android.R.drawable.ic_menu_edit, getTheme() ) );
        fab.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick( View view ){
                Context context = BookDetailActivity.this;
                Intent intent = new Intent( context, BookEditDetailActivity.class );
                intent.putExtra( BookListActivity.ARG_BOOK_TITLE, mBookTitle );
                context.startActivity( intent );
                finish();
            }
        } );

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
            mBookTitle = getIntent().getStringExtra( BookListActivity.ARG_BOOK_TITLE );
            Bundle arguments = new Bundle();

            arguments.putString( BookListActivity.ARG_BOOK_TITLE, mBookTitle );
            BookDetailFragment fragment = new BookDetailFragment();
            fragment.setArguments( arguments );
            getSupportFragmentManager().beginTransaction().add( R.id.book_detail_container, fragment ).commit();
        }
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
            navigateUpTo( new Intent( this, BookListActivity.class ) );
            return true;
        }
        return super.onOptionsItemSelected( item );
    }
}
