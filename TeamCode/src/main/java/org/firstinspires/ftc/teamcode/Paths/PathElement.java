package org.firstinspires.ftc.teamcode.Paths;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.path.heading.ConstantInterpolator;
import com.acmerobotics.roadrunner.path.heading.HeadingInterpolator;
import com.acmerobotics.roadrunner.path.heading.SplineInterpolator;

public class PathElement {

    public final static int START = 0;
    public final static int SPLINE = 1;
    public final static int SPLINEINTERPOLER = 5;
    public final static int STRAFE = 2;
    public final static int ROTATE = 3;
    public final static int DELAY = 4;
    public final static int REVERSE = 6;
    public final static int DRIVE = 7;
    public final static int BEGINCOMPOSITE = 8;
    public final static int CLOSECOMPOSITE = 9;
    
    public int type;
    private double x;
    private double y;
    private double interpolatorStart;
    private double interpolatorEnd;
    private double rot;

    public PathElement(int type, double x, double y, double rot , double interpolatorStart, double interpolatorEnd){
        this.type = type;
        this.x = x;
        this.y = y;
        this.interpolatorStart = interpolatorStart;
        this.interpolatorEnd = interpolatorEnd;
        this.rot = rot;
    }

    //Statics
    public static PathElement newStart(double x, double y, double head){
        return new PathElement(START, x, y, head, 0, 0);
    }

    public static PathElement newSpline(double x, double y, double head, double start, double end){
        return new PathElement(SPLINEINTERPOLER, x, y, head, start,end);
    }

    public static PathElement newSpline(double x, double y, double head) {
        return new PathElement(SPLINE, x, y, head, 0, 0);
    }

    public static PathElement newStrafe(double x, double y){
        return new PathElement(STRAFE, x, y, 0.0, 0, 0);
    }

    public static PathElement newRot(double rot){
        return new PathElement(ROTATE, 0, 0, rot, 0, 0);
    }

    public static PathElement newDelay(double time){
        return new PathElement(DELAY, time, 0, 0, 0, 0);
    }

    public static PathElement newReverse(boolean reversed){
        return new PathElement(REVERSE, (reversed) ? 1 : 0, 0, 0, 0, 0);
    }

    public static PathElement newDrive(double distance){
        return new PathElement(DRIVE, distance, 0, 0, 0, 0);
    }

    public static PathElement[] newDelayFull(double time){
        return new PathElement[]{new PathElement(DELAY, time, 0, 0, 0, 0)};
    }

    public static PathElement newBeginComp(){
        return new PathElement(BEGINCOMPOSITE, 0, 0, 0, 0, 0);
    }

    public static PathElement newCloseComp(){
        return new PathElement(CLOSECOMPOSITE, 0, 0, 0, 0, 0);
    }

    //functions
    public Pose2d getPose2d(){
        return new Pose2d(x, y, getRot());
    }

    public Vector2d getVector2d(){
        return new Vector2d(x, y);
    }

    public HeadingInterpolator getInterpolator(){
        return new SplineInterpolator(Math.toRadians(interpolatorStart), Math.toRadians(interpolatorEnd));
    }

    public double getRot(){
        return Math.toRadians(rot);
    }

    public double getDelay(){
        return (type == DELAY) ? x : 0.0;
    }

    public double distance(){
        return x;
    }

    public boolean reverse(){
        return x == 1;
    }

    public String toString(){
        return type + "," + x + "," + y +"," + rot + "," + interpolatorStart + "," + interpolatorEnd;
    }
}
