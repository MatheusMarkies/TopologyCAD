package com.brasens.utilities.math;

/**
 * 12-08-2020
 * @author Matheus Markies
 */
public class Vector3D {

    public static float KEPSILON = 0.00001F;
    public static float KEPSILONNORMALSQRT = 1e-15F;

    public double x;
    public double y;
    public double z;

    public double x() {
        return x;
    }

    public void x(double x) {
        this.x = x;
    }

    public double y() {
        return y;
    }

    public void y(double y) {
        this.y = y;
    }

    public double z() {
        return z;
    }

    public void z(double z) {
        this.z = z;
    }

    public Vector3D() {
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3D lerp(Vector3D a, Vector3D b, float t) {
        return new Vector3D(
                a.x() + (b.x() - a.x()) * t,
                a.y() + (b.y() - a.y()) * t,
                a.z() + (b.z() - a.z()) * t
        );
    }

    public static Vector3D moveTowards(Vector3D current, Vector3D target, float maxDistanceDelta) {
        // avoid vector ops because current scripting backends are terrible at inlining
        double toVector_x = target.x() - current.x();
        double toVector_y = target.y() - current.y();
        double toVector_z = target.z() - current.z();

        double sqdist = toVector_x * toVector_x + toVector_y * toVector_y + toVector_z * toVector_z;

        if (sqdist == 0 || (maxDistanceDelta >= 0 && sqdist <= maxDistanceDelta * maxDistanceDelta))
            return target;
        double dist = Math.sqrt(sqdist);

        return new Vector3D(current.x() + toVector_x / dist * maxDistanceDelta,
                current.y() + toVector_y / dist * maxDistanceDelta,
                current.z() + toVector_z / dist * maxDistanceDelta);
    }

    public static Vector3D scale(Vector3D a, Vector3D b) {
        return new Vector3D(a.x() * b.x(), a.y() * b.y(), a.z() * b.z());
    }

    public static Vector3D cross(Vector3D lhs, Vector3D rhs) {
        return new Vector3D(
                lhs.y() * rhs.z() - lhs.z() * rhs.y(),
                lhs.z() * rhs.x() - lhs.x() * rhs.z(),
                lhs.x() * rhs.y() - lhs.y() * rhs.x());
    }

    public static Vector3D reflect(Vector3D inDirection, Vector3D inNormal) {
        double factor = -2F * dot(inNormal, inDirection);
        return new Vector3D(factor * inNormal.x() + inDirection.x(),
                factor * inNormal.y() + inDirection.y(),
                factor * inNormal.z() + inDirection.z());
    }

    public static Vector3D Normalize(Vector3D value) {
        double mag = magnitude(value);
        if (mag > KEPSILON)
            return value.multiply(1 / mag);
        else
            return new Vector3D(0, 0, 0);
    }

    public static double dot(Vector3D lhs, Vector3D rhs) {
        return lhs.x() * rhs.x() + lhs.y() * rhs.y() + lhs.z() * rhs.z();
    }

    public static Vector3D Project(Vector3D vector, Vector3D onNormal) {
        double sqrMag = dot(onNormal, onNormal);
        double dot = dot(vector, onNormal);
        return new Vector3D(onNormal.x() * dot / sqrMag,
                onNormal.y() * dot / sqrMag,
                onNormal.z() * dot / sqrMag);
    }

    static double clamp(double d, double min, double max) {
        double t = d < min ? min : d;
        return t > max ? max : t;
    }

    public static double angle(Vector3D from, Vector3D to) {
        double denominator = (float) Math.sqrt(from.magnitude() * from.magnitude() * to.magnitude() * to.magnitude());
        if (denominator < KEPSILONNORMALSQRT)
            return 0F;

        double dot = clamp(dot(from, to) / denominator, -1, 1);
        return Math.toDegrees(Math.acos(dot));
    }

    public static double signedAngle(Vector3D from, Vector3D to, Vector3D axis) {
        double unsignedAngle = angle(from, to);

        double cross_x = from.y() * to.z() - from.z() * to.y();
        double cross_y = from.z() * to.x() - from.x() * to.z();
        double cross_z = from.x() * to.y() - from.y() * to.x();
        double sign = (axis.x() * cross_x + axis.y() * cross_y + axis.z() * cross_z);

        int f = 1;

        if (sign < 0)
            f = -1;

        return unsignedAngle * f;
    }

    public static double Distance(Vector3D a, Vector3D b) {
        double diff_x = a.x() - b.x();
        double diff_y = a.y() - b.y();
        double diff_z = a.z() - b.z();
        return (float) Math.sqrt(diff_x * diff_x + diff_y * diff_y + diff_z * diff_z);
    }

    public static double magnitude(Vector3D vector) {
        return (float) Math.sqrt(vector.x() * vector.x() + vector.y() * vector.y() + vector.z() * vector.z());
    }

    public double magnitude() {
        return (float) Math.sqrt(this.x() * this.x() + this.y() * this.y() + this.z() * this.z());
    }

    public static Vector3D min(Vector3D lhs, Vector3D rhs) {
        return new Vector3D(Math.min(lhs.x(), rhs.x()), Math.min(lhs.y(), rhs.y()), Math.min(lhs.z(), rhs.z()));
    }

    public static Vector3D max(Vector3D lhs, Vector3D rhs) {
        return new Vector3D(Math.max(lhs.x(), rhs.x()), Math.max(lhs.y(), rhs.y()), Math.max(lhs.z(), rhs.z()));
    }

    public static Vector3D add(Vector3D a, Vector3D b) {
        return new Vector3D(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    public static Vector3D subtract(Vector3D a, Vector3D b) {
        return new Vector3D(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    public static Vector3D multiply(Vector3D a, Vector3D b) {
        return new Vector3D(a.x() * b.x(), a.y() * b.y(), a.z() * b.z());
    }

    public static Vector3D multiply(Vector3D a, double m) {
        return new Vector3D(a.x() * m, a.y() * m, a.z() * m);
    }

    public static Vector3D multiply(Vector3D a, float m) {
        return new Vector3D(a.x() * (double) m, a.y() * (double) m, a.z() * (double) m);
    }

    public static Vector3D divide(Vector3D a, Vector3D b) {
        return new Vector3D(a.x() / b.x(), a.y() / b.y(), a.z() / b.z());
    }

    public Vector3D add(Vector3D b) {
        return new Vector3D(this.x() + b.x(), this.y() + b.y(), this.z() + b.z());
    }

    public Vector3D subtract(Vector3D b) {
        return new Vector3D(this.x() - b.x(), this.y() - b.y(), this.z() - b.z());
    }

    public Vector3D multiply(Vector3D b) {
        return new Vector3D(this.x() * b.x(), this.y() * b.y(), this.z() * b.z());
    }

    public Vector3D multiply(double m) {
        return new Vector3D(this.x() * m, this.y() * m, this.z() * m);
    }

    public Vector3D multiply(float m) {
        return new Vector3D(this.x() * (double) m, this.y() * (double) m, this.z() * (double) m);
    }

    public Vector3D divide(Vector3D b) {
        return new Vector3D(this.x() / b.x(), this.y() / b.y(), this.z() / b.z());
    }

    @Override
    public String toString() {
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public static Vector3D zeroVector = new Vector3D(0, 0, 0);
    public static Vector3D oneVector = new Vector3D(1, 1, 1);
    public static Vector3D upVector = new Vector3D(0, 1, 0);
    public static Vector3D downVector = new Vector3D(0, -1, 0);
    public static Vector3D leftVector = new Vector3D(-1, 0, 0);
    public static Vector3D rightVector = new Vector3D(1, 0, 0);
    public static Vector3D forwardVector = new Vector3D(0, 0, 1);
    public static Vector3D backVector = new Vector3D(0, 0, -1);

}
