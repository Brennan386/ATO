package com.jordan.ato_smsrelay;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ThemeListActivity extends ListActivity {
    /**
     * Called when the activity is first created.
     */
    final private String[] list = new String[] {"TEST","TEST2","3","Hello World"};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this,
                R.layout.themelist, list));
        getListView().setTextFilterEnabled(true);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

    /* new AlertDialog.Builder(this)
            .setTitle("Hello")
            .setMessage("from " + getListView().getItemAtPosition(position))
            .setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })
            .show();

    Toast.makeText(ListviewActivity.this,
            "ListView: " + l.toString() + "\n" +
                    "View: " + v.toString() + "\n" +
                    "position: " + String.valueOf(position) + "\n" +
                    "id: " + String.valueOf(id),
            Toast.LENGTH_LONG).show(); */
    }

}
