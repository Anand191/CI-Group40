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
        BufferedWriter bw=null;
        try {
            bw = new BufferedWriter(new FileWriter("/home/anand/UvA/Period 2/data.txt", true));
        }catch (IOException ioe) {
            ioe.printStackTrace();
            niter=0;
        }

        outfile = bw;
        neuralNetwork = new NeuralNetwork(12, 8, 2);
        outfile=bw;
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

            action.steering = turn*6/7.0D + DriversUtils.alignToTrackAxis(sensors, 0.5)/7.0D;

        maxspeed = neuralNetwork.getOutput(sensors);
        System.out.println(maxspeed);

        action.accelerate = 1.0;
        action.brake = 0.0D;

        maxspeed = Math.max(30.0D,maxspeed);

        if (sensors.getSpeed()>maxspeed){
            action.accelerate = 0.0D;
            action.brake = 1.0D;
        }

        /*double[][] s = new double[19][2];

        double maxR=0.0D;
        double maxL = 0.0D;
        int idxL = 0, idxR = 0;

        for(int i=0; i<19; i++){
            s[i][0] = -Math.cos(Math.PI*i/18)*tracksensor[i];
            s[i][1] = Math.sin(Math.PI*i/18)*tracksensor[i];
            if (tracksensor[i]>maxL){
                maxL = tracksensor[i];
                idxL = i;
            }
            if (tracksensor[18-i] > maxR){
                maxR = tracksensor[i];
                idxR = i;
            }
        }

        double[][] r = new double[19][2];
        double[][] l = new double[19][2];
        double norm;

        for(int i=0; i<idxL; i++){
            norm = Math.pow(Math.pow(s[i+1][0]-s[i][0], 2.0D)+ Math.pow(s[i+1][1]-s[i][1], 2.0D), 0.5D);
            r[i][0] = (s[i+1][0] - s[i][0]) / norm;
            r[i][1] = (s[i+1][1] - s[i][1]) / norm;

        }

        for(int i=0; i<18-idxR; i++){
            norm = Math.pow(Math.pow(s[17-i][0]-s[18-i][0], 2.0D)+ Math.pow(s[17-i][1]-s[18-i][1], 2.0D), 0.5D);
            l[i][0] = (s[17-i][0] - s[18-i][0]) / norm;
            l[i][1] = (s[17-i][1] - s[18-i][1]) / norm;
        }

        double rho = 0.0D;
        double dot;
        for (int i=0; i<idxL; i++){
            dot = l[i][0]*l[i+1][0] + l[i][1]*l[i+1][1];
            rho += Math.acos(dot)*180/Math.PI * Math.signum(l[i][0]*l[i+1][1] - l[i][1]*l[i+1][0]);
        }
        for (int i=0; i < 17-idxR; i++){
            dot = r[i][0]*r[i+1][0] + r[i][1]*r[i+1][1];
            rho += Math.acos(dot)*180/Math.PI * Math.signum(r[i][0]*r[i+1][1] - r[i][1]*r[i+1][0]);
        }
        System.out.println(rho);
        double aR = Math.abs(rho);
        double lambda0, lambda1;
        if (aR < 3){
            lambda0 = 250.0D;
            lambda1 = -1.0D;
        }else if(aR < 25){
            lambda0 = 180.0D;
            lambda1 = -1.0D;
        }else if(aR < 35){
            lambda0 = 140.0D;
            lambda1 = -1.0D;
        }else if(aR < 45){
            lambda0 = 100.0D;
            lambda1 = -1.0D;
        } else if(aR < 55){
            lambda0 = 75.0D;
            lambda1 = -1.0D;
            turn *= 2;
        } else{
            lambda0 = 60.0D;
            lambda1 = -1.0D;
            turn *= 3;
        }
        double a = lambda1*sensors.getSpeed() + lambda0;
        action.accelerate = Math.max(a, 0.0D);
        action.brake = Math.max(-a, 0.0D);

        if (tracksensor[0] < 1.5){
            turn -= 0.05;
        }
        else if (tracksensor[18] < 1.5){
            turn += 0.05;

        }
        action.steering = 3*turn/5.0D + 2*DriversUtils.alignToTrackAxis(sensors, 0.5)/5.0D;


        System.out.println(sensors.getZSpeed());
        System.out.println(sensors.getSpeed());

        if (sensors.getZSpeed()/sensors.getSpeed() > 0.01D){
            if (sensors.getSpeed()> 150){
                action.accelerate = 0.0D;
                action.brake = 0.8D;
            }
        }*/



        //double turn = (tracksensor[8] - tracksensor[10])/(tracksensor[10] + tracksensor[8]);





        /*double alpha0 = 1.0D;
        double alpha1 = 1.0D;
        double beta0 = 1.0D;
        double beta1 = 1.0D;
        double brakeThreshold = Math.pow(10,8);

        brakeThreshold  *= Math.pow(alpha0*sensors.getSpeed(), alpha1);

        double steepness = Math.exp( Math.max(tracksensor[maxI-1], tracksensor[maxI+1]) - max );
        brakeThreshold *= Math.pow(beta0/steepness , beta1);

        double theta = 10.0;
        double val;
        action.accelerate = 1.0;
        action.brake = 0.0D;
        val = Math.tanh(theta*(max - brakeThreshold));

        action.accelerate = Math.max(val, 0);
        action.brake = Math.max(0, -val);

        if (sensors.getSpeed() <= 60.0D) {
            action.accelerate = 1.0D;
            action.brake = 0.0D;
        }*/


        if(niter%100==0){

        System.out.println("--------------" + getDriverName() + "--------------");
        System.out.println("Steering: " + action.steering);
        System.out.println("Acceleration: " + action.accelerate);
        System.out.println("Brake: " + action.brake);
        System.out.println("-------------------"+niter+"--------+-------------");}

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
        }


        return action;
    }
}