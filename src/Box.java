public class Box {
    boolean explored;   //explored tha einai kai ayta pou einai endos optikou pediou
    char content;

    public Box()
    {
        content = 'N';
        explored = false;
    }

    public Box(char z){
        content = z;        //z = 'X' || 'O' || 'N'
        explored = false;
    }

    public char getContent()
    {
        return content;
    }

    public void setExplored()
    {
        this.explored=true;
    }
    
    public boolean getExplored(){return explored;}

}
