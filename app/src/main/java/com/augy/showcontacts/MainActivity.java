package com.augy.showcontacts;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID_UPDATE = 11;

    DBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ContactListFragment())
                    .commit();
        }

        mDbHelper = new DBHelper(this);

        getSupportLoaderManager().initLoader(LOADER_ID_UPDATE, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // These are the Contacts rows that we will retrieve.
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        if (id == LOADER_ID_UPDATE) {
            return new CursorLoader(this, baseUri,
                    CONTACTS_SUMMARY_PROJECTION, null, null,
                    null + " COLLATE LOCALIZED ASC") {
                @Override
                public Cursor loadInBackground() {
                    Cursor cursor = super.loadInBackground();
                    if (cursor != null) {
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            Contact contact = new Contact();
                            contact.displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            contact.phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            mDbHelper.addContact(contact);
                        }
                    }

                    return cursor;
                }
            };
        }

        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        MainApplication.getEventBus().post(new ContactUpdateEvent());

    }

    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
