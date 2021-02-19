package dev.kabin.util.pools.objectpool;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Documented
public @interface Borrowed {
    String origin();
}
