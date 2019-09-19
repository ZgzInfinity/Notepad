package es.unizar.eina.notepad;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CategoryEdit extends AppCompatActivity {

    private EditText mNameText;
    private EditText mIdText;
    private Long mRowId;

    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.category_edit);

        mNameText = (EditText) findViewById(R.id.name);

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
        builder.setMessage(R.string.name_error_msg);

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
            Cursor note = mDbHelper.fetchCategory(mRowId);
            startManagingCursor(note);
            mNameText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.CATEGORY_KEY_NAME)));

            mIdText.setText(mRowId.toString());
        }
        else {
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

    private boolean saveState() {
        String name = mNameText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createCategory(name);
            if (id > 0) {
                mRowId = id;
            }

            return id > 0;
        } else {
            return mDbHelper.updateCategory(mRowId, name);
        }
    }
}
