package com.wefirst.ultimateascent;

/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project. */
/*----------------------------------------------------------------------------*/
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Dashboard;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class UltimateAscent extends SimpleRobot {

    private boolean m_robotMainOverridden;
    CANJaguar driveMotors[];
    RobotDrive driveTrain;
    Joystick joystickLeft = new Joystick(cRIOPorts.LEFT_JOYSTICK);
    Joystick joystickRight = new Joystick(cRIOPorts.RIGHT_JOYSTICK);
    Joystick joystickShoot = new Joystick(cRIOPorts.SHOOTING_JOYSTICK);
    AxisCamera cam;
    CriteriaCollection cc;
    DriverStationLCD lcd;
    Dashboard dashHigh;
    Dashboard dashLow;

    public UltimateAscent() {
        super();
        m_robotMainOverridden = true; // for testing - remove for competitions (I think)
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
        robotInit();
        operatorControl();
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
            driveMotors[0] = new CANJaguar(cRIOPorts.LEFT_MOTOR);
            driveMotors[1] = new CANJaguar(cRIOPorts.RIGHT_MOTOR);
            driveTrain = new RobotDrive(driveMotors[0], driveMotors[2]);
            cam = AxisCamera.getInstance();
            cam.writeMaxFPS(15);
            cam.writeCompression(20);
            cam.writeColorLevel(50);
            cam.writeBrightness(40);
            cam.writeResolution(AxisCamera.ResolutionT.k160x120);
            // cam.writeExposureControl(AxisCamera.ExposureT.automatic);
            cc = new CriteriaCollection();
            cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
            cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
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
        // sendToDisplay("Entering autonomous:");
        driveTrain.setSafetyEnabled(false); // if true would stop the motors if there is no input, which there wouldn't be in autonomous
        while (isAutonomous() && isEnabled()) {
            Camera.imageGrab(cam, cc);
            updateDashboard();
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
        // sendToDisplay("Entering teleopp:");
        driveTrain.setSafetyEnabled(true); // stops the motors if input stops
        while (isOperatorControl() && isEnabled()) {
            try {
                drive();
                updateDashboard();
            } catch (Exception any) {
                any.printStackTrace();
            }
            for (int x = 0; x < driveMotors.length; x++) {
                try {
                    driveMotors[x].disableControl();
                } catch (CANTimeoutException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void drive() {
        driveTrain.tankDrive(joystickLeft, joystickRight); // tank drive
    }

    protected void disabled() {
        System.err.println("Robot is disabled.");
        // sendToDisplay("Robot is disabled.");
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
            } /* while loop */
        }
    }

    void updateDashboard() {
        dashHigh = DriverStation.getInstance().getDashboardPackerHigh();

        dashLow = DriverStation.getInstance().getDashboardPackerLow();

        dashLow.addCluster();
        {
            dashLow.addDouble(DriverStation.getInstance().getMatchTime());
        }
        dashLow.finalizeCluster();

        dashLow.commit();
    }

    void sendToDisplay(String str) {
        lcd = DriverStationLCD.getInstance();
        lcd.println(DriverStationLCD.Line.kUser2, 1, str);
        lcd.updateLCD();
    }
}
