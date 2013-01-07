/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
// this is a change!
package com.wefirst.ultimateascent;


import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Victor;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class UltimateAscent extends SimpleRobot {
    
        Victor motors[] = {new Victor (cRIOPorts.LEFT_FRONT_MOTOR), new Victor (cRIOPorts.LEFT_BACK_MOTOR), new Victor (cRIOPorts.RIGHT_FRONT_MOTOR), new Victor (cRIOPorts.RIGHT_BACK_MOTOR)};
        RobotDrive driveTrain;
        Joystick joystickLeft = new Joystick(cRIOPorts.LEFT_JOYSTICK);
        Joystick joystickRight = new Joystick(cRIOPorts.RIGHT_JOYSTICK);
        Joystick joystickShoot = new Joystick(cRIOPorts.SHOOTING_JOYSTICK);
        
    /**
     * This function is called once at execution
     */
    protected void robotInit() {
        try {
            driveTrain = new RobotDrive(motors[0], motors[1], motors[2], motors[3]);
        } catch (Exception any) {
            any.printStackTrace();
        }
    }
    
    /**
     * This function is called once each time the robot enters autonomous mode.
     */    
    public void autonomous() {
        System.err.println("Entering autonomous:");
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        System.err.println("Entering teleopp:");
        try {
            driveTrain.tankDrive(joystickLeft, joystickRight);
        } catch (Exception any) {
            any.printStackTrace();
        }  
    }
}
