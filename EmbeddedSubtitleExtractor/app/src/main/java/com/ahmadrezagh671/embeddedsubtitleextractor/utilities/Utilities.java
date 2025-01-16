package com.ahmadrezagh671.embeddedsubtitleextractor.utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.EditText;

public class Utilities {

    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            CharSequence appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(context.getPackageName(), 0)
            );
            return appName.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "App Name Not Found";
        }
    }

    public static String getEditTextString(EditText editText){
        String s = editText.getText().toString();

        if (s.isEmpty())
            s = "example_file_" + System.currentTimeMillis();

        if (s.endsWith(".srt"))
            s = s.substring(0,s.length()-4);

        return s;
    }

}
