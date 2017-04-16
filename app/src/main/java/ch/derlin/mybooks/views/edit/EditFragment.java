package ch.derlin.mybooks.views.edit;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import ch.derlin.mybooks.R;
import ch.derlin.mybooks.books.Book;
import ch.derlin.mybooks.service.DboxBroadcastReceiver;
import ch.derlin.mybooks.service.DboxService;
import ch.derlin.mybooks.views.MainActivity;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link EditActivity}
 * on handsets.
 * The containing activity must implement the {@ref EditFragmentHolder}
 * interface for the "save" and "cancel" options to work.
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class EditFragment extends Fragment implements View.OnClickListener{
    /**
     * Interface which must be implemented by the attached activity.
     */
    public interface EditFragmentHolder{
        /**
         * the fragment will call this method to attach its
         * save callback. It is the responsibility of the activity
         * to attach it to the proper button (fab, toolbar item, ...)
         *
         * @param listener the save callback
         */
        void attachSaveListener( View.OnClickListener listener );

        /**
         * the fragment will call this method when the task is finished.
         * A boolean to true ensures that the book has been saved to dropbox, while a
         * boolean to false acts like a "cancel".
         * been saved.
         *
         * @param book       the edited book
         * @param actionDone true if saved, false if canceled.
         */
        void done( Book book, boolean actionDone );
    }

    // ----------------------------------------------------

    private DboxBroadcastReceiver mReceiver = new DboxBroadcastReceiver(){

        @Override
        protected void onError( String msg ){
            Toast.makeText( getActivity(), msg, Toast.LENGTH_LONG ).show();
        }


        @Override
        protected void onUploadOk(){
            // feedback and return
            Toast.makeText( getActivity(), "changes saved.", Toast.LENGTH_LONG ).show();
            mHolder.done( mBook, true );
        }
    };

    // ----------------------------------------------------
    private Book mBook;
    private String mOldTitle; // the key for the dropbox service, in
    // case the title is also modified

    private EditText mEditTitle, mEditAuthor, mEditDate, mEditNotes;

    private EditFragmentHolder mHolder;

    // ----------------------------------------------------


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EditFragment(){
    }


    @Override
    public void onCreate( Bundle savedInstanceState ){
        super.onCreate( savedInstanceState );


        Activity activity = this.getActivity();
        DboxService mService = DboxService.getInstance();

        // initialise the book
        mBook = new Book();
        mOldTitle = getArguments().getString( MainActivity.ARG_BOOK_TITLE );
        if( mOldTitle != null ) mBook = mService.getBook( mOldTitle ); // edit vs add mode


        /*CollapsingToolbarLayout appBarLayout = ( CollapsingToolbarLayout ) activity.findViewById( R.id.toolbar_layout );

        if( appBarLayout != null ){
            appBarLayout.setTitle( mBook.title );
        }*/

        if( activity instanceof EditFragmentHolder ){
            mHolder = ( EditFragmentHolder ) activity;
            mHolder.attachSaveListener( this );
        }

    }


    @Override
    public void onResume(){
        super.onResume();
        mReceiver.registerSelf( getActivity() );
    }


    @Override
    public void onPause(){
        super.onPause();
        mReceiver.unregisterSelf( getActivity() );
    }

    // ----------------------------------------------------


    @Override
    public void onClick( View v ){

        mBook.title = mEditTitle.getText().toString();
        mBook.author = mEditAuthor.getText().toString();
        mBook.date = mEditDate.getText().toString();
        mBook.notes = mEditNotes.getText().toString();

        // check required fields
        if( mBook.title.isEmpty() || mBook.author.isEmpty() ){
            Toast.makeText( getActivity(), "Title and author are required", Toast.LENGTH_LONG ).show();
            return;
        }

        // check service running
        DboxService service = DboxService.getInstance();
        if( service == null ){
            Toast.makeText( getActivity(), "Oops (service null)", Toast.LENGTH_SHORT ).show();
            return;
        }


        if( mOldTitle != null ){  // book to edit
            if( !service.editBook( mOldTitle, mBook ) ){
                Toast.makeText( getActivity(), "Oops (book to edit not found)", Toast.LENGTH_LONG ).show();
            }
        }else{ // book to add
            service.addBook( mBook );
        }
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ){
        View rootView = inflater.inflate( R.layout.fragment_edit, container, false );

        if( mBook != null ){
            mEditTitle = ( EditText ) rootView.findViewById( R.id.details_title );
            mEditAuthor = ( EditText ) rootView.findViewById( R.id.details_author );
            mEditDate = ( EditText ) rootView.findViewById( R.id.details_date );
            mEditNotes = ( EditText ) rootView.findViewById( R.id.details_notes );
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
