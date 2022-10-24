import 'dart:convert';
import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_phone/flutter_phone.dart';
import 'package:flutter_phone_example/contacts_page.dart';
import 'package:flutter_phone_example/records_page.dart';
class App extends StatefulWidget {
  const App({Key? key}) : super(key: key);

  @override
  State<App> createState() => _AppState();
}

class _AppState extends State<App> {
  List listRecord = [];

  @override
  void initState() {
    super.initState();
    /**
     * 全局唯一
     */
    FlutterPhone.addPhoneListener((result) {
      print("监听信息");
      print(result);
    });


  }

  String? simJsonStr;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('电话'),
        actions: [
          TextButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ContactsPage(),
                  ),
                );
              },
              child: Text(
                "联系人",
                style: TextStyle(color: Colors.white),
              )),
          TextButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => RecordsPage(),
                  ),
                );
              },
              child: Text(
                "通话记录",
                style: TextStyle(color: Colors.white),
              )),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            Center(
              child: TextButton(
                child: Text('获取SIM卡信息'),
                onPressed: () async {
                  simJsonStr = await FlutterPhone.simInfo();
                  setState(() {});
                },
              ),
            ),
            Center(
              child: TextButton(
                child: Text(simJsonStr ?? "暂无SIM卡信息"),
                onPressed: null,
              ),
            ),

            Center(
              child: TextButton(
                child: Text('拨打电话1'),
                onPressed: () {
                  if(simJsonStr!=null){
                    var simJson = jsonDecode(simJsonStr!);
                    FlutterPhone.call("15136139162",
                        isDirect: true,
                        slotIndex: int.parse(simJson['simList'][0]['slotIndex'].toString()));
                  }else{
                    debugPrint("请先获取SIM卡信息");
                  }

                },
              ),
            ),
            Center(
              child: TextButton(
                child: Text('拨打电话2'),
                onPressed: () {
                  if(simJsonStr!=null){
                    var simJson = jsonDecode(simJsonStr!);
                    FlutterPhone.call("15136139162",
                        isDirect: true,
                        slotIndex: int.parse(
                            simJson['simList'][1]['slotIndex'].toString()));
                  }else{
                    debugPrint("请先获取SIM卡信息");
                  }

                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
