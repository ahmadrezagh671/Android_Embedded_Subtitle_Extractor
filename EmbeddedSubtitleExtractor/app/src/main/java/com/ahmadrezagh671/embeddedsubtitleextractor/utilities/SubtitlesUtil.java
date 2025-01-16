package com.ahmadrezagh671.embeddedsubtitleextractor.utilities;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;

public class SubtitlesUtil {

    public static File extractSubtitles(Context context, String videoFilePath) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File myFile = new File(dir, "testfile.srt");
        String outputSrtFilePath = myFile.getPath();

        String command = "-i \"" + videoFilePath + "\" -map 0:s:0 -y \"" + outputSrtFilePath + "\"";

        int rc = FFmpeg.execute(command);

        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
            //logger("File saved: " + outputSrtFilePath);
            return myFile;
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
            //logger("Command execution cancelled by user.");
            return null;
        } else {
            Log.e(Config.TAG, Config.getLastCommandOutput());
            //logger(Config.getLastCommandOutput());
            return null;
        }
    }

}
