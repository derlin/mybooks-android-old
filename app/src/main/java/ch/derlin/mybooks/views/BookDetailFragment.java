package ch.derlin.mybooks.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.books.Book;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookDetailFragment extends Fragment{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_BOOK = "book_parcel";

    /**
     * The dummy content this fragment is presenting.
     */
    private Book mBook;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookDetailFragment(){
    }


    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );

        Activity activity = this.getActivity();

        if( activity.getIntent().hasExtra( ARG_BOOK ) ){
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mBook = activity.getIntent().getParcelableExtra( ARG_BOOK );

            CollapsingToolbarLayout appBarLayout = ( CollapsingToolbarLayout ) activity.findViewById( R.id
                    .toolbar_layout );
            if( appBarLayout != null ){
                appBarLayout.setTitle( mBook.title );
            }
        }
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ){
        View rootView = inflater.inflate( R.layout.book_detail, container, false );

        // Show the dummy content as text in a TextView.
        if( mBook != null ){
//            ( ( TextView ) rootView.findViewById( R.id.details_title ) ).setText( mBook.title );
            ( ( TextView ) rootView.findViewById( R.id.details_author ) ).setText( mBook.author );
            ( ( TextView ) rootView.findViewById( R.id.details_date ) ).setText( mBook.date );
            ( ( TextView ) rootView.findViewById( R.id.details_notes ) ).setText( mBook.notes );
        }

        return rootView;
    }
}
