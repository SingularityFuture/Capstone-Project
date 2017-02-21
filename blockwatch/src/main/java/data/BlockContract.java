package data;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Michael on 2/20/2017.
 */
    /**
     * Defines table and column names for the block database. This class is not necessary, but keeps
     * the code organized.
     */
    public class BlockContract {

        /*
         * The "Content authority" is a name for the entire content provider, similar to the
         * relationship between a domain name and its website. A convenient string to use for the
         * content authority is the package name for the app, which is guaranteed to be unique on the
         * Play Store.
         */
        public static final String CONTENT_AUTHORITY = "com.example.blockwatch";

        /*
         * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
         * the content provider for Blockwatch.
         */
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

        /*
         * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that Blockwatch
         * can handle. For instance,
         *
         *     content://com.example.android.sunshine/block/
         *     [           BASE_CONTENT_URI         ][ PATH_BLOCK]
         *
         * is a valid path for looking at block data.
         */
        public static final String PATH_BLOCK = "block";

        /* Inner class that defines the table contents of the block table */
        public static final class BlockEntry implements BaseColumns {

            /* The base CONTENT_URI used to query the Block table from the content provider */
            public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                    .appendPath(PATH_BLOCK)
                    .build();

            /* Used internally as the name of our block table. */
            public static final String TABLE_NAME = "block";

            // Store columns for one blockchain transaction
            public static final String COLUMN_DATE = "date";

        }
    }