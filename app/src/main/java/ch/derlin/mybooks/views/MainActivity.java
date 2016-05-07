package ch.derlin.mybooks.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.books.Book;
import ch.derlin.mybooks.service.DboxBroadcastReceiver;
import ch.derlin.mybooks.service.DboxService;
import ch.derlin.mybooks.views.details.DetailActivity;
import ch.derlin.mybooks.views.details.DetailFragment;
import ch.derlin.mybooks.views.edit.EditActivity;
import ch.derlin.mybooks.views.edit.EditFragment;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.SectionIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

import java.util.ArrayList;
import java.util.List;

/**
 * In "one-pane" mode, this activity:
 * - displays a list of books (title-author)
 * - displays a fab to add books, launching the {@link EditActivity}
 * - launches the {@link DetailActivity}  on list item click
 * <p>
 * <p>
 * In "two-pane" mode, {@ref mTwoPane}, it does pretty everything, except the add book.
 * - the fab does not change (add books)
 * - details and edit happen in a fragment on the left
 * - the save, edit and delete buttons are added on the toolbar and shown/hidden depending
 * on the current fragment
 * - the switch between fragments as well as the book currently shown/edited are kept in the
 * {@ref mCurrentState} and {@ref mTwoPaneBook} variables
 * <p>
 *  <br />----------------------------------------------------<br/>
 *  Derlin - MyBooks Android, May, 2016
 * @author Lucy Linder
 */
public class MainActivity extends AppCompatActivity implements EditFragment.EditFragmentHolder{

    public static final String ARG_BOOK_TITLE = "book_title";
    private static final int ADD_REQUEST_CODE = 0;
    private static final int EDIT_REQUEST_CODE = 1;

    private boolean mTwoPane;   // is the screen wide enough
    private BooksAdapter mAdapter; // the book list adapter

    private DboxService mService;
    private String mRev; // the current dbx file revision, to avoid refreshing the list when no changes

    private FloatingActionButton mFab; // the fab, which is moved with the snackbar

    // -------- used only in two panes mode:

    private enum State{
        NONE, DETAILS, EDIT // what type of fragment is on the right pane
    }

    private State mCurrentState = State.NONE; // current fragment type
    private MenuItem mActionDelete, mActionEdit, mActionSave; // the toolbar buttons
    private View.OnClickListener mSaveListener; // the save listener from the EditFragment
    private Book mTwoPaneBook = null; // the book currently shown/edited, if any


    // ----------------------------------------------------

    private DboxBroadcastReceiver mReceiver = new DboxBroadcastReceiver(){
        @Override
        protected void onBooksChanged( String rev ){
            if( mRev != null ) Snackbar.make( mFab, "External changes. Updating.", Snackbar.LENGTH_LONG ).show();
            mAdapter.setBooksList( mService.getBooks() );
            mRev = rev;
        }


        @Override
        protected void onError( String msg ){
            Toast.makeText( MainActivity.this, msg, Toast.LENGTH_LONG ).show();
        }


        @Override
        protected void onUploadOk(){
            Snackbar.make( mFab, "Changes saved.", Snackbar.LENGTH_LONG ).show();
        }


        @Override
        protected void onBooksUnchanged(){
            Snackbar.make( mFab, "Books up to date.", Snackbar.LENGTH_LONG ).show();
        }


        @Override
        public void onBookDeleted( final Book book ){
            Snackbar.make( mFab, "book deleted.", Snackbar.LENGTH_LONG ).setAction( "undo", new View.OnClickListener(){
                @Override
                public void onClick( View v ){
                    mService.addBook( book );
                    if( mTwoPane ) showDetailsFragment( book );
                }
            } ).show();
        }
    };

    // ----------------------------------------------------


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        // check the pane mode
        if( findViewById( R.id.book_detail_container ) != null ){
            mTwoPane = true;
        }

        // the StartActivity ensures that the service is running --> should not be null
        mService = DboxService.getInstance();

        if( mService != null && mService.getBooks() != null ){
            // if the books are already loaded
            mAdapter = new BooksAdapter( mService.getBooks() );
            mRev = mService.getLatestRev();

        }else{
            // if not loaded, the broadcast receiver's onChange method will be called soon
            mAdapter = new BooksAdapter();
        }

        // setup recyclerview (listview)
        RecyclerView recyclerView = ( RecyclerView ) findViewById( R.id.book_list );
        setRecyclerViewLayoutManager( recyclerView, mAdapter );

        // display the toolbar
        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getTitle() );

        // floating button management
        mFab = ( FloatingActionButton ) findViewById( R.id.fab );
        mFab.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick( View view ){
                // on click, launch the addbook activity, whatever the pane mode
                Intent intent = new Intent( MainActivity.this, EditActivity.class );
                startActivityForResult( intent, ADD_REQUEST_CODE );
            }

        } );

    }


    @Override
    protected void onResume(){
        super.onResume();
        mReceiver.registerSelf( this );
        if( mRev == null || !mRev.equals( mService.getLatestRev() ) ){
            // either books not already loaded, or changes happened
            List<Book> books = mService.getBooks();
            if( books != null ) mAdapter.setBooksList( books );
        }
    }


    @Override
    protected void onPause(){
        super.onPause();
        mReceiver.unregisterSelf( this );
    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ){
        // nothing special to do, just log the event
        if( requestCode == ADD_REQUEST_CODE || requestCode == EDIT_REQUEST_CODE ){
            Log.d( getPackageName(), "Activity result " + resultCode );
        }else{
            super.onActivityResult( requestCode, resultCode, data );
        }
    }

    // ----------------------------------------------------


    /**
     * Set RecyclerView's LayoutManager
     */
    public void setRecyclerViewLayoutManager( RecyclerView recyclerView, RecyclerView.Adapter adapter ){

        // ---------- init recycler

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( linearLayoutManager );
        recyclerView.setAdapter( adapter );

        int scrollPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        recyclerView.scrollToPosition( scrollPosition );

        // ---------- set index on the right

        VerticalRecyclerViewFastScroller fastScroller = ( VerticalRecyclerViewFastScroller ) findViewById( R.id
                .fast_scroller );

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView( recyclerView );

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView.addOnScrollListener( fastScroller.getOnScrollListener() );

        // Connect the section indicator to the scroller
        SectionIndicator sectionTitleIndicator = ( SectionIndicator ) findViewById( R.id
                .fast_scroller_section_title_indicator );

        fastScroller.setSectionIndicator( sectionTitleIndicator );

    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ){
        getMenuInflater().inflate( R.menu.toolbar_menu_list, menu );

        // get the reference to the hidden/shown menu items in two pane mode
        // the clicks will be handled in {@ref onOptionsItemSelected}
        mActionDelete = menu.findItem( R.id.action_delete );
        mActionSave = menu.findItem( R.id.action_save );
        mActionEdit = menu.findItem( R.id.action_edit );

        // handle the search bar
        final SearchView searchView = ( SearchView ) menu.findItem( R.id.action_search ).getActionView();
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit( String query ){
                // use the default behavior
                return false;
            }


            @Override
            public boolean onQueryTextChange( String s ){
                mAdapter.filter( s );
                return false;
            }
        } );
        return true;
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item ){
        // the action bar will automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if( id == R.id.action_sync ){
            mService.startFetchBooks();
            Toast.makeText( this, "checking Dropbox server...", Toast.LENGTH_SHORT ).show();
            return true;

        }else if( item == mActionDelete ){
            DboxService.getInstance().deleteBook( mTwoPaneBook.title );
            showNoFragment();
            return true;

        }else if( item == mActionEdit ){
            assert mTwoPaneBook != null;
            showEditFragment( mTwoPaneBook );
            return true;

        }else if( item == mActionSave ){
            if( mSaveListener != null ){
                mSaveListener.onClick( null );
                return true;
            }
            return false;
        }
        return super.onOptionsItemSelected( item );
    }


    // ----------------------------------------------------


    private void bookSelected( final Book book ){
        // in a special function, because at first I wanted
        // to show a confirm dialog if in edit mode to avoid
        // loosing unsaved changes. Problem: detecting
        // unsaved changes !
        showDetailsFragment( book );

    }


    private void showNoFragment(){
        // remove all the fragment, in case the currently showing book is
        // deleted
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if( fragments != null ){
            for( Fragment fragment : fragments ){
                getSupportFragmentManager().beginTransaction().remove( fragment ).commit();
            }
        }
    }


    private void showDetailsFragment( Book book ){
        // update the toolbar
        mActionDelete.setVisible( true );
        mActionEdit.setVisible( true );
        mActionSave.setVisible( false );

        // launch the fragment
        Bundle arguments = new Bundle();
        arguments.putString( MainActivity.ARG_BOOK_TITLE, book.title );
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments( arguments );
        getSupportFragmentManager().beginTransaction().replace( R.id.book_detail_container, fragment ).commit();

        // update the state
        mTwoPaneBook = book;
        mCurrentState = State.DETAILS;
    }


    private void showEditFragment( Book book ){

        // update the toolbar
        mActionDelete.setVisible( false );
        mActionEdit.setVisible( false );
        mActionSave.setVisible( true );

        // launch the fragment
        Bundle arguments = new Bundle();
        arguments.putString( MainActivity.ARG_BOOK_TITLE, book.title );
        EditFragment fragment = new EditFragment();
        fragment.setArguments( arguments );
        getSupportFragmentManager().beginTransaction().replace( R.id.book_detail_container, fragment ).commit();

        // update the state
        mTwoPaneBook = book;
        mCurrentState = State.EDIT;
    }


    @Override
    public void attachSaveListener( final View.OnClickListener listener ){
        // used by the {@link EditFragment}, {@see EditFragmentHolder}
        mSaveListener = listener;

    }


    @Override
    public void done( Book book, boolean actionDone ){
        // used by the {@link EditFragment}, {@see EditFragmentHolder}
        mSaveListener = null;
        showDetailsFragment( book );
    }


    @Override
    public void onBackPressed(){
        if( mCurrentState == State.EDIT ){
            // if state changed, necessarily in two panes mode,
            // go back to the details fragment
            bookSelected( mTwoPaneBook );

        }else{
            super.onBackPressed();
        }
    }


    // ----------------------------------------------------

    public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> implements SectionIndexer{

        private List<Book> mBooksList;
        private List<Book> mOriginalBooksList;


        public BooksAdapter( List<Book> items ){
            mBooksList = new ArrayList<>( items );
            mOriginalBooksList = new ArrayList<>( items );
        }


        public BooksAdapter(){
            mBooksList = new ArrayList<>();
            mOriginalBooksList = new ArrayList<>();
        }


        public void setBooksList( List<Book> books ){
            mOriginalBooksList = new ArrayList<>( books );
            mBooksList = mOriginalBooksList;
            notifyDataSetChanged();
        }


        public void filter( String search ){

            List<Book> filtered;
            if( search == null || search.isEmpty() ){
                filtered = mOriginalBooksList;
            }else{
                search = search.toLowerCase();
                filtered = new ArrayList<>();
                for( Book book : mOriginalBooksList ){
                    if( book.match( search ) ) filtered.add( book );
                }//end for
            }

            mBooksList = filtered;
            notifyDataSetChanged();
        }


        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ){
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.activity_main_list_content,
                    parent, false );
            return new ViewHolder( view );
        }


        @Override
        public void onBindViewHolder( final ViewHolder holder, int position ){

            holder.mBook = mBooksList.get( position );
            holder.mTitleView.setText( holder.mBook.title );
            holder.mAuthorView.setText( holder.mBook.author );

            holder.mView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick( View v ){
                    if( mTwoPane ){
                        bookSelected( holder.mBook );

                    }else{
                        // todo: don't destroy current activity ?
                        // launch activity
                        Context context = v.getContext();
                        Intent intent = new Intent( context, DetailActivity.class );
                        intent.setFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                        intent.putExtra( MainActivity.ARG_BOOK_TITLE, holder.mBook.title );

                        context.startActivity( intent );
                    }
                }
            } );
        }


        @Override
        public int getItemCount(){
            return mBooksList.size();
        }


        @Override
        public Object[] getSections(){
            return mBooksList.toArray();
        }


        @Override
        public int getPositionForSection( int sectionIndex ){
            return 0;
        }


        @Override
        public int getSectionForPosition( int position ){
            if( position >= mBooksList.size() ){
                position = mBooksList.size() - 1;
            }
            return position;
        }


        public class ViewHolder extends RecyclerView.ViewHolder{
            public final View mView;
            public final TextView mTitleView;
            public final TextView mAuthorView;
            public Book mBook;


            public ViewHolder( View view ){
                super( view );
                mView = view;
                mTitleView = ( TextView ) view.findViewById( R.id.title );
                mAuthorView = ( TextView ) view.findViewById( R.id.author );
            }


            @Override
            public String toString(){
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }

}
