using System;
using System.IO;
using System.Runtime.InteropServices;

namespace ExonymWalletBridge
{
    internal static class NativeLibraryLoader
    {
        #if !UNITY_ANDROID
        private static bool _loaded = false;

        public static void LoadNativeLibrary()
        {
            if (_loaded)
                return;

            string libraryPath = GetNativeLibraryPath();

            if (string.IsNullOrEmpty(libraryPath) || !File.Exists(libraryPath))
            {
                throw new DllNotFoundException($"Native library not found at path: {libraryPath}");
            }

            IntPtr handle = LoadLibrary(libraryPath);

            if (handle == IntPtr.Zero)
            {
                throw new DllNotFoundException($"Failed to load native library from path: {libraryPath}");
            }

            _loaded = true;
        }

        private static string GetBaseDirectory()
        {
            return AppDomain.CurrentDomain.BaseDirectory;
        }
        
        private static string GetNativeLibraryPath()
        {
            string basePath = GetBaseDirectory();
            string nativeFolderPath = Path.Combine(basePath, "native");
            string libraryPath = Path.Combine(nativeFolderPath, "libexonymwallet.dylib");
            return libraryPath;
        }
        
        private static IntPtr LoadLibrary(string path)
        {
            Console.WriteLine($"Attempting to load library: {path}");
            IntPtr handle = dlopen(path, RTLD_NOW);

            if (handle == IntPtr.Zero)
            {
                string error = Marshal.PtrToStringAnsi(dlerror());
                Console.WriteLine($"dlopen failed: {error}");
            }
            else
            {
                Console.WriteLine($"Library loaded successfully: {path}");
            }

            return handle;
        }

        #endif

        
        private const int RTLD_NOW = 2;
        
        [DllImport("libdl")]
        private static extern IntPtr dlerror();

        [DllImport("libdl")]
        private static extern IntPtr dlopen(string fileName, int flags);
    }
}