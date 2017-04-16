package ch.derlin.mybooks.views.edit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

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
public class EditActivity extends AppCompatActivity implements EditFragment.EditFragmentHolder {

    private View.OnClickListener mListener; // the save callback of the editFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the view
        setContentView(R.layout.activity_edit_simple);

        // display the toolbar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // attach listener to the floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // on click, call the save listener (see edit fragment)
                if (mListener != null) {
                    mListener.onClick(null);
                }
            }

        });

        if (savedInstanceState == null) {
            // create the detail fragment
            Bundle arguments = new Bundle();
            String bookTitle = getIntent().getStringExtra(MainActivity.ARG_BOOK_TITLE);
            setTitle(bookTitle == null ? "New Book" : bookTitle);
            arguments.putString(MainActivity.ARG_BOOK_TITLE, bookTitle);
            EditFragment fragment = new EditFragment();
            fragment.setArguments(arguments);
            //getSupportFragmentManager().beginTransaction().add(R.id.book_edit_container, fragment).commit();
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, fragment).commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void attachSaveListener(View.OnClickListener listener) {
        mListener = listener;
    }


    @Override
    public void done(Book book, boolean actionDone) {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
