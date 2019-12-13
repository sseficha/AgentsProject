import java.awt.*;
import java.io.*;
import java.util.ArrayList;

class Stats {

    private String filename;
    private ArrayList<Stat> stats;

    /**
     * The class implements one solution metrics.
     * It holds information about the number of the agents, the size of the map and the time to find the solution.
     */
    static class Stat {

        private int numberOfAgents;
        private String algorithm;
        private Point sizeOfMap;
        private int time;

        Stat (int numberOfAgents, String algorithm, Point sizeOfMap, int time) {
            this.numberOfAgents = numberOfAgents;
            this.algorithm = algorithm;
            this.sizeOfMap = sizeOfMap;
            this.time = time;
        }


        private boolean equals (Stat x) {

            return numberOfAgents == x.numberOfAgents
                   && algorithm.equals(x.algorithm)
                   && sizeOfMap == x.sizeOfMap;
        }


        @Override
        public String toString () {

            return numberOfAgents + ";" + algorithm + ";" + sizeOfMap.x + ";" + sizeOfMap.y + ";" + time;
        }

    }


    /**
     * It loads the previous stats from a file.
     * If there is no file with that name, it just holds the path of the file
     *
     * @param file The given file that wants to be loaded
     */
    Stats (String file) {

        stats = new ArrayList<>();
        filename = file;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(";");

                int numberOfAgents = Integer.parseInt(data[0]);
                String algorithm = data[1];
                int sizeOfMapX = Integer.parseInt(data[2]);
                int sizeOfMapY = Integer.parseInt(data[3]);
                int time = Integer.parseInt(data[4]);

                stats.add(new Stat(numberOfAgents, algorithm, new Point(sizeOfMapX, sizeOfMapY), time));
            }

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }


    /**
     * It puts a new stat at the ArrayList
     *
     * @param numberOfAgents The number of agents used
     * @param sizeOfMap      The size of the map used
     * @param time           The time consumed till solution be found
     */
    private void putStat (int numberOfAgents, String algorithm, Point sizeOfMap, int time) {

        stats.add(new Stat(numberOfAgents, algorithm, sizeOfMap, time));

    }


    /**
     * Saves the ArrayList with the stats at the file that class holds.
     */
    private void save () {

        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename))) {
            stats.forEach((i) -> {
                try {
                    out.write(i.toString() + "/n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            System.out.println("Something gone wrong. Please, rerun the program.");
            System.exit(10);
        }

    }


    /**
     * It calculates the mean time that the agents have done to find the solution at various situations.
     *
     * @return An ArrayList with all the mean values at every situation
     */
    private ArrayList<Stat> calculateStats () {

        ArrayList<Stat> metrics = new ArrayList<>();
        ArrayList<Integer> metricsCount = new ArrayList<>();

        stats.forEach(statInBase -> metrics.forEach(statToMetrics -> {
            if (statInBase.equals(statToMetrics)) {
                statToMetrics.time += statInBase.time;
                metricsCount.set(metrics.indexOf(statToMetrics), metricsCount.get(metrics.indexOf(statToMetrics) + 1));
            } else {
                metrics.add(statInBase);
                metricsCount.set(metrics.indexOf(statToMetrics), 1);
            }
        }));

        for (int i = 0; i < metrics.size(); i++) {
            metrics.get(i).time /= metricsCount.get(i);
        }

        return metrics;
    }

}
