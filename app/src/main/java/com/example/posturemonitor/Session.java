package com.example.posturemonitor;

public class Session {
    public long startMillis;
    public long endMillis;
    public int slouches;

    public Session() { }

    public Session(long startMillis, long endMillis, int slouches) {
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.slouches = slouches;
    }

    public long getDurationMillis() {
        return Math.max(0, endMillis - startMillis);
    }
}
