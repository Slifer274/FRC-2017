package com._604robotics.robot2017.modules;

import com._604robotics.robot2017.constants.Calibration;
import com._604robotics.robot2017.constants.Ports;
import com._604robotics.robotnik.action.Action;
import com._604robotics.robotnik.action.ActionData;
import com._604robotics.robotnik.action.controllers.ElasticController;
import com._604robotics.robotnik.action.field.FieldMap;
import com._604robotics.robotnik.data.DataMap;
import com._604robotics.robotnik.module.Module;
import com._604robotics.robotnik.prefabs.devices.AnalogUltrasonic;
import com._604robotics.robotnik.prefabs.devices.ArcadeDrivePIDOutput;
import com._604robotics.robotnik.prefabs.devices.InvertPIDSource;
import com._604robotics.robotnik.prefabs.devices.UltrasonicPair;
import com._604robotics.robotnik.trigger.Trigger;
import com._604robotics.robotnik.trigger.TriggerMap;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Drive extends Module {
    // 19.7 clicks per inch
    // -490/490 is 360 degrees with both wheels driving, 115 is 90 degrees

    // Locking a side: put it 18 in the opposite direction
    // 430 is 180 degrees with one side locked

    // When decreasing angle it needs a little bit less than you'd think
	private boolean calibrated = false;
	private boolean reset = false;
	    
	private final RobotDrive drive = new RobotDrive(
            Ports.DRIVE_FRONT_LEFT_MOTOR,
            Ports.DRIVE_REAR_LEFT_MOTOR,
            Ports.DRIVE_FRONT_RIGHT_MOTOR,
            Ports.DRIVE_REAR_RIGHT_MOTOR);

    private final Encoder encoderLeft = new Encoder(
            Ports.DRIVE_ENCODER_LEFT_A,
            Ports.DRIVE_ENCODER_LEFT_B,
            false, CounterBase.EncodingType.k4X);
    private final Encoder encoderRight = new Encoder(
            Ports.DRIVE_ENCODER_RIGHT_A,
            Ports.DRIVE_ENCODER_RIGHT_B,
            true, CounterBase.EncodingType.k4X);

    /*
    private final TankDrivePIDOutput pidOutput = new TankDrivePIDOutput(drive);
    private final PIDController pidLeft = new PIDController(
            Calibration.DRIVE_LEFT_PID_P,
            Calibration.DRIVE_LEFT_PID_I,
            Calibration.DRIVE_LEFT_PID_D,
            encoderLeft,
            pidOutput.left);
    private final PIDController pidRight = new PIDController(
            Calibration.DRIVE_RIGHT_PID_P,
            Calibration.DRIVE_RIGHT_PID_I,
            Calibration.DRIVE_RIGHT_PID_D,
            encoderRight,
            pidOutput.right);
    */
    
	private final AnalogGyro horizGyro = new AnalogGyro(Ports.HORIZGYRO);
    private InvertPIDSource invertedGyro;
	
    private final ArcadeDrivePIDOutput pidOutput = new ArcadeDrivePIDOutput(drive);
    private final PIDController pidMove = new PIDController(
    		Calibration.DRIVE_LEFT_PID_P,
    		Calibration.DRIVE_LEFT_PID_I,
    		Calibration.DRIVE_LEFT_PID_D,
    		encoderLeft,
    		pidOutput.move);
    private final PIDController pidRotate;
    
    private final ArcadeDrivePIDOutput pidOutput2 = new ArcadeDrivePIDOutput(drive);
    private final PIDController pidMove2 = new PIDController(
    		Calibration.DRIVE_LEFT_PID_P,
    		Calibration.DRIVE_LEFT_PID_I,
    		Calibration.DRIVE_LEFT_PID_D,
    		encoderLeft,
    		pidOutput2.move);
    
    //private final AnalogGyro horizGyro = new AnalogGyro(Ports.HORIZGYRO);
    //private final AnalogUltrasonic ultra = new AnalogUltrasonic(0);
    private final UltrasonicPair ultra = new UltrasonicPair(new AnalogUltrasonic(Ports.ULTRASONIC_LEFT), new AnalogUltrasonic(Ports.ULTRASONIC_RIGHT), Calibration.ULTRA_SEPARATION);
    
    private final Timer timer = new Timer();
    
    /*
    private double pid_power_cap = 0.6;
    private double PIDUltraOut = 0D;
    private final PIDController pidUltra = new PIDController(Calibration.DRIVE_ULTRA_PID_P, 
    		Calibration.DRIVE_ULTRA_PID_I, Calibration.DRIVE_LEFT_PID_D, null, new PIDOutput () {
    	public void pidWrite (double output) {
    		if( output > 0 ) PIDUltraOut = (output > pid_power_cap) ? pid_power_cap : output;
    		else PIDUltraOut = (output < -pid_power_cap) ? -pid_power_cap : output;
    	}
    });
    */    
    
    private boolean started;

    private double lastLeftRate;
    private double lastRightRate;
    private double leftAccel;
    private double rightAccel;
    
    public Drive () { 
    	lastLeftRate = 0;
    	lastRightRate = 0;
    	leftAccel = 0;
    	rightAccel = 0;
    	started = false;
    	//horizGyro.calibrate();
        {
        	calibrated = true;
        }

        encoderLeft.setPIDSourceType(PIDSourceType.kDisplacement);
        encoderRight.setPIDSourceType(PIDSourceType.kDisplacement);
        
        horizGyro.setPIDSourceType(PIDSourceType.kDisplacement);
        invertedGyro = new InvertPIDSource(horizGyro);
        
        invertedGyro.setPIDSourceType(PIDSourceType.kDisplacement);
    	
    	pidRotate = new PIDController(
                Calibration.DRIVE_ROTATE_PID_P,
                Calibration.DRIVE_ROTATE_PID_I,
                Calibration.DRIVE_ROTATE_PID_D,
                invertedGyro,
                pidOutput.rotate);
    	SmartDashboard.putData("PID Rotate", pidRotate);
    	SmartDashboard.putData("PID Move", pidMove);

        /*
        pidLeft.setOutputRange(-Calibration.DRIVE_LEFT_PID_MAX, Calibration.DRIVE_LEFT_PID_MAX);
        pidRight.setOutputRange(-Calibration.DRIVE_RIGHT_PID_MAX, Calibration.DRIVE_RIGHT_PID_MAX);
        pidLeft.setAbsoluteTolerance(Calibration.DRIVE_LEFT_PID_TOLERANCE);
        pidRight.setAbsoluteTolerance(Calibration.DRIVE_RIGHT_PID_TOLERANCE);
		*/
		
        pidMove.setOutputRange(-Calibration.DRIVE_LEFT_PID_MAX, Calibration.DRIVE_LEFT_PID_MAX);
        pidMove.setAbsoluteTolerance(Calibration.DRIVE_LEFT_PID_TOLERANCE);
        
        pidRotate.setOutputRange(-Calibration.DRIVE_ROTATE_PID_MAX, Calibration.DRIVE_ROTATE_PID_MAX);
        pidRotate.setAbsoluteTolerance(Calibration.DRIVE_ROTATE_PID_TOLERANCE);

        this.set(new DataMap() {{
            add("Left Drive Clicks", encoderLeft::get);
            add("Right Drive Clicks", encoderRight::get);

            add("Left Drive Rate", encoderLeft::getRate);
            add("Right Drive Rate", encoderRight::getRate);
            
            add("Left Drive Accel", () -> leftAccel);
            add("Right Drive Accel", () -> rightAccel);

            //add("Left PID Error", pidLeft::getAvgError);
            //add("Right PID Error", pidRight::getAvgError);
            
            add("Move PID Error", pidMove::getAvgError);
            add("Rotate PID Error", pidRotate::getAvgError);

            add("Ultra Inches", ultra::getDistance);
            add("Ultra Angle", ultra::getAngle);
            add("Ultra Difference", ultra::getDifference);
            
            add("Ultra Left", ultra::getLeftDistance);
            add("Ultra Right", ultra::getRightDistance);
            
            add("Timer Seconds", timer::get);
            
            add("Horizontal Gyro Angle", horizGyro::getAngle);
        }});

        this.set(new TriggerMap() {{
        	add("Gyro Calibrated", () -> calibrated);
        	add("Reset", () -> reset);
        	add("Forward Again", () -> true);
            // add("At Move Servo Target", () -> pidMove.isEnabled() && pidMove.onTarget());
            add("At Move Servo Target", new Trigger() {
                private final Timer timer = new Timer();
                private boolean timing = false;
                public boolean run () {
                    if (pidMove.isEnabled() && pidMove.onTarget()) {
                        if (!timing) {
                            timing = true;
                            timer.start();
                        }
                        return timer.get() >= 0.5;
                    } else {
                        if (timing) {
                            timing = false;
                            timer.stop();
                            timer.reset();
                        }
                        return false;
                    }
                }
            });
            add("At Move Servo Target 2", () -> pidMove2.isEnabled() && pidMove2.onTarget());            
            add("At Rotate Servo Target", () -> pidRotate.isEnabled() && pidRotate.onTarget());
            add("Aligned", () -> Math.abs(ultra.getDifference()) <= 0.5);
            add("Past Ultra Target", () -> ultra.getDistance() < Calibration.ULTRA_TARGET && ultra.getAngle() < 3);
            add("Timer Setpoint", () -> timer.get() > Calibration.ROTATE_WAIT);
            add("Timer Forward", () -> timer.get() > Calibration.TIMER_FORWARD);
            add("North", () -> -Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < Calibration.ROTATE_TOLERANCE);
            add("East", () -> Calibration.ROTATE_TARGET_A - Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < Calibration.ROTATE_TARGET_A + Calibration.ROTATE_TOLERANCE);
            add("West", () -> -Calibration.ROTATE_TARGET_A- Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < -Calibration.ROTATE_TARGET_A + Calibration.ROTATE_TOLERANCE);
            add("South", () -> Calibration.ROTATE_TARGET_A * 2 -Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < Calibration.ROTATE_TARGET_A * 2 + Calibration.ROTATE_TOLERANCE);
            add("South Neg", () -> Calibration.ROTATE_TARGET_A * -2 -Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < -Calibration.ROTATE_TARGET_A * 2 + Calibration.ROTATE_TOLERANCE);
            add("NorthWest", () -> -Calibration.ROTATE_TARGET_B-Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < -Calibration.ROTATE_TARGET_B + Calibration.ROTATE_TOLERANCE);
            add("NorthEast", () -> Calibration.ROTATE_TARGET_B-Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < Calibration.ROTATE_TARGET_B + Calibration.ROTATE_TOLERANCE);

            add("Left Target", () -> -Calibration.ROTATE_LEFT_TARGET-Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < -Calibration.ROTATE_LEFT_TARGET + Calibration.ROTATE_TOLERANCE);
            add("Right Target", () -> Calibration.ROTATE_RIGHT_TARGET-Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < Calibration.ROTATE_RIGHT_TARGET + Calibration.ROTATE_TOLERANCE);
            
            add("Left Wiggle Target", () -> -Calibration.ROTATE_WIGGLE_TARGET-Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < -Calibration.ROTATE_WIGGLE_TARGET + Calibration.ROTATE_TOLERANCE);
            add("Right Wiggle Target", () -> Calibration.ROTATE_WIGGLE_TARGET-Calibration.ROTATE_TOLERANCE < horizGyro.getAngle() && horizGyro.getAngle() < Calibration.ROTATE_WIGGLE_TARGET + Calibration.ROTATE_TOLERANCE);
            
            add("FALSE", () -> false);
            
            add("L3", () -> !started && horizGyro.getAngle() <= -4 );
            add("L2", () -> !started && horizGyro.getAngle() >= -4 && horizGyro.getAngle() <= -2 );
            add("L1", () -> !started && horizGyro.getAngle() >= -2 && horizGyro.getAngle() <= -1 );
            add("L0", () -> !started && horizGyro.getAngle() >= -1 && horizGyro.getAngle() <= -0.5 );
            add("CC", () -> !started && horizGyro.getAngle() >= -0.5 && horizGyro.getAngle() <= 0.5 );
            add("R0", () -> !started && horizGyro.getAngle() >= 0.5 && horizGyro.getAngle() <= 1 );
            add("R1", () -> !started && horizGyro.getAngle() >= 1 && horizGyro.getAngle() <= 2 );
            add("R2", () -> !started && horizGyro.getAngle() >= 2 && horizGyro.getAngle() <= 4 );
            add("R3", () -> !started && horizGyro.getAngle() >= 4);
            
        }});

        this.set(new ElasticController() {{
            addDefault("Off", new Action() {
            	public void begin (ActionData data) {
            		timer.reset();
            		timer.start();
            	}
            	
                public void run (ActionData data) {
                    drive.tankDrive(0, 0);
                }
                
                public void end (ActionData data) {
                	timer.stop();
                	timer.reset();
                }
            });
            
            add("Tank Drive", new Action(new FieldMap () {{
                define("Left Power", 0D);
                define("Right Power", 0D);
            }}) {
            	public void begin(ActionData data) {
            		started = true;
            	}
                public void run (ActionData data) {
                	drive.tankDrive(data.get("Left Power"), data.get("Right Power"));
                	leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
                }

                public void end (ActionData data) {
                    drive.stopMotor();
                }
            });
            
            add("Kinematic Drive", new Action(new FieldMap () {{
                define("Time", 3);
            }}) {
            	public void begin (ActionData data) {
            		timer.reset();
            		timer.start();
            	}
            	
                public void run (ActionData data) {
                	if (timer.get() < data.get("Time")) {
                		drive.tankDrive(0.881, 0.889);
                	}
                }

                public void end (ActionData data) {
                    drive.stopMotor();
                    
                    timer.stop();
                    timer.reset();
                }
            });
            
            add("Kinematic Rotate", new Action(new FieldMap () {{
                define("Power", 0D);
                define("Time", 0D);
            }}) {
            	public void begin (ActionData data) {
            		timer.reset();
            		timer.start();
            	}
            	
                public void run (ActionData data) {
                	if (timer.get() < data.get("Time")) {
                		drive.tankDrive(data.get("Power"), -data.get("Power"), false);
                	}
                }

                public void end (ActionData data) {
                    drive.stopMotor();
                    
                    timer.stop();
                    timer.reset();
                }
            });
            
            add("Arcade Drive", new Action(new FieldMap () {{
                define("Move Power", 0D);
                define("Rotate Power", 0D);
            }}) {
            	public void begin (ActionData data) {
            		started = true;
            	}
                public void run (ActionData data) {
                	drive.arcadeDrive(data.get("Move Power"), data.get("Rotate Power"));
                	leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
                }

                public void end (ActionData data) {
                    drive.stopMotor();
                }
            });

            /*
            add("Straight Drive", new Action(new FieldMap () {{
                define("Move Power", 0D);
            }}) {
                public void run (ActionData data) {
                    // double throttle = data.is("Throttled") ? 0.5 : 1;
//                	System.out.print("Move");
//                	System.out.println(data.get("Move Power"));
//                	System.out.print("Rotate");
//                	System.out.println(data.get("Rotate Power"));
                	drive.arcadeDrive(data.get("Move Power"), -horizGyro.getAngle());
                }

                public void end (ActionData data) {
                    drive.stopMotor();
                }
            });
            */
            
            add("Servo Move", new Action(new FieldMap() {{
                define("Clicks", 0D);
                define("Limit", Calibration.DRIVE_LEFT_PID_MAX);
            }}) {
                public void begin (ActionData data) {
                	/* Get current value */
                	pidMove.setOutputRange(-data.get("Limit"), data.get("Limit"));
                    encoderLeft.reset();
                    encoderRight.reset();
                    //horizGyro.reset();
                    
                    pidMove.setSetpoint(data.get("Clicks"));
                    pidMove.enable();
                    //pidRotate.setSetpoint(0);
                    //pidRotate.enable();
                }
                
                public void run (ActionData data){
                    if (pidMove.getSetpoint() != data.get("Clicks")) {
                        pidMove.reset();
                        
                        encoderLeft.reset();
                        encoderRight.reset();
                        
                        pidMove.setSetpoint(data.get("Clicks"));
                        pidMove.enable();
                    }
                    /*
                    if (pidRotate.getSetpoint() != 0) {
                        pidRotate.reset();
                        
                        horizGyro.reset();
                        
                        pidRotate.setSetpoint(0);
                        pidRotate.enable();
                    }
                    */
                    leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
                }
                
                public void end (ActionData data) {
                    pidMove.reset();
                    pidMove.disable();
                    /* Should be normal default; pidMove does not have getter for output range */
                    pidMove.setOutputRange(-Calibration.DRIVE_LEFT_PID_MAX, Calibration.DRIVE_LEFT_PID_MAX);
                    //pidRotate.reset();
                }
            });
            
            add("Servo Move 2", new Action(new FieldMap() {{
                define("Clicks", 0D);
                define("Limit", Calibration.DRIVE_LEFT_PID_MAX);
            }}) {
                public void begin (ActionData data) {
                	/* Get current value */
                	pidMove2.setOutputRange(-data.get("Limit"), data.get("Limit"));
                    encoderLeft.reset();
                    encoderRight.reset();
                    //horizGyro.reset();
                    
                    pidMove2.setSetpoint(data.get("Clicks"));
                    pidMove2.enable();
                    //pidRotate.setSetpoint(0);
                    //pidRotate.enable();
                }
                
                public void run (ActionData data){
                    if (pidMove2.getSetpoint() != data.get("Clicks")) {
                        pidMove2.reset();
                        
                        encoderLeft.reset();
                        encoderRight.reset();
                        
                        pidMove2.setSetpoint(data.get("Clicks"));
                        pidMove2.enable();
                    }
                    /*
                    if (pidRotate.getSetpoint() != 0) {
                        pidRotate.reset();
                        
                        horizGyro.reset();
                        
                        pidRotate.setSetpoint(0);
                        pidRotate.enable();
                    }
                    */
                    leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
                }
                
                public void end (ActionData data) {
                    pidMove2.reset();
                    /* Should be normal default; pidMove does not have getter for output range */
                    pidMove2.setOutputRange(-Calibration.DRIVE_LEFT_PID_MAX, Calibration.DRIVE_LEFT_PID_MAX);
                    //pidRotate.reset();
                }
            });
            
            add("Servo Rotate", new Action(new FieldMap() {{
                define("Angle", 0D);
            }}) {
                public void begin (ActionData data) {
                    horizGyro.reset();
                    
                    pidRotate.setSetpoint(data.get("Angle"));
                    pidRotate.enable();
                }
                
                public void run (ActionData data){
                    System.out.println(pidRotate.getError());

                    if (pidRotate.getSetpoint() != data.get("Angle")) {
                        pidRotate.reset();
                        
                        horizGyro.reset();

                        pidRotate.setSetpoint(data.get("Angle"));
                        pidRotate.enable();
                    }
                    leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
                }
                
                public void end (ActionData data) {
                    pidRotate.reset();
                }
            });
            add("Calibrate", new Action() {
            	public void begin (ActionData data) {
            		reset = false;
            		encoderLeft.reset();
            		encoderRight.reset();
            		horizGyro.reset();
            	}
            	public void end (ActionData data) {
            		reset = true;
            	}
            });
            
            add("Manual Rotate Right", new Action(new FieldMap () {{
            	define("Power", 0D);
            }}) {
            	public void begin(ActionData data) {
            		timer.start();
            	}
            	public void run (ActionData data) {
            		if( timer.get() < 3 ) {
                		drive.arcadeDrive(0, -data.get("Power"));
            		} else if(timer.get() < 6) {
            			drive.tankDrive(0.7, 0.7);
            		} else {
            			drive.stopMotor();
            		}
            		leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
            	}
            	public void end(ActionData data) {
            		timer.stop();
            		timer.reset();
            		drive.stopMotor();
            	}
            });
            
            
            
//            add("Macro Right", new Action(new FieldMap () {{
//            	define("Power", 0D);
//            	define("Clicks", 0D);
//                define("Limit", Calibration.DRIVE_LEFT_PID_MAX);
//            }}) {
//            	public void begin(ActionData data) {
//            		System.err.println("Start Start Macro Right");
//            		timer.start();
//            		pidMove.setOutputRange(-data.get("Limit"), data.get("Limit"));
//                    encoderLeft.reset();
//                    encoderRight.reset();
//                    //horizGyro.reset();
//                    
//                    pidMove.setSetpoint(data.get("Clicks"));
//                    //pidMove.enable();
//                    System.err.println("End Start Macro Right");
//            	}
//            	public void run (ActionData data) {
//            		boolean doneTime=false;
//            		boolean printed=false;
//            		if( timer.get() < 3 ) {
//            			if (!printed) {
//            				System.err.println("Timer section");
//            				printed=true;
//            			}
//            			if( horizGyro.getAngle() < 53 ) {
//            				drive.arcadeDrive(0, -data.get("Power"));
//            			}
//            		} else {
//            			doneTime=true;
//            			if (printed) {
//            				System.err.println("PID section");
//            				pidMove.enable();
//            				printed=false;
//            			}
//            			if (pidMove.getSetpoint() != data.get("Clicks")) {
//                            pidMove.reset();
//                            
//                            encoderLeft.reset();
//                            encoderRight.reset();
//                            
//                            pidMove.setSetpoint(data.get("Clicks"));
//                            pidMove.enable();
//                        }
//            		}
//            	}
//            	public void end(ActionData data) {
//            		System.err.println("Start End Macro Right");
//            		timer.stop();
//            		timer.reset();
//            		pidMove.reset();
//                    pidMove.disable();
//                    /* Should be normal default; pidMove does not have getter for output range */
//                    pidMove.setOutputRange(-Calibration.DRIVE_LEFT_PID_MAX, Calibration.DRIVE_LEFT_PID_MAX);
//                    //pidRotate.reset();
//                    System.err.println("End End Macro Right");
//            	}
//            });
            
            add("Macro Right", new Action(new FieldMap () {{
            }}) {
            	public void begin(ActionData data) {
            		timer.start();
            	}
            	public void run (ActionData data) {
            		if( timer.get() < 3 && horizGyro.getAngle() < 53 ) {
                		drive.arcadeDrive(0, -data.get("Power"));
            		} else if( timer.get() < 4.5 ) {
            			drive.tankDrive(0, 0);
            		} else if( timer.get() < 8 ) {
            			drive.tankDrive(0.881, 0.889);
            		} else {
            			drive.stopMotor();
            		}
            		leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
            	}
            	public void end(ActionData data) {
            		timer.stop();
            		timer.reset();
            	}
            });
            
            add("Macro Left", new Action(new FieldMap () {{
            }}) {
            	public void begin(ActionData data) {
            		timer.start();
            	}
            	public void run (ActionData data) {
            		if( timer.get() < 3 && horizGyro.getAngle() > -50 ) {
                		drive.arcadeDrive(0, data.get("Power"));
            		} else if( timer.get() < 4.5 ) {
            			drive.tankDrive(0, 0);
            		} else if( timer.get() < 8 ) {
            			drive.tankDrive(0.881, 0.889);
            		} else {
            			drive.stopMotor();
            		}
            		leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
            	}
            	public void end(ActionData data) {
            		timer.stop();
            		timer.reset();
            		drive.stopMotor();
            	}
            });
            
            add("Manual Rotate Left", new Action(new FieldMap () {{
            	define("Power", 0D);
            }}) {
            	// begin: calibrate gyro
            	public void begin(ActionData data) {
            		timer.start();
            	}
            	public void run (ActionData data) {
            		if( timer.get() < 3 ) {
            			drive.arcadeDrive(0, data.get("Power"));
            		} else if( timer.get() < 6 )  {
            			drive.tankDrive(0.7, 0.7);
            		} else {
            			drive.stopMotor();
            		}
            		leftAccel = encoderLeft.getRate() - lastLeftRate;
                	rightAccel = encoderRight.getRate() - lastRightRate;
                	lastLeftRate = encoderLeft.getRate();
                	lastRightRate = encoderRight.getRate();
            	}
            	public void end(ActionData data) {
            		timer.stop();
            		timer.reset();
            		drive.stopMotor();
            	}
            });
            
            add("Ultra Oscil", new Action(new FieldMap() {{
                define("inches", 0D);
            }}) {
                public void run (ActionData data){
                    if(ultra.inRange()) {
	                	double displacement = ultra.getDistance(1) - data.get("inches");
	                	double distance = Math.abs(displacement);
	                	
	                	double power = 0;
	                	if (distance > 48) {
	                	    power = 0.5;
	                	} else if (distance > 12) {
	                		power = 0.4;
	                	} else if (distance > 6) {
	                		power = 0.3;
	                	} else if (distance > 3) {
	                		power = 0.25;
	                	} else if (distance > 1) {
	                		power = 0.2;
	                	}
	                	
	                	power *= Math.signum(displacement);
	                	
	                	if (power != 0) {
	                	    drive.tankDrive(power, power, false);
	                	} else {
	                	    drive.stopMotor();
	                	}
                    } else {
                    	drive.stopMotor();
                    }
                }
                
                public void end (ActionData data) {
                	drive.stopMotor();
                }
            });
            
            add("Ultra Align", new Action() {
                public void run (ActionData data){
                    if(ultra.inRange()) {
	                	double difference = ultra.getDifference(1);
	                	if (difference < -1) {
	                		drive.tankDrive(-0.15, 0.15, false);
	                	} else if (difference > 1) {
	                		drive.tankDrive(0.15, -0.15, false);
	                	} else {
	                		drive.stopMotor();
	                	}
                    } else {
                    	drive.tankDrive(-0.15, 0.15, false);
                    }
                }
                
                public void end (ActionData data) {
                	drive.stopMotor();
                }
            });
            
            add("Ultra Align Right", new Action() {
                public void run (ActionData data){
                    if(ultra.inRange()) {
	                	double difference = ultra.getDifference(1);
	                	if (difference < -1) {
	                		drive.tankDrive(-0.15, 0.15, false);
	                	} else if (difference > 1) {
	                		drive.tankDrive(0.15, -0.15, false);
	                	} else {
	                		drive.stopMotor();
	                	}
                    } else {
                    	drive.tankDrive(-0.15, 0.15, false);
                    }
                }
                
                public void end (ActionData data) {
                	drive.stopMotor();
                }
            });
            add("Ultra Align Left", new Action() {
                public void run (ActionData data){
                    if(ultra.inRange()) {
	                	double difference = ultra.getDifference(1);
	                	if (difference < -1) {
	                		drive.tankDrive(-0.15, 0.15, false);
	                	} else if (difference > 1) {
	                		drive.tankDrive(0.15, -0.15, false);
	                	} else {
	                		drive.stopMotor();
	                	}
                    } else {
                    	drive.tankDrive(0.15, -0.15, false);
                    }
                }
                
                public void end (ActionData data) {
                	drive.stopMotor();
                }
            });
            
            add("Ultra Drive", new Action(new FieldMap() {{
            	define("Move power", 0D);
            	define("Rotate power", 0D);
            }}) {
            	public void run(ActionData data){
            		double adjust = 0;
            		boolean right = false;
            		if( ultra.inRange() ) {
            			double difference = ultra.getDifference();
            			if( difference < 0 || !ultra.inRange() ) {
            				right = true;
            			}
            			if( Math.abs(difference)> 1 / Math.E  )
            			{
            				adjust = Math.log(Math.abs(difference)*Math.E) * 0.1;
                			adjust /= 2;
                			if( adjust > 0.5 ) {
                				adjust = 0.5;
                			}
                			else if( adjust < -0.5 ) {
                				adjust = -0.5;
                			}
            			}
            		}
            		if( right ) {
            			adjust *=-1;
            		}
            		drive.tankDrive(data.get("Move Power"), data.get("Rotate Power")+adjust);
            	}
            	public void end(ActionData data){
            		drive.stopMotor();
            	}
            });
            
            add("Ultra Straight 2", new Action(new FieldMap() {{
                define("inches", 0D);
            }}) {
                public void run (ActionData data){
                    if(ultra.inRange()) {
	                	double leftDisplacement = ultra.getLeftDistance(1) - data.get("inches");
	                	double leftDistance = Math.abs(leftDisplacement);
	                	
	                	double leftPower = 0;
	                	if (leftDistance > 12) {
	                		leftPower = 0.5;
	               		} else if (leftDistance > 6) {
	               			leftPower = 0.4;
	               		} else if (leftDistance > 3) {
	               			leftPower = 0.3;
	               		} else if (leftDistance > 1) {
	               			leftPower = 0.25;
	               		} else if (leftDistance > 0.5) {
	               			leftPower = 0.2;
	               		}
	                	
	                	leftPower *= Math.signum(leftDisplacement);
	                	
	                	double rightDisplacement = ultra.getRightDistance(1) - data.get("inches");
	                	double rightDistance = Math.abs(rightDisplacement);
	                	
	                	double rightPower = 0;
	                	if (rightDistance > 12) {
	                		rightPower = 0.5;
	               		} else if (rightDistance > 6) {
	               			rightPower = 0.4;
	               		} else if (rightDistance > 3) {
	               			rightPower = 0.3;
	               		} else if (rightDistance > 1) {
	               			rightPower = 0.25;
	               		} else if (rightDistance > 0.5) {
	               			rightPower = 0.2;
	               		}
	                	
	                	rightPower *= Math.signum(rightDisplacement);
	                	
	                	if(leftPower != 0 && rightPower != 0) {
	                		drive.tankDrive(leftPower, rightPower, false);
	                	} else {
	                		drive.stopMotor();
	                	}
                    }    
                }
                
                public void end (ActionData data) {
                	drive.stopMotor();
                }
            });
        }});
    }
}