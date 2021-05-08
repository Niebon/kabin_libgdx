package dev.kabin.util.graph;

/**
 * A mutable node that holds data.
 *
 * @param <T> the type of data.
 */
public interface Node<T> {

    /**
     * @return the size of this node.
     */
    int size();

    /**
     * Adds the given child to this node.
     *
     * @param data data of the child to be added.
     * @return If the data already was present under one of the children of this node, then the index of that child is returned.
     * Otherwise, the index of the new child is returned.
     */
    int addChild(Node<T> data);

    /**
     * @param index the index of the child to be accessed. The first child is accessed by 0, the second one by 1, and so on.
     * @return the node associated
     * @throws NoSuchChildException if no child exists under the given index.
     */
    Node<T> getChild(int index);

    /**
     * @return true if and only if this instance has no children.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @return the data of this node.
     */
    T data();

}
