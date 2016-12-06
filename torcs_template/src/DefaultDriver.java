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
        return "BrumBrum40";
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

        if (tracksensor[0] < 1.5){
            turn -= 0.05;
        }
        else if (tracksensor[18] < 1.5){
            turn += 0.05;
        }

        double maxspeed = neuralNetwork.getOutput(sensors);

        action.accelerate = 1.0;
        action.brake = 0.0D;

        maxspeed = Math.max(30.0D, maxspeed);

        boolean TL = turn > 0.1;
        boolean TR = turn < -0.1;

        double mC1 = 200.0D;
        double mC2 = 200.0D;
        double mR1 = 200.0D;
        double mR2 = 200.0D;
        double mL1 = 200.0D;
        double mL2 = 200.0D;
        for (int i = 14; i < 23; i++) { //check for enemy close in front of you
            mC1 = Math.min(mC1, sensors.getOpponentSensors()[i]);
        }
        for (int i = 23; i < 27; i++) { // check for enemy close on you front-right
            mR1 = Math.min(mR1, sensors.getOpponentSensors()[i]);
        }
        for (int i = 10; i < 15; i++) { // check for enemy close on you front-left
            mL1 = Math.min(mL1, sensors.getOpponentSensors()[i]);
        }
        for (int i = 28; i < 33; i++) { // check for enemy close on you back-right
            mR2 = Math.min(mR2, sensors.getOpponentSensors()[i]);
        }
        for (int i = 4; i < 9; i++) { // check for enemy close on you back-left
            mL2 = Math.min(mL2, sensors.getOpponentSensors()[i]);
        }
        for (int i = 0; i < 4; i++) { // check for enemy close on you back-left
            mC2 = Math.min(mC2, sensors.getOpponentSensors()[i]);
        }
        for (int i = 33; i < 36; i++) { // check for enemy close on you back-left
            mC2 = Math.min(mC2, sensors.getOpponentSensors()[i]);
        }

        boolean flagFi = Math.min(Math.min(mR1, mL1), mC1) == 200;
        boolean flagLs = Math.min(Math.min(mR2, mL2), mC2) == 200;
        String position;
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

        boolean C1 = mC1<10;
        boolean R1 = mR1<10;
        boolean L1 = mL1<10;
        boolean C2 = mC2<10;
        boolean R2 = mR2<10;
        boolean L2 = mL2<10;


        if ((L1 & TL)|(R1&TR)){ // give berth
            action.steering *= 0.9;
        }
        if ((L2 & TL) | (R2 & TR)) { // close
            action.steering *= 1.25D;
        }
        if ((L1 & TR)|(R1 & TL)){ // slow down
            action.accelerate = 0.5D;
        }
        if (((mL2<35) & !(TR|TL))){ // get in front (left)
            action.steering += 0.075D;
        }
        if (((mR2<35) & !(TR|TL))){ // get in front (right)
            action.steering -= 0.075D;
        }

        if (sensors.getSpeed()>maxspeed | C1 ){ //brake if you go too fast | brake if you are hitting someone
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