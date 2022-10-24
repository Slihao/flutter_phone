import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_phone/flutter_phone.dart';
import 'package:flutter_phone_example/audio_page.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';

class RecordsPage extends StatefulWidget {
  const RecordsPage({Key? key}) : super(key: key);

  @override
  _RecordsPageState createState() => _RecordsPageState();
}

class _RecordsPageState extends State<RecordsPage> {
  List list = [];


  @override
  void initState() {
    super.initState();
    getRecords();
  }

  getRecords() async {
    String? jsonStr = await FlutterPhone.getAllCallRecords();
    if (jsonStr != null) {
      var data = jsonDecode(jsonStr);
      list.clear();
      list.addAll(data['list'] ?? []);
      if (mounted) {
        setState(() {});
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("通话记录"),
      ),
      body: ListView.separated(
          itemCount: list.length,
          separatorBuilder: (c, index) {
            return Divider();
          },
          itemBuilder: (c, index) {
            /// 通话记录
            //                 row.put("id",id);
            //                 row.put("type",type);
            //                 row.put("typeText",typeText);
            //                 row.put("name",name);
            //                 row.put("number",number);
            //                 row.put("cacheNumber",cacheNumber);
            //                 row.put("duration",duration);
            //                 row.put("dateTimeMillis",dateTimeMillis);
            //                 row.put("dateString",dateString);
            //                 row.put("phoneAccountId",phoneAccountId);
            return InkWell(
              onTap: (){
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => AudioPage(phoneNumber: list[index]['phoneNumber']),
                  ),
                );
              },
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    children: [
                      Text(list[index]['name']),
                      Text(list[index]['phoneNumber']),
                    ],
                  ),
                  Text(list[index]['dateString']),
                ],
              ),
            );
          }),
    );
  }
}
