void success(String text) {
  print('\u{1F7E2} \x1B[32m$text\x1B[0m');
}

void warn(String text) {
  print('\x1B[33m$text\x1B[0m');

}

void error(String text){
  print('\x1B[31m$text\x1B[0m');
}
