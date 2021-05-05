package com.Shahab.netmart;

public class ModelBooking {
    private String bookingId, bookingTime, deliveryFee, orderCost;

    public ModelBooking() {

    }

    public ModelBooking(String bookingId, String bookingTime, String deliveryFee, String orderCost) {
        this.bookingId = bookingId;
        this.bookingTime = bookingTime;
        this.deliveryFee = deliveryFee;
        this.orderCost = orderCost;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(String orderCost) {
        this.orderCost = orderCost;
    }

}