package com.example.ashu4642.mcommunicate.P2P;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ashu4642.mcommunicate.R;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * An activity to select a single file.
 *
 * @return Returns Intent to onActivityResult with String extra containing the path to the user selected file
 * <code>intent.getStringExtra(FileSelectActivity.SELECTED_PATH)</code>
 *
 */
public class FileSelectActivity extends Activity {

    /** A string constant for the bundle extra indicating where to start the file selection browser */
    public static final String TARGET_PATH = "com.davidiserovich.FileSelectActivity.TARGET_PATH";

    /** Constant key for the bundle extra indicating the file that the user picked */
    public static final String SELECTED_PATH = "com.davidiserovich.FileSelectActivity.SELECTED_PATH";

    /** The ListView displaying the current directory */
    ListView fileList;

    /** The adapter providing the list items for the fileList */
    ArrayAdapter<File> fileListAdapter;

    /** The path of the file the user selects */
    private String selectedFilePath;

    /** The currently displayed directory */
    private File currentDirectory;

    /** The list of files in the current directory */
    private File[] items;

    // Cached icons
    protected Drawable folderIcon;
    protected Drawable fileIcon;

    /**
     * Initialize the file list using data set in the calling intents, or defaulting to /
     */
    private void initializeFilelist(){
        Intent launchingIntent = getIntent();
        String startPath = launchingIntent.getStringExtra(TARGET_PATH);
        File startFile;

        // Check that the start path makes sense
        if (startPath == null || !(startFile = new File(startPath)).isDirectory()){
            Toast.makeText(this, startPath + " is not a valid directory!", Toast.LENGTH_SHORT).show();
            startFile = new File("/");
        }

        currentDirectory = startFile;

        // Initialize the list
        populateList();


		/*
		 * Set the listener to return the file's full path as the activity result if it's a file
		 *  or navigate deeper if it's a directory
		 */
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                // The first entry is always the previous directory ..
                if (pos == 0){
                    if (currentDirectory.getParent() != null){
                        currentDirectory = currentDirectory.getParentFile();
                        populateList();
                    }
                }
                else {
                    // The position in the view is one less than the position in the list of files
                    pos -= 1;
                    File f = fileListAdapter.getItem(pos);

                    if (f.isDirectory()){
                        currentDirectory = f;
                        populateList();
                    }

                    else {
                        selectedFilePath = f.getAbsolutePath();
                        Intent resultData = new Intent();
                        resultData.putExtra(SELECTED_PATH, selectedFilePath);
                        setResult(RESULT_OK, resultData);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selection);
        Log.d("what is this", findViewById(R.id.files_list).toString());
        fileList = (ListView)findViewById(R.id.files_list);

        // Cache the icons
        folderIcon = getResources().getDrawable(R.drawable.collections_collection);
        fileIcon = getResources().getDrawable(R.drawable.collections_view_as_list);

        initializeFilelist();
    }

    /**
     * Handle press of cancel button, returning failed.
     * @param v the button
     */
    public void onClickCancel(View v){
        // onPause sets failure code for single-file selection
        finish();
    }

    /**
     * Set the array adapter for the file list
     */
    private void populateList(){
        // Fill up the items list
        items = currentDirectory.listFiles();

        // Sort alphabetically, showing directories first
        Arrays.sort(items, new Comparator<File>(){
            public int compare(File f1, File f2)
            {
                if ( f1.isDirectory() && !f2.isDirectory() ) return -1;
                if ( f2.isDirectory() && !f1.isDirectory() ) return 1;


                return f1.getName().compareTo(f2.getName());
            }
        });

        if (items != null){
            fileListAdapter = new ArrayAdapter<File>(this, R.id.files_list, items){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = convertView;
                    if (v == null) {
                        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        v = vi.inflate(R.layout.file_list_item, null);
                    }
                    if (v != null) {
                        TextView titleView = (TextView) v.findViewById(R.id.filename);
                        ImageView icon = (ImageView) v.findViewById(R.id.icon_image);
                        if (position == 0){
                            titleView.setText("..");
                            icon.setImageDrawable(folderIcon);
                        }
                        else{
                            titleView.setText(items[position-1].getName());
                            if (items[position-1].isDirectory())
                                icon.setImageDrawable(folderIcon);
                            else
                                icon.setImageDrawable(fileIcon);

                        }
                    }else{
                        Log.d("Something", "Is Wrong");
                    }
                    return v;

                }

            };

            fileList.setAdapter(fileListAdapter);
        }
        else {
            Toast.makeText(this, "Directory " + currentDirectory.toString() + " inaccessible.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Browse up one directory
     *
     * @return true if success, false if fail
     */

    private boolean navigateUp(){
        if (currentDirectory.getParent() != null){
            currentDirectory = currentDirectory.getParentFile();
            populateList();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Go up one directory, or exit out if we're at the root directory
        if (!navigateUp()){
            super.onBackPressed();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_file_selection, menu);
        return true;
    }
}