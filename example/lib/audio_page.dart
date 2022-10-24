import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_phone/flutter_phone.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';

class AudioPage extends StatefulWidget {
  final String phoneNumber;

  const AudioPage({Key? key, required this.phoneNumber}) : super(key: key);

  @override
  _AudioPageState createState() => _AudioPageState();
}

class _AudioPageState extends State<AudioPage> {
  List list = [];

  @override
  void initState() {
    super.initState();
    getRecords();
  }

  getRecords() async {
    String? jsonStr =
        await FlutterPhone.getCallRecordsAudioByPhoneNumber(widget.phoneNumber);
    if (jsonStr != null) {
      var data = jsonDecode(jsonStr);
      list.clear();
      list.addAll(data ?? []);
      if (mounted) {
        setState(() {});
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("通话录音"),
      ),
      body: ListView.separated(
          itemCount: list.length,
          separatorBuilder: (c, index) {
            return Divider();
          },
          itemBuilder: (c, index) {
            // row.put("id", 0);
            // row.put("filename", filename);
            // row.put("duration", duration);
            // row.put("size", size);
            // row.put("lastModified", lastModified);
            // row.put("path", path);
            return Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text("时间：${list[index]['lastModified']}"),
                      SizedBox(height: 10,),
                      Text(list[index]['path']),
                    ],
                  ),
                ),
                SizedBox(width: 10,),
                Text("时长:${list[index]['duration']}"),
              ],
            );
          }),
    );
  }
}
