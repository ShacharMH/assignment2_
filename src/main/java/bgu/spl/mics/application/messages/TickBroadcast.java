package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private volatile int CurrentTime;
    private final int duration;

    public TickBroadcast(int Time, int duration) {
        CurrentTime = Time;
        this.duration = duration;
    }


    public int getCurrentTime() {
        return CurrentTime;
    }


    public int getDuration() {
        return duration;
    }
}