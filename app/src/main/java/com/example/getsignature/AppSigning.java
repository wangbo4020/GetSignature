package com.example.getsignature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import androidx.annotation.NonNull;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 获取签名工具类
 */
public class AppSigning {
    private static final String TAG = "AppSigning";

    public final static String MD5 = "MD5";
    public final static String SHA1 = "SHA1";
    public final static String SHA256 = "SHA256";
    private static HashMap<String, ArrayList<String>> mSignMap = new HashMap<>();

    /**
     * 返回一个签名的对应类型的字符串
     *
     * @param context
     * @param type
     * @return 因为一个安装包可以被多个签名文件签名，所以返回一个签名信息的list
     */
    public static ArrayList<String> getSignInfo(@NonNull Context context, @NonNull String packageName, @NonNull String type) throws PackageManager.NameNotFoundException {
        final String cacheKey = packageName + ":" + type;
        if (mSignMap.get(cacheKey) != null) {
            return mSignMap.get(cacheKey);
        }
        ArrayList<String> mList = new ArrayList<String>();
        Signature[] signs = getSignatures(context, packageName);
        for (Signature sig : signs) {
            String tmp = null;
            if (MD5.equals(type)) {
                tmp = getSignatureByteString(sig, MD5);
            } else if (SHA1.equals(type)) {
                tmp = getSignatureByteString(sig, SHA1);
            } else if (SHA256.equals(type)) {
                tmp = getSignatureByteString(sig, SHA256);
            }
            if (tmp != null) mList.add(tmp);
        }
        mSignMap.put(cacheKey, mList);
        return mList;
    }

    /**
     * 获取签名sha1值
     *
     * @param context
     * @return
     */
    public static String getSHA1(@NonNull Context context, @NonNull String packageName) throws PackageManager.NameNotFoundException {
        String res = null;
        ArrayList<String> mlist = getSignInfo(context, packageName, SHA1);
        if (mlist != null && mlist.size() != 0) {
            res = mlist.get(0);
        }
        return res;
    }

    /**
     * 获取签名MD5值
     *
     * @param context
     * @return
     */
    public static String getMD5(@NonNull Context context, @NonNull String packageName) throws PackageManager.NameNotFoundException {
        String res = null;
        ArrayList<String> mlist = getSignInfo(context, packageName, MD5);
        if (mlist != null && mlist.size() != 0) {
            res = mlist.get(0);
        }
        return res;
    }

    /**
     * 获取签名SHA256值
     *
     * @param context
     * @return
     */
    public static String getSHA256(@NonNull Context context, @NonNull String packageName) throws PackageManager.NameNotFoundException {
        String res = null;
        ArrayList<String> mlist = getSignInfo(context, packageName, SHA256);
        if (mlist != null && mlist.size() != 0) {
            res = mlist.get(0);
        }
        return res;
    }

    /**
     * 返回对应包的签名信息
     *
     * @param context
     * @param packageName
     * @return
     */
    private static Signature[] getSignatures(@NonNull Context context, @NonNull String packageName) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return packageInfo.signatures;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            throw e;
        }
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成16进制）
     *
     * @param sig
     * @param type
     * @return
     */
    private static String getSignatureString(Signature sig, String type) {
        byte[] hexBytes = sig.toByteArray();
        String fingerprint = "error!";
        try {
            MessageDigest digest = MessageDigest.getInstance(type);
            if (digest != null) {
                byte[] digestBytes = digest.digest(hexBytes);
                StringBuilder sb = new StringBuilder();
                for (byte digestByte : digestBytes) {
                    sb.append((Integer.toHexString((digestByte & 0xFF) | 0x100)).substring(1, 3));
                }
                fingerprint = sb.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return fingerprint;
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成 95:F4:D4:FG 这样的字符串形式）
     *
     * @param sig
     * @param type
     * @return
     */
    private static String getSignatureByteString(Signature sig, String type) {
        byte[] hexBytes = sig.toByteArray();
        String fingerprint = "error!";
        try {
            MessageDigest digest = MessageDigest.getInstance(type);
            if (digest != null) {
                byte[] digestBytes = digest.digest(hexBytes);
                StringBuilder sb = new StringBuilder();
                for (byte digestByte : digestBytes) {
                    sb.append(((Integer.toHexString((digestByte & 0xFF) | 0x100)).substring(1, 3)).toUpperCase());
                    sb.append(":");
                }
                fingerprint = sb.substring(0, sb.length() - 1).toString();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return fingerprint;
    }
}


