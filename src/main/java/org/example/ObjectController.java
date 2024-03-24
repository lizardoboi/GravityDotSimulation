package org.example;

import java.awt.geom.Point2D;

public class ObjectController {
    private PhysicalObject object;

    public ObjectController(double x, double y) {
        object = new PhysicalObject(x, y);
    }

    public void accelerate(double accelerationMagnitude, double direction) {
        double accelerationX = accelerationMagnitude * Math.cos(direction);
        double accelerationY = accelerationMagnitude * Math.sin(direction);
        object.applyForce(new Point2D.Double(accelerationX, accelerationY));
    }

    public void decelerate(double decelerationMagnitude, double direction) {
        double decelerationX = decelerationMagnitude * Math.cos(direction);
        double decelerationY = decelerationMagnitude * Math.sin(direction);
        object.applyForce(new Point2D.Double(-decelerationX, -decelerationY));
    }
    public void applyForce(Point2D.Double force) {
        object.applyForce(force);
    }

    public void updateObject() {
        object.update();
    }

    public Point2D.Double getPosition() {
        return object.getPosition();
    }

    public void setPosition(double newX, double newY) {
        object.setPosition(newX, newY);
    }

    public Point2D.Double getVelocity() {
        return object.getVelocity();
    }

    public PhysicalObject getObject() {
        return object;
    }
}
