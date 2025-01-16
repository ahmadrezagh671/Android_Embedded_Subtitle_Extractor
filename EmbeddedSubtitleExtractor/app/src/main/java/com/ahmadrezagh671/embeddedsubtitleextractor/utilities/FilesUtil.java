package com.ahmadrezagh671.embeddedsubtitleextractor.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

public class FilesUtil {

    public static String fileToString(File file){
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Process each line (e.g., print to console)
                stringBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            // Handle exception
            Log.e("FilesUtil", "fileToString: " + e.getMessage());
        }
        return stringBuilder.toString();
    }

    public static boolean checkDirectoryExists(String name) {
        File file = new File(Environment.getExternalStorageDirectory(), "Download/"+name);
        if (file.exists() && file.isDirectory()) {
            Log.i("FilesUtil", "Directory exists: " + file.getAbsolutePath());
            return true;
        } else {
            Log.i("FilesUtil", "Directory does not exist.");
            return false;
        }
    }

    public static void createFolder(Context context, String folderName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, folderName);
        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/" + folderName);  // Scoped directory path

        Uri newFolderUri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (newFolderUri != null) {
            Log.i("FilesUtil", "Folder created successfully: " + folderName);
        } else {
            Log.i("FilesUtil", "Failed to create folder.");

        }
    }

    public static Uri createFileWithData(Context context,String folderName, String fileName,byte[] data) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/" + folderName);  // Scoped directory path
        values.put(MediaStore.Files.FileColumns.MIME_TYPE, 5);

        Uri newFileUri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (newFileUri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(newFileUri)) {
                if (outputStream != null) {
                    // Write the data to the file
                    outputStream.write(data);
                    outputStream.flush();
                    Log.i("FilesUtil", "File created and data written successfully: " + fileName);
                } else {
                    Log.i("FilesUtil", "Failed to get output stream for writing data.");
                }
            } catch (IOException e) {
                Log.i("FilesUtil", "Error writing data to file: " + e.getMessage());
            }
            Log.i("FilesUtil", "File created successfully: " + fileName);
            return newFileUri;
        } else {
            Log.i("FilesUtil", "Failed to create File.");
            return null;
        }
    }

    public static void ChooseVideo(ActivityResultLauncher<Intent> resultLauncher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});
            resultLauncher.launch(intent);
        }else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
            resultLauncher.launch(intent);
        }
        Log.i("FilesUtil", "loading video");
    }
    
}
