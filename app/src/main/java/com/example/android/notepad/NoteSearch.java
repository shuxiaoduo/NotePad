package com.example.android.notepad;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Lenovo on 2020-05-11.
 */

public class NoteSearch extends Activity{
    private static final String TAG = "NoteSearch";
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_CREATE_DATE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.setContentView(R.layout.note_search);
        Intent intent = getIntent();

        // If there is no data associated with the Intent, sets the data to the default URI, which
        // accesses a list of notes.
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        final ListView listview= (ListView) findViewById(R.id.listview);
        NotePadProvider.DatabaseHelper dh=new NotePadProvider.DatabaseHelper(this);
        final SQLiteDatabase db=dh.getReadableDatabase();
        SearchView search= (SearchView) findViewById(R.id.search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Cursor cursor=db.query(
                        NotePad.Notes.TABLE_NAME,
                        PROJECTION,
                        NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?",
                        new String[]{"%"+s+"%","%"+s+"%"},
                        null,
                        null,
                        NotePad.Notes.DEFAULT_SORT_ORDER);
                int[] viewIDs = { R.id.text3,R.id.text4};

                // Creates the backing adapter for the ListView.
                SimpleCursorAdapter adapter
                        = new SimpleCursorAdapter(
                        NoteSearch.this,                             // The Context for the ListView
                        R.layout.notesearch_listview,          // Points to the XML for a list item
                        cursor,                           // The cursor to get items from
                        new String[]{NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_CREATE_DATE},
                        viewIDs
                );

                // Sets the ListView's adapter to be the cursor adapter that was just created.
                listview.setAdapter(adapter);
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Constructs a new URI from the incoming URI and the row ID
                Uri uri = ContentUris.withAppendedId(getIntent().getData(), l);

                // Gets the action from the incoming Intent
                String action = getIntent().getAction();

                // Handles requests for note data
                if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

                    // Sets the result to return to the component that called this Activity. The
                    // result contains the new URI
                    setResult(RESULT_OK, new Intent().setData(uri));
                } else {

                    // Sends out an Intent to start an Activity that can handle ACTION_EDIT. The
                    // Intent's data is the note ID URI. The effect is to call NoteEdit.
                    startActivity(new Intent(Intent.ACTION_EDIT, uri));
                }
            }
        });


    }
}
