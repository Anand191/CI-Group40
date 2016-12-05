import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;
import java.lang.Math;

import java.io.*;

public class DefaultDriver extends AbstractDriver {

    private NeuralNetwork neuralNetwork;
    BufferedWriter outfile;
    int niter;

    public DefaultDriver() {
        initialize();
        /*BufferedWriter bw=null;
        try {
            bw = new BufferedWriter(new FileWriter("/home/anand/UvA/Period 2/data.txt", true));
        }catch (IOException ioe) {
            ioe.printStackTrace();
            niter=0;
        }

        outfile = bw;*/
        neuralNetwork = new NeuralNetwork(12, 8, 2);
        //neuralNetwork.storeGenome();
        /*neuralNetwork = neuralNetwork.loadGenome();
        System.out.println("-------------- NN caricato ------------");
        for(int i=0;i<19;i++)
            neuralNetwork.weights[i]+=(Math.random()-0.5)/10;
        for(int i=0;i<19;i++)
            System.out.println(neuralNetwork.weights[i]);*/
    }

    private void initialize() {
        this.enableExtras(new AutomatedClutch());
        this.enableExtras(new AutomatedGearbox());
        this.enableExtras(new AutomatedRecovering());
        this.enableExtras(new ABS());
    }
    /*
    public void saveNetwork()
    {
        neuralNetwork.storeGenome();
    }*/

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
        //action.steering = DriversUtils.alignToTrackAxis(sensors, 0.5);

        double[] tracksensor = sensors.getTrackEdgeSensors();

        double max = 0.0;
        int maxI = 9;
        for(int i=0;i<19;i++){
            if (tracksensor[i]>max){
                max = tracksensor[i];
                maxI = i;
            }
        }
        double turn = (9 - maxI)/9.0;
        double maxspeed;

        if (tracksensor[0] < 1.5){
            turn -= 0.05;
        }
        else if (tracksensor[18] < 1.5){
            turn += 0.05;
        }


        maxspeed = neuralNetwork.getOutput(sensors);
        //System.out.println(maxspeed);

        action.accelerate = 1.0;
        action.brake = 0.0D;

        maxspeed = Math.max(30.0D, maxspeed);

        boolean TL = turn > 0;
        boolean TR = !TL;

        boolean flagFi = true;
        boolean flagLs = true;
        String position;
        for (int i = 0; i < 36; i++){
            if (i >= 9 & i<28){
                flagFi = flagFi & (sensors.getOpponentSensors()[i] == 200.0);
            }
            else {
                flagLs = flagLs & (sensors.getOpponentSensors()[i] == 200.0);
            }
        }
        if (!flagFi) {
            if (!flagLs) {
                position = "middle";
            }
            else{
                position = "last";
            }
        }
        else{
            position = "first";
        }

        boolean C1 = false;
        boolean R1 = false;
        boolean L1 = false;
        boolean R2 = false;
        boolean L2 = false;
        for (int i = 14; i < 23; i++) { //check for enemy close in front of you
            C1 = C1 | (sensors.getOpponentSensors()[i] < 10.0);
        }
        for (int i = 23; i < 27; i++) { // check for enemy close on you front-right
            R1 = R1 | (sensors.getOpponentSensors()[i] < 10.0);
        }
        for (int i = 10; i < 15; i++) { // check for enemy close on you front-left
            L1 = L1 | (sensors.getOpponentSensors()[i] < 10.0);
        }
        for (int i = 28; i < 33; i++) { // check for enemy close on you front-right
            R2 = R2 | (sensors.getOpponentSensors()[i] < 10.0);
        }
        for (int i = 4; i < 9; i++) { // check for enemy close on you front-left
            L2 = L2 | (sensors.getOpponentSensors()[i] < 10.0);
        }

        if (position == "first"){
            maxspeed *= 1.15D;
            action.steering = turn*6/7.0D + DriversUtils.alignToTrackAxis(sensors, 0.5)/7.0D;

        }
        else if (position == "middle") {
            action.steering = turn*9/10.0D + DriversUtils.alignToTrackAxis(sensors, 0.5)/10.0D;
            maxspeed *= 1.25D;
        }
        else if (position == "last"){
            maxspeed *= 1.4D;
            action.steering = turn;
        }

        /*if ((L1 & TL)|(R1&TR)){
            action.steering *= 0.8;
        }

        if ((L2 & TL) | (R2 & TR)) {
            action.steering *= 1.1D;
        }

        if ((L1 & TR)|(R1 & TL)){
            action.accelerate = 0.65;
        }*/

        if (sensors.getSpeed()>maxspeed | C1 ){
            action.accelerate = 0.0D;
            action.brake = 1.0D;
        }

        /*if(niter%10==0) {

        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("-------------------"+niter+"--------+-------------");}
            System.out.println("Rank:" + sensors.getRacePosition());
            for (int i = 0; i < 36; i++) {
                System.out.print(" " + sensors.getOpponentSensors()[i]);
            }
            System.out.println();
        }

        try {
            outfile.write(Double.toString(action.accelerate));
            outfile.write(", ");
            outfile.write(Double.toString(action.brake));
            outfile.write(", ");
            outfile.write(Double.toString(action.steering));
            outfile.write(", ");
            for (int i=0; i<19; i++) {
                outfile.write(Double.toString(tracksensor[i]));
                outfile.write(", ");
            }
            outfile.write(Double.toString(sensors.getZSpeed()));
            outfile.write(", ");
            outfile.write(Double.toString(sensors.getAngleToTrackAxis()));
            outfile.write(", ");
            outfile.write(Double.toString(sensors.getSpeed()));
            outfile.write(", ");
            outfile.write(Double.toString(sensors.getGear()));
            //bw.write(",");
            outfile.newLine();
            if (niter%1000==0)
                outfile.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }*/


        return action;
    }
}