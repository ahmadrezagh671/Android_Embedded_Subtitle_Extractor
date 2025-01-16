package com.ahmadrezagh671.embeddedsubtitleextractor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ext.SdkExtensions;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ahmadrezagh671.embeddedsubtitleextractor.utilities.FilesUtil;
import com.ahmadrezagh671.embeddedsubtitleextractor.utilities.RealPathUtil;
import com.ahmadrezagh671.embeddedsubtitleextractor.utilities.SubtitlesUtil;
import com.ahmadrezagh671.embeddedsubtitleextractor.utilities.Utilities;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "EmbeddedSubtitlesExtractor";
    ActivityResultLauncher<Intent> resultLauncherChooseVideo;
    EditText editText;
    Button buttonOpenVideo;

    private static final int PERMISSION_REQUEST_CODE = 167;


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

        editText = findViewById(R.id.editText);
        buttonOpenVideo = findViewById(R.id.buttonOpenVideo);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) < 2) {
            requestPermissions();
        }

        if (!FilesUtil.checkDirectoryExists(Utilities.getAppName(this)))
            FilesUtil.createFolder(this, Utilities.getAppName(this));

        resultLauncherChooseVideo = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        Context context = getApplicationContext();

                        if (result.getData() == null){
                            logger("Failed To load The File");
                            return;
                        }

                        Uri videoUri = result.getData().getData();

                        Uri realPath = RealPathUtil.getRealPathFromURI(context,videoUri);

                        String subtitleName = Utilities.getEditTextString(editText) + ".srt";

                        File subtitleFile = SubtitlesUtil.extractSubtitles(context,realPath.toString());
                        if (subtitleFile == null){
                            logger("FFmpeg Error, Subtitles may not be found.");
                            return;
                        }

                        String subtitleStr = FilesUtil.fileToString(subtitleFile);
                        if(subtitleStr.length() > 1000)
                            logger("subtitleStr: " + subtitleStr.substring(0,900));
                        else
                            logger("subtitleStr: " + subtitleStr);


                        Uri newFileUri = FilesUtil.createFileWithData(context, Utilities.getAppName(context),subtitleName,subtitleStr.getBytes());

                        newFileUri = RealPathUtil.getRealPathFromURI(context,newFileUri);

                        logger("File Saved: " + newFileUri.toString());
                    } catch (Exception e) {
                        logger("failed: " + e.getMessage());
                    }

                }
        );

        buttonOpenVideo.setOnClickListener(view -> {
            FilesUtil.ChooseVideo(resultLauncherChooseVideo);
            logger("Loading File . . .");
        });
    }

    private void logger(String log){
        Log.i(TAG, log);
        ((TextView) findViewById(R.id.textView)).setText(log);
    }

    private void requestPermissions() {
        // Check for EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    logger("Permission granted for: " + permissions[i]);
                } else {
                    logger("Permission denied for: " + permissions[i]);
                }
            }
        }
    }
}