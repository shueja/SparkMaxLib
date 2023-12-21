package frc.robot.lib.sparkmax;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.REVLibError;
import com.revrobotics.RelativeEncoder;

class RelativeEncoderConfig extends Config<RelativeEncoder, RelativeEncoderConfig> {
    public int countsPerRev = 42;
    public int averageDepth = 8;
    public boolean inverted;
    public int measurementPeriod = 200;
    public double positionConversionFactor = 1;
    public double velocityConversionFactor = 1;
    public final static List<Call<RelativeEncoder, ?, RelativeEncoderConfig>> calls = List.of(
        call(
            RelativeEncoder::setInverted,
            c->c.inverted
        ),
        call(
            RelativeEncoder::setPositionConversionFactor,
            c->c.positionConversionFactor
        ),
        call(
            RelativeEncoder::setVelocityConversionFactor,
            c->c.velocityConversionFactor
        ),
        call(
            RelativeEncoder::setMeasurementPeriod,
            c->c.measurementPeriod
        ),
        call(
            RelativeEncoder::setAverageDepth,
            c->c.averageDepth
        )
    );
    }
