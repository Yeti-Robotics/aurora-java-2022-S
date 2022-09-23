package frc.robot.subsystems.Drivetrain;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.StatorCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.sensors.WPI_CANCoder;
import com.swervedrivespecialties.swervelib.SdsModuleConfigurations;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import frc.robot.Constants.DriveConstants;

public class SwerveModule {
    private final WPI_TalonFX driveMotor;
    private final WPI_TalonFX steerMotor;

    private final WPI_CANCoder absoluteEncoder;
    private final boolean absoluteEncoderReversed;
    private final double absoluteEncoderOffsetRad;

    private final PIDController steeringPIDController;

    private SwerveModuleState state;

    public SwerveModule(
            int driveMotorID, int steerMotorID,
            boolean driveInverted,
            int absoluteEncoderID, boolean absoluteEncoderReversed, double absoluteEncoderOffsetRad) {

        this.absoluteEncoderReversed = absoluteEncoderReversed;
        this.absoluteEncoderOffsetRad = absoluteEncoderOffsetRad;
        absoluteEncoder = new WPI_CANCoder(absoluteEncoderID);

        driveMotor = new WPI_TalonFX(driveMotorID);
        steerMotor = new WPI_TalonFX(steerMotorID);

        driveMotor.setNeutralMode(NeutralMode.Brake);
        steerMotor.setNeutralMode(NeutralMode.Brake);

        driveMotor.setInverted(driveInverted);

        steerMotor.configStatorCurrentLimit(new StatorCurrentLimitConfiguration(true, 10, 15, 0.5));

        driveMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 0);
        steerMotor.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor, 0, 0);

        steeringPIDController = new PIDController(DriveConstants.STEER_MOTOR_P, 0.0, DriveConstants.STEER_MOTOR_D);
        steeringPIDController.enableContinuousInput(0.0, 2 * Math.PI);

        state = new SwerveModuleState();

        resetEncoders();
    }

    public void resetEncoders() {
        driveMotor.setSelectedSensorPosition(0);
        steerMotor.setSelectedSensorPosition(0);
    }

    public double getDrivePosition() {
        return driveMotor.getSelectedSensorPosition();
    }

    public double getSteerRad() {
        double steerRadians = Math.toRadians(absoluteEncoder.getAbsolutePosition()) - absoluteEncoderOffsetRad;

        if (steerRadians < 0) {
            return 2 * Math.PI + steerRadians;
        }

        return steerRadians;
    }

    public double getDriveVelocity() {
        return driveMotor.getSelectedSensorVelocity() * 10 / 2048
                * SdsModuleConfigurations.MK4_L2.getDriveReduction() *
                SdsModuleConfigurations.MK4_L2.getWheelDiameter() * Math.PI;
    }

    public double getSteerVelocity() {
        return steerMotor.getSelectedSensorVelocity() * 10;
    }

    public SwerveModuleState getState() {
        return new SwerveModuleState(getDriveVelocity(), new Rotation2d(getSteerRad()));
    }

    public void setDesiredState(SwerveModuleState desiredState) {
        if (Math.abs(desiredState.speedMetersPerSecond) < 0.001) {
            stop();
            return;
        }
        desiredState = SwerveModuleState.optimize(desiredState, getState().angle);

        driveMotor.setVoltage(desiredState.speedMetersPerSecond / DriveConstants.MAX_VELOCITY_METERS_PER_SECOND
         * DriveConstants.MAX_VOLTAGE);
        steerMotor.set(steeringPIDController.calculate(getSteerRad(), desiredState.angle.getRadians()));

        System.out.println(getSteerRad());
    }

    public void stop() {
        driveMotor.setVoltage(0.0);
        steerMotor.set(0.0);
    }
}
