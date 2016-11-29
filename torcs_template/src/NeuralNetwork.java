import scr.SensorModel;

import java.io.*;

public class NeuralNetwork implements Serializable {

    private static final long serialVersionUID = -88L;
    double w_0 [][] = new double[21][20];
    double b_0 [] = new double[20];
    double w_1[] = new double[20];
    double b_1 = 0.0;

    NeuralNetwork(int inputs, int hidden, int outputs) {
        System.out.println("-------------- bella zio costruisco un NN ------------");
        String path = "/home/anand/UvA/Period 2/Computational Intelligence/Codes/Simple_NN/";
        String files0 = path+"Weights_0.csv";
        String files1 = path+"Weights_1.csv";
        String files2 = path+"Weights_2.csv";
        String files3 = path+"Weights_3.csv";

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(files0));
            int i = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] w = line.split(cvsSplitBy);

                for (int j=0; j<w.length; j++){
                   w_0 [i][j] = Double.parseDouble(w[j]);
                }
                i++;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            br = new BufferedReader(new FileReader(files1));
            int i = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                b_0 [i] = Double.parseDouble(line.split(cvsSplitBy)[0]);
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            br = new BufferedReader(new FileReader(files2));
            int i =0;
            while ((line = br.readLine()) != null) {

                // use comma as separator

                w_1 [i] = Double.parseDouble(line.split(cvsSplitBy)[0]);
                i++;

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            br = new BufferedReader(new FileReader(files3));
            if ((line = br.readLine()) != null) {

                // use comma as separator
                    b_1  = Double.parseDouble(line.split(cvsSplitBy)[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



//        weights=new double[19];
//        out_steering=new double[2];
//        steering=new double[2][19];
//        for(int i=0;i<19;i++)
//        { steering[0][i]=0;
//        steering[1][i]=0 ;
//         weights[i]= 1/10.;}
    }

    public double getOutput(SensorModel a) {
        double[] tracksensor = a.getTrackEdgeSensors();
        double[] input = new double[21];
        for (int i=0;i<19;i++){
            input[i] = tracksensor[i];
        }
        input[19] = a.getZSpeed();
        input[20] = a.getAngleToTrackAxis();

        double[] res = new double[20];
        for(int i=0; i<21; i++){
            for (int j=0; j <20; j++){
                res[j] += input[i]*w_0[i][j];
            }
        }
        for (int i=0; i <20; i++){
           res[i] += b_0[i];
           res[i] = Math.max(0.0D, res[i]);
        }

        double res2 = 0;
        for (int i=0; i <20; i++){
            res2 += res[i]*w_1[i];
        }
        res2 += b_1;


        return res2;
    }


    public double getSteering(SensorModel a) {
        double[] tracksensor = a.getTrackEdgeSensors();
        double res=0;
        for(int i=0;i<19;i++){

        }
        return res;
    }

    //Store the state of this neural network
   /* public void storeGenome() {
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
    }*/

}
