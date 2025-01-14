package com.ahmadrezagh671.embeddedsubtitleextractor;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Config;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EmbeddedSubtitlesExtractor";
    ActivityResultLauncher<Intent> resultLauncherChooseVideo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!checkDirectoryExists(getAppName(this)))
            createFolder(this, getAppName(this));

        resultLauncherChooseVideo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Context context = getApplicationContext();

                            Uri realPath = result.getData().getData();

                            Uri videoUriStr = getRealPathFromUri(context,realPath);

                            String videoName = getEditTextString();
                            String subUri = getFileDirectory(context) +"/"+ videoName + ".srt";

                            extractSubtitles(videoUriStr.toString(),subUri);

                        } catch (Exception e) {
                            logger("failed : " + e.getMessage());
                        }

                    }
                }
        );
    }

    public static boolean checkDirectoryExists(String name) {
        File file = new File(Environment.getExternalStorageDirectory(), "Download/"+name);
        if (file.exists() && file.isDirectory()) {
            Log.d("StorageUtils", "Directory exists: " + file.getAbsolutePath());
            return true;
        } else {
            Log.d("StorageUtils", "Directory does not exist.");
            return false;
        }
    }

    private void createFolder(Context context, String folderName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, folderName);
        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/" + folderName);  // Scoped directory path

        Uri newFolderUri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (newFolderUri != null) {
            logger("Folder created successfully: " + folderName);
        } else {
            logger("Failed to create folder.");
        }
    }

    public String getEditTextString(){
        String s = ((EditText) findViewById(R.id.editText)).getText().toString();

        if (s.isEmpty())
            s = "example_file_" + System.currentTimeMillis();

        if (s.endsWith(".srt"))
            s = s.substring(0,s.length()-4);

        return s;
    }

    public void logger(String log){
        Log.i(TAG, log);
        ((TextView) findViewById(R.id.textView)).setText(log);
    }

    public void ChooseVideo(View view) {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});
        resultLauncherChooseVideo.launch(intent);
        logger("loading video");
    }


    public static String getFileDirectory(Context context) {
        // Get the app name dynamically
        String appName = getAppName(context);
        // Construct the directory path
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + appName;
    }

    public void extractSubtitles(String videoFilePath, String outputSrtFilePath) {

        logger("FFmpegKit running with video: "+ videoFilePath + " and output: " + outputSrtFilePath);

        String command = "-i \"" + videoFilePath + "\" -map 0:s:0 -y \"" + outputSrtFilePath + "\"";

        int rc = FFmpeg.execute(command);

        if (rc == RETURN_CODE_SUCCESS) {
            //Log.i(Config.TAG, "Command execution completed successfully.");
            logger("File saved: " + outputSrtFilePath);
        } else if (rc == RETURN_CODE_CANCEL) {
            //Log.i(Config.TAG, "Command execution cancelled by user.");
        } else {
            //Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            //Config.printLastCommandOutput(Log.INFO);
            logger(Config.getLastCommandOutput());
        }
    }

    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            CharSequence appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(context.getPackageName(), 0)
            );
            return appName.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "App Name Not Found";
        }
    }

    public Uri getRealPathFromUri(Context context, Uri uri) {
        String realPath = null;

        // Query the MediaStore
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{MediaStore.MediaColumns.DATA}, // Column to retrieve
                null,
                null,
                null
        );

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            if (cursor.moveToFirst()) {
                realPath = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        return Uri.parse(realPath);
    }
}