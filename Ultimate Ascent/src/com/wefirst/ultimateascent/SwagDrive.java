package com.wefirst.ultimateascent;

import edu.wpi.first.wpilibj.RobotDrive;

public class SwagDrive extends RobotDrive {
    
    private final double SWAG_BARRIER = 0.1d;
    private final double SWAG_MULTIPLIER = 10d;
    private final int SWAG_LEVEL = 9000;
    private final int SWAG_PERIOD = 500;
    
    private double oldMove = 0d;
    private double oldRotate = 0d;
    
    private int swagLevel = 0;
    private int swagPeriod = 0;

    public SwagDrive(int frontLMotor, int frontRMotor, int rearLMotor, int rearRMotor) {
        super(frontLMotor, rearLMotor, frontRMotor, rearRMotor);
    }
    
    public void swagDrive(double moveValue, double rotateValue) {
        double moveToSend = moveValue;
        double rotateToSend = rotateValue;
        
        if(swagPeriod == 0) {
            double moveDiff = Math.abs(Math.abs(moveValue) - Math.abs(oldMove));
            double rotateDiff = Math.abs(Math.abs(rotateValue) - Math.abs(oldRotate));
            
            if(moveDiff < SWAG_BARRIER) {
                moveToSend = (moveDiff*SWAG_MULTIPLIER) + moveValue;
            } else {
                swagLevel++;
            }
            
            if(rotateDiff < SWAG_BARRIER) {
                rotateToSend = (rotateDiff*SWAG_MULTIPLIER) + rotateValue;
            } else {
                swagLevel++;
            }
            
            if(swagLevel > 9000) {
                swagPeriod = SWAG_PERIOD;
                swagLevel = 0;
            }
        } else {
            moveToSend = 0;
            rotateToSend = 1.0;
            swagPeriod--;
        }
        
        arcadeDrive(moveToSend, rotateToSend);
        
        oldMove = moveToSend;
        oldRotate = rotateToSend;
    }
}