package dev.kabin.util.shapes.deltacomplexes;

/**
 * A modifiable 1-simplex.
 */
public class Simplex1Impl implements Simplex1 {

    private Simplex0 start;
    private Simplex0 end;

    @Override
    public Simplex0 start() {
        return start;
    }

    @Override
    public Simplex0 end() {
        return end;
    }

}
