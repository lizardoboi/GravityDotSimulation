package org.example;

import java.awt.geom.Point2D;

public class PhysicalObject {
    private Point2D.Double position;
    private Point2D.Double velocity;
    private Point2D.Double acceleration;
    private static final double MASS = 10.0; // Константа массы
    private double x;
    private double y;
    public PhysicalObject(double x, double y) {
        this.x = x;
        this.y = y;
        position = new Point2D.Double(x, y);
        velocity = new Point2D.Double(0, 0);
        acceleration = new Point2D.Double(0, 0);
    }
    public double getMass() {
        return MASS;
    }
    // Метод для установки ускорения по заданному углу

    public void applyForce(Point2D.Double force) {
        acceleration.x += force.x;
        acceleration.y += force.y;
    }

    public void update() {
        velocity.x += acceleration.x;
        velocity.y += acceleration.y;
        position.x += velocity.x;
        position.y += velocity.y;
        acceleration.x = 0;
        acceleration.y = 0;
    }

    public Point2D.Double getPosition() {
        return position;
    }

    public void setPosition(double newX, double newY) {
        position.setLocation(newX, newY);
    }

    public Point2D.Double getVelocity() {
        return velocity;
    }
}