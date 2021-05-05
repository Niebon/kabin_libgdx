package dev.kabin.util;

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

}
