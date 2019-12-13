import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class MyMap extends JPanel {

    private Box[][] map;
    
    public static final Color NONE = new Color(50,205,50);
    public static final Color EXPLORED = new Color(240,230,140);
    //public static final Color AGENT2 = new Color(150,255,0);
    public static final Color OBSTACLE = new Color(30,144,255);
    //public static final Color AGENT1 = new Color(255,150,0);
    
    
    private ArrayList<Point> agentPositions;
    private BufferedImage treasureImage;
    private BufferedImage agentImage1;
    private BufferedImage agentImage2;
    private int numteam1pl;


    public MyMap(int numteam1pl){      //init with text file...path+name hardcoded for now
        this.numteam1pl = numteam1pl;
        agentPositions = new ArrayList<>();

        int w, h;
        System.out.println("Give Dimensions width,height : ");
        w = new Scanner(System.in).nextInt();
        h = new Scanner(System.in).nextInt();

        map = new Box[h][w];

        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++) {
                int choice = new Random().nextInt(2);
                if (choice == 0)
                    map[i][j] = new Box('O');
                else
                    map[i][j] = new Box('N');
            }

        map[0][0] = 'N';

        int posx = new Random().nextInt(w);
        int posy = new Random().nextInt(h);

        map[posy][posx] = 'X';

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++)
                System.out.print(map[i][j] + " ");
            System.out.println();
        }

        boolean b = false;
        try {
            b = evaluate(new Point(0, 0));
        } catch (ClassNotFoundException ex) {
            System.out.println("CLASSNOTFOUND");
        }

        if (b)
            System.out.println("\nthere is a path!");
        else {
            System.out.println("\ntry again!");
            main(null);
        }
    }


        try {
            r = new BufferedReader(new FileReader(System.getProperty("user.dir")+ "/sampleMap.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<ArrayList<Character>> tabledMap = new ArrayList<>();
        ArrayList<Character> tabledMapLine;
        String line;
        String[] splitLine;

        do {
            tabledMapLine=null;
            line=null;
            splitLine=null;
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(line == null)
                break;
            splitLine = line.split(" ");
            tabledMapLine = new ArrayList<>();
            for(int i=0;i<splitLine.length;i++)
            {
                tabledMapLine.add(splitLine[i].charAt(0));
            }
            tabledMap.add(tabledMapLine);
        }
        while (line!=null);

        map = new Box[tabledMap.size()][tabledMap.get(0).size()];
        for (int i=0;i<tabledMap.size();i++)
        {
            for (int j=0;j<tabledMap.get(i).size();j++)
            {
                map[i][j]=new Box(tabledMap.get(i).get(j));
//                System.out.print(map[i][j].getContent());   //just for debug
            }
//            System.out.println();   //just for debug
        }
//        System.out.println();   //just for debug

        try{
            treasureImage= ImageIO.read(new File("treasure-chest.png"));
        }
        catch (IOException ex)
        {
            System.out.println("THERE IS NO SUCH IMAGE treasure!");
        }

        try{
            agentImage1= ImageIO.read(new File("red.jpg"));
            agentImage2= ImageIO.read(new File("green.jpg"));
        }
        catch (IOException ex)
        {
            System.out.println("THERE IS NO SUCH IMAGE green-red!");
        }
    }

    /*public MyMap(int numteam1pl){      //init with text file...path+name hardcoded for now
        this.numteam1pl=numteam1pl;
        agentPositions=new ArrayList<>();
        BufferedReader r=null;
        try {
            r = new BufferedReader(new FileReader(System.getProperty("user.dir")+ "/sampleMap.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<ArrayList<Character>> tabledMap = new ArrayList<>();
        ArrayList<Character> tabledMapLine;
        String line;
        String[] splitLine;

        do {
            tabledMapLine=null;
            line=null;
            splitLine=null;
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(line == null)
                break;
            splitLine = line.split(" ");
            tabledMapLine = new ArrayList<>();
            for(int i=0;i<splitLine.length;i++)
            {
                tabledMapLine.add(splitLine[i].charAt(0));
            }
            tabledMap.add(tabledMapLine);
        }
        while (line!=null);

        map = new Box[tabledMap.size()][tabledMap.get(0).size()];
        for (int i=0;i<tabledMap.size();i++)
        {
            for (int j=0;j<tabledMap.get(i).size();j++)
            {
                map[i][j]=new Box(tabledMap.get(i).get(j));
//                System.out.print(map[i][j].getContent());   //just for debug
            }
//            System.out.println();   //just for debug
        }
//        System.out.println();   //just for debug

        try{
            treasureImage= ImageIO.read(new File("treasure-chest.png"));
        }
        catch (IOException ex)
        {
            System.out.println("THERE IS NO SUCH IMAGE treasure!");
        }

        try{
            agentImage1= ImageIO.read(new File("red.jpg"));
            agentImage2= ImageIO.read(new File("green.jpg"));
        }
        catch (IOException ex)
        {
            System.out.println("THERE IS NO SUCH IMAGE green-red!");
        }
    }*/


    public Box[][] getMap () {
        return map;
    }


    public int lengthX () {
        return map[0].length;
    }


    public int lengthY () {
        return map.length;
    }


    public Box getBox (Point pos) {
        return map[pos.x][pos.y];
    }


    public void explore (int i, int j) {
        this.map[i][j].setExplored();
    }


    public void initializeAgentPos (ArrayList<Point> pos) {
        agentPositions = pos;
    }


    public void setAgentPositions (int pid, Point x) {
        agentPositions.set(pid, x);
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size=map.length;
        boolean found =false;
        g.clearRect(0,0,getWidth(),getHeight());

        this.setLayout(new GridLayout(size, size));
        int w,h;
        w=getWidth()/size;
        h=getHeight()/size;

        for(int i=0;i<size;i++)
            for(int j=0;j<size;j++)
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

                if(map[i][j].getExplored()) {
                    c = EXPLORED;
                    g.setColor(c);
                    g.fillRect(j*w, i*h, w, h);
                }

                if (agentPositions.contains(p)) {
                    Image temp;
                    if (agentPositions.indexOf(p) % 2 == 0) {
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

}
