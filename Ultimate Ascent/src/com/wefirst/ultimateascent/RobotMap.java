package com.wefirst.ultimateascent;

import edu.wpi.first.wpilibj.Victor;

public class RobotMap {
    
    public static Victor LEFT_MOTOR;
    
    public static void init(){
        LEFT_MOTOR = new Victor(cRIOPorts.LEFT_MOTOR);
    }
}
