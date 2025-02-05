package org.firstinspires.ftc.teamcode.roverRuckus.Robot_r5;


import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.path.heading.SplineInterpolator;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.PSRobotLibs.lib.hardware.LEDRiver;
import org.firstinspires.ftc.teamcode.PSRobotLibs.lib.hardware.PSMotor;

import static java.lang.Math.abs;

@TeleOp(name = "r5.Tele", group = "r5")
public class TeleOp_r5 extends Config_r5 {

    private double lastAngle = 0;
    private boolean autoPlaceInPos = false;
    private double slidePos = 0;
    private double autoSlidePower;
    private boolean toggleCollectorLift = false;
    private boolean autoSlide = false;

    enum TeleMode {
        Collect,
        Hang,
        AutoPlace
    }

    //calibaration value for 3rd person driving
    double cal = 0.0;
    //Toggle shooter variable
    private boolean shooterOn = false;
    private boolean collectorLift = false;
    //Automatic drive on/off
    private boolean pathDrive = false;
    //location to automatically drive robot to place
    Pose2d placePos = new Pose2d(50, 57, 225);
    // object for automatic drive
    Trajectory trajectory;

    private int automaticHang = 0;

    private TeleMode mode = TeleMode.Collect;


    double xPrev = 0;
    double yPrev = 0;
    Vector2d position = new Vector2d(0, 0);

    enum Action {
        PICK, DRIVETOPLACE
    }

    Action action = Action.PICK;

    long startTime;

    //init
    @Override
    public void init() {
        //initialize robot
        config(this);
        //set servo ratchet position to on
        lift.ratchetOn();
        //debug telemetry
        telemetry.addData("gyro", "ready");
        telemetry.update();

        //set drive type to driver prefrence
        for (PSMotor motor : drive.motors) {
            motor.motorObject.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }

        //drive.calibrationPosition = new Vector2d(57, 57);
        Pose2d savedPosition = getSavedPosition();
        gyro.cal = Math.toDegrees(-savedPosition.getHeading());

        drive.trackerWheels.setPoseEstimate(savedPosition);
        //telemetry.addData("bridge.pos", lift.bridge.setBridge2(Math.toDegrees(atan2(-0, -1))));

        startTime = getSavedTime();

        lift.bridge.canopy.setPosition(0);
    }

    @Override
    public void init_loop() {
        double timeFromStartOfMatch = (System.currentTimeMillis() - startTime) / 1000;
        telemetry.addData("Time", timeFromStartOfMatch);
        telemetry.update();
//        if (5 < timeFromStartOfMatch && 6 > timeFromStartOfMatch) {
//            lights.setBrightness(0);
//            lights.solid(new LEDRiver.Color(255, 0, 0, 0));
//        }
//        if (6 < timeFromStartOfMatch && 7 < timeFromStartOfMatch) {
//            lights.setBrightness(0);
//            lights.solid(new LEDRiver.Color(255, 255, 0, 0));
//        }
//        if (7 < timeFromStartOfMatch && 8 > timeFromStartOfMatch) {
//            lights.setBrightness(0);
//            lights.solid(new LEDRiver.Color(0, 255, 0, 0));
//        }

    }

    @Override
    public void start() {
//        lights.setTeamColor();
    }

    //loop
    @Override
    public void loop() {
        //thrid person driving, disable if driver prefers


        if (gamepad1.a) {
            mode = TeleMode.Collect;
        }
        if (gamepad1.b) {
            mode = TeleMode.Hang;
        }
        telemetry.addData("Mode", mode);
        telemetry.addData("gyro", gyro.getHeading());


        //actions
        switch (action) {
            case PICK:
                if (gamepad1.left_stick_button) {
                    drive.thirdPerson = true;
                    cal = gyro.getHeading();
                }
                if (gamepad1.a) {
                    drive.thirdPerson = false;
                }
                if (drive.thirdPerson) {
                    robot.drive.mecanum.updateMecanumThirdPerson(gamepad1, (mode == TeleMode.Hang) ? 1.0 : .5, -Math.toRadians(gyro.getHeading() - cal));

                } else {
                    robot.drive.mecanum.updateMecanum(gamepad1, (mode == TeleMode.Hang) ? 0.5 : 1);

                }
                if (gamepad1.right_stick_button) {
                    //drive.calibrationPosition = new Vector2d(0, 0);
                    placePos = drive.getEstimatedPose();
                    slidePos = collector.extension.getEncoderPosition();
                }
                if (gamepad1.dpad_right) {
                    trajectory = new TrajectoryBuilder(drive.getEstimatedPose(), DriveConstants_r5.BASE_CONSTRAINTS)
                            // .lineTo(new Vector2d(0, 0),LinearInterpolator(drive.getEstimatedPose().getHeading(), 0))
//                            .beginComposite()
//                            .lineTo(new Vector2d(placePos.getX() - 10, placePos.getY() - 10), new SplineInterpolator(drive.getEstimatedPose().getHeading(), placePos.getHeading()))
                            .lineTo(placePos.pos(), new SplineInterpolator(drive.getEstimatedPose().getHeading(), placePos.getHeading()))
//                            .closeComposite()
                            .build();

                    drive.followTrajectory(trajectory);

                    action = Action.DRIVETOPLACE;
                }

                break;
            case DRIVETOPLACE:
                if (drive.isFollowingTrajectory()) {
                    drive.update();
                    if (collector.extension.getEncoderPosition() < slidePos) {
                        autoSlidePower = 1;
                    } else {
                        autoSlidePower = 0;
                    }
                } else {

                    action = Action.PICK;
                }
                break;
        }

        telemetry.addData("Action", action);
        telemetry.addData("Error", drive.getFollowingError());
        telemetry.addData("pose", drive.getEstimatedPose());

        lift.extension.setPower((gamepad2.dpad_up || (mode == TeleMode.Hang) ? gamepad1.dpad_up : false) ? -1.0 : (gamepad2.dpad_down || (mode == TeleMode.Hang) ? gamepad1.dpad_down : false) ? 1.0 : 0.0);
        collector.extension.setPower(((gamepad2.right_bumper) ? -1 : ((gamepad2.left_bumper) ? 1 : 0)) + ((Math.abs(gamepad1.right_stick_y) > 0.5) ? gamepad1.right_stick_y : 0) + autoSlidePower);

        if (gamepad2.right_stick_x > .8)

        {
            lift.bridge.openBridge();
            telemetry.addData("S", "Open");
        } else if (gamepad2.right_stick_x < -.8)

        {
            lift.bridge.closeBridge();
            telemetry.addData("S", "Close");
        } else

        {
            lift.bridge.stopBridge();
            telemetry.addData("S", "Stop");
        }

        if (gamepad2.left_stick_x > .8)

        {
            lift.bridge.canopy.setPosition(0);
        } else if (gamepad2.left_stick_x < -.8)

        {
            lift.bridge.canopy.setPosition(1.0);
        }

        autoPlaceInPos = (drive.getEstimatedPose().

                getY() > placePos.getY() - 2) && (drive.getEstimatedPose().

                getX() > placePos.getX() - 2) && (drive.getEstimatedPose().

                getY() < placePos.

                getY() + 2) & (drive.getEstimatedPose().

                getX() < placePos.

                getX() + 2);
        if (gamepad1.left_bumper)

        {
            lift.bridge.doorServo.setPosition(1);
        } else

        {
            lift.bridge.doorServo.setPosition(.67);
        }

        switch (mode)

        {
            case Collect:
                //collector in/out
                if (gamepad1.x || gamepad2.x) {
                    shooterOn = true;
                } else if (gamepad1.y || gamepad2.y) {
                    shooterOn = false;
                }
                //shooter output
                if (shooterOn) {
                    double shooterPower = gamepad1.left_trigger * 2 - 1;
                    collector.shooterLeft.setPower(shooterPower);
                } else {
                    collector.shooterLeft.setPower(0);
                }

                collector.setCollectorPower(gamepad1.right_trigger + gamepad2.right_trigger - (gamepad1.left_trigger + gamepad2.left_trigger));

                //lift/lower collectory
                if (gamepad1.right_bumper || gamepad2.a || toggleCollectorLift) {
                    collector.collectorRotate.setPosition(1);
                } else {
                    collector.collectorRotate.setPosition(0.2);
                }
                if (gamepad1.right_bumper) {
                    toggleCollectorLift = false;
                }

                if (gamepad1.a) {
                    autoSlide = true;
                    toggleCollectorLift = true;
                }
                if (autoSlide) {
                    if (collector.extension.getEncoderPosition() < slidePos - 100) {
                        autoSlidePower = 1;
                    } else if (collector.extension.getEncoderPosition() > slidePos + 100) {
                        autoSlidePower = -0.5;
                    } else {
                        autoSlidePower = 0;
                        autoSlide = false;
                    }
                }
                break;
            case Hang:
                break;
            case AutoPlace:
                break;
        }
//        Pose2d rob = drive.getEstimatedPose();
//        Pose2d TW = drive.getTrackerWheelPos();
//
//        telemetry.addData("POSE.X", rob.getX());
//        telemetry.addData("POSE.Y", rob.getY());
//        telemetry.addData("POSE.HEADING", rob.getHeading());
//        telemetry.addData("TW.X", TW.getX());
//        telemetry.addData("TW.Y", TW.getY());
//        telemetry.addData("TW.HEADING", TW.getHeading());
//        double yTrackerWheelCounts = collector.shooterRight.getEncoderPosition();
//        double xTrackerWheelCounts = collector.shooterLeft.getEncoderPosition();
//        double yTrackerWheelCountsChange = yTrackerWheelCounts - yPrev;
//        double xTrackerWheelCountsChange = xTrackerWheelCounts - xPrev;
//        double wheelCirm = 3*Math.PI;
//        double yWheelFromCenter = 7.125;
//        double xWheelFromCenter = 7.375;
//        double angleChange = gyro.getHeading()-lastAngle;
//        double xTrackerWheelInches = -((wheelCirm*(xTrackerWheelCountsChange/4096.0))+(xWheelFromCenter*4.0*Math.PI*(angleChange/360.0)));
//        double yTrackerWheelInches = -((wheelCirm*(yTrackerWheelCountsChange/4096.0))-(yWheelFromCenter*4.0*Math.PI*(angleChange/360.0)));
//        xPrev = xTrackerWheelCounts;
//        yPrev = yTrackerWheelCounts;
//        Vector2d change = new Vector2d(xTrackerWheelInches, yTrackerWheelInches);
//        change = change.rotated(Math.toRadians(gyro.getHeading()));
//        position = position.plus(change);
//
//        telemetry.addData("CHANGE.X", change.getX());
//        telemetry.addData("CHANGE.Y", change.getY());
//        telemetry.addData("POSITION.X", position.getX());
//        telemetry.addData("POSITION.Y", position.getY());
//
//        telemetry.addData("raw inches x", (wheelCirm*(xTrackerWheelCountsChange/4096.0)));
//        telemetry.addData("raw inches y", (wheelCirm*(yTrackerWheelCountsChange/4096.0)));
//        telemetry.addData("rot sub x",(xWheelFromCenter*2*Math.PI*(angleChange/360.0)));
//        telemetry.addData("rot sub y",(yWheelFromCenter*2*Math.PI*(angleChange/360.0)));
//        telemetry.addData("angleChange", angleChange);
//        telemetry.addData("tracker wheel pos X", xTrackerWheelInches);
//        telemetry.addData("tracker wheel pos Y", yTrackerWheelInches);
//
//        telemetry.addData("tracker wheel pos Heading", gyro.getHeading());
//        lastAngle = gyro.getHeading();
//        //Set automatic drive position todo add adjust odo to gamepad 2
//        if (gamepad1.y) {
//            placePos = drive.getEstimatedPose();
//        }

//        //run automatic drive
//        if (gamepad1.b) {
//            //if already started just update
//            if (pathDrive) {
//                //only if trajectory is still active update
//                if (drive.isFollowingTrajectory()) {
//                    drive.update();
//                }
//            } else {
//                //create path to mineral place point
//                trajectory = new TrajectoryBuilder(drive.getEstimatedPose(), new DriveConstraints(40.0, 60.0, 2, 3))
//                        .lineTo(new Vector2d(placePos.getX(), placePos.getY()))
//                        .build();
//                //start follow trajectory
//                drive.followTrajectory(trajectory);
//                //set flag variable
//                pathDrive = true;
//            }
//            //if not automatic drive then use joysticks
//        } else {
//            //set path to off
//            pathDrive = false;
//            //check if third person driving is on
//            if (drive.thirdPerson) {
//                //update drive using gyro angle
//                robot.drive.mecanum.updateMecanumThirdPerson(gamepad1, (true) ? 1.0 : .5, -Math.toRadians(gyro.getHeading() - cal));
//            } else {
//                //udpate drive without third person driving
//                robot.drive.mecanum.updateMecanum(gamepad1, (true) ? 1.0 : .5);
//            }
//        }

//        //collector in/out
//        collector.extension.setPower((gamepad1.dpad_up || gamepad2.right_bumper) ? 1 : ((gamepad1.dpad_down || gamepad2.left_bumper) ? -1 : 0));
        //shooter toggle
//        if (gamepad1.x || gamepad2.x) {
//            shooterOn = true;
//        } else if (gamepad1.a || gamepad2.y) {
//            shooterOn = false;
//        }
//        //shooter output
//        if (shooterOn) {
//            collector.shooterLeft.setPower(-1);
//        } else {
//            collector.shooterLeft.setPower(0);
//        }
        //collectory power
//        collector.setCollectorPower(gamepad1.right_trigger+gamepad2.right_trigger - (gamepad1.left_trigger+gamepad2.left_trigger));

//        //lift/lower collectory
//        if (gamepad1.right_bumper || gamepad2.a) {
//            collector.collectorRotate.setPosition(0.2);
//        } else {
//            collector.collectorRotate.setPosition(0.9);
//        }
//        //Lift
//        int liftEnc = lift.extension.getEncoderPosition();
//        telemetry.addData("lift", liftEnc);
//        if (gamepad2.right_stick_y > .8) {
//            lift.extension.setPower((liftEnc > lift.liftBelowHookHeight + 40) ? -1.0 : (liftEnc < lift.liftBelowHookHeight - 40) ? 1.0 : 0.0);
//        } else if (gamepad2.right_stick_y < -.8) {
//            if(automaticHang == 0) {
//                lift.extension.setPower((liftEnc > lift.liftHookHeight + 40) ? -1.0 : (liftEnc < lift.liftHookHeight - 40) ? 1.0 : 0.0);
//                if(liftEnc < lift.liftHookHeight + 40 && liftEnc > lift.liftHookHeight - 40){
//                    automaticHang = 1;
//                }
//            }else if(automaticHang == 1){
//                lift.extension.setPower((liftEnc > lift.liftHangHeight + 40) ? -1.0 : (liftEnc < lift.liftHangHeight - 40) ? 1.0 : 0.0);
//                if(liftEnc < lift.liftHangHeight + 40 && liftEnc > lift.liftHangHeight - 40){
//                    lift.ratchetOff();
//                }
//            }
//        } else {
//            automaticHang = 0;
//            lift.extension.setPower((gamepad2.dpad_up) ? -1.0 : (gamepad2.dpad_down) ? 1.0 : 0.0);
//        }
//        //set brdige servo position
//        if (!(abs(gamepad2.left_stick_x) < 0.6f && abs(gamepad2.left_stick_y) < 0.6f)) {
////            if (gamepad2.left_stick_x < -.9) telemetry.addData("bridge ", lift.bridge.setBridge2(0));
////            if (gamepad2.left_stick_x > .9) telemetry.addData("bridge ", lift.bridge.setBridge2(180));
//
//            telemetry.addData("bridge.pos", lift.bridge.setBridge2(Math.toDegrees(atan2(-gamepad2.left_stick_y, -gamepad2.left_stick_x))));
//            telemetry.addData("bridge.angle", Math.toDegrees(atan2(-gamepad2.left_stick_y, -gamepad2.left_stick_x)));
////
//        }
//        telemetry.addData("bridgeServoPos", lift.bridge.bridgeRotate.getCachedPosition());
//
//        //move bridge for finer adjustement
//        if (gamepad2.dpad_right) {
//
//            lift.bridge.bridgeRotate.setPosition(lift.bridge.bridgeRotate.getCachedPosition() + 0.001);
//        } else if (gamepad2.dpad_left) {
//            lift.bridge.bridgeRotate.setPosition(lift.bridge.bridgeRotate.getCachedPosition() - 0.001);
//        }
//        if (gamepad2.b) {
//            lift.bridge.bridgeRotate.off();
//        }
//
//        //ratchet on/off
//        if (gamepad2.right_stick_button) {
//            lift.ratchetOn();
//        } else if (gamepad2.left_stick_button) {
//            lift.ratchetOff();
//        }
//
//        //mineral drop door position
//        lift.bridge.doorServo.setPosition((gamepad1.left_bumper) ? lift.dropNormal : lift.dropInit);
    }
}
