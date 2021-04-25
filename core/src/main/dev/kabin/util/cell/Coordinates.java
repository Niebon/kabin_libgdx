package dev.kabin.util.cell;

public class Coordinates {

    private int i, j; // cell coordinates.
    private float x, y; // x,y relative to cell.

    private Coordinates(int i, int j, float x, float y) {
        this.i = i;
        this.j = j;
        this.x = x;
        this.y = y;
    }

    public static Coordinates of(int i, int j, float x, float y) {
        if (x < 0 || x >= 1) throw new IllegalArgumentException("x must be contained in [0,1).");
        if (y < 0 || y >= 1) throw new IllegalArgumentException("y must be contained in [0,1).");
        return new Coordinates(i, j, x, y);
    }

    private static int floorFloatToInt(float f) {
        return (int) Math.floor(f); // Math.toIntExact(Math.round(Math.floor(xCandidate)));
    }

    public int getCellX() {
        return i;
    }

    public int getCellY() {
        return j;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * Modifies this instance, by the given data induced by {@code +}.
     *
     * @param i horizontal cell coordinate.
     * @param j vertical cell coordinate.
     */
    public Coordinates add(int i, int j) {
        return addX(i).addY(j);
    }

    /**
     * @param i the horizontal cell coordinate to add.
     * @return this.
     */
    public Coordinates addX(int i) {
        this.i = this.i + i;
        return this;
    }

    /**
     * @param j the horizontal cell coordinate to add.
     * @return this.
     */
    public Coordinates addY(int j) {
        this.j = this.j + j;
        return this;
    }

    /**
     * Modifies this instance, by the given data induced by {@code +}.
     *
     * @param x horizontal coordinate.
     */
    public Coordinates addX(float x) {
        float xCandidate = this.x + x;
        if (xCandidate >= 1f) {
            int horizontalCellShift = floorFloatToInt(xCandidate);
            this.i = this.i + horizontalCellShift;
            this.x = xCandidate - horizontalCellShift;
        } else if (xCandidate < 0f) {
            int horizontalCellShift = floorFloatToInt(xCandidate);
            this.i = this.i + horizontalCellShift;
            this.x = xCandidate - horizontalCellShift;
        } else {
            this.x = xCandidate;
        }
        return this;
    }

    /**
     * Modifies this instance, by the given data induced by {@code +}.
     *
     * @param y vertical coordinate.
     */
    public Coordinates addY(float y) {
        float yCandidate = this.y + y;
        if (yCandidate >= 1f) {
            int verticalCellShift = floorFloatToInt(yCandidate);
            this.j = this.j + verticalCellShift;
            this.y = yCandidate - verticalCellShift;
        } else if (yCandidate < 0f) {
            int horizontalCellShift = floorFloatToInt(yCandidate);
            this.j = this.j + horizontalCellShift;
            this.y = yCandidate - horizontalCellShift;
        } else {
            this.y = yCandidate;
        }
        return this;
    }

    public Coordinates add(float x, float y) {
        return addX(x).addY(y);
    }

}
