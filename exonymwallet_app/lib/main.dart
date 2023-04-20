import 'package:flutter/cupertino.dart';
import 'package:provider/provider.dart';

import 'app.dart';
import 'model/app_state_model.dart';

const routeHome = "/";
const routeSetup = "/settings";
const routeSettings = "/settings";
const routeIdentity = "/identity";

void main() {
  return runApp(ChangeNotifierProvider<AppStateModel>(
    create: (_) => AppStateModel()..allIds(),
    child: const ExonymApp(),) );
}