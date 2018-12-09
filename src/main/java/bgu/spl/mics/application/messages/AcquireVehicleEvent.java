package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class AcquireVehicleEvent implements Event {

    private String address;
    private int distance;

    public AcquireVehicleEvent(String address, int distance) {
        this.address = address;
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }
}
