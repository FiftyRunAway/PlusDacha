package org.example;

public class Event {

    private String eventName;
    private String eventDate;
    private int index;

    public Event(String eventName, String eventDate, int index) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.index = index;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public int getIndex() {
        return index;
    }
}
