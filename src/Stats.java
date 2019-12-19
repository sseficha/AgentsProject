import java.awt.*;
import java.io.*;
import java.util.ArrayList;

class Stats {

    private ArrayList<Stat> stats;
    private String filename;
    private double startTime;

    /**
     * The class implements one solution metrics.
     * It holds information about the number of the agents, the size of the map and the time to find the solution.
     */
    static class Stat {

        private int numberOfAgents;
        private String algorithm;
        private int sight;
        private Point sizeOfMap;
        private double time;

        Stat (int numberOfAgents, String algorithm, int sight, Point sizeOfMap, double time) {
            this.numberOfAgents = numberOfAgents;
            this.algorithm = algorithm;
            this.sight = sight;
            this.sizeOfMap = sizeOfMap;
            this.time = time;
        }


        public String toFile () {
            return numberOfAgents + ";" + algorithm + ";" + sight + ";" + sizeOfMap.x + ";" + sizeOfMap.y + ";" + time + System.lineSeparator();
        }


        private boolean equals (Stat x) {

            return numberOfAgents == x.numberOfAgents
                   && algorithm.equals(x.algorithm)
                   && sight == x.sight
                   && sizeOfMap.equals(x.sizeOfMap);
        }


        @Override
        public String toString () {

            return "Agents: " + numberOfAgents + ", Algorithm: " + algorithm + ", Sight: " + sight + ", Size: " + sizeOfMap.x + "*" + sizeOfMap.y + ", Time: " + time + " seconds." + System.lineSeparator();
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
                int sight = Integer.parseInt(data[2]);
                int sizeOfMapX = Integer.parseInt(data[3]);
                int sizeOfMapY = Integer.parseInt(data[4]);
                double time = Double.parseDouble(data[5]);

                stats.add(new Stat(numberOfAgents, algorithm, sight, new Point(sizeOfMapX, sizeOfMapY), time));
            }

        } catch (IOException ignored) {
        }

        startTime = System.nanoTime();
    }


    /**
     * It puts a new stat at the ArrayList.
     *
     * @param numberOfAgents The number of agents used
     * @param sight          The vision range of the agents
     * @param sizeOfMap      The size of the map used
     * @param nanoTime       The current time after the solution has found
     */
    public void putStat (int numberOfAgents, String algorithm, int sight, Point sizeOfMap, long nanoTime, int ticker) {

        double timeElapsed = ((nanoTime - startTime) / (ticker * Math.pow(10, 9)));

        stats.add(new Stat(numberOfAgents, algorithm, sight, sizeOfMap, timeElapsed));
    }


    /**
     * Saves the ArrayList with the stats at the file that class holds.
     */
    public void save () {

        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename))) {
            stats.forEach((i) -> {
                try {
                    out.write(i.toFile());
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
    public ArrayList<Stat> calculateStats () {

        ArrayList<Stat> metrics = new ArrayList<>();
        ArrayList<Integer> metricsCount = new ArrayList<>();

        for (Stat stat : stats) {

            int index = -1;
            for (int i = 0; i < metrics.size(); i++) {
                if (stat.equals(metrics.get(i))) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                metrics.get(index).time += stat.time;
                metricsCount.set(index, metricsCount.get(index) + 1);
            } else {
                metrics.add(stat);
                metricsCount.add(1);
            }
        }

        for (int i = 0; i < metrics.size(); i++) {
            metrics.get(i).time /= metricsCount.get(i);
        }

        return metrics;
    }

}