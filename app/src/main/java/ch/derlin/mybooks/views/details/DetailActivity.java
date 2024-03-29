package ch.derlin.mybooks.views.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.service.DboxService;
import ch.derlin.mybooks.views.MainActivity;
import ch.derlin.mybooks.views.edit.EditActivity;

/**
 * An activity representing a single Book detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MainActivity}.
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class DetailActivity extends AppCompatActivity{

    private String mBookTitle; // current title shown


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_detail );

        // set toolbar
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.detail_toolbar );
        setSupportActionBar( toolbar );

        // set floating button to edit action
        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setImageDrawable( getResources().getDrawable( android.R.drawable.ic_menu_edit, getTheme() ) );
        fab.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick( View view ){
                Context context = DetailActivity.this;
                Intent intent = new Intent( context, EditActivity.class );
                intent.putExtra( MainActivity.ARG_BOOK_TITLE, mBookTitle );
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
            mBookTitle = getIntent().getStringExtra( MainActivity.ARG_BOOK_TITLE );
            Bundle arguments = new Bundle();
            arguments.putString( MainActivity.ARG_BOOK_TITLE, mBookTitle );
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments( arguments );
            getSupportFragmentManager().beginTransaction().add( R.id.book_detail_container, fragment ).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ){
        getMenuInflater().inflate( R.menu.toolbar_menu_details, menu );
        return true;
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item ){
        int id = item.getItemId();
        switch( id ){
            case android.R.id.home:
                navigateUpTo( new Intent( this, MainActivity.class ) );
                return true;
            case R.id.action_delete:
                DboxService.getInstance().deleteBook( mBookTitle );
                finish();
                return true;
        }
        return super.onOptionsItemSelected( item );
    }
}
