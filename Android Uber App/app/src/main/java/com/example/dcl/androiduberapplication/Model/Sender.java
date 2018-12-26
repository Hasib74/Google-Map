package com.example.dcl.androiduberapplication.Model;



public class Sender {

    public String to;
    public Notification notification;

    public Sender() {

    }

    public Sender(String to, Notification notification) {
        this.to = to;
        this.notification = notification;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
