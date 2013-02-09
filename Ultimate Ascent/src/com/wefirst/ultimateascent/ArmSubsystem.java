package com.wefirst.ultimateascent;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.PIDSubsystem;

//Check out this article:
//http://wpilib.screenstepslive.com/s/3120/m/7882/l/79335?data-resolve=true&data-manual-id=
//could be VERY helpful

public class ArmSubsystem extends PIDSubsystem {

    private static final double Kp = 0.0;
    private static final double Ki = 0.0;
    private static final double Kd = 0.0;
    Victor victor;
    
    AnalogChannel potentiometer = new AnalogChannel(cRIOPorts.POTENTIOMETER);

    // Initialize your subsystem here
    public ArmSubsystem(Victor victor) {
        super("ArmSubsystem", Kp, Ki, Kd);
        this.victor = victor;

        // Use these to get going:
        // setSetpoint() -  Sets where the PID controller should move the system
        //                  to
        // enable() - Enables the PID controller.
    }
    
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    protected double returnPIDInput() {
        // Return your input value for the PID loop
        // e.g. a sensor, like a potentiometer:
        // yourPot.getAverageVoltage() / kYourMaxVoltage;
        return potentiometer.getAverageVoltage();
    }
    
    protected void usePIDOutput(double output) {
        // Use output to drive your system, like a motor
        // e.g. yourMotor.set(output);
        victor.set(output);
    }
}
