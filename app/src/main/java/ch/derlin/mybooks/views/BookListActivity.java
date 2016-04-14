package ch.derlin.mybooks.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.books.Book;
import ch.derlin.mybooks.dropbox.DboxConfig;
import com.dropbox.sync.android.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.SectionIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity{

    private static final String TAG = BookListActivity.class.getCanonicalName();
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private DbxFile mBooksFile;

    private Map<String, Book> mBooksMap;

    private static final DbxPath DBX_PATH = new DbxPath( "mybooks.json" );


    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_book_list );

        if( !getDbxFile() ){
            // todo: show error
            return;
        }
        parseBooksFile();

        // setup recyclerview (listview)
        RecyclerView recyclerView = ( RecyclerView ) findViewById( R.id.book_list );
        assert recyclerView != null;
        recyclerView.setAdapter( new BooksAdapter( mBooksMap.values() ) );

        VerticalRecyclerViewFastScroller fastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller);

        // Connect the recycler to the scroller (to let the scroller scroll the list)
        fastScroller.setRecyclerView( recyclerView );

        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView.addOnScrollListener( fastScroller.getOnScrollListener() );

        // Connect the section indicator to the scroller
        SectionIndicator sectionTitleIndicator = ( SectionIndicator ) findViewById( R.id.fast_scroller_section_title_indicator );
        fastScroller.setSectionIndicator( sectionTitleIndicator );

        setRecyclerViewLayoutManager( recyclerView );

        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getTitle() );

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick( View view ){
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG ).setAction( "Action", null
                ).show();
            }
        } );


        if( findViewById( R.id.book_detail_container ) != null ){
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }


    @Override
    protected void onPause(){
        if(mBooksFile != null){
            mBooksFile.close();
            mBooksFile = null;
        }
        super.onPause();

    }

    // ----------------------------------------------------
    /**
     * Set RecyclerView's LayoutManager
     */
    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition =
                    ((LinearLayoutManager ) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }
    // ----------------------------------------------------


    private boolean getDbxFile(){

        DbxAccount dbAccount = DboxConfig.getAccountManager( this ).getLinkedAccount();

        if( dbAccount == null ){
            Log.e( TAG, "No linked account." );

        }else{

            try{
                DbxFileSystem fs = DbxFileSystem.forAccount( dbAccount );
                try{
                    mBooksFile = fs.open( DBX_PATH );

                }catch( DbxException.NotFound e ){
                    mBooksFile = fs.create( DBX_PATH );
                }

                return true;

            }catch( DbxException e ){
                Log.e( TAG, "failed to open or create file.", e );
            }
        }

        return false;
    }


    private boolean parseBooksFile(){

        try{
            mBooksMap = new GsonBuilder().create().fromJson( mBooksFile.readString(), new TypeToken<Map<String,
                    Book>>(){}.getType() );
            Log.d( TAG, "" + mBooksMap );
            return true;

        }catch( IOException e ){
            e.printStackTrace();
        }

        return false;
    }


    // ----------------------------------------------------

    public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.ViewHolder> implements SectionIndexer{

        private final List<Book> mBooksList;


        public BooksAdapter( Collection<Book> items ){
            this( new ArrayList<Book>( items ) );
        }


        public BooksAdapter( List<Book> items ){
            mBooksList = items;
        }


        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ){
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.book_list_content, parent, false );
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
                        Bundle arguments = new Bundle();
                        arguments.putParcelable( BookDetailFragment.ARG_BOOK, holder.mBook );
                        BookDetailFragment fragment = new BookDetailFragment();
                        fragment.setArguments( arguments );
                        getSupportFragmentManager().beginTransaction().replace( R.id.book_detail_container, fragment
                        ).commit();
                    }else{
                        // todo: don't destroy current activity
                        Context context = v.getContext();
                        Intent intent = new Intent( context, BookDetailActivity.class );
                        intent.putExtra( BookDetailFragment.ARG_BOOK, holder.mBook );

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
            return  mBooksList.toArray();
        }


        @Override
        public int getPositionForSection( int sectionIndex ){
            return 0;
        }


        @Override
        public int getSectionForPosition( int position ){
            if (position >= mBooksList.size()) {
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
