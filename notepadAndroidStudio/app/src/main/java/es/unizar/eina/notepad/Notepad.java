package es.unizar.eina.notepad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import es.unizar.eina.send.SendAbstraction;
import es.unizar.eina.send.SendAbstractionImpl;


public class Notepad extends AppCompatActivity {

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int ACTIVITY_CATEGORIES = 2;

    private static final int CATEGORIES_ID = Menu.FIRST;
    private static final int INSERT_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 3;
    private static final int SEND_MAIL_ID = Menu.FIRST + 4;
    private static final int SEND_SMS_ID = Menu.FIRST + 5;

    private NotesDbAdapter mDbHelper;
    private ListView mList;
    private NotesDbAdapter.NotesOrderBy mOrderBy;
    private Long mCheckedCategoryFk;
    private int mFocusPosition;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mList = (ListView) findViewById(R.id.list);
        mOrderBy = NotesDbAdapter.NotesOrderBy.Title;
        mCheckedCategoryFk = Long.valueOf(-1);
        mFocusPosition = 0;
        fillData(mOrderBy, mCheckedCategoryFk);

        registerForContextMenu(mList);
    }

    private void fillData(NotesDbAdapter.NotesOrderBy orderBy, Long categoryFilter) {
        // Get all of the notes from the database and create the item list
        Cursor notesCursor = mDbHelper.fetchAllNotes(orderBy, categoryFilter);
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.NOTE_KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.list_row, notesCursor, from, to);
        mList.setAdapter(notes);
        mList.setSelection(mFocusPosition);
        mFocusPosition = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.menu_items, menu);
        menu.add(Menu.NONE, CATEGORIES_ID, Menu.NONE, R.string.title_activity_categories_list);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CATEGORIES_ID:
                listCategories();
                return true;
            case INSERT_ID:
                createNote();
                return true;
            case R.id.filter_by_category:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.filter_title);
                builder.setNegativeButton(R.string.remove_filter_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckedCategoryFk = Long.valueOf(-1);
                        fillData(mOrderBy, mCheckedCategoryFk);
                        dialog.dismiss();
                    }
                });
                final Cursor cursor = mDbHelper.fetchAllCategories();
                int checkedItem;
                for (checkedItem = 0; cursor.moveToNext(); ++checkedItem) {
                    if (cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_ROWID)) == mCheckedCategoryFk) {
                        break;
                    }
                }
                builder.setSingleChoiceItems(cursor, checkedItem, NotesDbAdapter.CATEGORY_KEY_NAME, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cursor.moveToPosition(which);
                        mCheckedCategoryFk = cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_ROWID));
                        fillData(mOrderBy, mCheckedCategoryFk);
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            case R.id.order_title:
                mOrderBy = NotesDbAdapter.NotesOrderBy.Title;
                fillData(mOrderBy, mCheckedCategoryFk);
                return true;
            case R.id.order_category:
                mOrderBy = NotesDbAdapter.NotesOrderBy.Category;
                fillData(mOrderBy, mCheckedCategoryFk);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, SEND_SMS_ID, Menu.NONE, R.string.menu_send_sms);
        menu.add(Menu.NONE, SEND_MAIL_ID, Menu.NONE, R.string.menu_send_mail);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.note_menu_delete);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.note_menu_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SEND_SMS_ID:
                mFocusPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                sendNote(info.id, getResources().getString(R.string.send_sms_method));
                return true;
            case SEND_MAIL_ID:
                mFocusPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                sendNote(info.id, getResources().getString(R.string.send_mail_method));
                return true;
            case DELETE_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData(mOrderBy, mCheckedCategoryFk);
                return true;
            case EDIT_ID:
                mFocusPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editNote(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void listCategories() {
        Intent i = new Intent(this, CategoryList.class);
        startActivityForResult(i, ACTIVITY_CATEGORIES);
    }

    private void sendNote(long id, String method) {
        Cursor note = mDbHelper.fetchNote(id);
        startManagingCursor(note);
        String subject = note.getString(
                note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_TITLE));
        String body = note.getString(
                note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_BODY));
        SendAbstraction sendImpl = new SendAbstractionImpl(this, method);
        sendImpl.send(subject, body);
    }

    private void createNote() {
        mFocusPosition = mList.getCount();
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    private void editNote(long id) {
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData(mOrderBy, mCheckedCategoryFk);
    }
}
