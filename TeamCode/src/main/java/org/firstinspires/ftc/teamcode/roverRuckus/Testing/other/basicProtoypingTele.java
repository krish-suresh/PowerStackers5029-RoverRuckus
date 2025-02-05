package org.firstinspires.ftc.teamcode.roverRuckus.Testing.other;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.PSRobotLibs.lib.PSEnum;
import org.firstinspires.ftc.teamcode.PSRobotLibs.lib.PSRobot;
import org.firstinspires.ftc.teamcode.PSRobotLibs.lib.hardware.PSMotor;
import org.firstinspires.ftc.teamcode.PSRobotLibs.lib.hardware.PSServo;
@TeleOp(name = "prototypetele",group = "robot")
@Disabled
public class basicProtoypingTele extends OpMode {
    PSRobot robot;
    PSMotor rightDrivetMotor;
    PSMotor leftDriveMotor;
    PSMotor otherMotor;
    PSServo servo;

    @Override
    public void init() {
        robot = new PSRobot(this);
        rightDrivetMotor = robot.motorHandler.newMotor("rdrive", 40);
        leftDriveMotor = robot.motorHandler.newMotor("ldrive", 40);
        otherMotor = robot.motorHandler.newMotor("motor", 40);
        servo = robot.servoHandler.newServo("servo", PSEnum.ServoTotalRotation.HS_485HB,0,false);

    }

    @Override
    public void loop() {
        rightDrivetMotor.setPower(gamepad1.right_stick_y);
        leftDriveMotor.setPower(gamepad1.left_stick_y);

        otherMotor.setPower(gamepad1.dpad_up?0.5:gamepad1.dpad_down?-0.5:0);
        double step = 0.0001;
        servo.setPosition((gamepad1.right_bumper)?servo.getCachedPosition()+step:(gamepad1.left_bumper)?servo.getCachedPosition()-step:servo.getCachedPosition());
    }
}
