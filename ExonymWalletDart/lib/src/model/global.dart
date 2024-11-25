import 'dart:ffi';
import 'package:ffi/ffi.dart';

const LIB_NAME = "libexonymwallet.dylib";
// const LIB_FOLDER = "../libexonymwallet/target/gluonfx/aarch64-darwin/";
const LIB_FOLDER = "lib/dylib/";
const LIB_FOLDER_ARM64 = "arm64";
const LIB_FOLDER_AMD64 = "amd64";
const LIB_FOLDER_MACOS_X86 = "macOSx86";
const LIB_FOLDER_MACOS_M1 = "macOSm1";
const INFORMAL_LIB_NAME = "(libexonymwallet)";

Pointer<Char> toCString(String string){
  return string.toNativeUtf8().cast<Char>();

}

String fromCString(Pointer<Char> cstring){
  return cstring.cast<Utf8>().toDartString();

}