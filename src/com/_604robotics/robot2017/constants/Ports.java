package com._604robotics.robot2017.constants;

public class Ports {
    private Ports() {}

    /* Controllers */
    public static final int CONTROLLER_DRIVER = 0;
    
    /* Digital */
    public static final int COMPRESSOR = 0;
    public static final int PEW = 9;
    
    /* Analog */
    public static final int ULTRASONIC_LEFT = 1;
    public static final int ULTRASONIC_RIGHT = 3;
    
    public static final int HORIZGYRO = 0;
    
    /* Motors */
    public static final int DRIVE_FRONT_LEFT_MOTOR = 1;
    public static final int DRIVE_REAR_LEFT_MOTOR = 2;
    public static final int DRIVE_FRONT_RIGHT_MOTOR = 3;
    public static final int DRIVE_REAR_RIGHT_MOTOR = 4;
   
    public static final int CLIMBER_MOTOR = 0;
    
    public static final int INTAKE_FORWARD_MOTOR = 1;
    public static final int INTAKE_REVERSE_MOTOR = 6;
    
    /* Encoders */
    public static final int DRIVE_ENCODER_RIGHT_A = 2;
    public static final int DRIVE_ENCODER_RIGHT_B = 3;
    public static final int DRIVE_ENCODER_LEFT_A = 1;
    public static final int DRIVE_ENCODER_LEFT_B = 0;
    
    /* Solenoids */
    public static final int SHIFTER_SOLENOID_FORWARD = 0;
    public static final int SHIFTER_SOLENOID_REVERSE = 1;
    public static final int FLIPFLOP_SOLENOID_FORWARD = 6;
    public static final int FLIPFLOP_SOLENOID_REVERSE = 7;
    
    /* Relay */
    public static final int SPIKELIGHT = 0;
    public static final int LED = 1;
}
