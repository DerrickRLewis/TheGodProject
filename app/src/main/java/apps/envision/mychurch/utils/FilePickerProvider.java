package apps.envision.mychurch.utils;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

import apps.envision.mychurch.App;

public class FilePickerProvider extends FileProvider {
    private static final String FILE_PROVIDER = ".filepicker.provider";

    public static String getAuthority(@NonNull Context context) {
        return context.getPackageName() + FILE_PROVIDER;
    }

    public static Uri getUriForFile(@NonNull File file) {
        Context context = App.getContext();
        return getUriForFile(context, getAuthority(context), file);
    }
}
