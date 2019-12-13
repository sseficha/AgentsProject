import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class Player extends Agent {

    private ArrayList<DFAgentDescription> teammates;
    private int id;
    public String team;
    private Point position;
    private int sight;  // Range of vision
    private MyMap map;

    public Player (MyMap map, int sight, Point initPoint) {

        position = initPoint;
        this.sight = sight;
        this.map = map;

        // setup();
    }


    protected void setup() {

        teammates = new ArrayList<>();
        id = Integer.parseInt(getArguments()[1].toString());
        team = getArguments()[0].toString();

        // add agent to Yellow Pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(team);
        sd.setName(getName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (
                FIPAException fe) {
            fe.printStackTrace();
        }

        //get a list of other agents in yellow pages (teammates)
        DFAgentDescription[] result = null;
        DFAgentDescription template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType(team);
        template.addServices(sd);
        try {
            result = DFService.search(this, template);
            for (DFAgentDescription dfAgentDescription : result) {
                if (!dfAgentDescription.getName().equals(this.getName())) ///////////////////////////
                    teammates.add(dfAgentDescription);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        //Communication Behavior (Must update known/unknown map)
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action () {
                ArrayList<Point> surroundings;
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    surroundings = unformat(msg.getContent());
                    for (Point surrounding : surroundings) {
                        map.explore(surrounding.x, surrounding.y);
                    }
                } else {
                    block();
                }
            }
        });

        //Look/Send/Evaluate/Move Behavior
        addBehaviour(new TickerBehaviour(this, 1500) {
            String msg;

            @Override
            protected void onTick () {
                msg = look();
                send(msg);
                Point nextPos = new Point(evaluate());
                move(nextPos);
            }
        });
    }

    public String look () {
        ArrayList<Point> surroundings = new ArrayList<>();

        for (int i = -sight; i <= sight; i++) {
            if (position.x + i < 0 || position.x + i >= map.lengthX())
                continue;
            for (int j = -sight; j <= sight; j++) {
                if (position.y + j < 0 || position.y + j >= map.lengthY())
                    continue;
                surroundings.add(new Point(position.x + i, position.y + j));
            }
        }
        for (Point surrounding : surroundings) {
            map.explore(surrounding.x, surrounding.y);
        }

        return format(surroundings);

    }

    public String format (ArrayList<Point> surroundings) {
        StringBuilder msg = new StringBuilder();
        String x, y;
        for (Point surrounding : surroundings) {
            x = String.valueOf(surrounding.x);
            y = String.valueOf(surrounding.y);
            msg.append(x).append(y);
        }
        return msg.toString();
    }

    public ArrayList<Point> unformat (String surroundings) {
        int x, y;
        ArrayList<Point> u_surroundings = new ArrayList<>();
        for (int i = 0; i < surroundings.length(); i += 2) {
            x = Character.getNumericValue(surroundings.charAt(i));
            y = Character.getNumericValue(surroundings.charAt(i + 1));
            u_surroundings.add(new Point(x, y));
        }
        return u_surroundings;
    }

    public void send(String content){

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        for (DFAgentDescription teammate : teammates) {
            msg.addReceiver(teammate.getName());    //+ agent with gui
        }
        msg.setContent(content);
        send(msg);
    }

    /**
     * The distance between the two points in map.
     * The function calculates how many steps the agent have to do.
     *
     * @param pos1 The first position
     * @param pos2 The second position
     * @return The distance between the two Points
     */
    private int dist (Point pos1, Point pos2) {

        int distX = Math.abs(pos1.x - pos2.x);
        int distY = Math.abs(pos1.y - pos2.y);

        return Math.max(distX, distY);
    }

    /**
     * Redirects the estimate distance method due to agent id factors.
     *
     * @return The estimation distance
     */
    private int estimateDistToTarget (Point pos) {

        int playerId = id % teammates.size();

        switch (playerId) {
            case 0:
                return estimateDistToTargetNorth(pos);
            case 1:
                return estimateDistToTargetEast(pos);
            case 2:
                return estimateDistToTargetSouth(pos);
            case 3:
                return estimateDistToTargetWest(pos);
            case 4:
                return estimateDistToTargetMiddle(pos);
            default:
                throw new IllegalStateException("Unexpected value: " + playerId);
        }
    }

    /**
     * Calculates an estimation for the distance to the target (Towards North)
     *
     * @return The estimation distance
     */
    private int estimateDistToTargetNorth (Point pos) {

        return pos.x;
    }

    /**
     * Calculates an estimation for the distance to the target (Towards East)
     *
     * @return The estimation distance
     */
    private int estimateDistToTargetEast (Point pos) {

        return Math.abs(pos.y - map.lengthY());
    }

    /**
     * Calculates an estimation for the distance to the target (Towards South)
     *
     * @return The estimation distance
     */
    private int estimateDistToTargetSouth (Point pos) {

        return pos.y;
    }

    /**
     * Calculates an estimation for the distance to the target (Towards West)
     *
     * @return The estimation distance
     */
    private int estimateDistToTargetWest (Point pos) {

        return Math.abs(pos.x - map.lengthY());
    }

    /**
     * Calculates an estimation for the distance to the target (Mean Value)
     *
     * @return The estimation distance
     */
    private int estimateDistToTargetMiddle (Point pos) {

        return (dist(pos, new Point(map.lengthX() / 2, map.lengthY() / 2)));
    }


    /**
     * Returns the index of the set with the minimum value.
     * If there are multiple solutions, returns randomly one of them.
     *
     * @param set The ArrayList with the values
     * @return The index to the minimum value
     */
    private int minIndexState (ArrayList<Integer> set) {

        int minInteger = Collections.min(set);

        ArrayList<Integer> minArrayIndexes = new ArrayList<>();
        for (int i = 0; i < set.size(); i++) {
            if (set.get(i) == minInteger)
                minArrayIndexes.add(i);
        }

        return new Random().nextInt(minArrayIndexes.size());
    }


    /**
     * Check if a given point belongs to the map
     *
     * @param value The given point
     * @param sizeX The size for the first dimension
     * @param sizeY The size for the second dimension
     * @return True if it can belong, false otherwise
     */
    private boolean pointInMap (Point value, int sizeX, int sizeY) {
        return value.x >= 0 && value.y >= 0 && value.x < sizeX && value.y < sizeY;
    }


    /**
     * Evaluates the possible moves from a box in the map
     *
     * @param parent The parent box
     * @return An ArrayList with all the possible moves
     */
    private ArrayList<Point> expand (Point parent) {

        ArrayList<Point> expand = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0)
                    continue;
                Point child = new Point(parent.x + i, parent.y + j);

                if (pointInMap(child, map.lengthX(), map.lengthY())
                    && !Objects.equals(map.getBox(child).getContent(), 'O')) {
                    expand.add(child);
                }
            }
        }

        return expand;
    }


    /**
     * The main evaluate method that is called after every update of the agents and the map.
     * Redirects the evaluate method due to team and agent id factors.
     *
     * @return The next box that the agent has to go.
     */
    private Point evaluate () {

        if (Objects.equals(team, "team2"))
            return evaluateBestFS();

        if (id % teammates.size() == 5)
            return evaluateRandom();

        return evaluateAStar();
    }

    /**
     * It implements a slightly different A* Algorithm and is called from evaluate().
     *
     * @return The next box that the agent has to go.
     */
    private Point evaluateAStar () {

        ArrayList<Point> closedSet = new ArrayList<>();
        ArrayList<Point> openSet = new ArrayList<>();
        ArrayList<Point> openSetNextMove = new ArrayList<>();
        ArrayList<Integer> openSetValue = new ArrayList<>();
        openSet.add(position);
        openSetNextMove.add(null);
        openSetValue.add(0);

        // Find if the solution have found
        boolean solutionFound = false;
        Point solution = null;
        for (int i = 0; i < map.lengthX(); i++) {
            for (int j = 0; j < map.lengthY(); j++) {
                if (map.getBox(new Point(i, j)).getExplored()
                    && Objects.equals(map.getBox(new Point(i, j)).getContent(), 'X')) {
                    solutionFound = true;
                    solution = new Point(i, j);
                }
            }
        }


        while (openSet.size() != 0) {

            // Find the min value
            int minIndex = minIndexState(openSetValue);

            // If point exists in closedSet, remove and continue
            if (closedSet.contains(openSet.get(minIndex))) {
                openSet.remove(minIndex);
                openSetNextMove.remove(minIndex);
                openSetValue.remove(minIndex);
                continue;
            }

            // If this is the solution, return the next box to move
            if (solutionFound && openSet.get(minIndex).equals(solution)) {
                return openSetNextMove.get(minIndex);
            }

            // If this is an unreached box, return the next box to move
            if (!map.getBox(openSet.get(minIndex)).getExplored()) {
                return openSetNextMove.get(minIndex);
            }

            // Produce possible moves
            ArrayList<Point> children = expand(openSet.get(minIndex));

            // Check if there are not new moves
            if (children.size() == 0) {
                closedSet.add(openSet.get(minIndex));
                openSet.remove(minIndex);
                openSetNextMove.remove(minIndex);
                openSetValue.remove(minIndex);

                continue;
            }

            // Evaluate moves
            ArrayList<Integer> childrenValue = new ArrayList<>();
            ArrayList<Point> childrenNextMove = new ArrayList<>();

            for (Point child : children) {
                int realDist = dist(position, child);
                int valueToTarget = realDist;
                if (solutionFound)
                    valueToTarget += dist(child, solution);
                else
                    valueToTarget += estimateDistToTarget(child);
                childrenValue.add(valueToTarget);
                Point nextMove = openSetNextMove.get(minIndex);
                if (realDist == 1)
                    nextMove = child;
                childrenNextMove.add(nextMove);
            }


            // Put moves for checking
            openSet.addAll(children);
            openSetNextMove.addAll(childrenNextMove);
            openSetValue.addAll(childrenValue);


            // Put current box to closedSet and remove it from everywhere
            closedSet.add(openSet.get(minIndex));
            openSet.remove(minIndex);
            openSetNextMove.remove(minIndex);
            openSetValue.remove(minIndex);
        }

        return evaluateRandom();
    }

    /**
     * It implements a slightly different Best First Algorithm and is called from evaluate().
     *
     * @return The next box that the agent has to go.
     */
    private Point evaluateBestFS () {

        ArrayList<Point> closedSet = new ArrayList<>();
        ArrayList<Point> openSet = new ArrayList<>();
        ArrayList<Point> openSetNextMove = new ArrayList<>();
        ArrayList<Integer> openSetValue = new ArrayList<>();
        openSet.add(position);
        openSetNextMove.add(null);
        openSetValue.add(0);

        // Find if the solution have found
        boolean solutionFound = false;
        Point solution = null;
        for (int i = 0; i < map.lengthX(); i++) {
            for (int j = 0; j < map.lengthY(); j++) {
                if (map.getBox(new Point(i, j)).getExplored()
                    && Objects.equals(map.getBox(new Point(i, j)).getContent(), 'X')) {
                    solutionFound = true;
                    solution = new Point(i, j);
                }
            }
        }


        while (openSet.size() != 0) {

            // Find the min value
            int minIndex = minIndexState(openSetValue);

            // If point exists in closedSet, remove and continue
            if (closedSet.contains(openSet.get(minIndex))) {
                openSet.remove(minIndex);
                openSetNextMove.remove(minIndex);
                openSetValue.remove(minIndex);
                continue;
            }

            // If this is the solution, return the next box to move
            if (solutionFound && openSet.get(minIndex).equals(solution)) {
                return openSetNextMove.get(minIndex);
            }

            // If this is an unreached box, return the next box to move
            if (!map.getBox(openSet.get(minIndex)).getExplored()) {
                return openSetNextMove.get(minIndex);
            }

            // Produce possible moves
            ArrayList<Point> children = expand(openSet.get(minIndex));

            // Check if there are not new moves
            if (children.size() == 0) {
                closedSet.add(openSet.get(minIndex));
                openSet.remove(minIndex);
                openSetNextMove.remove(minIndex);
                openSetValue.remove(minIndex);

                continue;
            }

            // Evaluate moves
            ArrayList<Integer> childrenValue = new ArrayList<>();
            ArrayList<Point> childrenNextMove = new ArrayList<>();

            for (Point child : children) {
                int realDist = dist(position, child);
                childrenValue.add(realDist);
                Point nextMove = openSetNextMove.get(minIndex);
                if (realDist == 1)
                    nextMove = child;
                childrenNextMove.add(nextMove);
            }


            // Put moves for checking
            openSet.addAll(children);
            openSetNextMove.addAll(childrenNextMove);
            openSetValue.addAll(childrenValue);


            // Put current box to closedSet and remove it from everywhere
            closedSet.add(openSet.get(minIndex));
            openSet.remove(minIndex);
            openSetNextMove.remove(minIndex);
            openSetValue.remove(minIndex);
        }

        return evaluateRandom();
    }

    /**
     * It implements a random choice for the agent and is called from evaluate().
     *
     * @return The next box that the agent has to go.
     */
    public Point evaluateRandom () {

        Point nextPos = new Point();

        while (true) {
            Random rand = new Random();

            int posX = rand.nextInt(3) - 1 + (int) position.getX();
            int posY = rand.nextInt(3) - 1 + (int) position.getY();

            nextPos.setLocation(posX, posY);

            if (!pointInMap(nextPos, map.lengthX(), map.lengthY()))
                continue;

            if (Objects.equals(map.getBox(nextPos).getContent(), 'O'))
                continue;

            break;
        }
        return nextPos;
    }


    /**
     * Makes the move of the agent according to the evaluation and updates the map.
     *
     * @param pos The next box that the agent has to go
     */
    private void move (Point pos) { /////////////////////////////////////////////////////////////
        position.setLocation(pos);

        gameLauncher.map.setAgentPositions(id, pos);
        gameLauncher.map.explore(pos.x, pos.y);
        map.explore(pos.x, pos.y);
        gameLauncher.map.repaint();

        if (Objects.equals(map.getBox(position).getContent(), 'X')) {
            System.out.println("Found");
            //==================????
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            //msg.addReceiver(masterID);
            String message = "FOUND TREASURE";// ! The team" + team + " won!";
            msg.setContent(message);
            send(msg);


            //prosorino
            gameLauncher.killAgents();

            //==================????
        }
    }

    @Override
    protected void takeDown () {
        System.out.println("AGENT " + getName() + " IS DOWN NOW!");
        try {
            DFService.deregister(this);
        } catch (Exception ignored) {
        }
    }

}
