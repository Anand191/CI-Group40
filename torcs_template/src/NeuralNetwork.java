import scr.SensorModel;

import java.io.*;

public class NeuralNetwork implements Serializable {

    private static final long serialVersionUID = -88L;
    double[] weights;
    double[][] steering;
    double[] out_steering;

    NeuralNetwork(int inputs, int hidden, int outputs) {
        System.out.println("-------------- bella zio costruisco un NN ------------");
        weights=new double[19];
        out_steering=new double[2];
        steering=new double[2][19];
        for(int i=0;i<19;i++)
        { steering[0][i]=0;
        steering[1][i]=0 ;
         weights[i]= 1/10.;}
    }

    public double getOutput(SensorModel a) {
        double[] tracksensor = a.getTrackEdgeSensors();
        double res=0;
        for(int i=0;i<19;i++){
            res+=tracksensor[i]*weights[i];
        }
        return res;
    }
    public double getSteering(SensorModel a) {
        double[] tracksensor = a.getTrackEdgeSensors();
        double res=0;
        for(int i=0;i<19;i++){
            res+=tracksensor[i]*steering[0][i];
        }
        return res;
    }

    //Store the state of this neural network
    public void storeGenome() {
        ObjectOutputStream out = null;
        try {
            //create the memory folder manually
            out = new ObjectOutputStream(new FileOutputStream("./out/production/memory/mydriver.mem"));
            System.out.println("-------------- bella zio salvo un NN ------------");
            for(int i=0;i<19;i++)
                System.out.println(weights[i]);


    } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.writeObject(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load a neural network from memory
    public NeuralNetwork loadGenome() {

        // Read from disk using FileInputStream
        FileInputStream f_in = null;
        try {
            f_in = new FileInputStream("./out/production/memory/mydriver.mem");
            System.out.println("-------------- bella zio carico un NN ------------");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Read object using ObjectInputStream
        ObjectInputStream obj_in = null;
        try {
            obj_in = new ObjectInputStream(f_in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read an object

        try {
            if (obj_in != null) {
                return (NeuralNetwork) obj_in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
