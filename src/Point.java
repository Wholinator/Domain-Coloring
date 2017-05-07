/**
 * Created by Matthew on 5/6/2017.
 */
public class Point {
    double x;
    double y;
    int _x;
    int _y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(double inx, double iny) {
        x = inx;
        y = iny;
    }

    public Point(int inx, int iny) {
        _x = inx;
        _y = iny;
    }

    public double getXD() {
        return x;
    }

    public double getYD() {
        return y;
    }

    public int getXI() {
        return _x;
    }

    public int getYI() {
        return _y;
    }
}
