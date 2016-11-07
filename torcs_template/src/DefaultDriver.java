import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.io.*;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork neuralNetwork;
    BufferedWriter outfile;
    int niter;

    public DefaultDriver() {
        initialize();
        BufferedWriter bw=null;
        try {
            bw = new BufferedWriter(new FileWriter("/home/luca/Documents/Programmi/Java/CI-Group40/torcs_template/data.txt", true));
        }catch (IOException ioe) {
            ioe.printStackTrace();
            niter=0;
        }

        outfile = bw;
        neuralNetwork = new NeuralNetwork(12, 8, 2);
        outfile=bw;
//        neuralNetwork = neuralNetwork.loadGenome();
    }

    private void initialize() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        this.enableExtras(new AutomatedRecovering());
        this.enableExtras(new ABS());
    }

    @Override
    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            DefaultDriverGenome myGenome = (DefaultDriverGenome) genome;
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }

    @Override
    public double getAcceleration(SensorModel sensors) {
        double[] sensorArray = new double[4];
        double output = neuralNetwork.getOutput(sensors);
        return 1;
    }

    @Override
    public double getSteering(SensorModel sensors) {
        Double output = neuralNetwork.getOutput(sensors);
        return 0.5;
    }

    @Override
    public String getDriverName() {
        return "Example Controller";
    }

    @Override
    public Action controlWarmUp(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlQualification(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlRace(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action defaultControl(Action action, SensorModel sensors) {
        if (action == null) {
            action = new Action();
        }
        niter+=1;
        action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);
        if (sensors.getSpeed() > 60.0D) {
            action.accelerate = 0.0D;
            action.brake = 0.0D;
        }

        if (sensors.getSpeed() > 70.0D) {
            action.accelerate = 0.0D;
            action.brake = -1.0D;
        }

        if (sensors.getSpeed() <= 60.0D) {
            action.accelerate = (80.0D - sensors.getSpeed()) / 80.0D;
            action.brake = 0.0D;
        }

        if (sensors.getSpeed() < 30.0D) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        }
        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("-------------------"+niter+"--------+-------------");

        try {
            //bw = new BufferedWriter(new FileWriter("/home/luca/Documents/Programmi/Java/CI-Group40/torcs_template/data.txt", true));
            outfile.write(Double.toString(action.accelerate));
            outfile.write(",");
            outfile.write(Double.toString(action.brake));
            outfile.write(",");
            outfile.write(Double.toString(action.steering));
            //bw.write(",");
            outfile.newLine();
            if (niter%1000==0)
                outfile.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        return action;
    }
}