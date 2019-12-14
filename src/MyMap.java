import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MyMap extends JPanel {

    private Box[][] map;
    
    public static final Color NONE = new Color(50,205,50);
    public static final Color EXPLORED = new Color(240,230,140);
    public static final Color OBSTACLE = new Color(30,144,255);
    
    private ArrayList<Point> agentPositions;
    private BufferedImage treasureImage;
    private BufferedImage agentImage1;
    private BufferedImage agentImage2;
    private int numteam1pl;


    public MyMap (int numteam1pl, int s) {//init with text file...path+name hardcoded for now

        this.numteam1pl = numteam1pl;
        agentPositions = new ArrayList<>();
        map = createMap(s);

        // Load images
        try{
            treasureImage = ImageIO.read(new File("treasure-chest.png"));
        }
        catch (IOException ex)
        {
            System.out.println("THERE IS NO SUCH IMAGE treasure!");
        }

        try{
            agentImage1 = ImageIO.read(new File("red.jpg"));
            agentImage2 = ImageIO.read(new File("green.jpg"));
        }
        catch (IOException ex)
        {
            System.out.println("THERE IS NO SUCH IMAGE green-red!");
        }
    }

    public Box[][] getMap() {
        return map;
    }

    public int lengthX() {
        return map[0].length;
    }

    public int lengthY() {
        return map.length;
    }

    public Box getBox(Point pos) {
        return map[pos.x][pos.y];
    }


    public void explore(int i, int j){
        this.map[i][j].setExplored();
    }
    
    public void initializeAgentPos(ArrayList<Point> pos) {
        agentPositions = pos;
    }
    
    public void setAgentPositions(int pid, Point x) {
            agentPositions.set(pid,x);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size=map.length;
        boolean found =false;
        g.clearRect(0,0,getWidth(),getHeight());

        this.setLayout(new GridLayout(size, size));
        int w,h;
        w = getWidth()/size;
        h = getHeight()/size;

        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
            {
                Color c;
                g.setColor(NONE);
                g.fillRect(j*w,i*h,w,h);

                switch (map[i][j].getContent())
                {
                    case 'X':
                        g.drawImage(treasureImage.getScaledInstance(w,h,Image.SCALE_SMOOTH),j*w,i*h,this);
                        break;
                    case 'O':
                        c= OBSTACLE;
                        g.setColor(c);
                        g.fillRect(j*w,i*h,w,h);
                        break;
                    default:
                        break;
                }

                Point p = new Point(i,j);

                if(map[i][j].getExplored() && map[i][j].getContent() == 'N') {
                    c = EXPLORED;
                    g.setColor(c);
                    g.fillRect(j*w, i*h, w, h);
                }

                if (agentPositions.contains(p)) {
                    Image temp;
                    if (agentPositions.indexOf(p) < numteam1pl) {
                        temp = agentImage1.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    } else {
                        temp = agentImage2.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    }

                    g.setColor(g.getColor());
                    g.fillRect(j*w, i*h, w, h);
                    g.drawImage(temp, j*w, i*h, this);

                }

            }
        g.dispose();
    }


    private Box[][] createMap (int s) {

        Box[][] map = new Box[s][s];

        // Random generate map
        do {
            for (int i = 0; i < s; i++) {
                for (int j = 0; j < s; j++) {
                    if (new Random().nextBoolean())
                        map[i][j] = new Box('O');
                    else
                        map[i][j] = new Box('N');
                }
            }

            // Set (0, 0) to 'N' to be able to be starting point
            map[0][0].setContent('N');

            // Set destination point
            int midS = s / 2;
            int posX = new Random().nextInt(midS) + midS;
            int posY = new Random().nextInt(midS) + midS;
            map[posY][posX].setContent('X');


            // Check if there is a path to destination
        } while (!checkPath(map, new Point(0, 0)));

        return map;
    }

    private boolean checkPath (Box[][] mapForCheck, Point position) {

        ArrayList<Point> closedSet = new ArrayList<>();
        ArrayList<Point> openSet = new ArrayList<>();
        ArrayList<Integer> openSetValue = new ArrayList<>();
        openSet.add(position);
        openSetValue.add(0);

        while (openSet.size() != 0) {

            // Find the min value
            int minIndex = openSetValue.indexOf(Collections.min(openSetValue));

            // If point exists in closedSet, remove and continue
            if (closedSet.contains(openSet.get(minIndex))) {
                openSet.remove(minIndex);
                openSetValue.remove(minIndex);
                continue;
            }

            // If this is the solution, return the next box to move
            Point sol = openSet.get(minIndex);
            if (mapForCheck[sol.x][sol.y].getContent() == 'X') {
                return true;
            }

            // Produce possible moves
            ArrayList<Point> children = expand(mapForCheck, openSet.get(minIndex));

            // Check if there are not new moves
            if (children.size() == 0) {
                closedSet.add(openSet.get(minIndex));
                openSet.remove(minIndex);
                openSetValue.remove(minIndex);

                continue;
            }

            // Evaluate moves
            ArrayList<Integer> childrenValue = new ArrayList<>();

            for (Point child : children) {
                int realDist = dist(position, child);
                childrenValue.add(realDist);
            }


            // Put moves for checking
            openSet.addAll(children);
            openSetValue.addAll(childrenValue);


            // Put current box to closedSet and remove it from everywhere
            closedSet.add(openSet.get(minIndex));
            openSet.remove(minIndex);
            openSetValue.remove(minIndex);
        }

        return false;

    }

    private ArrayList<Point> expand (Box[][] mapForCheck, Point parent) {

        ArrayList<Point> expand = new ArrayList<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (i == 0 && j == 0)
                    continue;
                Point child = new Point(parent.x + i, parent.y + j);

                if (pointInMap(child, mapForCheck.length, mapForCheck[0].length)
                    && mapForCheck[child.x][child.y].getContent() != 'O') {
                    expand.add(child);
                }
            }
        }

        return expand;

    }

    private boolean pointInMap (Point value, int sizeX, int sizeY) {
        return value.x >= 0 && value.y >= 0 && value.x < sizeX && value.y < sizeY;
    }

    private int dist (Point pos1, Point pos2) {

        int distX = Math.abs(pos1.x - pos2.x);
        int distY = Math.abs(pos1.y - pos2.y);

        return Math.max(distX, distY);
    }

}
