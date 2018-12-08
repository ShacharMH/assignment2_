package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class DeliveryEvent implements Event {

    private DeliveryVehicle deliveryVehicle;
    private String address;
    private int distance;

    public DeliveryEvent(DeliveryVehicle deliveryVehicle, String address, int distance) {
        this.deliveryVehicle = deliveryVehicle;
        this.address = address;
        this.distance = distance;
    }

    public DeliveryVehicle getDeliveryVehicle() {
        return deliveryVehicle;
    }

    public int getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }
}
