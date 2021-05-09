package dev.kabin.util;

import dev.kabin.util.collections.IntToIntMap;
import dev.kabin.util.graph.Node;

import java.util.ArrayList;

public record PathData(ArrayList<Node<Integer>> pathSegments,
                       IntToIntMap pathSegmentNumberToConnectedListIndex) {

    public boolean containsSegmentIndex(int index) {
        return pathSegmentNumberToConnectedListIndex.containsKey(index);
    }

    public boolean isConnected(int pathSegmentIndexA, int pathSegmentIndexB) {
        if (containsSegmentIndex(pathSegmentIndexA) && containsSegmentIndex(pathSegmentIndexB)) {
            return pathSegmentNumberToConnectedListIndex.get(pathSegmentIndexA) == pathSegmentNumberToConnectedListIndex.get(pathSegmentIndexB);
        } else return false;
    }


}
