package es.unizar.eina.notepad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * <p>
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    enum NotesOrderBy {
        Category, Title,
    };

    public static final String KEY_ROWID = "_id";

    public static final String NOTE_KEY_TITLE = "title";
    public static final String NOTE_KEY_BODY = "body";
    public static final String NOTE_KEY_FK_CATEGORY = "fk_category";

    public static final String CATEGORY_KEY_NAME = "name";

    private static final String TAG = "NotesDbAdapter";
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_NOTE =
            "create table notes (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, fk_category integer,"
                    + "foreign key(fk_category) references categories(_id) on delete set null);";
    private static final String DATABASE_CREATE_CATEGORY =
            "create table categories (_id integer primary key autoincrement, "
                    + "name text not null);";
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE_NOTE = "notes";
    private static final String DATABASE_TABLE_CATEGORY = "categories";
    private static final int DATABASE_VERSION = 4;

    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     * initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Crea una nueva nota a partir del título y texto proporcionados. Si la
     * nota se crea correctamente, devuelve el nuevo rowId de la nota; en otro
     * caso, devuelve -1 para indicar el fallo.
     *
     * @param title
     *        el título de la nota;
     *        title != null y title.length() > 0
     * @param body
     *        el texto de la nota;
     *        body != null
     * @param categoryFk
     *        id (o null) de la categoria a asignar a esta nota;
     *        categoryFk == null or categoyFk > 0
     * @return rowId de la nueva nota o -1 si no se ha podido crear
     */
    public long createNote(String title, String body, Long categoryFk) {
        if (title == null || title.length() == 0 || body == null || (categoryFk != null && categoryFk <= 0)) {
            return -1;
        }

        // Comprueba que haya al menos un caracter (ascii) en el titulo
        if (!title.matches(".*\\w.*")) {
            return -1;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(NOTE_KEY_TITLE, title);
        initialValues.put(NOTE_KEY_BODY, body);
        initialValues.put(NOTE_KEY_FK_CATEGORY, categoryFk);

        return mDb.insert(DATABASE_TABLE_NOTE, null, initialValues);
    }

    /**
     * Borra la nota cuyo rowId se ha pasado como parámetro
     *
     * @param rowId
     *        el identificador de la nota que se desea borrar;
     *        rowId > 0
     * @return true si y solo si la nota se ha borrado
     */
    public boolean deleteNote(long rowId) {
        if (rowId <= 0) {
            return false;
        }

        return mDb.delete(DATABASE_TABLE_NOTE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteNotesWhere(String filterClause) {
        return mDb.delete(DATABASE_TABLE_NOTE, filterClause, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes(NotesOrderBy orderByField, Long categoryFilter) {
        String orderBy = null;
        String whereClause = categoryFilter == -1 ? null : NOTE_KEY_FK_CATEGORY + "=" + categoryFilter.toString();

        switch (orderByField) {
            case Category:
                orderBy = NOTE_KEY_FK_CATEGORY;
                break;
            case Title:
                orderBy = NOTE_KEY_TITLE;
                break;
        }

        return mDb.query(DATABASE_TABLE_NOTE, new String[]{KEY_ROWID, NOTE_KEY_TITLE,
                NOTE_KEY_BODY, NOTE_KEY_FK_CATEGORY}, whereClause, null, null, null, orderBy);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_NOTE, new String[]{KEY_ROWID,
                                NOTE_KEY_TITLE, NOTE_KEY_BODY, NOTE_KEY_FK_CATEGORY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Actualiza una nota a partir de los valores de los parámetros. La nota que
     * se actualizará es aquella cuyo rowId coincida con el valor del parámetro.
     * Su título y texto se modificarán con los valores de title y body,
     * respectivamente.
     *
     * @param rowId
     *        el identificador de la nota que se desea borrar;
     *        rowId > 0
     * @param title
     *        el título de la nota;
     *        title != null y title.length() > 0
     * @param body
     *        el texto de la nota;
     *        body != null
     * @param categoryFk
     *        id (o null) de la categoria a asignar a esta nota;
     *        categoryFk == null or categoryFk > 0
     * @return true si y solo si la nota se actualizó correctamente
     */
    public boolean updateNote(long rowId, String title, String body, Long categoryFk) {
        if (rowId <= 0 || title == null || title.length() == 0 || body == null || (categoryFk != null && categoryFk <= 0)) {
            return false;
        }

        // Comprueba que haya al menos un caracter (ascii) en el titulo
        if (!title.matches(".*\\w.*")) {
            return false;
        }

        ContentValues args = new ContentValues();
        args.put(NOTE_KEY_TITLE, title);
        args.put(NOTE_KEY_BODY, body);
        args.put(NOTE_KEY_FK_CATEGORY, categoryFk);

        return mDb.update(DATABASE_TABLE_NOTE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Create a new category using the name provided. If the category is
     * successfully created return the new rowId for that category, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the name of the category
     *             name != null and name.length() > 0
     * @return rowId or -1 if failed
     */
    public long createCategory(String name) {
        if (name == null || name.length() == 0) {
            return -1;
        }

        // Comprueba que haya al menos un caracter (ascii) en el nombre de la categoria
        if (!name.matches(".*\\w.*")) {
            return -1;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(CATEGORY_KEY_NAME, name);

        return mDb.insert(DATABASE_TABLE_CATEGORY, null, initialValues);
    }

    /**
     * Delete the category with the given rowId
     *
     * @param rowId id of category to delete
     *              rowId > 0
     * @return true if deleted, false otherwise
     */
    public boolean deleteCategory(long rowId) {
        if (rowId <= 0) {
            return false;
        }

        return mDb.delete(DATABASE_TABLE_CATEGORY, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all categories in the database
     *
     * @return Cursor over all categories
     */
    public Cursor fetchAllCategories() {
        return mDb.query(DATABASE_TABLE_CATEGORY, new String[]{KEY_ROWID, CATEGORY_KEY_NAME},
                null, null, null, null, CATEGORY_KEY_NAME);
    }

    /**
     * Return a Cursor positioned at the category that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching category, if found
     * @throws SQLException if category could not be found/retrieved
     */
    public Cursor fetchCategory(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE_CATEGORY, new String[]{KEY_ROWID,
                                CATEGORY_KEY_NAME}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the category using the details provided. The category to be updated is
     * specified using the rowId, and it is altered to use the name
     * values passed in
     *
     * @param rowId id of category to update
     *              rowId > 0
     * @param name value to set category name to
     *             name != null and name.length() > 0
     * @return true if the category was successfully updated, false otherwise
     */
    public boolean updateCategory(long rowId, String name) {
        if (rowId <= 0 || name == null || name.length() == 0) {
            return false;
        }

        // Comprueba que haya al menos un caracter (ascii) en el nombre de la categoria
        if (!name.matches(".*\\w.*")) {
            return false;
        }

        ContentValues args = new ContentValues();
        args.put(CATEGORY_KEY_NAME, name);

        return mDb.update(DATABASE_TABLE_CATEGORY, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE_NOTE);
            db.execSQL(DATABASE_CREATE_CATEGORY);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys = ON");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_NOTE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CATEGORY);
            onCreate(db);
        }
    }
}