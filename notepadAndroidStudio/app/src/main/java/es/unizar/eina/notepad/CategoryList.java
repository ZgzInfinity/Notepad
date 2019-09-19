package es.unizar.eina.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class CategoryList extends AppCompatActivity {

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;

    private NotesDbAdapter mDbHelper;
    private ListView mList;
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
        mFocusPosition = 0;
        fillData();

        registerForContextMenu(mList);
    }

    private void fillData() {
        // Get all of the categories from the database and create the item list
        Cursor categoriesCursor = mDbHelper.fetchAllCategories();
        startManagingCursor(categoriesCursor);

        // Create an array to specify the fields we want to display in the list (only NAME)
        String[] from = new String[]{NotesDbAdapter.CATEGORY_KEY_NAME};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter categories =
                new SimpleCursorAdapter(this, R.layout.list_row, categoriesCursor, from, to);
        mList.setAdapter(categories);
        mList.setSelection(mFocusPosition);
        mFocusPosition = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createCategory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.cat_menu_delete);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.cat_menu_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteCategory(info.id);
                fillData();
                return true;
            case EDIT_ID:
                mFocusPosition = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editCategory(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createCategory() {
        Intent i = new Intent(this, CategoryEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    private void editCategory(long id) {
        Intent i = new Intent(this, CategoryEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
