package data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//import com.example.blockwatch.data.BlockContract.BlockEntry;

/**
 * Manages a local database for block data.
 */

/**
 * Created by test on 2/20/2017.
 */

public class BlockDbHelper extends SQLiteOpenHelper {    /*
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "block.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     *
     * The reason DATABASE_VERSION starts at 3 is because Blockwatch has been used in conjunction
     * with the Android course for a while now. Believe it or not, older versions of Blockwatch
     * still exist out in the wild. If we started this DATABASE_VERSION off at 1, upgrading older
     * versions of Blockwatch could cause everything to break. Although that is certainly a rare
     * use-case, we wanted to watch out for it and warn you what could happen if you mistakenly
     * version your databases.
     */
    private static final int DATABASE_VERSION = 1;

    public BlockDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * cache our block data.
         */
        final String SQL_CREATE_BLOCK_TABLE =

                "CREATE TABLE " + BlockContract.BlockEntry.TABLE_NAME + " (" +

                /*
                 * BlockEntry did not explicitly declare a column called "_ID". However,
                 * BlockEntry implements the interface, "BaseColumns", which does have a field
                 * named "_ID". We use that here to designate our table's primary key.
                 */
                        BlockContract.BlockEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        BlockContract.BlockEntry.COLUMN_HASH       + " INTEGER NOT NULL, "                 +

                /*
                 * To ensure this table can only contain one block entry per date, we declare
                 * the date column to be unique. We also specify "ON CONFLICT REPLACE". This tells
                 * SQLite that if we have a block entry for a certain date and we attempt to
                 * insert another block entry with that date, we replace the old block entry.
                 */
                        " UNIQUE (" + BlockContract.BlockEntry.COLUMN_HASH + ") ON CONFLICT REPLACE);";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_BLOCK_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BlockContract.BlockEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}