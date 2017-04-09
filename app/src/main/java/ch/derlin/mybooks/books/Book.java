package ch.derlin.mybooks.books;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Derlin - MyBooks Android, May, 2016
 *
 * @author Lucy Linder
 */
public class Book implements Parcelable{
    public String title;
    public String author;
    public String date;
    public String notes;


    public Book(){
    }


    /**
     * {@see normalizeKey}
     */
    public String getNormalizedKey(){
        return normalizeKey( title );
    }

    // ----------------------------------------------------


    /**
     * returns a normalized version of the book title, i.e.:
     * 1) to lower case
     * 2) accented characters replaced by their non accented counterparts
     * 3) replace not a-z or 0-9 characters by spaces
     * 4) trim + replace multiple spaces by one
     *
     * @return the normalized title
     */
    public static String normalizeKey( String key ){
        key = key.toLowerCase() //
                .replace( "é", "e" )//
                .replace( "è", "e" )//
                .replace( "ê", "e" )//
                .replace( "à", "a" )//
                .replace( "ç", "c" )//
                .replace( "ù", "u" )//
                .replace( "û", "u" );

        key = key.replaceAll( "[^a-z0-9 ]", " " ).replaceAll( " +", " " ).trim();
        return key;
    }

    // ----------------------------------------------------
    // Parcelable interface

    @Override
    public int describeContents(){
        return 0;
    }


    public Book( Parcel in ){
        this.title = in.readString();
        this.author = in.readString();
        this.date = in.readString();
        this.notes = in.readString();
    }


    @Override
    public void writeToParcel( Parcel dest, int flags ){
        dest.writeString( title );
        dest.writeString( author );
        dest.writeString( date );
        dest.writeString( notes );
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public Book createFromParcel( Parcel in ){
            return new Book( in );
        }


        public Book[] newArray( int size ){
            return new Book[ size ];
        }
    };


    public boolean match( String search ){
        return title.toLowerCase().contains( search ) || //
                author.toLowerCase().contains( search ) || //
                ( date != null && date.toLowerCase().contains( search ) ) || //
                ( notes != null && notes.toLowerCase().contains( search ) );
    }

    // ------------------------------------

    private static Collator frenchCollator = Collator.getInstance(Locale.FRANCE);

    public static final Comparator<Book> TITLE_COMPARATOR = new Comparator<Book>() {
        @Override
        public int compare(Book b1, Book b2) {
            return frenchCollator.compare(b1.title.toLowerCase(), b2.title.toLowerCase());
        }
    };

    public static final Comparator<Book> DATE_COMPARATOR = new Comparator<Book>() {
        @Override
        public int compare(Book b1, Book b2) {
            int compare = b1.date.replaceAll("[^\\d]", "").compareTo(b2.date.replaceAll("[^\\d]", ""));
            return compare == 0 ? TITLE_COMPARATOR.compare(b1, b2) : compare;
        }
    };
}
