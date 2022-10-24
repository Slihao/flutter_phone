package com.slh.flutter_phone.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;


public class AudioUtils {

    //上下文
    Context context;
    Activity activity;

    //上下文
    public AudioUtils(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
    }

    //系统录音目录
    private static String getSystemRecord() {
        File parent = Environment.getExternalStorageDirectory();
        File child;

        child = new File(parent, "Sounds/CallRecord");
        if (!child.exists()) {
            child = new File(parent, "record");
        }
        if (!child.exists()) {
            child = new File(parent, "MIUI/sound_recorder/call_rec");
        }
        if (!child.exists()) {
            child = new File(parent, "Recorder");
        }
        if (!child.exists()) {
            child = new File(parent, "Recordings/Call Recordings");
            if (!child.exists()) {
                child = new File(parent, "Recordings");
            }
        }
        if (!child.exists()) {
            child = new File(parent, "Record/Call");
        }
        if (!child.exists()) {
            child = new File(parent, "Sounds");
        }
        if (!child.exists()) {
            return null;
        }
        return child.getAbsolutePath();
    }

    //常用系统录音文件存放文件夹
    private static ArrayList<String> getRecordFiles() {
        String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        ArrayList<String> list = new ArrayList<>();
        File file = new File(parentPath, "record");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "Sounds/CallRecord");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "MIUI/sound_recorder/call_rec");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "Recorder");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "Recordings/Call Recordings");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "Recordings");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "Record/Call");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "Sounds");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        //oppp android-10 手机存储系统录音
        file = new File(parentPath, "Music/Recordings/Call Recordings");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }
        file = new File(parentPath, "PhoneRecord");
        if (file.exists()) {
            list.add(file.getAbsolutePath());
        }

        // 或者其余机型系统录音文件夹 添加
        return list;
    }

    //其它APP录音文件
    private static boolean isNotRecordAppDir(File dir) {
        String name = dir.getName();
        if ("Android".equals(name)) {
            return false;
        } else if ("不是录音文件夹都可以写在这".equals(name)) {
            return false;
        }
        //加入一些会录音的app,会生成录音文件,防止使用其他录音文件而没有使用系统录音文件
        return true;
    }

    //读取录音时长
    public static int getRecDuration(String filename) {
        int duration = 0;
        int second = 0;
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filename);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.release();
        return duration;
    }

    //查找录音文件
    private static File searchRecordFile(long time, File dir, int count) {
        //计算调用次数 --- 层级不必太多
        if (dir.isDirectory() && isNotRecordAppDir(dir) && count < 4) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    //10秒之内生成的文件 默认为当前的录音文件
                    if (matchFileNameIsRecord(file.getName()) && file.lastModified() - time > -10 * 1000
                            && file.length() > 0 && file.isFile()) {
                        return file;
                    }
                    if (file.isDirectory()) {
                        return searchRecordFile(time, file, count + 1);
                    }
                }
            }
        }
        return null;
    }

    //录音后缀匹配
    private static boolean matchFileNameIsRecord(String name) {
        //录音文件匹配规则 -- 可以自行添加其他格式录音匹配
        try {
            if (name.toLowerCase().endsWith(".mp3".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".wav".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".3gp".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".mp4".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".mpa".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".amr".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".3gpp".toLowerCase())) {
                return true;
            } else if (name.toLowerCase().endsWith(".m4a".toLowerCase())) {
                return true;
            }
        } catch (Exception e) {
            Log.e("res:Exception:不是录音文件", String.valueOf(e));
        }
        return false;
    }

    //输出文件信息:列表
    public static void toStrings(JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject data = array.optJSONObject(i);
            toString(data);
        }
    }

    //输出文件信息:单个
    public static void toString(JSONObject data) {
        String text = data.optString("filename") + "(文件)\t" +
                data.optInt("duration") + "(时长)\t" + data.optDouble("size") + "(大小)\t" +
                data.optLong("lastModified") + "(修改时间)\t" + data.optString("path") + "(路径)";
        Log.e("res:录音", text);
    }

    //寻找文件
    public String getAudioFileList(String phoneNumber) {
        //红米k30
        if (Build.MODEL.equals("Redmi K30")) {
            Log.e("res:brand", "红米k30");
        }
        JSONArray audioList = new JSONArray();
        try {
            long time_begin = System.currentTimeMillis();
            File dir;
            //系统固定文件夹下搜索
            Log.e("res:dir", "系统固定文件夹下搜索 " + Build.MODEL);
            String recordDir = getSystemRecord();
            if (!TextUtils.isEmpty(recordDir)) {
                Log.e("res:dir:", recordDir);
                dir = new File(recordDir);
                JSONArray tmp = getAudioFiles(phoneNumber, dir);
                int tmpSize = (tmp != null) ? tmp.length() : 0;
                Log.e("res:录音文件数量", String.valueOf(tmpSize));
                if (tmpSize > 0) audioList = tmp;
            }
            //不同机型不同路径
            if (Build.MODEL.equals("KOZ-AL40") || Build.MODEL.equals("Redmi K30")) {
                //使用常用系统下文件夹下搜索(公司荣耀手机)
                Log.e("res:dir", "使用常用系统下文件夹下搜索(公司荣耀手机)");
                ArrayList<String> recordFiles = getRecordFiles();
                for (int i = 0; i < recordFiles.size(); i++) {
                    dir = new File(recordFiles.get(i));
                    Log.e("res:常用dir", dir.getPath());
                    JSONArray tmp = getAudioFiles(phoneNumber, dir);
                    int tmpSize = (tmp != null) ? tmp.length() : 0;
                    Log.e("res:录音文件数量", String.valueOf(tmpSize));
                    if (tmpSize > 0) audioList = tmp;
                }
            }
            long time_finish = System.currentTimeMillis();

            Log.e("res:全局搜索录音文件夹所花时间", Double.parseDouble(String.valueOf(time_finish - time_begin)) / 1000 + "s 当前时间" + date("Y-m-d H:i:s"));
        } catch (Exception e) {
            Log.e("Exception", String.valueOf(e));
        }
        String jsonStr = audioList.toString();
        return jsonStr;
    }

    //查找录音文件
    private JSONArray getAudioFiles(String phoneNumber, File dir) throws JSONException {
        JSONArray list = new JSONArray();
        //是否目录
        if (!dir.isDirectory() || !isNotRecordAppDir(dir)) return null;
        //读取文件
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return null;
        //处理文件
        for (int i = 0; i < files.length; i++) {
            JSONObject row = new JSONObject();
            File file = files[i];
            //是否文件
            if (!file.isFile()) continue;
            Log.e("res:file", file.getPath());
            //文件名称
            String filename = file.getName();
            String tempPhoneNumber = phoneNumber.replaceAll(" ", "");
            if (tempPhoneNumber.startsWith("+") && tempPhoneNumber.length() > 11 && tempPhoneNumber.length() < 18) {
                tempPhoneNumber = tempPhoneNumber.substring(tempPhoneNumber.length() - 11);
            }
            if (!filename.toLowerCase().replaceAll(" ", "").contains(tempPhoneNumber)) continue;
            if (!matchFileNameIsRecord(filename)) continue;
            //文件大小
            Double size = new Double(String.format("%.2f", (double) file.length() / 1000));
//            //最后修改时间
            long lastModified = file.lastModified();
//            //时效控制：读取最近3天内的文件(3*86400*1000)
            //if( System.currentTimeMillis()-lastModified > 3*86400*1000 ) continue;
            //if (System.currentTimeMillis() - lastModified > time) continue;
            //路径
            String path = file.getPath();
            //判断：文件大小、文件后缀
            if (!matchFileNameIsRecord(filename)) continue;
            long duration = getRecDuration(file.getPath());
            //放到列表
            row.put("id", 0);
            row.put("filename", filename);
            row.put("duration", duration);
            row.put("size", size);
            row.put("lastModified", lastModified);
            row.put("path", path);
            list.put(row);
        }
        toStrings(list); //日志
        return list;
    }


    //获取时间
    public static String date(String style) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));  //时区
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8")); //时区
        String y = String.format("%04d", calendar.get(Calendar.YEAR));
        String m = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        String d = String.format("%02d", calendar.get(Calendar.DATE));
        String h = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
        String i = String.format("%02d", calendar.get(Calendar.MINUTE));
        String s = String.format("%02d", calendar.get(Calendar.SECOND));
        String result = "";
        if (style == "") style = "Y-m-d H:i:s";
        switch (style) {
            case "Y-m-d":
                result = y + "-" + m + "-" + d;
                break;
            case "Y-m-d H:i":
                result = y + "-" + m + "-" + d + " " + h + ":" + i;
                break;
            case "Y-m-d H:i:s":
                result = y + "-" + m + "-" + d + " " + h + ":" + i + ":" + s;
                break;
            case "Ymd":
                result = y + m + d;
                break;
            case "YmdHis":
                result = y + m + d + h + i + s;
                break;
        }
        return result;
    }

}
