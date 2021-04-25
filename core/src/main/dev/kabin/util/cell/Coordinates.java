package dev.kabin.util.cell;

public class Coordinates {

    private int cellX, cellY; // cell coordinates.
    private float x, y; // x,y relative to cell.

    private Coordinates(int cellX, int cellY, float x, float y) {
        this.cellX = cellX;
        this.cellY = cellY;
        this.x = x;
        this.y = y;
    }

    public static Coordinates of(int cellX, int cellY, float x, float y) {
        if (x < 0 || x >= 1) throw new IllegalArgumentException("x must be contained in [0,1).");
        if (y < 0 || y >= 1) throw new IllegalArgumentException("y must be contained in [0,1).");
        return new Coordinates(cellX, cellY, x, y);
    }

    private static int floorFloatToInt(float f) {
        return (int) Math.floor(f); // Math.toIntExact(Math.round(Math.floor(xCandidate)));
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
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
     * @param cellX horizontal cell coordinate.
     * @param cellY vertical cell coordinate.
     */
    public Coordinates add(int cellX, int cellY) {
        return addX(cellX).addY(cellY);
    }

    /**
     * @param cellX the horizontal cell coordinate to add.
     * @return this.
     */
    public Coordinates addX(int cellX) {
        this.cellX = this.cellX + cellX;
        return this;
    }

    /**
     * @param cellY the horizontal cell coordinate to add.
     * @return this.
     */
    public Coordinates addY(int cellY) {
        this.cellY = this.cellY + cellY;
        return this;
    }

    /**
     * Modifies this instance, by the given data induced by {@code +}.
     *
     * @param x horizontal coordinate.
     */
    public Coordinates addX(float x) {
        float xCandidate = this.x + x;
        if (xCandidate >= 1f || xCandidate < 0f) {
            int cellXShift = floorFloatToInt(xCandidate);
            this.cellX = this.cellX + cellXShift;
            this.x = xCandidate - cellXShift;
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
        if (yCandidate >= 1f || yCandidate < 0f) {
            int cellYShift = floorFloatToInt(yCandidate);
            this.cellY = this.cellY + cellYShift;
            this.y = yCandidate - cellYShift;
        } else {
            this.y = yCandidate;
        }
        return this;
    }

    public Coordinates add(float x, float y) {
        return addX(x).addY(y);
    }

}
