package dev.kabin.util.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lists {

    public static <T> List<T> concat(Collection<T> list, T item) {
        return Stream.of(list, item).<T>mapMulti((o, c) -> {
            if (o instanceof Collection<?> coll) {
                coll.forEach(elt -> c.accept((T) elt));
            } else {
                c.accept((T) o);
            }
        }).collect(Collectors.toList());
    }

    public static <T> ArrayList<T> arrayListOf(T obj) {
        var ret = new ArrayList<T>();
        ret.add(obj);
        return ret;
    }

    public static <T> ArrayList<T> arrayListOf(T... obj) {
        return new ArrayList<>(java.util.Arrays.asList(obj));
    }

    public static <T> ArrayList<T> arrayListOf(Collection<T> c, T obj) {
        var ret = new ArrayList<>(c);
        ret.add(obj);
        return ret;
    }

}
