import 'dart:io';
import 'package:path/path.dart' as path;

Future<void> deleteContainer(String containerName) async {
  var dirPath = path.join(Directory.current.path, "identities", containerName);
  Directory d = Directory(dirPath);
  if (await d.exists()){
    d.delete(recursive: true);
  }
}