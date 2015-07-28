package com.jordan.ato_smsrelay;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // used to store the settings value
    public Boolean enableDebug = false;
    public void loadSettings() {
        // Load the settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        enableDebug = sharedPrefs.getBoolean("debugEnable",false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSettings();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public String getContactID(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri,
                new String[] { ContactsContract.PhoneLookup._ID }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactID = null;
        if (cursor.moveToFirst()) {
            contactID = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.PhoneLookup._ID));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactID;
    }

    public String convertInternationalNumberToLocal(String international){
            return international.replace("+44","0"); // TODO: make this work for all country codes...
    }

    private String importFromSMS(Uri location) {
        // public static final String INBOX = "content://sms/inbox";
        // public static final String SENT = "content://sms/sent";
        // public static final String DRAFT = "content://sms/draft";
        Cursor cursor = getContentResolver().query(location, null, null, null, null);

        String msgData = "";
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgCurrent = "";
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    if (cursor.getColumnName(idx).equals("person")) {
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        if (address!=null) {
                            String number = convertInternationalNumberToLocal(address);
                            String name = new String();
                            if (number != null) {
                                String ID = getContactID(getApplicationContext(), number);
                                if (ID != null) {
                                    name = mNavigationDrawerFragment.contactList.get(ID);
                                }
                            }
                            if (name != null) {
                                msgCurrent += " person:" + name;
                            }
                        }
                    } else if (cursor.getColumnName(idx).equals("address")){
                        msgCurrent += " address:" + convertInternationalNumberToLocal(cursor.getString(idx));
                    }
                    else {
                        msgCurrent += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                    }
                }

                if(enableDebug) {
                    Log.v("SMS DEBUG:", msgCurrent);
                }
                msgData += "\n" + msgCurrent;

                cursor.moveToNext();
            } while (!cursor.isLast());
        } else {
            // empty box, no SMS
            return "";
        }
        return msgData;
    }

    public void onSectionAttached(int number) {
        mTitle = "Contact";
        if (!mNavigationDrawerFragment.contactList.isEmpty()) {
            mTitle = mNavigationDrawerFragment.nameList[number - 1];

            new Thread(new Runnable() {
                public void run() {
                    importFromSMS(Uri.parse("content://sms/inbox"));
                }
            }).start();

            //importFromSMS(Uri.parse("content://sms/sent"));
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
