using System;
using System.Runtime.InteropServices;
using ExonymWalletBridge.Schema;

namespace ExonymWalletBridge
{
    public class ExonymWallet : IDisposable
    {
        private IntPtr _isolate;
        private IntPtr _thread;
        private bool _disposed;

        public ExonymWallet()
        {
            NativeLibraryLoader.LoadNativeLibrary();

            var isolateParams = new graal_create_isolate_params_t
            {
                version = 1,
                reserved_address_space_size = UIntPtr.Zero
            };

            Console.WriteLine($"Params - version: {isolateParams.version}, reserved_address_space_size: {isolateParams.reserved_address_space_size}");

            int result = NativeMethods.graal_create_isolate(ref isolateParams, out _isolate, out _thread);

            Console.WriteLine($"Graal Create Isolate: {result}, isolate: {_isolate}, thread: {_thread}");

            if (result != 0)
            {
                throw new Exception($"Failed to create isolate. Error code: {result}");
            }

            int twelve = NativeMethods.hello_exonym(_thread);
            
            if (twelve == 12)
            {
                Console.WriteLine("Exonym - Coordination Tools for a New Commons");
            }
            else
            {
                Console.WriteLine("An error occurred - libexonymwallet not loaded!");
            }
        }

        public void Ping()
        {
            NativeMethods.hello_exonym(_thread);
        }

        public string OpenSystemParams()
        {
            IntPtr resultPtr = NativeMethods.open_system_params(_thread);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }
        
        public JoinResponse C30JoinToAuthProtocol(string alpha, string beta, string gamma, string path, int test)
        {
            IntPtr resultPtr = NativeMethods.c30_join_to_auth_protocol(_thread, alpha, beta, gamma, path, test);
            string json = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr); 
            return JoinResponse.Deserialise(json);
        }

        public bool HasPlayerKeyForGame(string alpha, string beta, string path)
        {
            int result = NativeMethods.has_player_key_for_game(_thread, alpha, beta, path);
            return result != 0; 
        }

        public string GeneratePlayerKeyForGame(string alpha, string beta, string path)
        {
            IntPtr resultPtr = NativeMethods.generate_player_key_for_game(_thread, alpha, beta, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr); 
            return result;
        }        

        public string NewRulebook(string name, string path)
        {
            IntPtr resultPtr = NativeMethods.new_rulebook(_thread, name, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr); 
            return result;
        }

        public string AddNewRule(string rule, string path)
        {
            IntPtr resultPtr = NativeMethods.add_new_rule(_thread, rule, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string ExtendRulebook(string inputFileLocation, string outputFileLocation)
        {
            IntPtr resultPtr = NativeMethods.extend_rulebook(_thread, inputFileLocation, outputFileLocation);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string WalletReport(string username, string passwordAsSha256Hex, string path)
        {
            IntPtr resultPtr = NativeMethods.wallet_report(_thread, username, passwordAsSha256Hex, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string AuthenticationReport(string username, string passwordAsSha256Hex, string request, string path)
        {
            IntPtr resultPtr = NativeMethods.authentication_report(_thread, username, passwordAsSha256Hex, request, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string LeadListTest(string url)
        {
            IntPtr resultPtr = NativeMethods.lead_list_test(_thread, url);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string SpawnNetworkMap(string path)
        {
            IntPtr resultPtr = NativeMethods.spawn_network_map(_thread, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string ViewActor(string uid, string path)
        {
            IntPtr resultPtr = NativeMethods.view_actor(_thread, uid, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string ListActors(string uid, string path)
        {
            IntPtr resultPtr = NativeMethods.list_actors(_thread, uid, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string ListRulebooks(string path)
        {
            IntPtr resultPtr = NativeMethods.list_rulebooks(_thread, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }
        
        public string ProofForRulebookSSO(string username, string passwordAsSha256Hex, string uLinkChallenge, string path)
        {
            IntPtr resultPtr = NativeMethods.proof_for_rulebook_sso(_thread, username, passwordAsSha256Hex, uLinkChallenge, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string ProofForRulebookSSOAnon(string username, string passwordAsSha256Hex, string uLinkChallenge, string path)
        {
            IntPtr resultPtr = NativeMethods.proof_for_rulebook_sso_anon(_thread, username, passwordAsSha256Hex, uLinkChallenge, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string GenerateDelegationRequestForThirdParty(string username, string passwordAsSha256Hex, string uLinkChallenge, string name, string path)
        {
            IntPtr resultPtr = NativeMethods.generate_delegation_request_for_third_party(_thread, username, passwordAsSha256Hex, uLinkChallenge, name, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string FillDelegationRequest(string username, string passwordAsSha256Hex, string uLinkChallenge, string path)
        {
            IntPtr resultPtr = NativeMethods.fill_delegation_request(_thread, username, passwordAsSha256Hex, uLinkChallenge, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string VerifyDelegationRequest(string username, string passwordAsSha256Hex, string requestLink, string proofLink, string path)
        {
            IntPtr resultPtr = NativeMethods.verify_delegation_request(_thread, username, passwordAsSha256Hex, requestLink, proofLink, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string NonInteractiveProof(string username, string passwordAsSha256Hex, string nonInteractiveProofRequest, string path)
        {
            IntPtr resultPtr = NativeMethods.non_interactive_proof(_thread, username, passwordAsSha256Hex, nonInteractiveProofRequest, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string SftpPut(string username, string passwordAsSha256Hex, string sftpCredentialUid, string fileName, string token, string remotePath, string path)
        {
            IntPtr resultPtr = NativeMethods.sftp_put(_thread, username, passwordAsSha256Hex, sftpCredentialUid, fileName, token, remotePath, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string SftpTemplate(string path)
        {
            IntPtr resultPtr = NativeMethods.sftp_template(_thread, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string SftpAdd(string username, string passwordAsSha256Hex, string path)
        {
            IntPtr resultPtr = NativeMethods.sftp_add(_thread, username, passwordAsSha256Hex, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string SftpRemove(string username, string passwordAsSha256Hex, string name, string path)
        {
            IntPtr resultPtr = NativeMethods.sftp_remove(_thread, username, passwordAsSha256Hex, name, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string OnboardSybilTestnet(string username, string sybilClass, string passwordAsSha256Hex, string path)
        {
            IntPtr resultPtr = NativeMethods.onboard_sybil_testnet(_thread, username, sybilClass, passwordAsSha256Hex, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string OnboardRulebook(string username, string issuancePolicy, string passwordAsSha256Hex, string path)
        {
            IntPtr resultPtr = NativeMethods.onboard_rulebook(_thread, username, issuancePolicy, passwordAsSha256Hex, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string OnboardRulebookModeratorUid(string username, string passwordAsSha256Hex, string modUid, string path)
        {
            IntPtr resultPtr = NativeMethods.onboard_rulebook_moderator_uid(_thread, username, passwordAsSha256Hex, modUid, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string Authenticate(string username, string passwordAsSha256Hex, string path)
        {
            IntPtr resultPtr = NativeMethods.authenticate(_thread, username, passwordAsSha256Hex, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string SetupWalletPath(string username, string plainTextPassword, string path)
        {
            IntPtr resultPtr = NativeMethods.setup_wallet_path(_thread, username, plainTextPassword, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public bool DeleteWallet(string username, string plainTextPassword, string path)
        {
            int result = NativeMethods.delete_wallet(_thread, username, plainTextPassword, path);
            return result != 0; // Non-zero indicates success
        }

        public string GenerateResetProof(string username, string plainTextPassword, string path)
        {
            IntPtr resultPtr = NativeMethods.generate_reset_proof(_thread, username, plainTextPassword, path);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }

        public string Sha256AsHex(string toHash)
        {
            IntPtr resultPtr = NativeMethods.sha_256_as_hex(_thread, toHash);
            string result = Marshal.PtrToStringAnsi(resultPtr);
            NativeMethods.free_cstring(_thread, resultPtr);
            return result;
        }
       
        public void Dispose()
        {
            if (!_disposed)
            {
                if (_thread != IntPtr.Zero)
                {
                    NativeMethods.graal_detach_thread(_thread);
                    _thread = IntPtr.Zero;
                }
                if (_isolate != IntPtr.Zero)
                {
                    NativeMethods.graal_tear_down_isolate(_isolate);
                    _isolate = IntPtr.Zero;
                }
                _disposed = true;
            }
        }
    }
}
