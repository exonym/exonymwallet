using System;
using Newtonsoft.Json;

namespace ExonymWalletBridge.Schema
{
    public class JoinResponse
    {
        [JsonProperty("canRejoin")]
        public bool CanRejoin { get; set; }

        [JsonProperty("revokedModerators")]
        public string[] RevokedModerators { get; set; }

        [JsonProperty("penaltyType")]
        public string PenaltyType { get; set; }

        [JsonProperty("bannedLiftedUTC")]
        public DateTime? BannedLiftedUTC { get; set; }

        [JsonProperty("tovutc")]
        public DateTime? Tovutc { get; set; }

        [JsonProperty("nibble6")]
        public string Nibble6 { get; set; }

        [JsonProperty("x0Hash")]
        public string X0Hash { get; set; }

        [JsonProperty("issuerUid")]
        public string IssuerUid { get; set; }

        [JsonProperty("issued")]
        public bool Issued { get; set; }
    
    
        public static JoinResponse Deserialise(string json)
        {
            try
            {
                return JsonConvert.DeserializeObject<JoinResponse>(json);
            }
            catch (JsonException ex)
            {
                Console.WriteLine($"JSON Deserialization error: {ex.Message}");
                return null; // Return null if deserialization fails
            }
        }
    
    }

}