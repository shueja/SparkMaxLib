package frc.robot.lib.sparkmax;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revrobotics.CANSparkMax;
import com.revrobotics.REVLibError;
import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.SparkMaxAlternateEncoder;
import com.revrobotics.SparkMaxRelativeEncoder;
import com.revrobotics.CANSparkMax.SoftLimitDirection;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxAbsoluteEncoder.Type;

import edu.wpi.first.math.Pair;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;

public class SparkMaxConfig extends Config<CANSparkMax, SparkMaxConfig> {
    static SparkMaxConfig defaults = new SparkMaxConfig();
    static AbsoluteEncoderConfig absEncDefaults = new AbsoluteEncoderConfig();
    static RelativeEncoderConfig hallEncDefaults = new RelativeEncoderConfig();
    static RelativeEncoderConfig altEncDefaults = new RelativeEncoderConfig();
    static {
        altEncDefaults.averageDepth = 64;
        altEncDefaults.countsPerRev = 4096;
    }

    public boolean isInverted = false;
    public float forwardSoftLimit = 0;
    public boolean forwardSoftLimitEnabled = false;
    public float reverseSoftLimit = 0;
    public boolean reverseSoftLimitEnabled = false;
    public int stallLimit = 80;
    public int freeLimit = 20;
    public double nominalVoltage = 0;
    public boolean voltageCompensationEnabled = false;
    public int followerID = -1;
    public boolean followerInvert = false;

    public SparkMaxRelativeEncoder.Type encoderPortType = SparkMaxRelativeEncoder.Type.kNoSensor;
    public boolean alternateEncoderMode = false;
    public RelativeEncoderConfig encoderPortEncoder;
    public RelativeEncoderConfig altEncoder;
    public AbsoluteEncoderConfig absEncoder;
    static List<Call<CANSparkMax, ?, SparkMaxConfig>> calls = List.of(
                // Inversion
                call(
                        (s, inv) -> {
                            s.setInverted(inv);
                            return s.getLastError();
                        },
                        c -> c.isInverted),
                // Forward soft limit
                call(
                        (s, lim) -> s.setSoftLimit(SoftLimitDirection.kForward, lim),
                        c -> c.forwardSoftLimit),
                call(
                        (s, en) -> s.enableSoftLimit(SoftLimitDirection.kForward, en),
                        c -> c.forwardSoftLimitEnabled),
                // Backward soft limit
                call(
                        (s, lim) -> s.setSoftLimit(SoftLimitDirection.kReverse, lim),
                        c -> c.reverseSoftLimit),
                call(
                        (s, en) -> s.enableSoftLimit(SoftLimitDirection.kReverse, en),
                        c -> c.reverseSoftLimitEnabled),
                call(
                        (s, lims) -> s.setSmartCurrentLimit(lims.getFirst(), lims.getSecond()),
                        c -> new Pair<Integer, Integer>(c.stallLimit, c.freeLimit)),
                call(
                        (s, set) -> set.getSecond() ? s.enableVoltageCompensation(set.getFirst())
                                : s.disableVoltageCompensation(),
                        c -> new Pair<Double, Boolean>(c.nominalVoltage, c.voltageCompensationEnabled)),
                call(
                        (s, set) -> s.follow(
                                set.getFirst() < 0 ? CANSparkMax.ExternalFollower.kFollowerSparkMax
                                        : CANSparkMax.ExternalFollower.kFollowerDisabled,
                                set.getFirst(), set.getSecond()),
                        c -> new Pair<Integer, Boolean>(c.followerID, c.followerInvert))
        );

    public SparkMaxConfig() {
        encoderPortEncoder = hallEncDefaults.clone();
        altEncoder = altEncDefaults.clone();
        absEncoder = absEncDefaults.clone();
    }

    public SparkMaxConfig(Consumer<SparkMaxConfig> editDefaults) {
        this();
        editDefaults.accept(this);
    }

    public SparkMaxConfig copy(Consumer<SparkMaxConfig> editDefaults) {
        var clone = this.clone();
        editDefaults.accept(clone);
        return clone;
    }
    public SparkMaxConfig copy() {
        return this.clone();
    }

    @Override
    public SparkMaxConfig clone() {
        var newConfig = (SparkMaxConfig) super.clone();
        newConfig.encoderPortEncoder = encoderPortEncoder.clone();
        newConfig.altEncoder = altEncoder.clone();
        newConfig.absEncoder = absEncoder.clone();
        return newConfig;
    }

    /**
     * Applies a configuration to a provided CANSparkMax. 
     * 
     * The purpose of this is for compatibility with wrappers around CANSparkMax.
     * If you are not using a wrapper, consider create(int id, MotorType type, restoreFactoryDefaults)
     * IMPORTANT: The passed-in object should be unconfigured, 
     * and preferably constructed in this method's parameter list.
     * Otherwise, configurations applied here may fail, since some 
     * configurations (particularly around encoders, analog sensors,
     * and limit switches) can only be applied once.
     * 
     * 
     * @param s
     * @param restoreFactoryDefaults
     * @return
     */
    public CANSparkMax apply(CANSparkMax s, boolean restoreFactoryDefaults) {
        // TODO repeat until ok
        if (restoreFactoryDefaults)
            config(s::restoreFactoryDefaults);
        for (Call<CANSparkMax, ?, SparkMaxConfig> config : calls) {
            applyConfig(s, this, defaults, config, restoreFactoryDefaults);
        }
        // Configure the encoder port;
        if (encoderPortType != SparkMaxRelativeEncoder.Type.kNoSensor) {
            try {
                var encoder = s.getEncoder(encoderPortType, encoderPortEncoder.countsPerRev);
                encoderPortEncoder.apply(encoder, RelativeEncoderConfig.calls, hallEncDefaults, restoreFactoryDefaults);
            } catch (Exception e) {
                DriverStation.reportError(e.getMessage(), e.getStackTrace());
            }
            
        }
        // Configure alternate encoder mode;
        if (alternateEncoderMode) {
            try {
                var encoder = s.getAlternateEncoder(SparkMaxAlternateEncoder.Type.kQuadrature, altEncoder.countsPerRev);
                altEncoder.apply(encoder, RelativeEncoderConfig.calls, altEncDefaults, restoreFactoryDefaults);
            } catch (Exception e) {
                DriverStation.reportError(e.getMessage(), e.getStackTrace());
            }
            
        } else {
            // limit switches + absolute encoder only available in normal mode
            try {
                var encoder = s.getAbsoluteEncoder(Type.kDutyCycle);
                absEncoder.apply(encoder, AbsoluteEncoderConfig.calls, absEncDefaults, restoreFactoryDefaults);
            } catch (Exception e) {
                DriverStation.reportError(e.getMessage(), e.getStackTrace());
            }
        }
        return s;
    }

    public CANSparkMax create(int id, MotorType type, boolean restoreFactoryDefaults) {
        return apply(new CANSparkMax(id, type), restoreFactoryDefaults);
    }


    public static void config(Supplier<REVLibError> configCall) {
        try {
        int attempts = 0;
        REVLibError error = REVLibError.kOk;
        do {
            if (attempts > 0) {
                Timer.delay(0.010);
            }
            error = configCall.get();
            attempts++;
        } while ((error == REVLibError.kTimeout) && attempts <= 4);
        if (error != REVLibError.kOk) {
            // TODO log a failure
        }} catch (Exception e) {
            DriverStation.reportError(e.getMessage(), e.getStackTrace());
            DataLogManager.log(e.getMessage());
        }
    }

    /**
     * Returns whether or not the supplied configuration has any changes from this one
     * that could not be updated at runtime
     * @return
     */
    public boolean safeToUpdate(SparkMaxConfig newConfig) {
        return 
            encoderPortType == newConfig.encoderPortType &&
            encoderPortEncoder.countsPerRev == newConfig.encoderPortEncoder.countsPerRev &&
            alternateEncoderMode == newConfig.alternateEncoderMode &&
            (alternateEncoderMode ? (
                altEncoder.countsPerRev == newConfig.altEncoder.countsPerRev
            ) : (true
                // limit switches
            ))

        ;
    }
}
