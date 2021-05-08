package dev.kabin.util.graph;

import java.util.ArrayList;

public class SimpleNode<T> implements Node<T> {


    private final ArrayList<Node<T>> nodes = new ArrayList<>();
    private final T data;

    public SimpleNode(T data) {
        this.data = data;
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public int addChild(Node<T> data) {
        nodes.add(data);
        return nodes.size() - 1;
    }

    @Override
    public Node<T> getChild(int index) {
        return nodes.get(index);
    }

    @Override
    public T data() {
        return data;
    }


}
