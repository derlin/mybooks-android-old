package ch.derlin.mybooks.views.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.books.Book;
import ch.derlin.mybooks.service.DboxService;
import ch.derlin.mybooks.views.BookListActivity;
import ch.derlin.mybooks.views.IFab;
import ch.derlin.mybooks.views.details.BookDetailActivity;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookEditDetailFragment extends Fragment implements View.OnClickListener{

    /**
     * The dummy content this fragment is presenting.
     */
    private Book mBook;
    private String mOldTitle;

    private EditText mEditTitle, mEditAuthor, mEditDate, mEditNotes;
    private DboxService mService;
    // ----------------------------------------------------


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookEditDetailFragment(){
    }


    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );


        Activity activity = this.getActivity();
        mService = DboxService.getInstance();

        mBook = new Book();
        if( activity.getIntent().hasExtra( BookListActivity.ARG_BOOK_TITLE ) ){
            mOldTitle = activity.getIntent().getStringExtra( BookListActivity.ARG_BOOK_TITLE );
            mBook = mService.getBook( mOldTitle );
        }

        CollapsingToolbarLayout appBarLayout = ( CollapsingToolbarLayout ) activity.findViewById( R.id.toolbar_layout );

        if( appBarLayout != null ){
            appBarLayout.setTitle( mBook.title );
        }

        if( activity instanceof IFab ){
            ( ( IFab ) activity ).getIFab().setOnClickListener( this );
        }

    }


    @Override
    public void onClick( View v ){
        mBook.title = mEditTitle.getText().toString();
        mBook.author = mEditAuthor.getText().toString();
        mBook.date = mEditDate.getText().toString();
        mBook.notes = mEditNotes.getText().toString();

        // check required fields
        if( mBook.title.isEmpty() || mBook.author.isEmpty() ){
            Snackbar.make( v, "Title and author are required", Snackbar.LENGTH_LONG ).show();
            return;
        }

        // check service running
        DboxService service = DboxService.getInstance();
        if( service == null ){
            Snackbar.make( v, "Oops (service null)", Snackbar.LENGTH_SHORT ).show();
            return;
        }


        if( mOldTitle != null ){  // book to edit
            if( !service.editBook( mOldTitle, mBook ) ){
                Snackbar.make( v, "Oops (book to edit not found)", Snackbar.LENGTH_LONG ).show();
                return;
            }
        }else{ // book to add
            service.addBook( mBook );
        }

        v.setEnabled( false );
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ){
        View rootView = inflater.inflate( R.layout.book_edit_detail, container, false );

        if( mBook != null ){
            mEditTitle = ( ( EditText ) rootView.findViewById( R.id.details_title ) );
            mEditAuthor = ( ( EditText ) rootView.findViewById( R.id.details_author ) );
            mEditDate = ( ( EditText ) rootView.findViewById( R.id.details_date ) );
            mEditNotes = ( ( EditText ) rootView.findViewById( R.id.details_notes ) );
        }

        setBooksInfos();

        return rootView;
    }


    private void setBooksInfos(){
            mEditTitle.setText( mBook.title );
            mEditAuthor.setText( mBook.author );
            mEditDate.setText( mBook.date );
            mEditNotes.setText( mBook.notes );
    }

}
