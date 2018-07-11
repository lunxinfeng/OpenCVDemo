package com.izis.yzext;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lxf on 18-5-28.
 */

public class FileUtil {

    public static void save(Bitmap bitmap) {
        FileOutputStream out = null;
        if (bitmap != null) {
            try {
                String mImagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String mImageName = System.currentTimeMillis() + ".png";
                File fileFolder = new File(mImagePath);

                if (!fileFolder.exists())
                    fileFolder.mkdirs();

                File file = new File(mImagePath, mImageName);

                if (!file.exists())
                    file.createNewFile();

                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
