package dev.kabin.util.shapes;

@Deprecated
public abstract class GrowingDirectedIndexedRectBoxed<T extends Number & Comparable<T>> extends GrowingDirectedRectBoxed<T> implements Indexed {
    private int index;

    public GrowingDirectedIndexedRectBoxed(GrowingDirectedIndexedRectBoxed<T> copy) {
        super(copy);
        index = copy.getIndex();
    }

    public GrowingDirectedIndexedRectBoxed() {
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
