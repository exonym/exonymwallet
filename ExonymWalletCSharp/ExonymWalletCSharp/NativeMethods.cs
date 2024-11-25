using System;
using System.Runtime.InteropServices;

namespace ExonymWalletBridge
{
    internal static class NativeMethods
    {
        #if UNITY_EDITOR_WIN || UNITY_STANDALONE_WIN
            private const string DllName = "exonymwallet.dll";
        #elif UNITY_EDITOR_OSX || UNITY_STANDALONE_OSX
            private const string DllName = "libexonymwallet.dylib";
        #elif UNITY_EDITOR_LINUX || UNITY_STANDALONE_LINUX
            private const string DllName = "libexonymwallet.so";
        #elif UNITY_ANDROID // Unity strips 'lib' prefix and '.so' extension on Android
            private const string DllName = "exonymwallet"; 
        #elif UNITY_IOS // For iOS static libraries
            private const string DllName = "__Internal"; 
        #else
            private const string DllName = "libexonymwallet.dylib";
        #endif

            // P/Invoke declarations
        [DllImport(DllName, EntryPoint = "graal_create_isolate", CallingConvention = CallingConvention.Cdecl)]
        public static extern int graal_create_isolate(
            ref graal_create_isolate_params_t isolateParams,
            out IntPtr isolate,
            out IntPtr thread);

        [DllImport(DllName, EntryPoint = "graal_detach_thread", CallingConvention = CallingConvention.Cdecl)]
        public static extern int graal_detach_thread(IntPtr thread);

        [DllImport(DllName, EntryPoint = "graal_tear_down_isolate", CallingConvention = CallingConvention.Cdecl)]
        public static extern int graal_tear_down_isolate(IntPtr isolateThread);

        [DllImport(DllName, EntryPoint = "hello_exonym", CallingConvention = CallingConvention.Cdecl)]
        public static extern int hello_exonym(IntPtr thread);

        [DllImport(DllName, EntryPoint = "free_cstring", CallingConvention = CallingConvention.Cdecl)]
        public static extern void free_cstring(IntPtr thread, IntPtr cString);
            
        [DllImport(DllName, EntryPoint = "c30_join_to_auth_protocol", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr c30_join_to_auth_protocol(IntPtr thread, string alpha, string beta, string gamma, string path, int test);

        [DllImport(DllName, EntryPoint = "has_player_key_for_game", CallingConvention = CallingConvention.Cdecl)]
        public static extern int has_player_key_for_game(IntPtr thread, string alpha, string beta, string path);

        [DllImport(DllName, EntryPoint = "generate_player_key_for_game", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr generate_player_key_for_game(IntPtr thread, string alpha, string beta, string path);

        [DllImport(DllName, EntryPoint = "open_system_params", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr open_system_params(IntPtr thread);

        [DllImport(DllName, EntryPoint = "new_rulebook", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr new_rulebook(IntPtr thread, string name, string path);

        [DllImport(DllName, EntryPoint = "add_new_rule", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr add_new_rule(IntPtr thread, string rule, string path);

        [DllImport(DllName, EntryPoint = "extend_rulebook", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr extend_rulebook(IntPtr thread, string inputFileLocation, string outputFileLocation);

        [DllImport(DllName, EntryPoint = "wallet_report", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr wallet_report(IntPtr thread, string username, string passwordAsSha256Hex, string path);

        [DllImport(DllName, EntryPoint = "authentication_report", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr authentication_report(IntPtr thread, string username, string passwordAsSha256Hex, string request, string path);

        [DllImport(DllName, EntryPoint = "lead_list_test", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr lead_list_test(IntPtr thread, string url);

        [DllImport(DllName, EntryPoint = "spawn_network_map", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr spawn_network_map(IntPtr thread, string path);

        [DllImport(DllName, EntryPoint = "view_actor", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr view_actor(IntPtr thread, string uid, string path);

        [DllImport(DllName, EntryPoint = "list_actors", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr list_actors(IntPtr thread, string uid, string path);

        [DllImport(DllName, EntryPoint = "list_rulebooks", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr list_rulebooks(IntPtr thread, string path);

        [DllImport(DllName, EntryPoint = "proof_for_rulebook_sso", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr proof_for_rulebook_sso(IntPtr thread, string username, string passwordAsSha256Hex, string uLinkChallenge, string path);

        [DllImport(DllName, EntryPoint = "proof_for_rulebook_sso_anon", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr proof_for_rulebook_sso_anon(IntPtr thread, string username, string passwordAsSha256Hex, string uLinkChallenge, string path);

        [DllImport(DllName, EntryPoint = "generate_delegation_request_for_third_party", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr generate_delegation_request_for_third_party(IntPtr thread, string username, string passwordAsSha256Hex, string uLinkChallenge, string name, string path);

        [DllImport(DllName, EntryPoint = "fill_delegation_request", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr fill_delegation_request(IntPtr thread, string username, string passwordAsSha256Hex, string uLinkChallenge, string path);

        [DllImport(DllName, EntryPoint = "verify_delegation_request", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr verify_delegation_request(IntPtr thread, string username, string passwordAsSha256Hex, string requestLink, string proofLink, string path);

        [DllImport(DllName, EntryPoint = "non_interactive_proof", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr non_interactive_proof(IntPtr thread, string username, string passwordAsSha256Hex, string nonInteractiveProofRequest, string path);

        [DllImport(DllName, EntryPoint = "sftp_put", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr sftp_put(IntPtr thread, string username, string passwordAsSha256Hex, string sftpCredentialUid, string fileName, string token, string remotePath, string path);

        [DllImport(DllName, EntryPoint = "sftp_template", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr sftp_template(IntPtr thread, string path);

        [DllImport(DllName, EntryPoint = "sftp_add", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr sftp_add(IntPtr thread, string username, string passwordAsSha256Hex, string path);

        [DllImport(DllName, EntryPoint = "sftp_remove", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr sftp_remove(IntPtr thread, string username, string passwordAsSha256Hex, string name, string path);

        [DllImport(DllName, EntryPoint = "onboard_sybil_testnet", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr onboard_sybil_testnet(IntPtr thread, string username, string sybilClass, string passwordAsSha256Hex, string path);

        [DllImport(DllName, EntryPoint = "onboard_rulebook", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr onboard_rulebook(IntPtr thread, string username, string issuancePolicy, string passwordAsSha256Hex, string path);

        [DllImport(DllName, EntryPoint = "onboard_rulebook_moderator_uid", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr onboard_rulebook_moderator_uid(IntPtr thread, string username, string passwordAsSha256Hex, string modUid, string path);

        [DllImport(DllName, EntryPoint = "authenticate", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr authenticate(IntPtr thread, string username, string passwordAsSha256Hex, string path);

        [DllImport(DllName, EntryPoint = "setup_wallet_path", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr setup_wallet_path(IntPtr thread, string username, string plainTextPassword, string path);

        [DllImport(DllName, EntryPoint = "delete_wallet", CallingConvention = CallingConvention.Cdecl)]
        public static extern int delete_wallet(IntPtr thread, string username, string plainTextPassword, string path);

        [DllImport(DllName, EntryPoint = "generate_reset_proof", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr generate_reset_proof(IntPtr thread, string username, string plainTextPassword, string path);

        [DllImport(DllName, EntryPoint = "sha_256_as_hex", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr sha_256_as_hex(IntPtr thread, string toHash);        

    }

    [StructLayout(LayoutKind.Sequential)]
    public struct graal_create_isolate_params_t
    {
        public int version;
        public UIntPtr reserved_address_space_size;
        // Include other fields if necessary
    }
}