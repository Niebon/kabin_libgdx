package dev.kabin.entities;


public interface GroupTyped<T extends Enum<T>> {

    T getGroupType();

}
