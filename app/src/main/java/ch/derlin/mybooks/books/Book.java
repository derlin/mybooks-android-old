package ch.derlin.mybooks.books;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Context:
 *
 * @author Lucy Linder
 *         Date 12.04.16.
 */
public class Book implements Parcelable{
    public String title;
    public String author;
    public String date;
    public String notes;


    public Book(){
    }


    public Book( Parcel in ){
        this.title = in.readString();
        this.author = in.readString();
        this.date = in.readString();
        this.notes = in.readString();
    }


    public String getNormalizedKey(){
        return normalizeKey( title );
    }

    // ----------------------------------------------------


    public static String normalizeKey( String key ){
        key = key.toLowerCase() //
                .replace( "é", "e" )//
                .replace( "è", "e" )//
                .replace( "ê", "e" )//
                .replace( "à", "a" )//
                .replace( "ç", "c" )//
                .replace( "ù", "u" )//
                .replace( "û", "u" );

        key = key.replaceAll( "[^a-z0-9 ]", " " ).replaceAll( " +", " " );
        return key;
    }

    // ----------------------------------------------------


    @Override
    public int describeContents(){
        return 0;
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
}
