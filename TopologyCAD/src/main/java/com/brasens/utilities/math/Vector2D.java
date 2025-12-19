package com.brasens.utilities.math;

import java.io.Serializable;

/**
 * 12-08-2020
 * @author Matheus Markies
 */
public class Vector2D implements Serializable {

    private double X;
    private double Y;

    public Vector2D(double X, double Y) {
        this.X = X;
        this.Y = Y;
    }

    public void y(double Y) {
        this.Y = Y;
    }

    public void x(double X) {
        this.X = X;
    }

    public double y() {
        return Y;
    }

    public double x() {
        return X;
    }

    public static Vector2D add(Vector2D a, Vector2D b) {
        return new Vector2D(a.x() + b.x(), a.y() + b.y());
    }

    public static Vector2D subtract(Vector2D a, Vector2D b) {
        return new Vector2D(a.x() - b.x(), a.y() - b.y());
    }

    public static Vector2D multiply(Vector2D a, Vector2D b) {
        return new Vector2D(a.x() * b.x(), a.y() * b.y());
    }

    public static Vector2D multiply(Vector2D a, double m) {
        return new Vector2D(a.x() * m, a.y() * m);
    }

    public static Vector2D multiply(Vector2D a, float m) {
        return new Vector2D(a.x() * (double) m, a.y() * (double) m);
    }

    public static Vector2D divide(Vector2D a, Vector2D b) {
        return new Vector2D(a.x() / b.x(), a.y() / b.y());
    }

    public static double distance(Vector2D a, Vector2D b) {
        return Math.sqrt((a.x() - b.x()) * (a.x() - b.x()) + (a.y() - b.y()) * (a.y() - b.y()));
    }

    public static Vector2D getCenter(Vector2D a, Vector2D b) {
        return new Vector2D(b.x() + (a.x() - b.x()) / 2, b.y() + (a.y() - b.y()) / 2);
    }

    public static double magnetude(Vector2D a) {
        return Math.sqrt(a.x() * a.x() + a.y() * a.y());
    }

    public static Vector2D normalize(Vector2D a) {
        return new Vector2D(a.x() / magnetude(a), a.y() / magnetude(a));
    }

    public static double scalar(Vector2D a, Vector2D b) {
        return (a.x() * b.x() + a.y() * b.y());
    }

    public static double dot(Vector2D a, Vector2D b) {
        return Vector2D.scalar(a, b) / (Vector2D.magnetude(a) * Vector2D.magnetude(b));
    }

    public static Vector2D clamp(Vector2D vec, double min, double max){
        return new Vector2D(Math.max(min, Math.min(max, vec.x())), Math.max(min, Math.min(max, vec.y())));
    }

    public static Vector2D clampMagnetude(Vector2D vec, double min, double max){
        double newMagnetude = Math.max(min, Math.min(max, magnetude(vec)));
        double z = newMagnetude / magnetude(vec);
        return new Vector2D(z * vec.x(), z * vec.y());
    }

    public static Vector2D lerp(Vector2D A,Vector2D B, double t)
    {
        return Vector2D.add(A,Vector2D.multiply(Vector2D.subtract(B,A),t));
    }

    public static Vector2D bezierCurve(Vector2D A,Vector2D B,Vector2D C, double t)
    {
        Vector2D p0 = Vector2D.lerp(A, C, t);
        Vector2D p1 = Vector2D.lerp(B, C, t);

        return Vector2D.lerp(p0, p1, t);
    }

    @Override
    public String toString() {
        return "X:" + x() + ",Y:" + y();
    }

    @Override
    public boolean equals(Object x) {
        if (x == null) {
            return false;
        }
        if (this.getClass() != x.getClass()) {
            return false;
        }
        Vector2D that = (Vector2D) x;
        return (this.x() == that.x() && this.y() == that.y());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.X) ^ (Double.doubleToLongBits(this.X) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.Y) ^ (Double.doubleToLongBits(this.Y) >>> 32));
        return hash;
    }
}