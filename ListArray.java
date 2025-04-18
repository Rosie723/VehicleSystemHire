package com.example.vehiclehiresystem;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ListArray implements List<Vehicle> {
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Vehicle> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(Vehicle vehicle) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Vehicle> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Vehicle> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public Vehicle get(int index) {
        return null;
    }

    @Override
    public Vehicle set(int index, Vehicle element) {
        return null;
    }

    @Override
    public void add(int index, Vehicle element) {

    }

    @Override
    public Vehicle remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator<Vehicle> listIterator() {
        return null;
    }

    @Override
    public ListIterator<Vehicle> listIterator(int index) {
        return null;
    }

    @Override
    public List<Vehicle> subList(int fromIndex, int toIndex) {
        return List.of();
    }
}
