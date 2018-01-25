package com.ociweb;


import com.ociweb.iot.astropi.AstroPiTwig.*;
import com.ociweb.iot.maker.*;
import static com.ociweb.iot.maker.Port.*;

public class Project008 implements FogApp
{
    ///////////////////////
    //Connection constants
    ///////////////////////


    @Override
    public void declareConnections(Hardware hardware) {
        ////////////////////////////
        //Connection specifications
        ///////////////////////////
        hardware.connect(AstroPi.GetAccel); // Roll and Pitch
        hardware.connect(AstroPi.GetGyro);  // Heading
        hardware.setTimerPulseRate(50);     // Data fusion via timer

        
    }


    @Override
    public void declareBehavior(FogRuntime runtime) {
        //////////////////////////////
        //Specify the desired behavior
        //////////////////////////////


    }
          
}
