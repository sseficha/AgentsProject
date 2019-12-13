/**
 * The class represents the inside of every point in the class MyMap.
 */
public class Box {

    /**
     * Can take the values:
     * 'X' for Target
     * 'O' for Object
     * 'N' for None/Path
     */
    private char content;
    private boolean explored;


    public Box () {
        content = 'N';
        explored = false;
    }

    public Box (char z) {
        content = z;        //z = 'X' || 'O' || 'N'
        explored = false;
    }

    public void setContent (char content) {
        if (content != 'X' && content != 'O' && content != 'N')
            throw new IllegalStateException("Unexpected value: " + content); ;

        this.content = content;
    }

    public char getContent () {
        return content;
    }

    public void setExplored () {
        this.explored = true;
    }

    public boolean getExplored () {
        return explored;
    }

}