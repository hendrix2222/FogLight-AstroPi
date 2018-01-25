package com.ociweb;

import com.ociweb.gl.api.ShutdownListener;
import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.astropi.IMUTransducer;
import com.ociweb.iot.astropi.listeners.AccelListener;
import com.ociweb.iot.astropi.listeners.MagListener;
import com.ociweb.iot.maker.FogCommandChannel;
import com.ociweb.iot.maker.FogRuntime;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class IMUBehavior implements StartupListener, ShutdownListener, AccelListener, MagListener, TimeListener {

    private FogCommandChannel ch;
    private IMUTransducer sensor;

    private double accVec[] = {0, 0, 0};
    private double magVec[] = {0, 0, 0};

    private double tempAccVec[] = {0, 0, 0};
    private double tempMagVec[] = {0, 0, 0};

    private ReentrantReadWriteLock accLock;
    private ReentrantReadWriteLock magLock;

    public IMUBehavior(FogRuntime runtime) {
        // Data protection locks
        accLock = new ReentrantReadWriteLock();
        magLock = new ReentrantReadWriteLock();

        // Create channel for communication
        this.ch = runtime.newCommandChannel();
        // Create transducer and give it our command channel so we can get data from it
        // Use this class as the listeners for data values
        sensor = new IMUTransducer(ch, this);
    }

    @java.lang.Override
    public boolean acceptShutdown() {
        // This will eventually be used to shut off and reset the LED array
        return false;
    }

    @java.lang.Override
    public void startup() {
        // We will eventually use this to set the startup display on the LED array
    }

    @java.lang.Override
    public void accelerationValues(double v, double v1, double v2) {
        // Read raw values in a thread-safe way here
        accLock.writeLock().lock();

        try{
            accVec[0] = v;
            accVec[1] = v1;
            accVec[2] = v2;
        }
        finally {
            accLock.writeLock().unlock();
        }
    }

    @java.lang.Override
    public void magneticValues(double v, double v1, double v2) {
        // Read raw values in a thread-safe way here
        magLock.writeLock().lock();

        try{
            magVec[0] = v;
            magVec[1] = v1;
            magVec[2] = v2;
        }
        finally {
            magLock.writeLock().unlock();
        }
    }

    @java.lang.Override
    public void timeEvent(long l, int i) {

        // Check for an exit condition

        // Do the data fusion and vector normalization here
        accLock.readLock().lock();
        try{
            System.arraycopy(accVec, 0, tempAccVec, 0, accVec.length);
        }
        finally {
            accLock.readLock().unlock();
        }

        magLock.readLock().lock();
        try{
            System.arraycopy(magVec, 0, tempMagVec, 0, magVec.length);
        }
        finally {
            magLock.readLock().unlock();
        }

        // Calc Roll and Pitch (degrees)

        // Normalize raw acc data
        double normAccX = tempAccVec[0] / Math.sqrt(tempAccVec[0] * tempAccVec[0] + tempAccVec[1] * tempAccVec[1] + tempAccVec[2] * tempAccVec[2]);
        double normAccY = tempAccVec[1] / Math.sqrt(tempAccVec[0] * tempAccVec[0] + tempAccVec[1] * tempAccVec[1] + tempAccVec[2] * tempAccVec[2]);

        // Roll and Pitch (radians)
        double Pitch = Math.asin(normAccX);
        double Roll = -Math.asin(normAccY / Math.cos(Pitch));

        // Calc Heading (degrees)

        // Compensate mag readings for tilt
        double magCompX = tempMagVec[0] * Math.cos(Pitch) + tempMagVec[2] * Math.sin(Pitch);
        double magCompY = tempMagVec[0] * Math.sin(Roll) * Math.sin(Pitch) + tempMagVec[1] * Math.cos(Roll) - tempMagVec[2] * Math.sin(Roll) * Math.cos(Pitch);

        double Heading = 180 * Math.atan2(magCompY, magCompX) / Math.PI;

        // Eventually we will update the dispaly as well
    }
}
