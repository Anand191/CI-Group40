import java.io.*;

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.controller.Human;
import race.TorcsConfiguration;

public class DefaultDriverAlgorithm extends AbstractAlgorithm {

    private static final long serialVersionUID = 654963126362653L;
    int n=1;
    DefaultDriverGenome[] drivers = new DefaultDriverGenome[n];
    int[] results = new int[n];

    public Class<? extends Driver> getDriverClass() {
        return DefaultDriver.class;
    }

    public void run(boolean continue_from_checkpoint) {
        if (!continue_from_checkpoint) {
            //init NN
            DefaultDriverGenome genome = new DefaultDriverGenome();
            drivers[0] = genome;
            /*for(int i=0;i<n;i++)
                drivers[i] = genome;*/




            //Start a race
            DefaultRace race = new DefaultRace();
            race.setTrack("aalborg", "road");
            race.laps = 1;

            //for speedup set withGUI to false
            results = race.runRace(drivers, true);



            // Save genome/nn
            DriversUtils.storeGenome(drivers[0]);
            /*System.out.println(results[0]);
            DriversUtils.storeGenome(drivers[results[0]-1]);*/
        }
        // create a checkpoint this allows you to continue this run later
        DriversUtils.createCheckpoint(this);
        //DriversUtils.clearCheckpoint();
    }

    public static void main(String[] args) {

        //Set path to torcs.properties
        TorcsConfiguration.getInstance().initialize(new File("torcs.properties"));
        /*
		 *
		 * Start without arguments to run the algorithm
		 * Start with -continue to continue a previous run
		 * Start with -show to show the best found
		 * Start with -show-race to show a race with 10 copies of the best found
		 * Start with -human to race against the best found
		 *
		 */


        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
        DriversUtils.registerMemory(algorithm.getDriverClass());
        if (args.length > 0 && args[0].equals("-show")) {
            new DefaultRace().showBest();
        } else if (args.length > 0 && args[0].equals("-show-race")) {
            new DefaultRace().showBestRace();
        } else if (args.length > 0 && args[0].equals("-human")) {
            new DefaultRace().raceBest();
        } else if (args.length > 0 && args[0].equals("-continue")) {
            if (DriversUtils.hasCheckpoint()) {
                DriversUtils.loadCheckpoint().run(true);
            } else {
                algorithm.run();
            }
        } else {
            algorithm.run();
        }

    }

}