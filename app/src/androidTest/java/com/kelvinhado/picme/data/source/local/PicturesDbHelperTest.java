package com.kelvinhado.picme.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;

import com.kelvinhado.picme.data.model.Picture;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by kelvin on 10/10/2017.
 */
public class PicturesDbHelperTest {
    private final Context mContext = InstrumentationRegistry.getTargetContext();
    /* Class reference to help load the constructor on runtime */
    private final Class mDbHelperClass = PicturesDbHelper.class;

    @Before
    public void setUp() {
        deleteTheDatabase();
    }


    /**
     * This method tests that our database contains all of the tables that we think it should
     * contain.
     *
     * @throws Exception in case the constructor hasn't been implemented yet
     */
    @Test
    public void create_database_test() throws Exception {

        /* Use reflection to try to run the correct constructor whenever implemented */
        SQLiteOpenHelper dbHelper =
                (SQLiteOpenHelper) mDbHelperClass.getConstructor(Context.class).newInstance(mContext);

        SQLiteDatabase database = dbHelper.getWritableDatabase();


        // TEST DB OPENED
        String databaseIsNotOpen = "The database should be open and isn't";
        assertEquals(databaseIsNotOpen, true, database.isOpen());

        /* This Cursor will contain the names of each table in our database */
        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" +
                        PicturePersistenceContract.PictureEntry.TABLE_NAME + "'", null);

        // TEST DB PROPERLY CREATED
        String errorInCreatingDatabase = "Error: This means that the database has not been created correctly";
        assertTrue(errorInCreatingDatabase, tableNameCursor.moveToFirst()); // return false if not

        // TEST IF EXPECTED TABLE IS PRESENT
        assertEquals("Error: Your database was created without the expected tables.",
                PicturePersistenceContract.PictureEntry.TABLE_NAME, tableNameCursor.getString(0));

        // close cursor
        tableNameCursor.close();
    }

    /**
     * This method tests inserting a single record into an empty table from a brand new database.
     * The purpose is to test that the database is working as expected
     *
     * @throws Exception in case the constructor hasn't been implemented yet
     */
    @Test
    public void insert_single_record_test() throws Exception {
        SQLiteOpenHelper dbHelper = (SQLiteOpenHelper) mDbHelperClass.getConstructor(Context.class).newInstance(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Picture picture = new Picture();
        picture.setId(33);
        picture.setTitle("Hello there");
        picture.setAlbumId(44);
        picture.setUrl("http://www.test.com");
        picture.setThumbnailUrl("http://www.test.com");
        ContentValues testValues = new ContentValues();
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_ID, 33);
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_ALBUM_ID, 44);
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_TITLE, "Hello there");
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_PICTURE_URL, "http://www.test.com");
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_THUMBNAIL_URL, "http://www.test.com");

        /* Insert ContentValues into database and get first row ID back */
        long firstRowId = database.insert(
                PicturePersistenceContract.PictureEntry.TABLE_NAME,
                null,
                testValues);

        // TEST INSERTION
        assertNotEquals("Unable to insert into the database", -1, firstRowId); // -1 = Fail

        /* query db */
        Cursor wCursor = database.query(
                /* Name of table on which to perform the query */
                PicturePersistenceContract.PictureEntry.TABLE_NAME,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Columns to group by */
                null,
                /* Columns to filter by row groups */
                null,
                /* Sort order to return in Cursor */
                null);

        // TEST RECORDED
        String emptyQueryError = "Error: No Record returned from waitlist query";
        assertTrue(emptyQueryError, wCursor.moveToFirst());

        /* Close cursor and database */
        wCursor.close();
        dbHelper.close();
    }


    /**
     * Tests to ensure that inserts into your database results in automatically
     * incrementing row IDs.
     *
     * @throws Exception in case the constructor hasn't been implemented yet
     */
    @Test
    public void autoincrement_test() throws Exception {

        /* run previous test to populate the db */
        insert_single_record_test();

        SQLiteOpenHelper dbHelper =
                (SQLiteOpenHelper) mDbHelperClass.getConstructor(Context.class).newInstance(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues testValues = new ContentValues();
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_ID, 33);
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_ALBUM_ID, 44);
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_TITLE, "Hello there");
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_PICTURE_URL, "http://www.test.com");
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_THUMBNAIL_URL, "http://www.test.com");

        long firstRowId = database.insert(
                PicturePersistenceContract.PictureEntry.TABLE_NAME,
                null,
                testValues);
        long secondRowId = database.insert(
                PicturePersistenceContract.PictureEntry.TABLE_NAME,
                null,
                testValues);

        // TEST INCREMENTATION
        assertEquals("ID Autoincrement test failed!",
                firstRowId + 1, secondRowId);
    }


    /**
     * Tests that onUpgrade works by inserting 2 rows then calling onUpgrade and verifies that the
     * database has been successfully dropped and recreated by checking that the database is there
     * but empty
     *
     * @throws Exception in case the constructor hasn't been implemented yet
     */
    @Test
    public void upgrade_database_test() throws Exception {
        /* Use reflection to try to run the correct constructor whenever implemented */
        SQLiteOpenHelper dbHelper =
                (SQLiteOpenHelper) mDbHelperClass.getConstructor(Context.class).newInstance(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues testValues = new ContentValues();
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_ID, 33);
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_ALBUM_ID, 44);
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_TITLE, "Hello there");
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_PICTURE_URL, "http://www.test.com");
        testValues.put(PicturePersistenceContract.PictureEntry.COLUMN_NAME_THUMBNAIL_URL, "http://www.test.com");

        database.insert(PicturePersistenceContract.PictureEntry.TABLE_NAME, null, testValues);
        database.insert(PicturePersistenceContract.PictureEntry.TABLE_NAME, null, testValues);

        dbHelper.onUpgrade(database, 0, 1);
        database = dbHelper.getReadableDatabase();

        /* This Cursor will contain the names of each table in our database */
        Cursor tableNameCursor = database.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" +
                        PicturePersistenceContract.PictureEntry.TABLE_NAME + "'",
                null);

        // TEST IF THE TABLE IS NOT DUPLICATED
        assertTrue(tableNameCursor.getCount() == 1);

        Cursor wCursor = database.query(
                /* Name of table on which to perform the query */
                PicturePersistenceContract.PictureEntry.TABLE_NAME,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Columns to group by */
                null,
                /* Columns to filter by row groups */
                null,
                /* Sort order to return in Cursor */
                null);

        // TEST IF RECORDS ARE DELETED ON UPGRADE.
        assertFalse("Database doesn't seem to have been dropped successfully when upgrading",
                wCursor.moveToFirst());

        tableNameCursor.close();
        database.close();
    }

    /**
     * Deletes the entire database.
     */
    void deleteTheDatabase() {
        try {
            /* Use reflection to get the database name from the db helper class */
            Field f = mDbHelperClass.getDeclaredField("DATABASE_NAME");
            f.setAccessible(true);
            mContext.deleteDatabase((String) f.get(null));
        } catch (NoSuchFieldException ex) {
            fail("Make sure you have a member called DATABASE_NAME in the AddressesDbHelper");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    }
}