package es.unizar.eina.notepad;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class NotepadTests {
    static private final int N_MAX_NOTES = 1000;
    static private final String TEST_PREFIX_NOTE = "TEST_NOTA_";
    static private final String TEST_PREFIX_CATEGORY = "TEST_CATEGORIA_";

    @Test
    public void notesDbAdapterNotesMethodsTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        NotesDbAdapter notesAdapter = new NotesDbAdapter(appContext);
        notesAdapter.open();


        // Método createNote de la clase NotesDbAdapter
        /* Parametro | Clases de equivalencia validas          | Clases de equivalencia no validas
         * ----------------------------------------------------------------------------------------
         * title     | 1. title != null                        | 3. title.length() == 0
         *           | 2. title.length() > 0                   | 4. title == null
         * ----------------------------------------------------------------------------------------
         * body      | 5. body != null                         | 6. body == null
         * ----------------------------------------------------------------------------------------
         * categoryFk| 7. categoryFk > 0                       | 8. categoryFk <= 0
         *           | 9. categoryFk == null
         */

        // Tests mínimos para cubrir clases de equivalencia válidas
        // Clases 1, 2, 5 y 9
        long createdNoteId = notesAdapter.createNote(TEST_PREFIX_NOTE + "0", "CuerpoTest", null);
        assertNotEquals(-1, createdNoteId);

        // Clase 7
        long createdCategoryId = notesAdapter.createCategory(TEST_PREFIX_CATEGORY);
        long createdNoteId2 = notesAdapter.createNote(TEST_PREFIX_NOTE + "1", "CuerpoTest", createdCategoryId);
        assertNotEquals(-1, createdNoteId2);

        notesAdapter.deleteNote(createdNoteId2);

        // Tests para cubrir cada clase de equivalencia no válida
        // Clase 3
        assertEquals(-1, notesAdapter.createNote("", "CuerpoTest", null));

        // Clase 4
        assertEquals(-1, notesAdapter.createNote(null, "CuerpoTest", null));

        // Clase 6
        assertEquals(-1, notesAdapter.createNote(TEST_PREFIX_NOTE, null, null));

        // Clase 8
        assertEquals(-1, notesAdapter.createNote(TEST_PREFIX_NOTE, "CuerpoTest", Long.valueOf(0)));


        // Método updateNote de la clase NotesDbAdapter
        /* Parametro | Clases de equivalencia validas          | Clases de equivalencia no validas
         * ----------------------------------------------------------------------------------------
         * rowId     | 1. rowId > 0                            | 2. rowId <= 0
         * ----------------------------------------------------------------------------------------
         * title     | 3. title != null                        | 5. title.length() == 0
         *           | 4. title.length() > 0                   | 6. title == null
         * ----------------------------------------------------------------------------------------
         * body      | 7. body != null                         | 8. body == null
         * ----------------------------------------------------------------------------------------
         * categoryFk| 9. categoryFk > 0                       | 10. categoryFk <= 0
         *           | 11. categoryFk == null
         */

        // Tests mínimos para cubrir clases de equivalencia válidas
        // Clases 1, 3, 4, 7 y 11
        assertTrue(notesAdapter.updateNote(createdNoteId, TEST_PREFIX_NOTE + "2", "CuerpoTest2", null));

        // Clase 9
        assertTrue(notesAdapter.updateNote(createdNoteId, TEST_PREFIX_NOTE + "2", "CuerpoTest3", createdCategoryId));

        // Tests para cubrir cada clase de equivalencia no válida
        // Clase 2
        assertFalse(notesAdapter.updateNote(0, TEST_PREFIX_NOTE + "2", "CuerpoTest2", null));

        // Clase 5
        assertFalse(notesAdapter.updateNote(createdNoteId, "", "CuerpoTest2", null));

        // Clase 6
        assertFalse(notesAdapter.updateNote(createdNoteId, null, "CuerpoTest2", null));

        // Clase 8
        assertFalse(notesAdapter.updateNote(createdNoteId, TEST_PREFIX_NOTE + "2", null, null));

        // Clase 10
        assertFalse(notesAdapter.updateNote(createdNoteId, TEST_PREFIX_NOTE + "2", "CuerpoTest2", Long.valueOf(0)));


        // Método deleteNote de la clase NotesDbAdapter
        /* Parametro | Clases de equivalencia validas          | Clases de equivalencia no validas
         * ----------------------------------------------------------------------------------------
         * rowId     | 1. rowId > 0                            | 2. rowId <= 0
         */

        // Tests mínimos para cubrir clases de equivalencia válidas
        // Clase 1
        assertTrue(notesAdapter.deleteNote(createdNoteId));

        // Tests para cubrir cada clase de equivalencia no válida
        // Clase 2
        assertFalse(notesAdapter.deleteNote(0));


        notesAdapter.deleteCategory(createdCategoryId);
        notesAdapter.close();
    }

    @Test
    public void notesDbAdapterCategoriesMethodsTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        NotesDbAdapter notesAdapter = new NotesDbAdapter(appContext);
        notesAdapter.open();


        // Método createCategory de la clase NotesDbAdapter
        /* Parametro | Clases de equivalencia validas          | Clases de equivalencia no validas
         * ----------------------------------------------------------------------------------------
         * name      | 1. name != null                         | 3. name.length() == 0
         *           | 2. name.length() > 0                    | 4. name == null
         */

        // Tests mínimos para cubrir clases de equivalencia válidas
        // Clases 1 y 2
        long createdCategoryId = notesAdapter.createCategory(TEST_PREFIX_CATEGORY);
        assertNotEquals(-1, createdCategoryId);

        // Tests para cubrir cada clase de equivalencia no válida
        // Clase 3
        assertEquals(-1, notesAdapter.createCategory(""));

        // Clase 4
        assertEquals(-1, notesAdapter.createCategory(null));


        // Método updateCategory de la clase NotesDbAdapter
        /* Parametro | Clases de equivalencia validas          | Clases de equivalencia no validas
         * ----------------------------------------------------------------------------------------
         * rowId     | 1. rowId > 0                            | 2. rowId <= 0
         * ----------------------------------------------------------------------------------------
         * name      | 3. name != null                         | 5. name.length() == 0
         *           | 4. name.length() > 0                    | 6. name == null
         */

        // Tests mínimos para cubrir clases de equivalencia válidas
        // Clases 1, 3 y 4
        assertTrue(notesAdapter.updateCategory(createdCategoryId, TEST_PREFIX_CATEGORY));

        // Tests para cubrir cada clase de equivalencia no válida
        // Clase 2
        assertFalse(notesAdapter.updateCategory(0, TEST_PREFIX_CATEGORY));

        // Clase 5
        assertFalse(notesAdapter.updateCategory(createdCategoryId, ""));

        // Clase 6
        assertFalse(notesAdapter.updateCategory(createdCategoryId, null));


        // Método deleteCategory de la clase NotesDbAdapter
        /* Parametro | Clases de equivalencia validas          | Clases de equivalencia no validas
         * ----------------------------------------------------------------------------------------
         * rowId     | 1. rowId > 0                            | 2. rowId <= 0
         */

        // Tests mínimos para cubrir clases de equivalencia válidas
        // Clase 1
        assertTrue(notesAdapter.deleteCategory(createdCategoryId));

        // Tests para cubrir cada clase de equivalencia no válida
        // Clase 2
        assertFalse(notesAdapter.deleteCategory(0));


        notesAdapter.close();
    }

    @Test
    public void notesVolumeTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        NotesDbAdapter notesAdapter = new NotesDbAdapter(appContext);
        notesAdapter.open();

        try {
            for (Integer i = 0; i < N_MAX_NOTES; ++i) {
                assertNotEquals(-1, notesAdapter.createNote(TEST_PREFIX_NOTE + i.toString(), "CUERPO_TEST", null));
            }
        } catch (Exception e) {
            fail();
        }

        notesAdapter.close();
    }

    @Test
    public void deleteTestNotes() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        NotesDbAdapter notesAdapter = new NotesDbAdapter(appContext);
        notesAdapter.open();

        notesAdapter.deleteNotesWhere(NotesDbAdapter.NOTE_KEY_TITLE + " LIKE '" + TEST_PREFIX_NOTE + "%'");

        notesAdapter.close();
    }

    @Test
    public void notesTextLengthTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        NotesDbAdapter notesAdapter = new NotesDbAdapter(appContext);
        notesAdapter.open();

        int textLength = 1;
        try {
            while (true) {
                // TODO: logear la longitud de la string (textLength)
                char[] chars = new char[textLength];
                Arrays.fill(chars, '*');
                assertNotEquals(-1, notesAdapter.createNote(TEST_PREFIX_NOTE + "LONGITUD_TEST", new String(chars), null));
                System.out.println("Probando titulo con: " + textLength + " caracteres");
                textLength *= 2;
            }
        } catch(Exception e) {

        }

        notesAdapter.close();
    }
}
