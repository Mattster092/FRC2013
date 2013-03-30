package com.wefirst.ultimateascent;

/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project. */
/*----------------------------------------------------------------------------*/
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.camera.AxisCamera;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class UltimateAscent extends SimpleRobot {

    private boolean m_robotMainOverridden = false;
    Victor driveMotors[];
    Victor armWinch1;
    Victor armWinch2;
    Jaguar shooter;
    Victor angle;
    Victor climber;
    Servo feeder;
    RobotDrive driveTrain;
    Attack3Joystick joystickLeft;
    Attack3Joystick joystickRight;
    Attack3Joystick joystickWinch;
    AnalogChannel shooterEncoder;
    AxisCamera cam;
    AnalogChannel winchPot;
    boolean deWinch = false;
    int angleTarget = 0;
    int savedLimit = Constants.SAVED_LIMIT_SHOOT;
    String output;

    public UltimateAscent() {
        super();
    }

    /**
     * Robot main program for free-form programs.
     *
     * This should be overridden by user subclasses if the intent is to not use
     * the autonomous() and operatorControl() methods. In that case, the program
     * is responsible for sensing when to run the autonomous and operator
     * control functions in their program.
     *
     * This method will be called immediately after the constructor is called.
     * If it has not been overridden by a user subclass (i.e. the default
     * version runs), then the robotInit(), disabled(), autonomous() and
     * operatorControl() methods will be called.
     */
    public void robotMain() { // for testing - remove for competitions (I think)
    }

    /**
     * Robot-wide initialization code should go here.
     *
     * Users should override this method for default Robot-wide initialization
     * which will be called when the robot is first powered on.
     *
     * Called exactly 1 time when the competition starts.
     */
    protected void robotInit() {
        try {
            driveMotors = new Victor[4];
            driveMotors[0] = new Victor(cRIOPorts.LEFT_MOTOR_1);
            driveMotors[1] = new Victor(cRIOPorts.LEFT_MOTOR_2);
            driveMotors[2] = new Victor(cRIOPorts.RIGHT_MOTOR_1);
            driveMotors[3] = new Victor(cRIOPorts.RIGHT_MOTOR_2);
            driveTrain = new RobotDrive(driveMotors[0], driveMotors[1], driveMotors[2], driveMotors[3]);

            armWinch1 = new Victor(cRIOPorts.WINCH1);
            armWinch2 = new Victor(cRIOPorts.WINCH2);
            shooter = new Jaguar(cRIOPorts.SHOOTER);
            angle = new Victor(cRIOPorts.ANGLE);
            climber = new Victor(cRIOPorts.CLIMBER);
            feeder = new Servo(cRIOPorts.FEEDER);

            joystickLeft = new Attack3Joystick(cRIOPorts.LEFT_JOYSTICK);
            joystickRight = new Attack3Joystick(cRIOPorts.RIGHT_JOYSTICK);
            joystickWinch = new Attack3Joystick(cRIOPorts.WINCH_JOYSTICK);
            shooterEncoder = new AnalogChannel(cRIOPorts.SHOOTER_ENCODER);
            winchPot = new AnalogChannel(cRIOPorts.POTENTIOMETER);

            camInit();

        } catch (Exception any) {
            any.printStackTrace();
        }
    }

    /**
     * Autonomous should go here. Users should add autonomous code to this
     * method that should run while the field is in the autonomous period.
     *
     * Called repeatedly while the robot is in the autonomous state.
     */
    public void autonomous() {
        System.err.println("Entering autonomous:");
        driveTrain.setSafetyEnabled(false); // if true would stop the motors if there is no input, which there wouldn't be in autonomous
        int autoStage = Constants.AUTO_ADJUST_SHOOTER;
        int setTo = Constants.AUTO_SHOOTER_LIMIT;
        
        //testing:
        setTo = (int)(DriverStation.getInstance().getBatteryVoltage() * Constants.SCALING_SLOPE + Constants.SCALING_INTERCEPT + 0.5);
        //end testing
        
        Timer.delay(0.5);

        while (isAutonomous() && isEnabled()) {
            if (autoStage == Constants.AUTO_ADJUST_SHOOTER) {
                if (shooterEncoder.getValue() > setTo) {
                    angle.set(1);
                } else if (shooterEncoder.getValue() < setTo) {
                    angle.set(-0.8);
                } else {
                    angle.set(0);
                    autoStage = Constants.AUTO_SHOOT;
                }
            } else if (autoStage == Constants.AUTO_SHOOT) {
                shooter.set(-1);
                Timer.delay(3);
                feeder.set(0.15);
                Timer.delay(6);
                autoStage = Constants.AUTO_FINISHED;
            } else if (autoStage == Constants.AUTO_FINISHED) {
                feeder.set(0.5);
                shooter.set(0);
                if (shooterEncoder.getValue() < Constants.SHOOTER_LOWER_LIMIT) {
                    angle.set(-0.8);
                } else {
                    angle.set(0);
                }
            }
        }
        feeder.set(0.5);
        shooter.set(0);
        angle.set(0);
    }

    /**
     * Operator control (tele-operated) code should go here. Users should add
     * Operator Control code to this method that should run while the field is
     * in the Operator Control (tele-operated) period.
     *
     * Called repeatedly while the robot is in the operator-controlled state.
     */
    public void operatorControl() {
        System.err.println("Entering teleopp:");
        while (isOperatorControl() && isEnabled()) {
            try {
                output = "";
                drive();
                arm();
                shoot();
                System.out.println(output);
            } catch (Exception any) {
                any.printStackTrace();
            }
        }
    }

    public void shoot() {
        double magnitude = -joystickWinch.getPower();
        shooter.set(magnitude);

        if (joystickWinch.getRawButton(3) && (shooterEncoder.getValue() > Constants.SHOOTER_UPPER_LIMIT || joystickWinch.getRawButton(9))) {//shooter up
            angle.set(1);
            angleTarget = 0;
        } else if (joystickWinch.getRawButton(2) && (shooterEncoder.getValue() < Constants.SHOOTER_LOWER_LIMIT || joystickWinch.getRawButton(9))) {//shooter down
            angle.set(-0.8);
            angleTarget = 0;
        } else {
            angle.set(0);
        }

        if (joystickWinch.getRawButton(7)) {
            angleTarget = savedLimit;
        } else if (joystickWinch.getRawButton(11)) {
            angleTarget = Constants.SAVED_LIMIT_PYRAMID;
        } else if (joystickWinch.getRawButton(10)) {
            angleTarget = Constants.SHOOTER_UPPER_LIMIT;
        } else if (joystickWinch.getRawButton(4)) {
            angleTarget = Constants.SHOOTER_LOWER_LIMIT;
        } else if (joystickWinch.getRawButton(6)) {
            savedLimit = shooterEncoder.getValue();
            angleTarget = 0;
            angle.set(0);
        }

        if (angleTarget != 0) {
            if (shooterEncoder.getValue() < angleTarget) {
                angle.set(-0.8);
            } else if (shooterEncoder.getValue() > angleTarget) {
                angle.set(1);
            } else {
                angle.set(0);
                angleTarget = 0;
            }
        }

        if (joystickWinch.getRawButton(1)) {
            feeder.set(0);
        } else {
            feeder.set(0.5);
        }
        output += "---Shooter---\n";
        output += "\nShooter angle: " + shooterEncoder.getValue();
        output += "\n\n";
    }

    public void arm() {

        double armSpeed = joystickWinch.getY();
        if (armSpeed < 0.2 && armSpeed > -0.2) {
            armSpeed = 0;
        } else {
            deWinch = false;
        }

        if (armSpeed < 0 && (winchPot.getValue() >= Constants.POT_WINCH_UP || joystickWinch.getRawButton(9))) {
            armSpeed = 0;
        }

        if (joystickWinch.getRawButton(8)) {
            deWinch = true;
        }

        if (deWinch) {
            if (winchPot.getValue() <= Constants.POT_WINCH_UP) {
                armSpeed = -1;
            } else {
                armSpeed = 0;
                deWinch = false;
            }
        }

        armWinch1.set(armSpeed);
        armWinch2.set(armSpeed);

        if (joystickRight.getRawButton(2)) {//arm down
            climber.set(-1);
        } else if (joystickRight.getRawButton(3)) {//arm up
            climber.set(1);
        } else {
            climber.set(0);
        }
        output += "---Arm---\n";
        output += "\nWinch position: " + winchPot.getValue();
        output += "\n\n";
    }

    public void drive() {
        double leftSpeed = -joystickLeft.getY();
        double rightSpeed = -joystickRight.getY();
        
        if (leftSpeed < 0.2 && leftSpeed > -0.2) {
            leftSpeed = 0;
        }
        if (rightSpeed < 0.2 && rightSpeed > -0.2) {
            rightSpeed = 0;
        }

        if (joystickLeft.getRawButton(1) || joystickRight.getRawButton(1)) {
            leftSpeed *= 0.75;
            rightSpeed *= 0.75;
        }

        driveTrain.tankDrive(leftSpeed, rightSpeed); // tank drive
    }

    protected void disabled() {
        System.err.println("Robot is disabled.");
    }

    /**
     * Start a competition. This code tracks the order of the field starting to
     * ensure that everything happens in the right order. Repeatedly run the
     * correct method, either Autonomous or OperatorControl when the robot is
     * enabled. After running the correct method, wait for some state to change,
     * either the other mode starts or the robot is disabled. Then go back and
     * wait for the robot to be enabled again.
     */
    public void startCompetition() {
        robotMain();
        if (!m_robotMainOverridden) {
            // first and one-time initialization
            robotInit();

            while (true) {
                if (isDisabled()) {
                    disabled();
                    while (isDisabled()) {
                        Timer.delay(0.01);
                    }
                } else if (isAutonomous()) {
                    autonomous();
                    while (isAutonomous() && isEnabled()) {
                        Timer.delay(0.01);
                    }
                } else {
                    operatorControl();
                    while (isOperatorControl() && isEnabled()) {
                        Timer.delay(0.01);
                    }
                }
            } // while loop
        }
    }

    void camInit() {
        cam = AxisCamera.getInstance();
        cam.writeMaxFPS(10);
        cam.writeCompression(20);
        cam.writeColorLevel(50);
        cam.writeBrightness(40);
        cam.writeResolution(AxisCamera.ResolutionT.k160x120);
        cam.writeExposureControl(AxisCamera.ExposureT.automatic);
    }
}