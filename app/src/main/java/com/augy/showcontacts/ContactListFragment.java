package com.augy.showcontacts;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

/**
 * A placeholder fragment containing a simple view.
 */
public class ContactListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOAD_CONTACT_LOADER_ID = 10;

    public ContactListFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        MainApplication.getEventBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainApplication.getEventBus().unregister(this);
    }

    // This is the Adapter being used to display the list's data.
    SimpleCursorAdapter mAdapter;
    DBHelper mDbHelper;

    // If non-null, this is the current filter the user has provided.
    String mCurFilter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("No phone numbers");

        mDbHelper = new DBHelper(getActivity());

        // We have a menu item to show in action bar.
        setHasOptionsMenu(true);

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.contact_row, null,
                new String[] { DBHelper.COLUMN_DISPLAY_NAME, DBHelper.COLUMN_PHONE_NUMBER },
                new int[] { R.id.display_name, R.id.mobile_number}, 0);
        setListAdapter(mAdapter);

        // Start out with a progress indicator.
        setListShown(false);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(LOAD_CONTACT_LOADER_ID, null, this);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Place an action bar item for searching.
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        View searchView = SearchViewCompat.newSearchView(getActivity());
        if (searchView != null) {
            SearchViewCompat.setOnQueryTextListener(searchView,
                    new SearchViewCompat.OnQueryTextListenerCompat() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Called when the action bar search text has changed.  Update
                            // the search filter, and restart the loader to do a new query
                            // with this filter.
                            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
                            getLoaderManager().restartLoader(LOAD_CONTACT_LOADER_ID, null, ContactListFragment.this);
                            return true;
                        }
                    });
            item.setActionView(searchView);
        }
    }

    @Override public void onListItemClick(ListView l, View v, int position, long id) {
        // Insert desired behavior here.
        Log.i("FragmentComplexList", "Item clicked: " + id);
    }

    // These are the Contacts rows that we will retrieve.
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_DISPLAY_NAME,
            DBHelper.COLUMN_PHONE_NUMBER,
    };

    @Subscribe
    public void updateContactEvent(ContactUpdateEvent event) {
        getLoaderManager().restartLoader(LOAD_CONTACT_LOADER_ID, null, ContactListFragment.this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
        Uri baseUri;
        if (mCurFilter != null) {
            baseUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(mCurFilter));
        } else {
            baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + ContactsContract.Contacts.DISPLAY_NAME + " != '' )" + "AND ("+ContactsContract.Contacts.HAS_PHONE_NUMBER +" != '0'"+"))";

        if (id == LOAD_CONTACT_LOADER_ID) {
            return new CursorLoader(getActivity(), null,
                    CONTACTS_SUMMARY_PROJECTION, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC") {
                @Override
                public Cursor loadInBackground() {
                    return mDbHelper.getContacts(mCurFilter);
                }
            };
        }

        return null;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOAD_CONTACT_LOADER_ID) {
            // Swap the new cursor in.  (The framework will take care of closing the
            // old cursor once we return.)
            mAdapter.swapCursor(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}