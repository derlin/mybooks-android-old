package ch.derlin.mybooks.views.details;

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
import ch.derlin.mybooks.service.DboxService;
import ch.derlin.mybooks.views.MainActivity;


/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link DetailActivity}
 * on handsets.
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class DetailFragment extends Fragment{

    private Book mBook;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DetailFragment(){
    }


    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );

        Activity activity = this.getActivity();
        String title = getArguments() != null ? //
                getArguments().getString( MainActivity.ARG_BOOK_TITLE ) : null;
        if( title != null ){
            mBook = DboxService.getInstance().getBook( title );

            CollapsingToolbarLayout appBarLayout = ( CollapsingToolbarLayout ) activity.findViewById( R.id
                    .toolbar_layout );

            if( appBarLayout != null ){
                appBarLayout.setTitle( title );
            }
        }
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ){
        View rootView = inflater.inflate( R.layout.fragment_detail, container, false );

        // Show the dummy content as text in a TextView.
        if( mBook != null ){
            ( ( TextView ) rootView.findViewById( R.id.details_title ) ).setText( mBook.title );
            ( ( TextView ) rootView.findViewById( R.id.details_author ) ).setText( mBook.author );
            ( ( TextView ) rootView.findViewById( R.id.details_date ) ).setText( mBook.date );
            ( ( TextView ) rootView.findViewById( R.id.details_notes ) ).setText( mBook.notes );
        }

        return rootView;
    }

}
