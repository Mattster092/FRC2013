package com.wefirst.ultimateascent;

/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved. */
/* Open Source Software - may be modified and shared by FRC teams. The code */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project. */
/*----------------------------------------------------------------------------*/
// this is a change!
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class UltimateAscent extends SimpleRobot {

    Victor driveMotors[] = {new Victor(cRIOPorts.LEFT_MOTOR), new Victor(cRIOPorts.LEFT_MOTOR)};
    RobotDrive driveTrain;
    Joystick joystickLeft = new Joystick(cRIOPorts.LEFT_JOYSTICK);
    Joystick joystickRight = new Joystick(cRIOPorts.RIGHT_JOYSTICK);
    Joystick joystickShoot = new Joystick(cRIOPorts.SHOOTING_JOYSTICK);
    AxisCamera cam;
    CriteriaCollection cc;

    public void robotMain() { // for testing
        robotInit();
        operatorControl();
    }

    /**
     * This function is called once at execution
     */
    protected void robotInit() {
        try {
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
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous() {
        System.err.println("Entering autonomous:");
        driveTrain.setSafetyEnabled(false); // if true would stop the motors if there is no input, which there wouldn't be in autonomous
        while (isAutonomous() && isEnabled()) {
            imageGrab();
        }
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        System.err.println("Entering teleopp:");
        driveTrain.setSafetyEnabled(true); // stops the motors if input stops
        while (isOperatorControl() && isEnabled()) {
            try {
                drive();
            } catch (Exception any) {
                any.printStackTrace();
            }
            for (int x = 0; x < driveMotors.length; x++) {
                driveMotors[x].set(0f);
            }
        }
    }

    public void imageGrab() {
        ColorImage image;
        try {
            image = cam.getImage();
            BinaryImage thresholdImage = image.thresholdRGB(0, 45, 0, 45, 0, 45);   // keep only white objects
            BinaryImage bigObjectsImage = thresholdImage.removeSmallObjects(false, 2);  // remove small artifacts
            BinaryImage convexHullImage = bigObjectsImage.convexHull(false); // fill in occluded rectangles
            BinaryImage filteredImage = convexHullImage.particleFilter(cc); // find filled in rectangles

            ParticleAnalysisReport[] reports = filteredImage.getOrderedParticleAnalysisReports();  // get list of results
            for (int i = 0; i < reports.length; i++) {                                // print results
                ParticleAnalysisReport r = reports[i];
                System.out.println("Particle: " + i + ":  Center of mass x: " + r.center_mass_x);
            }
            System.out.println(filteredImage.getNumberParticles() + "  " + Timer.getFPGATimestamp());

            filteredImage.free();
            convexHullImage.free();
            bigObjectsImage.free();
            thresholdImage.free();
            image.free();
        } catch (AxisCameraException ex) {
            ex.printStackTrace();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }
    }

    public void drive() {
        driveTrain.tankDrive(joystickLeft, joystickRight); // tank drive
        Timer.delay(0.1);
    }
}