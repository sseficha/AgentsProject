import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class Player extends Agent {

    private ArrayList<DFAgentDescription> teammates;
    public String team;
    private int id;
    private int idInTeam;
    private int sight;  // Range of vision
    private Point position;
    private Box[][] map;
    private int periodTime;
    private AID masterId;


    protected void setup () {

        teammates = new ArrayList<>();
        team = getArguments()[0].toString();
        id = Integer.parseInt(getArguments()[1].toString());
        idInTeam = Integer.parseInt(getArguments()[2].toString());
        sight = Integer.parseInt(getArguments()[3].toString());
        String[] initPoint = getArguments()[4].toString().split(",");
        position = new Point(Integer.parseInt(initPoint[0]), Integer.parseInt(initPoint[1]));
        periodTime = Integer.parseInt(getArguments()[5].toString());
        this.map = gameLauncher.map.getMap();
        masterId = null;


        // Add agent to Yellow Pages
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

        // Get a list of other agents in yellow pages (teammates)
        DFAgentDescription[] result;
        DFAgentDescription template = new DFAgentDescription();
        sd = new ServiceDescription();
        sd.setType(team);
        template.addServices(sd);
        try {
            result = DFService.search(this, template);
            for (DFAgentDescription dfAgentDescription : result) {
                if (!dfAgentDescription.getName().toString().equals(this.getName()))
                    teammates.add(dfAgentDescription);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        ACLMessage msg = this.receive();
        if (msg != null && msg.getContent().equals("GAME STARTED"))
            masterId = msg.getSender();


        // Communication Behavior (Must update known/unknown map)
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action () {
                ArrayList<Point> surroundings;
                ACLMessage msg = myAgent.receive();
                if (msg != null) {
                    surroundings = unformat(msg.getContent());
                    for (Point surrounding : surroundings) {
                        map[surrounding.x][surrounding.y].setExplored();
                    }
                } else {
                    block();
                }
            }
        });

        // Look/Send/Evaluate/Move Behavior
        addBehaviour(new TickerBehaviour(this, periodTime) {
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
            if (position.x + i < 0 || position.x + i >= map[0].length)
                continue;
            for (int j = -sight; j <= sight; j++) {
                if (position.y + j < 0 || position.y + j >= map.length)
                    continue;
                surroundings.add(new Point(position.x + i, position.y + j));
            }
        }
        for (Point surrounding : surroundings) {
            map[surrounding.x][surrounding.y].setExplored();
        }

        return format(surroundings);
    }

    public String format (ArrayList<Point> surroundings) {
        StringBuilder msg = new StringBuilder();
        String x, y;
        for (Point surrounding : surroundings) {
            x = String.valueOf(surrounding.x);
            y = String.valueOf(surrounding.y);
            msg.append(x).append(',').append(y).append(';');
        }
        return msg.toString();
    }

    public ArrayList<Point> unformat (String surroundings) {
        ArrayList<Point> u_surroundings = new ArrayList<>();
        String[] sUnformat = surroundings.split(";");

        for (String s : sUnformat) {
            String[] temp = s.split(",");

            u_surroundings.add(new Point(Integer.parseInt(temp[0]), Integer.parseInt(temp[1])));
        }

        return u_surroundings;
    }

    public void send (String content) {

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

        int playerId = idInTeam % teammates.size();

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

        return Math.abs(pos.y - map.length);
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

        return Math.abs(pos.x - map.length);
    }

    /**
     * Calculates an estimation for the distance to the target (Mean Value)
     *
     * @return The estimation distance
     */
    private int estimateDistToTargetMiddle (Point pos) {

        return (dist(pos, new Point(map[0].length / 2, map.length / 2)));
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

                if (pointInMap(child, map[0].length, map.length)
                    && !Objects.equals(map[child.x][child.y].getContent(), 'O')) {
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

        if (idInTeam % teammates.size() == 5)
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
        for (int i = 0; i < map[0].length; i++) {
            for (int j = 0; j < map.length; j++) {
                if (map[i][j].getExplored()
                    && Objects.equals(map[i][j].getContent(), 'X')) {
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
            if (!map[openSet.get(minIndex).x][openSet.get(minIndex).y].getExplored()) {
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
        for (int i = 0; i < map[0].length; i++) {
            for (int j = 0; j < map.length; j++) {
                if (map[i][j].getExplored()
                    && Objects.equals(map[i][j].getContent(), 'X')) {
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
            if (!map[openSet.get(minIndex).x][openSet.get(minIndex).y].getExplored()) {
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

            if (!pointInMap(nextPos, map[0].length, map.length))
                continue;

            if (Objects.equals(map[nextPos.x][nextPos.y].getContent(), 'O'))
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
    private void move (Point pos) {
        position.setLocation(pos);

        gameLauncher.map.setAgentPositions(id, pos);
        gameLauncher.map.explore(pos.x, pos.y);
        map[pos.x][pos.y].setExplored();
        gameLauncher.map.repaint();

        // If the treasure has been found, stop
        if (Objects.equals(map[position.x][position.y].getContent(), 'X')) {

            // Inform gameLauncher
            //==================????
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(masterId);
            String message = "FOUND TREASURE ! The " + team + " won!";
            msg.setContent(message);
            send(msg);

            String algorithm = "BestFS";
            if (team.equals("team2"))
                algorithm = "A*";
            gameLauncher.stats.putStat(teammates.size(), algorithm, new Point(map[0].length, map.length), System.nanoTime(), gameLauncher.TIME);

            // Kills all agents and shows message
            gameLauncher.killAgents();
            JOptionPane.showMessageDialog(null, message, "WINNER!", JOptionPane.INFORMATION_MESSAGE);
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
