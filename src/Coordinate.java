public class Coordinate {

    private int x;
    private int y;

    public Coordinate() {
        x = 0;
        y = 0;
    }

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Coordinate)) {
            return false;
        }

        Coordinate o = (Coordinate) obj;

        return this.x == o.x && this.y == o.y;
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}
