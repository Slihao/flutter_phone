package com.slh.flutter_phone;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.slh.flutter_phone.utils.PhoneUtils;

import org.json.JSONException;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * FlutterPhonePlugin
 */
public class FlutterPhonePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, ActivityCompat.OnRequestPermissionsResultCallback {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private EventChannel _channelEvent;
    public static EventChannel.EventSink EVENT_SINK;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PhoneUtils.CALL_PHONE_CODE) {
                PhoneUtils.simInfo();
            }
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PhoneUtils.READ_CONTACTS_CODE) {
                try {
                    PhoneUtils.getContacts(20, 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_phone");
        channel.setMethodCallHandler(this);
        _channelEvent = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_phone_event");
        _channelEvent.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                EVENT_SINK = events;
            }

            @Override
            public void onCancel(Object arguments) {

            }
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        Map<String, Object> map = call.arguments();
        if (call.method.equals("call")) {
            //拨打电话
            String phoneNumber = map.get("phoneNumber").toString();
            boolean isDirect = Boolean.parseBoolean(map.get("isDirect").toString());
            int slotIndex = Integer.parseInt(map.get("slotIndex").toString());
            PhoneUtils.call(phoneNumber, isDirect, slotIndex);
        } else if (call.method.equals("simInfo")) {
            //获取单个号码的通话记录
            result.success(PhoneUtils.simInfo());
        } else if (call.method.equals("getCallRecordsAudioByPhoneNumber")) {
            //获取单个号码的通话记录
            String phoneNumber = map.get("phoneNumber").toString();
            result.success(PhoneUtils.getCallRecordsAudioByPhoneNumber(phoneNumber));
        } else if (call.method.equals("getAllCallRecords")) {
            //获取通话记录列表
            try {
                result.success(PhoneUtils.getAllCallRecords());
            } catch (JSONException e) {
                e.printStackTrace();
                result.success("{\"page\":0,\"total\":0,\"list\":[]}");
            }
        } else if (call.method.equals("getContacts")) {
            //获取单个号码的通话记录
            int page = Integer.parseInt(map.get("page").toString());
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            try {
                result.success(PhoneUtils.getContacts(pageSize, page));
            } catch (JSONException e) {
                e.printStackTrace();
                result.success("{\"page\":0,\"total\":0,\"list\":[]}");
            }
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

        channel.setMethodCallHandler(null);
        _channelEvent.setStreamHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        PhoneUtils.init(binding.getActivity());
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        PhoneUtils.destroy();
    }


}
