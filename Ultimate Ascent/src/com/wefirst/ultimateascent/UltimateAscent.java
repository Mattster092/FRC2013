package com.wefirst.ultimateascent;

/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project. */
/*----------------------------------------------------------------------------*/
import edu.wpi.first.wpilibj.Accelerometer;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
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
    final double SPEED_LIMIT = 0.8;
    final double WINCH_SPEED_LIMIT = 0.9;
    Victor driveMotors[] = {new Victor(cRIOPorts.LEFT_MOTOR), new Victor(cRIOPorts.RIGHT_MOTOR)};
    Victor armWinch1 = new Victor(cRIOPorts.WINCH1);
    Victor armWinch2 = new Victor(cRIOPorts.WINCH2);
    Victor armHinge = new Victor(cRIOPorts.VICTOR1);
    Victor shooter1 = new Victor(cRIOPorts.VICTOR2);
    Victor shooter2 = new Victor(cRIOPorts.VICTOR3);
    RobotDrive driveTrain;
    Accelerometer accel;
    Gyro gyro;
    Joystick joystickLeft;
    Joystick joystickRight;
    Joystick joystickShoot;
    Joystick joystickWinch;
    AxisCamera cam;
    DriverStationLCD lcd;
    //ArmSubsystem winchSub = new ArmSubsystem(armWinch1, armWinch2);
    //ArmSubsystem hingeSub = new ArmSubsystem(armHinge);
    //double winchLevels[] = {0, 0, 0};
    //double hingeLevels[] = {0, 0, 0, 0};
    //int counter = 0, position = 0;

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
            driveTrain = new RobotDrive(driveMotors[0], driveMotors[1]);
            joystickLeft = new Joystick(cRIOPorts.LEFT_JOYSTICK);
            joystickRight = new Joystick(cRIOPorts.RIGHT_JOYSTICK);
            joystickShoot = new Joystick(cRIOPorts.SHOOTING_JOYSTICK);
            joystickWinch = new Joystick(cRIOPorts.WINCH_JOYSTICK);
            //accel = new Accelerometer(cRIOPorts.ACCELEROMETER);
            //gyro = new Gyro(cRIOPorts.GYRO);
            //camInit();
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
        float power = 0f;
        //sendToDisplay("Entering autonomous:");
        driveTrain.setSafetyEnabled(false); // if true would stop the motors if there is no input, which there wouldn't be in autonomous
        while (isAutonomous() && isEnabled()) {
            if (power < 1f) {
                power += 0.001f;
            }
            System.out.println(power);
            driveMotors[0].set(power);
            driveMotors[1].set(power);
            //Camera.imageGrab(cam, cc);
        }
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
        //sendToDisplay("Entering teleopp:");
        //driveTrain.setSafetyEnabled(true); // stops the motors if input stops
        //winchSub.setSetpoint(winchLevels[0]);
        //winchSub.enable();
        while (isOperatorControl() && isEnabled()) {
            try {
                drive();
                arm();
            } catch (Exception any) {
                any.printStackTrace();
            }
        }
    }

    public void arm() {
        double motorSpeed = 1 - ((joystickWinch.getY() + 1.0) / 2.0);
        double magnitude = 1 - ((joystickWinch.getZ() + 1.0) / 2.0);

        motorSpeed *= magnitude;

        if (motorSpeed >= 0) {
            motorSpeed = Math.min(WINCH_SPEED_LIMIT, motorSpeed);
        } else {
            motorSpeed = Math.max(-WINCH_SPEED_LIMIT, motorSpeed);
        }

        armWinch1.set(motorSpeed);
        armWinch2.set(motorSpeed);

        if (joystickWinch.getRawButton(3)) {
            armHinge.set(0.6f);
        } else if (joystickWinch.getRawButton(2)) {
            armHinge.set(-0.6f);
        }
        /*
         if (joystickWinch.getRawButton(3) && counter == 0 && position < winchLevels.length - 1) {
         counter = 300;
         position++;
         } else if (joystickWinch.getRawButton(2) && counter == 0 && position != 0) {
         counter = 300;
         position--;
         }
         if (counter > 0) {
         counter--;
         }
         if (winchSup.getSetpoint() != winchLevels[position]) {
         winchSub.setSetpoint(winchLevels[position]);
         }
         */
    }

    public void drive() {
        double magnitude = 1 - ((joystickLeft.getZ() + 1.0) / 2.0);
        double leftSpeed = joystickLeft.getY() * magnitude;
        double rightSpeed = joystickRight.getY() * magnitude;
        if (leftSpeed >= 0) {
            leftSpeed = Math.min(SPEED_LIMIT, leftSpeed);
        } else {
            leftSpeed = Math.max(-SPEED_LIMIT, leftSpeed);
        }
        if (rightSpeed >= 0) {
            rightSpeed = Math.min(SPEED_LIMIT, rightSpeed);
        } else {
            rightSpeed = Math.max(-SPEED_LIMIT, rightSpeed);
        }

        //System.out.println("Left: " + leftSpeed + " Right: " + rightSpeed + " ZAxis: " + joystickLeft.getZ() + " Magnitude: " + magnitude);
        driveTrain.tankDrive((leftSpeed), (rightSpeed)); // tank drive
        //System.out.println("accelerometer; " + accel.pidGet());
    }

    protected void disabled() {
        System.err.println("Robot is disabled.");
        //sendToDisplay("Robot is disabled.");
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

    void sendToDisplay(String str) {
        lcd = DriverStationLCD.getInstance();
        lcd.println(DriverStationLCD.Line.kUser2, 1, str);
        lcd.updateLCD();
    }

    void camInit() {
        cam = AxisCamera.getInstance();
        cam.writeMaxFPS(15);
        cam.writeCompression(20);
        cam.writeColorLevel(50);
        cam.writeBrightness(40);
        cam.writeResolution(AxisCamera.ResolutionT.k160x120);
        cam.writeExposureControl(AxisCamera.ExposureT.automatic);
    }
}