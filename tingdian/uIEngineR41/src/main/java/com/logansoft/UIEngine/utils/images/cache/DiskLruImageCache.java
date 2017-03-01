package com.logansoft.UIEngine.utils.images.cache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.jakewharton.disklrucache.DiskLruCache;
import com.logansoft.UIEngine.BuildConfig;
import com.logansoft.UIEngine.utils.Configure;
import com.logansoft.UIEngine.utils.MD5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import hdz.base.Function;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskLruImageCache implements ImageCache {

    private DiskLruCache mDiskCache;
    private CompressFormat mCompressFormat = CompressFormat.JPEG;
    private static int IO_BUFFER_SIZE = 8*1024;
    private int mCompressQuality = 70;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    public DiskLruImageCache(Context context,String uniqueName, int diskCacheSize,
        CompressFormat compressFormat, int quality ) {
        try {   
                final File diskCacheDir = getDiskCacheDir(context, uniqueName );
                mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
                mCompressFormat = compressFormat;
                mCompressQuality = quality;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor )
        throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    public void putBitmap( String key, Bitmap data ) {

        DiskLruCache.Editor editor = null;
        try {
           
            String md5key =  Function.MD5_GetString(key).toLowerCase();
            editor = mDiskCache.edit( md5key );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {               
                mDiskCache.flush();
                editor.commit();
                if ( Configure.DEBUG_MODE) {
                   Log.d( "cache_test_DISK_", "image put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( Configure.DEBUG_MODE ) {
                    Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
                }
            }   
        } catch (IOException e) {
            if ( Configure.DEBUG_MODE) {
                Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }           
        }

    }

    @Override
    public Bitmap getBitmap( String key ) {

        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            String md5key =  Function.MD5_GetString(key).toLowerCase();
            snapshot = mDiskCache.get( md5key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = 
                new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream( buffIn );              
            }   
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        if (Configure.DEBUG_MODE ) {
            Log.d( "cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
        }

        return bitmap;

    }

    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( Configure.DEBUG_MODE ) {
            Log.d( "cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
            mDiskCache = DiskLruCache.open(getCacheFolder(),APP_VERSION, VALUE_COUNT, mDiskCache.getMaxSize() );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

}
