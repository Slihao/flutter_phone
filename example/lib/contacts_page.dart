import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_phone/flutter_phone.dart';
import 'package:pull_to_refresh/pull_to_refresh.dart';

class ContactsPage extends StatefulWidget {
  const ContactsPage({Key? key}) : super(key: key);

  @override
  _ContactsPageState createState() => _ContactsPageState();
}

class _ContactsPageState extends State<ContactsPage> {
  RefreshController controller = RefreshController();
  List list = [];
  int mPageIndex = 1;

  @override
  void initState() {
    super.initState();
    getContacts(mPageIndex);
  }

  getContacts(int p) async {
    String? jsonStr = await FlutterPhone.getContacts(p, pageSize: 20);
    if (jsonStr != null) {
      var data = jsonDecode(jsonStr);
      if (p == 1) {
        list.clear();
        list.addAll(data['list'] ?? []);
        controller.refreshCompleted();
      } else {
        List _list = data['list'] ?? [];
        list.addAll(data['list'] ?? []);
        if (_list.isNotEmpty) {
          mPageIndex = p;
        }
        controller.loadComplete();
        if (list.length == data['total']) {
          controller.loadNoData();
        }
      }
      if (mounted) {
        setState(() {});
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("联系人"),
      ),
      body: SmartRefresher(
        controller: controller,
        enablePullDown: true,
        enablePullUp: true,
        onRefresh: () {
          getContacts(1);
        },
        onLoading: () {
          getContacts(mPageIndex + 1);
        },
        child: ListView.separated(
            itemCount: list.length,
            separatorBuilder: (c, index) {
              return Divider();
            },
            itemBuilder: (c, index) {
              // object.put("name", cursor.getString(0));
              //                 object.put("phoneNumber", cursor.getString(1));
              //                 object.put("id", cursor.getLong(2));
              //                 object.put("headUrl", ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, obj.optLong("id")).toString());
              return Row(
                children: [
                  Column(
                    children: [
                      Text(list[index]['name']),
                      Text(list[index]['phoneNumber']),
                    ],
                  ),

                ],
              );
            }),
      ),
    );
  }
}
