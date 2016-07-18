package com.wenming.library;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.wenming.library.crash.CrashHandler;
import com.wenming.library.encryption.IEncryption;
import com.wenming.library.save.ISave;
import com.wenming.library.save.imp.LogWriter;
import com.wenming.library.upload.ILogUpload;
import com.wenming.library.upload.UploadService;
import com.wenming.library.util.NetUtil;

/**
 * Created by wenmingvs on 2016/7/7.
 */
public class LogReport {

    private static LogReport logReport = new LogReport();
    /**
     * 设置上传的方式
     */
    public ILogUpload mUpload;
    /**
     * 设置缓存文件夹的大小,默认是30MB
     */
    private long mCacheSize = 30 * 1024 * 1024;

    /**
     * 设置日志保存的路径
     */
    public static String ROOT;

    /**
     * 设置加密方式
     */
    private IEncryption mEncryption;

    /**
     * 设置日志的保存方式
     */
    private ISave mLogSaver;

    /**
     * 设置在哪种网络状态下上传，true为只在wifi模式下，false是wifi和移动网络
     */
    private boolean mWifiOnly = true;


    private LogReport() {
    }

    public static LogReport getInstance() {
        return logReport;
    }

    public LogReport setCacheSize(long cacheSize) {
        this.mCacheSize = mCacheSize;
        return this;
    }

    public LogReport setEncryption(IEncryption encryption) {
        this.mEncryption = encryption;
        return this;
    }

    public LogReport setUploadType(ILogUpload logUpload) {
        mUpload = logUpload;
        return this;
    }

    public LogReport setUploadNetWork(boolean wifiOnly) {
        mWifiOnly = wifiOnly;
        return this;
    }


    public LogReport setLogDir(Context context, String logDir) {
        if (TextUtils.isEmpty(logDir)) {
            //如果SD不可用，则存储在沙盒中
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                ROOT = context.getExternalCacheDir().getAbsolutePath();
            } else {
                ROOT = context.getCacheDir().getAbsolutePath();
            }
        } else {
            ROOT = logDir;
        }
        return this;
    }

    public LogReport setLogSaver(ISave logSaver) {
        this.mLogSaver = logSaver;
        return this;
    }

    public void init(Context context) {
        if (TextUtils.isEmpty(ROOT)) {
            //如果SD不可用，则存储在沙盒中
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                ROOT = context.getExternalCacheDir().getAbsolutePath();
            } else {
                ROOT = context.getCacheDir().getAbsolutePath();
            }
        }
        if (mEncryption != null) {
            mLogSaver.setEncodeType(mEncryption);
        }
        CrashHandler.getInstance().init(context, mLogSaver);
        LogWriter.getInstance().init(mLogSaver);
    }

    public ILogUpload getUpload() {
        return mUpload;
    }

    public long getCacheSize() {
        return mCacheSize;
    }

    /**
     * 调用此方法，上传日志信息
     *
     * @param applicationContext
     */
    public void upload(Context applicationContext) {
        //如果没有设置上传，则不执行
        if (mUpload == null) {
            return;
        }
        //如果网络可用，而且是移动网络，但是用户设置了只在wifi下上传，返回
        if (NetUtil.isConnected(applicationContext) && !NetUtil.isWifi(applicationContext) && mWifiOnly) {
            return;
        }
        Intent intent = new Intent(applicationContext, UploadService.class);
        applicationContext.startService(intent);
    }


}
