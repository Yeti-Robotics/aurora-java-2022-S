package frc.robot.utils;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.Constants;
import frc.robot.subsystems.Drivetrain.DrivetrainSubsystem;
import frc.robot.subsystems.VisionSubsystem.VisionSubsystem;

public class MoveAndShootController {
    private final DrivetrainSubsystem drivetrainSubsystem;

    private Pose2d targetPose;
    private Pose2d originPose;

    private ChassisSpeeds chassisSpeeds;
    private Pose2d targetOffset;
    private final Rotation2d zeroRotation = new Rotation2d(0.0);

    private double vectorMagnitude;

    public MoveAndShootController(DrivetrainSubsystem drivetrainSubsystem) {
        this.drivetrainSubsystem = drivetrainSubsystem;

        targetPose = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
        originPose = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(0.0));
    }

    private void updateTargetLocation() {
        chassisSpeeds = drivetrainSubsystem.getChassisSpeeds();
        targetPose = originPose.relativeTo(drivetrainSubsystem.getPose());

        if (targetPose.getX() > 0) {
            chassisSpeeds.vxMetersPerSecond = -chassisSpeeds.vxMetersPerSecond;
            chassisSpeeds.vyMetersPerSecond = -chassisSpeeds.vyMetersPerSecond;
        }

        double vectorMagnitude = Math.sqrt(Math.pow(chassisSpeeds.vyMetersPerSecond, 2)
                + Math.pow(chassisSpeeds.vyMetersPerSecond, 2));
    }

    public double calculateShooterSpeed() {
        if (targetPose.getX() > 0.2) {
            return 0.0;
        }
        updateTargetLocation();

        return vectorMagnitude * 1.0;
    }

    public double calculateAngleOffset() {
        if (targetPose.getY() > 0.2) {
            return 0.0;
        }
        updateTargetLocation();

        return Math.toDegrees(Math.atan(vectorMagnitude / VisionSubsystem.getDistance()));
    }
}
