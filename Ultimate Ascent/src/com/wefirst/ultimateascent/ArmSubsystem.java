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
    Victor victors[];
    AnalogChannel potentiometer;

    // Initialize your subsystem here
    public ArmSubsystem(Victor victor1, Victor victor2) {
        super("ArmSubsystem", Kp, Ki, Kd);
        this.victors = new Victor[2];
        this.victors[0] = victor1;
        this.victors[1] = victor2;
        
        potentiometer = new AnalogChannel(cRIOPorts.POTENTIOMETER_WINCH);

        // Use these to get going:
        // setSetpoint() -  Sets where the PID controller should move the system
        //                  to
        // enable() - Enables the PID controller.
    }

    public ArmSubsystem(Victor victor) {
        super("ArmSubsystem", Kp, Ki, Kd);
        this.victors = new Victor[1];
        this.victors[0] = victor;
        
        potentiometer = new AnalogChannel(cRIOPorts.POTENTIOMETER_HINGE);

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
        for (int motors = 0; motors > victors.length; motors++) {
            victors[motors].set(output);
        }
    }
}
