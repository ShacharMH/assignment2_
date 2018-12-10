package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    private int CurrentTime;

public  TickBroadcast(int Time){
    CurrentTime=Time;
}




public int getCurrentTime(){
    return CurrentTime;
    }
}
