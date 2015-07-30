package com.jordan.wordnetquery;

import android.content.ContextWrapper;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import edu.smu.tspell.wordnet.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.jordan.wordnetquery.Decompress;

public class MainActivity extends ActionBarActivity {

    /**
     * Main entry point. The command-line arguments are concatenated together
     * (separated by spaces) and used as the word form to look up.
     */
    public String Output = new String ();
    public String FormOut = new String ();
    public String SynOut = new String ();

    public void outputText(String wordForm)
    {
        //  Get the synsets containing the word form
        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets(wordForm);
        //  Display the word forms and definitions for synsets retrieved
        if (synsets.length > 0)
        {
            for (int i = 0; i < synsets.length; i++)
            {
                System.out.println("");
                String[] wordForms = synsets[i].getWordForms();
                for (int j = 0; j < wordForms.length; j++)
                {
                    FormOut = ((j > 0 ? ", " : "") +
                            wordForms[j]);
                }
                SynOut = (": " + synsets[i].getDefinition());
                Output = (FormOut + " " + SynOut + "\n");
            }

            EditText textBox = (EditText) findViewById(R.id.OutputTxt);
            textBox.setText(Output);
        }
        else
        {
            Log.d("Wordnet","No synsets exist that contain " +
                    "the word form '" + wordForm + "'");
        }
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

    Button   mButton;
    EditText mEdit;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button);

        ContextWrapper c = new ContextWrapper(this);
        String dataDir = c.getApplicationContext().getFilesDir().getAbsolutePath();
        String Dir;
        Dir = dataDir + File.separator + "WordNet-3.0" + File.separator + "dict";
        System.setProperty("wordnet.database.dir", Dir);

        mButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        mEdit = (EditText) findViewById(R.id.name);
                        String text = mEdit.getText().toString();
                        outputText(text);
                    }
                });


        File f = new File(dataDir+File.separator+"WordNet-3.0");
        if (!(f.exists()&&f.isDirectory())){
            f.mkdirs();
            final File out = new File(dataDir);


            try {
                new Thread(new Runnable() {
                    public void run() {
                        Log.d("com.jordan.wordnetquery","Download begin");
                        downloadFile("https://www.dropbox.com/s/qojp3bqbolhf9cb/WordNet-3.0.zip?dl=1", out);
                    }
                }).join(); //thread.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String zipFile = dataDir + File.separator + "WordNet-3.0.zip";
            String unzipLocation = dataDir + File.separator;

            Decompress d = new Decompress(zipFile, unzipLocation);
            d.unzip();



            File zip = new File(zipFile);
            //zip.delete()

        }
    }

    private static void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch(FileNotFoundException e) {
            Log.e("FATAL ERROR", "FILE NOT FOUND");
            return; // swallow a 404
        } catch (IOException e) {
            Log.e("FATAL ERROR", "IOException");
            return; // swallow a 404
        }
    }
}
