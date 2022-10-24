package com.slh.flutter_phone.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.slh.flutter_phone.FlutterPhonePlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhoneUtils {
    private static final String TAG = "PhoneUtils";
    //权限申请的请求码
    //拨打电话
    public static final int CALL_PHONE_CODE = 0;
    //读取电话状态
    public static final int READ_PHONE_STATE_CODE = 1;
    //联系人列表
    public static final int READ_CONTACTS_CODE = 2;
    //读取电话状态 和读写权限
    public static final int WRITE_READ_AND_PHONE_STATE_CODE = 3;
    private static Context context;
    private static Activity activity;
    private static TelephonyManager mTelephonyManager;
    public static PhoneStateListener mListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //state 当前状态 incomingNumber,貌似没有去电的API
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.e(TAG, "挂断" + incomingNumber);
                    Map<String, Object> resultIdle = new HashMap<>();
                    resultIdle.put("type", "STATE_IDLE");
                    resultIdle.put("name", "挂断");
                    resultIdle.put("phone", incomingNumber);
                    FlutterPhonePlugin.EVENT_SINK.success(resultIdle);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.e(TAG, "接听 OFFHOOK" + incomingNumber);
                    Map<String, Object> resultOffhook = new HashMap<>();
                    resultOffhook.put("type", "STATE_OFFHOOK");
                    resultOffhook.put("name", "接听");
                    resultOffhook.put("phone", incomingNumber);
                    FlutterPhonePlugin.EVENT_SINK.success(resultOffhook);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.e(TAG, "响铃:RINGING" + incomingNumber);
                    Map<String, Object> resultRinging = new HashMap<>();
                    resultRinging.put("type", "STATE_RINGING");
                    resultRinging.put("name", "响铃");
                    resultRinging.put("phone", incomingNumber);
                    FlutterPhonePlugin.EVENT_SINK.success(resultRinging);
                    //输出来电号码
                    break;
            }
        }

    };

    public static void init(Activity activity) {
        PhoneUtils.context = activity.getApplicationContext();
        PhoneUtils.activity = activity;
        PhoneUtils.mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public static void destroy() {
        PhoneUtils.mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        PhoneUtils.context = null;
        PhoneUtils.activity = null;

    }

    //检查权限 Manifest.permission.READ_CONTACTS
    public static Boolean requestContactsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; //低版本无需申请权限
        Log.e(TAG, "Build.VERSION.SDK_INT>=23(Build.VERSION_CODES.M)Android6.0以上版本" + Build.VERSION.SDK_INT); //高版本
        //有权限：返回
        if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Log.e(TAG, "没有权限:" + Manifest.permission.READ_CONTACTS);
            activity.requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_CODE);
            return false;
        }
    }

    //检查权限 Manifest.permission.CALL_PHONE
    public static Boolean requestCallPhonePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; //低版本无需申请权限
        Log.e(TAG, "Build.VERSION.SDK_INT>=23(Build.VERSION_CODES.M)Android6.0以上版本" + Build.VERSION.SDK_INT); //高版本
        //有权限：返回
        if (activity.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Log.e(TAG, "没有权限:" + Manifest.permission.CALL_PHONE);
            activity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_CODE);
            return false;
        }
    }

    //检查权限 Manifest.permission.READ_PHONE_STATE
    public static Boolean requestReadPhoneStatePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; //低版本无需申请权限
        Log.e(TAG, "Build.VERSION.SDK_INT>=23(Build.VERSION_CODES.M)Android6.0以上版本" + Build.VERSION.SDK_INT); //高版本
        //有权限：返回
        if (activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Log.e(TAG, "没有权限:" + Manifest.permission.READ_PHONE_STATE);
            activity.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_CODE);
            return false;
        }
    }

    //检查权限 Manifest.permission.READ_PHONE_STATE   Manifest.permission.READ_EXTERNAL_STORAGE  Manifest.permission.WRITE_EXTERNAL_STORAGE
    public static Boolean requestReadPhoneStateAndWritePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; //低版本无需申请权限
        Log.e(TAG, "Build.VERSION.SDK_INT>=23(Build.VERSION_CODES.M)Android6.0以上版本" + Build.VERSION.SDK_INT); //高版本
        //有权限：返回
        if (activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "没有权限:" + Manifest.permission.READ_CALL_LOG);
            }
            if (activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "没有权限:" + Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "没有权限:" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            activity.requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_READ_AND_PHONE_STATE_CODE);
            return false;
        }
    }


    //获取手机卡信息 （包括卡槽数量  卡数数量  卡信息
    public static String simInfo() {
        if (!requestReadPhoneStatePermission()) {
            return null;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        int count = subscriptionManager.getActiveSubscriptionInfoCountMax();//手机SIM卡数
        //需要 Manifest.permission.READ_PHONE_STATE权限
        int activeCount = subscriptionManager.getActiveSubscriptionInfoCount();
        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        Log.e(TAG, "手机卡槽数量：count = " + count + ",activeCount = " + activeCount + ",subscriptionInfoList = " + subscriptionInfoList.size());
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            obj.put("simCount", count);
            obj.put("activeCount", activeCount);
            for (int i = 0; i < subscriptionInfoList.size(); i++) {
                SubscriptionInfo subscriptionInfo = subscriptionInfoList.get(i);
                JSONObject object = new JSONObject();
                object.put("countryIso", subscriptionInfo.getCountryIso());//cn
                object.put("carrierName", subscriptionInfo.getCarrierName());//中国移动
                object.put("slotIndex", subscriptionInfo.getSimSlotIndex());//0
                object.put("subscriptionId", subscriptionInfo.getSubscriptionId());//2
                array.put(object);
            }
            obj.put("simList", array);
            String jsonStr = obj.toString();
            Log.e(TAG, jsonStr);
            return jsonStr;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * @param phoneNumber 拨打的号码
     * @param slotIndex   获取卡信息里的数据
     */
    public static void call(@Nullable String phoneNumber, boolean isDirect, int slotIndex) {
        if (phoneNumber == null || phoneNumber.length() == 0) {
            Toast.makeText(context, "请输入电话号码", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.e(TAG, "拨打电话参数：phoneNumber = " + phoneNumber + ",isDirect = " + isDirect + ",slotIndex = " + slotIndex);
        if (isDirect) {
            //直接拨号需要拨号权限
            if (!requestCallPhonePermission()) {
                return;
            }
        }
        //call动作为直接拨打电话(需要加CALL权限) //dial动作为调用拨号盘
        Intent intent = new Intent(isDirect ? Intent.ACTION_CALL : Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("subscription", slotIndex);
        intent.putExtra("Subscription", slotIndex);
        intent.putExtra("com.android.phone.extra.slot", slotIndex);
        intent.putExtra("phone", slotIndex);
        intent.putExtra("com.android.phone.DialingMode", slotIndex);
        intent.putExtra("simId", slotIndex);
        intent.putExtra("phone_type", slotIndex);
        intent.putExtra("simSlot", slotIndex);
        intent.putExtra("sim_slot", slotIndex);
        intent.putExtra("slot", slotIndex);
        intent.putExtra("slot_id", slotIndex);
        intent.putExtra("slotIdx", slotIndex);
        intent.putExtra("extra_asus_dial_use_dualsim", slotIndex);
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            SubscriptionInfo info = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slotIndex);
            List<PhoneAccountHandle> handleList = telecomManager.getCallCapablePhoneAccounts();
            if (info != null) {
                for (int i = 0; i < handleList.size(); i++) {
                    PhoneAccountHandle handle = handleList.get(i);
                    Log.e(TAG, "ICCID = " + info.getIccId() + ",hanndleId = " + handle.getId() + ",subscriptionId = " + info.getSubscriptionId());
                    if (TextUtils.equals(handle.getId(), info.getSubscriptionId() + "") || TextUtils.equals(handle.getId(), info.getIccId())) {
                        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle);
                        break;
                    }
                }
            }

        }
//        else {
//            try {
//                final Method getSubIdMethod = SubscriptionManager.class.getDeclaredMethod("getSubId", int.class);
//                getSubIdMethod.setAccessible(true);
//                final long subIdForSlot = ((long[]) getSubIdMethod.invoke(SubscriptionManager.class, slotIndex))[0];
//                final ComponentName componentName = new ComponentName("com.android.phone", "com.android.services.telephony.TelephonyConnectionService");
//                Class classZ = Class.forName("android.telecom.PhoneAccountHandle");
//                Constructor con2 = classZ.getConstructor(String.class,String.class);
//                final  classZ phoneAccountHandle = con2.newInstance(componentName, String.valueOf(subIdForSlot));
//                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

        context.startActivity(intent);
    }


    /**
     * 获取联系人总数
     *
     * @return
     */
    private static int getCallRecordsSize() {
        int num = 0;
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (null != cursor) {
            num = cursor.getCount();
            cursor.close();
        }
        return num;
    }

    /**
     * 获取通话记录
     *
     * @return
     * @throws JSONException
     */
    public static String getAllCallRecords() throws JSONException {
        if (!requestReadPhoneStateAndWritePermission()) {
            return null;
        }
        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        int count = getCallRecordsSize();
        obj.put("total", count);
        //CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        String[] projection = {
                CallLog.Calls._ID, CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.DURATION,
                CallLog.Calls.DATE, CallLog.Calls.PHONE_ACCOUNT_ID
        };
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Uri uri = CallLog.Calls.CONTENT_URI.buildUpon()
//                    //.appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, String.valueOf(tempPageSize))//有效
//                    //.appendQueryParameter(CallLog.Calls.OFFSET_PARAM_KEY, String.valueOf(currentOffset))//无效
//                    .build();
//            Log.e(TAG, "查询：" + uri.toString());
//
//            Bundle args = new Bundle();
//            args.putInt(ContentResolver.QUERY_ARG_OFFSET, currentOffset);//无效
//            args.putInt(ContentResolver.QUERY_ARG_LIMIT, tempPageSize);//无效
//            args.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, CallLog.Calls.DEFAULT_SORT_ORDER);
//            cursor = context.getContentResolver().query(uri, projection, args, null);
//        } else {
//            // + " DESC limit " + tempPageSize + " offset " + currentOffset
//            cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, null, null,
//                    CallLog.Calls.DEFAULT_SORT_ORDER);
//        }
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                int type = cursor.getInt(1);
                String name = cursor.getString(2);
                String number = cursor.getString(3);
                String cacheNumber = cursor.getString(4);
                long duration = cursor.getLong(5);
                long dateTimeMillis = cursor.getLong(6);
                int phoneAccountId = cursor.getInt(7) + 1;
                //时效控制：读取最近3天内的文件(3*86400*1000)
                //if( System.currentTimeMillis()-dateTimeMillis > 3*86400*1000 ) continue;
//                if (System.currentTimeMillis() - dateTimeMillis > time) continue;
                //时间戳转换
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = format.format(dateTimeMillis);// dateTimeMillis = 1365514019004;  通话时间(什么时候通话的)
                if (cacheNumber == null) cacheNumber = number;
                if (name == null) name = "";
                //类型
                String typeText = "";
                if (type == CallLog.Calls.OUTGOING_TYPE) typeText = "呼出";
                if (type == CallLog.Calls.INCOMING_TYPE) typeText = "呼入";
                if (type == CallLog.Calls.MISSED_TYPE) typeText = "未接通";

                //JSON对象
                JSONObject row = new JSONObject();
                row.put("id", id);
                row.put("type", type);
                row.put("typeText", typeText);
                row.put("name", name.length() == 0 ? number : name);
                row.put("phoneNumber", number);
                row.put("cacheNumber", cacheNumber);
                row.put("duration", duration);
                row.put("dateTimeMillis", dateTimeMillis);
                row.put("dateString", dateString);
                row.put("phoneAccountId", phoneAccountId);
                //日志
                //toString(row);
                //添加到列表
                jsonArray.put(row);
            }
            cursor.close();
        }
        obj.put("list", jsonArray);
        return obj.toString();
    }


    /**
     * 根据手机号获取通话录音
     *
     * @param phoneNumber
     * @return
     */
    public static String getCallRecordsAudioByPhoneNumber(String phoneNumber) {
        if (!requestReadPhoneStateAndWritePermission()) {
            return null;
        }
        AudioUtils audioUtils = new AudioUtils(activity);
        String strJson = audioUtils.getAudioFileList(phoneNumber);
        return strJson;
    }

    /**
     * 获取联系人总数
     *
     * @return
     */
    private static int getContactsSize() {
        int num = 0;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
//        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.DATA1,
//                ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (null != cursor) {
            num = cursor.getCount();
            cursor.close();
        }
        return num;
    }

    /**
     * 分页查询系统联系人信息
     *
     * @param size 每页最大的数目
     * @param page 页数
     * @return
     */
    public static String getContacts(int size, int page) throws JSONException {
        if (!requestContactsPermission()) {
            return null;
        }

        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        int count = getContactsSize();
        int totalPage = count / size + (count % size == 0 ? 0 : 1);
        obj.put("total", count);
        obj.put("totalPage", totalPage);
        if (page < 1) {
            page = 1;
        }
        int tempPage = page;
        int tempPageSize = size <= 0 ? 20 : size;
        int currentOffset = (tempPage - 1) * tempPageSize;
        Log.e(TAG, "当前查询；page = " + tempPage + ",pageSize = " + tempPageSize);
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null,
                ContactsContract.Contacts._ID + " ASC limit " + tempPageSize + " offset " + currentOffset);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                JSONObject object = new JSONObject();
                object.put("name", cursor.getString(0));
                object.put("phoneNumber", cursor.getString(1));
                object.put("id", cursor.getLong(2));
                object.put("headUrl", ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, obj.optLong("id")).toString());
                array.put(object);
            }
            cursor.close();
        }
        obj.put("list", array);
        String jsonStr = obj.toString();
        Log.e(TAG, jsonStr);
        return jsonStr;
    }


    /**
     * 根据手机号码查询联系人姓名
     *
     * @param phoneNumber
     */
    public static void getContactNameByPhoneNumber(String phoneNumber) {
        String displayName;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                new String[]{phoneNumber}, null);
        Log.i(TAG, "cursor displayName count:" + cursor.getCount());
        if (cursor != null) {
            while (cursor.moveToNext()) {
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if (!TextUtils.isEmpty(displayName)) {
                    Log.e(TAG, "获取的通讯录 姓名是 : " + displayName);
                    break;
                }
            }
        }

    }

    /**
     * 添加通话记录
     */
    public static void addCallLOg(String name, String phoneNumber) {
        // type == CallLog.Calls.OUTGOING_TYPE) typeText = "呼出";   2
        // type == CallLog.Calls.INCOMING_TYPE) typeText = "呼入";   1
        // type == CallLog.Calls.MISSED_TYPE) typeText = "未接通";   3
        ContentValues values = new ContentValues();
        values.clear();
        values.put(CallLog.Calls.CACHED_NAME, name);
        values.put(CallLog.Calls.NUMBER, phoneNumber);
        values.put(CallLog.Calls.TYPE, "1");
/*      values.put(CallLog.Calls.DATE, calllog.getmCallLogDate());
        values.put(CallLog.Calls.DURATION, calllog.getmCallLogDuration());*/
        values.put(CallLog.Calls.NEW, "0");// 0已看1未看 ,由于没有获取默认全为已读
        context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
    }


    /**
     * 删除某个记录
     */
    public static void deleteCallLogByPhoneNumber(String phoneNumber) {
        int result = context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, "number=?", new String[]{phoneNumber});
        if (result > 0) {
            Log.d(TAG, "deleted success:" + phoneNumber);
        } else {
            Log.d(TAG, "deleted fail:" + phoneNumber);
        }
    }


    //打印对象
    private static void toString(JSONObject row) {
        String id = row.optString("id");
        String name = row.optString("name");
        String cacheNumber = row.optString("cacheNumber");
        String type = row.optString("type");
        String typeText = row.optString("typeText");
        String dateString = row.optString("dateString");
        int phoneAccountId = row.optInt("phoneAccountId");
        //通话时长
        Integer duration = row.optInt("duration");
        //通话时间
        long dateTimeMillis = row.optLong("dateTimeMillis");
        //格式化通话时间
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String ymdhis = format.format(dateTimeMillis);
        //日志
        String text = id + "(id)\t" + type + "(" + typeText + ")\t" + name + "(姓名)\t" + cacheNumber + "(号码)\t" + duration + "s(通话时长)\t" + ymdhis + " " + dateString + " (时间" + dateTimeMillis + ")\t" + phoneAccountId + "(卡)";
        Log.e(TAG, text);
    }
}
