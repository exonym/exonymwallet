package io.exonym.lib.pojo;

public class RevocationResponse {

    int totalTokens = 0;
    int modCount = 0;
    int invalidTokens = 0;
    int thisModTokens = 0;

    public int getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }

    public int getModCount() {
        return modCount;
    }

    public void setModCount(int modCount) {
        this.modCount = modCount;
    }

    public int getInvalidTokens() {
        return invalidTokens;
    }

    public void setInvalidTokens(int invalidTokens) {
        this.invalidTokens = invalidTokens;
    }

    public int getThisModTokens() {
        return thisModTokens;
    }

    public void setThisModTokens(int thisModTokens) {
        this.thisModTokens = thisModTokens;
    }
}
