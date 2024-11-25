using System;
using ExonymWalletBridge;

namespace ExonymWalletTest
{
    class Program
    {
        static void Main(string[] args)
        {
            try
            {
                using (var wallet = new ExonymWallet())
                {
                    // Test the Ping method
                    wallet.Ping();
                    Console.WriteLine("Ping successful!");

                    // Test opening system parameters
                    string systemParams = wallet.OpenSystemParams();
                    Console.WriteLine($"System Params: {systemParams}");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error: {ex.Message}");
                Console.WriteLine($"Error: {ex.StackTrace}");
            }
        }
    }
}