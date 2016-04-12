package ch.derlin.mybooks.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.dropbox.DboxConfig;
import ch.derlin.mybooks.views.dummy.DummyContent;
import com.dropbox.sync.android.*;

import java.util.List;

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

    private DbxFile booksFile;

    private static final DbxPath DBX_PATH = new DbxPath( "mybooks.json" );



    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_book_list );

        getDbxFile();

        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );
        toolbar.setTitle( getTitle() );

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick( View view ){
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG ).setAction( "Action", null ).show();
            }
        } );

        View recyclerView = findViewById( R.id.book_list );
        assert recyclerView != null;
        setupRecyclerView( ( RecyclerView ) recyclerView );

        if( findViewById( R.id.book_detail_container ) != null ){
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    // ----------------------------------------------------

    private void getDbxFile(){

        DbxAccount dbAccount = DboxConfig.getAccountManager( this ).getLinkedAccount();

        if( dbAccount == null ){
            Log.e( TAG, "No linked account." );

        }else{

            try{
                DbxFileSystem fs = DbxFileSystem.forAccount( dbAccount );
                try{
                    booksFile = fs.open( DBX_PATH );

                }catch( DbxException.NotFound e ){
                    booksFile = fs.create( DBX_PATH );

                }
            }catch( DbxException e ){
                Log.e( TAG, "failed to open or create file.", e );
            }
        }
    }


    // ----------------------------------------------------

    private void setupRecyclerView( @NonNull RecyclerView recyclerView ){
        recyclerView.setAdapter( new SimpleItemRecyclerViewAdapter( DummyContent.ITEMS ) );
    }


    public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>{

        private final List<DummyContent.DummyItem> mValues;


        public SimpleItemRecyclerViewAdapter( List<DummyContent.DummyItem> items ){
            mValues = items;
        }


        @Override
        public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ){
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.book_list_content, parent, false );
            return new ViewHolder( view );
        }


        @Override
        public void onBindViewHolder( final ViewHolder holder, int position ){
            holder.mItem = mValues.get( position );
            holder.mIdView.setText( mValues.get( position ).id );
            holder.mContentView.setText( mValues.get( position ).content );

            holder.mView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick( View v ){
                    if( mTwoPane ){
                        Bundle arguments = new Bundle();
                        arguments.putString( BookDetailFragment.ARG_ITEM_ID, holder.mItem.id );
                        BookDetailFragment fragment = new BookDetailFragment();
                        fragment.setArguments( arguments );
                        getSupportFragmentManager().beginTransaction().replace( R.id.book_detail_container, fragment ).commit();
                    }else{
                        Context context = v.getContext();
                        Intent intent = new Intent( context, BookDetailActivity.class );
                        intent.putExtra( BookDetailFragment.ARG_ITEM_ID, holder.mItem.id );

                        context.startActivity( intent );
                    }
                }
            } );
        }


        @Override
        public int getItemCount(){
            return mValues.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder{
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;


            public ViewHolder( View view ){
                super( view );
                mView = view;
                mIdView = ( TextView ) view.findViewById( R.id.id );
                mContentView = ( TextView ) view.findViewById( R.id.content );
            }


            @Override
            public String toString(){
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
