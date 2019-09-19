package es.unizar.eina.notepad;

import android.app.AlertDialog;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class NoteEdit extends AppCompatActivity {

    private EditText mTitleText;
    private EditText mBodyText;
    private Spinner mCategorySpinner;
    private CheckBox mNoCategory;
    private Long mRowId;

    private NotesDbAdapter mDbHelper;

    private EditText mIdText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        mCategorySpinner = (Spinner) findViewById(R.id.category);
        fillCategories();

        mNoCategory = (CheckBox) findViewById(R.id.no_category);

        mIdText = (EditText) findViewById(R.id.id);
        mIdText.setEnabled(false);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                    : null;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_title);
        builder.setMessage(R.string.title_error_msg);

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (saveState()) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    builder.show();
                }
            }

        });
    }

    private void populateFields() {
        if (mRowId != null) {
            setTitle(R.string.title_activity_edit_note);

            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_BODY)));

            int categoryFkColId = note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_FK_CATEGORY);
            boolean hasCategory = !note.isNull(categoryFkColId);
            mNoCategory.setChecked(!hasCategory);
            mCategorySpinner.setEnabled(hasCategory);

            if (hasCategory) {
                Long categoryFk = note.getLong(categoryFkColId);
                SpinnerAdapter adapter = mCategorySpinner.getAdapter();

                for (int i = adapter.getCount(); i >= 0; --i) {
                    if (adapter.getItemId(i) == categoryFk) {
                        mCategorySpinner.setSelection(i);
                        break;
                    }
                }
            }

            mIdText.setText(mRowId.toString());
        } else {
            setTitle(R.string.title_activity_create_note);
            mCategorySpinner.setEnabled(false);
            mIdText.setText("***");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        saveState();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    public void onCheckboxClicked(View view) {
        boolean hasCategory = ((CheckBox) view).isChecked();

        mCategorySpinner.setEnabled(!hasCategory);
    }

    private void fillCategories() {
        String[] from = new String[]{NotesDbAdapter.CATEGORY_KEY_NAME};
        int[] to = new int[]{android.R.id.text1};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, mDbHelper.fetchAllCategories(), from, to, 0);
        mCategorySpinner.setAdapter(adapter);
    }

    private boolean saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        Long categoryFk = mNoCategory.isChecked() ? null : mCategorySpinner.getSelectedItemId();

        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, categoryFk);
            if (id > 0) {
                mRowId = id;
            }

            return id > 0;
        } else {
            return mDbHelper.updateNote(mRowId, title, body, categoryFk);
        }
    }


}
