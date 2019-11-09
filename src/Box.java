public class Box {
    boolean explored;   //explored tha einai kai ayta pou einai endos optikou pediou
    char content;

    Box()
    {
        content = 'N';
        explored = false;
    }

    Box(char z){
        content = z;        //z = 'X' || 'O' || 'N'
        explored = false;
    }

    char getContent()
    {
        return content;
    }

    void setExplored()
    {
        this.explored=true;
    }

}
