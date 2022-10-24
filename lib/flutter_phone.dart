import 'dart:async';
import 'dart:collection';

import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

///限定参数和返回值
typedef PhoneListener = void Function(dynamic result);

///自定义关于拨号功能相关的插件
class FlutterPhone {
  static const MethodChannel _channel = MethodChannel('flutter_phone');
  static const EventChannel _channelEvent = EventChannel('flutter_phone_event');
  static StreamSubscription? _streamSubscription;

  ///获取手机卡信息
  static Future<String?> simInfo() {
    return _channel.invokeMethod<String>("simInfo");
  }

  /// phoneNumber:拨打电话话吗
  /// isDirect  true直接拨号  false跳转拨号界面
  /// index ( 0用卡1拨打 )  | ( 1用卡2拨打)
  static call(String phoneNumber, {bool isDirect = true, int slotIndex = 0}) {
    Map<String, Object> map = HashMap();
    map['phoneNumber'] = phoneNumber;
    map['isDirect'] = isDirect;
    map['slotIndex'] = slotIndex;
    _channel.invokeMethod("call", map);
  }

  ///获取通话记录
  static Future<String?> getAllCallRecords() {
    Map<String, Object> map = HashMap();
    return _channel.invokeMethod<String>("getAllCallRecords", map);
  }
  ///分页获取联系人列表
  static Future<String?> getContacts(int page ,{int pageSize = 20}) {
    Map<String, Object> map = HashMap();
    map['page'] = page;
    map['pageSize'] = pageSize;
    return _channel.invokeMethod<String>("getContacts", map);
  }
  ///获取单个号码通话记录及录音
  ///phoneNumber 要查询的号码的通话记录
  ///time 当前时间到time时间段内  不传或者0为所有
  static Future<String?>  getCallRecordsAudioByPhoneNumber(String phoneNumber, {num time = 0}) {
    Map<String, Object> map = HashMap();
    map['phoneNumber'] = phoneNumber;
    map['time'] = time;
   return _channel.invokeMethod<String>("getCallRecordsAudioByPhoneNumber", map);
  }

  ///监听 电话来电和挂断  全局只有一个
  static addPhoneListener(PhoneListener listener) {
    _streamSubscription ??= _channelEvent.receiveBroadcastStream().listen(
      (dynamic event) {
        debugPrint("电话监听：");
        print(event);
        listener(event);
      },
      onError: (dynamic error) {
        debugPrint('Received error: ${error.message}');
      },
      cancelOnError: true,
    );
  }
}
