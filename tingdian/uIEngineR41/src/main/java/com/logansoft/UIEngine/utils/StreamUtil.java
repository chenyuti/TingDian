
package com.logansoft.UIEngine.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class StreamUtil {
    /**
     * InputStream 转为 File
     * 
     * @param resut
     * @return
     */
    public static void inputStreamToFile(InputStream ins, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (ins != null) {
                    ins.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * String 转为 InputStream
     * 
     * @param resut
     * @return
     */
    public static InputStream stringToInStream(String resut) {
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(resut.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * The method closes resource
     * 
     * @param is Input stream to close
     * @param os Output stream to close
     */
    public static void closeResource(InputStream is, OutputStream os, FileInputStream fis,
            FileOutputStream fos) {
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
            }
        }

        if (fis != null) {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }

        if (fos != null) {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        byte[] result=output.toByteArray();
        output.close();
        return result;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int)count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
