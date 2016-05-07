package ch.derlin.mybooks;

import android.content.Context;
import android.util.AttributeSet;
import ch.derlin.mybooks.books.Book;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

/**
 * from <a href="https://github.com/danoz73/RecyclerViewFastScroller">
 * recyclerviewfastscroller</a>
 * <br />----------------------------------------------------<br/>
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class MySectionIndicator extends SectionTitleIndicator<Book>{

    public MySectionIndicator( Context context ){
        super( context );
    }


    public MySectionIndicator( Context context, AttributeSet attrs ){
        super( context, attrs );
    }


    public MySectionIndicator( Context context, AttributeSet attrs, int defStyleAttr ){
        super( context, attrs, defStyleAttr );
    }


    @Override
    public void setSection( Book book ){
        // Example of using a single character
        setTitleText( book.getNormalizedKey().toUpperCase().charAt( 0 ) + "" );
    }

}
