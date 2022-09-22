package frc.robot.commands;

import com.sun.source.tree.SwitchTree;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.NeckSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

public class AllinCommand extends CommandBase {
    private final IntakeSubsystem intakeSubsystem;
    private final NeckSubsystem neckSubsystem;

    private Timer timer;

    public AllinCommand(IntakeSubsystem intakeSubsystem, NeckSubsystem neckSubsystem) {
        this.intakeSubsystem = intakeSubsystem;
        this.neckSubsystem = neckSubsystem;

        addRequirements(intakeSubsystem, neckSubsystem);
    }

    @Override
    public void initialize() {
        timer.start();
    }

    @Override
    public void execute() {
        if (ShooterSubsystem.getShooterMode() == ShooterSubsystem.ShooterMode.OFF) {
            intakeSubsystem.rollIn();

            if (!neckSubsystem.getHighBeamBreak() && !neckSubsystem.getLowBeamBreak()) {
                neckSubsystem.stopNeck();
                return;
            }
            if (!neckSubsystem.getHighBeamBreak()) {
                neckSubsystem.stopFrontMotor();
                return;
            }

            neckSubsystem.moveUp();
        } else {
            intakeSubsystem.rollIn();

            if (neckSubsystem.getHighBeamBreak()) {
                timer.reset();
            }
            if (!timer.hasElapsed(0.5)) {
                neckSubsystem.stopNeck();
            }

            neckSubsystem.moveUp();
        }
    }

    @Override
    public void end(boolean interrupted) {
        intakeSubsystem.stopRoll();
        neckSubsystem.stopNeck();
    }

}