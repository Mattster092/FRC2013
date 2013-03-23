package com.wefirst.ultimateascent;

/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project. */
/*----------------------------------------------------------------------------*/
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Jaguar;
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
    Victor driveMotors[];
    Victor armWinch1 = new Victor(cRIOPorts.WINCH1);
    Victor armWinch2 = new Victor(cRIOPorts.WINCH2);
    Jaguar shooter = new Jaguar(cRIOPorts.SHOOTER);
    Victor angle = new Victor(cRIOPorts.ANGLE);
    Victor climber1 = new Victor(cRIOPorts.CLIMBER1);
    Victor climber2 = new Victor(cRIOPorts.CLIMBER2);
    
    RobotDrive driveTrain;
    //Accelerometer accel = new Accelerometer(1);
    Attack3Joystick joystickLeft;
    Attack3Joystick joystickRight;
    Attack3Joystick joystickWinch;
    DriverStationLCD lcd;
    Encoder leftEncoder, rightEncoder, leftWinchEncoder, rightWinchEncoder;
    double LPOWmod = 1;
    double RPOWmod = 1;
    //AxisCamera cam;
    
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

            joystickLeft = new Attack3Joystick(cRIOPorts.LEFT_JOYSTICK);
            joystickRight = new Attack3Joystick(cRIOPorts.RIGHT_JOYSTICK);
            joystickWinch = new Attack3Joystick(cRIOPorts.WINCH_JOYSTICK);
            leftEncoder = new Encoder(cRIOPorts.LEFT_ENCODER_1, cRIOPorts.LEFT_ENCODER_2, true);
            rightEncoder = new Encoder(cRIOPorts.RIGHT_ENCODER_1, cRIOPorts.RIGHT_ENCODER_2);
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
            driveMotors[2].set(power);
            driveMotors[3].set(power);
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
        //hingeSub.setSetpoint(hingeLevels[0]);
        leftEncoder.start();
        rightEncoder.start();
        while (isOperatorControl() && isEnabled()) {
            try {
                drive();
                arm();
                //testing();
                shoot();
            } catch (Exception any) {
                any.printStackTrace();
            }
        }
    }

    public void shoot()
    {
        double magnitude = joystickWinch.getPower();//1 - ((joystickWinch.getZ() + 1.0) / 2.0);
        shooter.set(-magnitude);
        
        if (joystickWinch.getRawButton(11)) {//shooter up
            angle.set(-1);
        } else if (joystickWinch.getRawButton(10)) {//shooter down
            angle.set(1);
        } else {
            angle.set(0);
        }        
    }
    
    
    public void arm() {
        
        double winch = joystickWinch.getY();
        if(winch < 0.2 && winch > -0.2)
        {
            winch = 0;
        }
        armWinch1.set(winch);
        armWinch2.set(winch);
        
        if (joystickWinch.getRawButton(2)) {//arm up
            climber1.set(-1);
            climber2.set(-1);
        } else if (joystickWinch.getRawButton(3)) {//arm down
            climber1.set(1);
            climber2.set(1);
        } else {
            climber1.set(0);
            climber2.set(0);
        }
    }

    public void drive() {

        double magnitude = -joystickLeft.getPower();
        double leftSpeed = joystickLeft.getY() * magnitude;
        double rightSpeed = joystickRight.getY() * magnitude;
           
        
        /* ENCODER CODE IS DANGEROUS!!!
         if (!leftEncoder.getStopped() && !rightEncoder.getStopped() && leftSpeed != 0 && rightSpeed != 0) {

         double leftShift;
         double rightShift;

         leftShift = leftEncoder.getRate();
         rightShift = rightEncoder.getRate();

         leftShift /= leftSpeed;
         rightShift /= rightSpeed;

         if (leftShift != rightShift) {
         if (leftShift < rightShift) {
         leftShift = (leftShift / Math.abs(leftShift)) * (Math.abs(rightShift) + Math.abs(leftShift));
         } else if (rightShift < leftShift) {
         rightShift = (rightShift / Math.abs(rightShift)) * (Math.abs(rightShift) + Math.abs(leftShift));
         }

         double scaledMax = Math.max(Math.abs(leftShift), Math.abs(rightShift));

         leftSpeed = leftShift / scaledMax;
         rightSpeed = rightShift / scaledMax;
         }
         System.out.println("Left joystick: " + joystickLeft.getY() + " Right hoystick: " + joystickRight.getY());
         System.out.println("Left: " + leftSpeed + " Right: " + rightSpeed + " ZAxis: " + joystickLeft.getZ() + " Magnitude: " + magnitude);
         }
         */

        // deffs time for new encoder code
        double leftShift;
        double rightShift;

        leftShift = leftEncoder.getRate();
        rightShift = rightEncoder.getRate();

        leftShift /= leftSpeed;
        rightShift /= rightSpeed;

        leftShift = Math.abs(leftShift);
        rightShift = Math.abs(rightShift);

        if (Math.abs(leftShift - rightShift) > 6) {
            if (leftShift > rightShift) {
                LPOWmod -= 0.01;
                RPOWmod = 1;
            } else if (leftShift < rightShift) {
                RPOWmod -= 0.01;
                LPOWmod = 1;
            }
        }

        leftSpeed *= LPOWmod;
        rightSpeed *= RPOWmod;

        //System.out.println("Left: " + joystickLeft.getY() + " Right: " + joystickRight.getY());
        //System.out.println("Left Speed: " + leftSpeed + " Right Speed: " + rightSpeed);
        //System.out.println("Left POW: " + LPOWmod + " Right POW: " + RPOWmod);

        leftSpeed = Utils.limit(SPEED_LIMIT, leftSpeed);
        rightSpeed = Utils.limit(SPEED_LIMIT, rightSpeed);

        driveTrain.tankDrive((leftSpeed), (rightSpeed)); // tank drive
    }

    public void testing() {
        //System.out.println("Accelerometer: " + accel.getAcceleration());
        /*
         if (joystickLeft.getRawButton(2)) {
         System.out.println("Left Enc: " + leftEncoder.getDistance());
         System.out.println("Right Enc: " + rightEncoder.getDistance());
         }
         */
        /*
        if (joystickLeft.getRawButton(2)) {
            driveMotors[0].disable();
        }
        if (joystickLeft.getRawButton(3)) {
            driveMotors[1].disable();
        }
        if (joystickLeft.getRawButton(4)) {
            driveMotors[2].disable();
        }
        if (joystickLeft.getRawButton(5)) {
            driveMotors[3].disable();
        }
        */
        /*
         if (joystickLeft.getRawButton(9)) {
         driveMotors[0] = new Victor(cRIOPorts.LEFT_MOTOR_1);
         driveMotors[1] = new Victor(cRIOPorts.LEFT_MOTOR_2);
         driveMotors[2] = new Victor(cRIOPorts.RIGHT_MOTOR_1);
         driveMotors[3] = new Victor(cRIOPorts.RIGHT_MOTOR_2);
         }
         */
    }

    protected void disabled() {
        System.err.println("Robot is disabled.");
        leftEncoder.reset();
        rightEncoder.reset();
        //sendToDisplay("Robot is disabled.");
    }

    void sendToDisplay(String str) {
        lcd = DriverStationLCD.getInstance();
        lcd.println(DriverStationLCD.Line.kUser2, 1, str);
        lcd.updateLCD();
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
/*
    void camInit() {
        cam = AxisCamera.getInstance();
        cam.writeMaxFPS(15);
        cam.writeCompression(20);
        cam.writeColorLevel(50);
        cam.writeBrightness(40);
        cam.writeResolution(AxisCamera.ResolutionT.k160x120);
        cam.writeExposureControl(AxisCamera.ExposureT.automatic);
    }
    */
}