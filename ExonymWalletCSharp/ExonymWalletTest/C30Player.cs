namespace ExonymWalletTest;

using System;

public class C30Player
{
    public string Alpha { get; private set; }
    public string Beta { get; private set; }

    public static C30Player Init()
    {
        var player = new C30Player();
        player.SetAlpha(Guid.NewGuid().ToString());
        player.SetBeta(Guid.NewGuid().ToString());
        return player;
    }

    public static C30Player Init(string alpha)
    {
        var player = new C30Player();
        player.SetAlpha(alpha);
        player.SetBeta(Guid.NewGuid().ToString());
        return player;
    }

    private void SetAlpha(string alpha)
    {
        Alpha = alpha;
    }

    private void SetBeta(string beta)
    {
        Beta = beta;
    }
}
