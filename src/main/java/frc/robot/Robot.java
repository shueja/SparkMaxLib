// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.lib.sparkmax.SparkMaxConfig;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  static SparkMaxConfig config = new SparkMaxConfig(c->{
    c.absEncoder.zeroOffset = 0.5;
    c.absEncoder.positionConversionFactor = 6.28;
    c.stallLimit = 60;
    c.isInverted = true;
  });
  static SparkMaxConfig followerConfig = config.copy(c->{
    c.followerID = 0;
    c.followerInvert = true;
  });
  CANSparkMax spark = config.create(0, MotorType.kBrushless, true);
  CANSparkMax sparkLimited = followerConfig.create(1, MotorType.kBrushless, true);


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    config.clone();
    config.apply(null, isAutonomous());
  }

  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {}

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}
