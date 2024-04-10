package com.ncr.gui.executor.impl2;

import java.util.*;

public class SynchonizedList implements List {

	private List list = new ArrayList();

	public synchronized boolean add(Object e) {
		return list.add(e);
	}

	public synchronized void add(int index, Object element) {
		list.add(index, element);
	}

	public synchronized boolean addAll(Collection c) {
		return list.addAll(c);
	}

	public synchronized boolean addAll(int index, Collection c) {
		return list.addAll(index, c);
	}

	public synchronized void clear() {
		list.clear();
	}

	public synchronized boolean contains(Object o) {
		return list.contains(o);
	}

	public synchronized boolean containsAll(Collection c) {
		return list.containsAll(c);
	}

	public synchronized Object get(int index) {
		return list.get(index);
	}

	public synchronized int indexOf(Object o) {
		return list.indexOf(o);
	}

	public synchronized boolean isEmpty() {
		return list.isEmpty();
	}

	public synchronized Iterator iterator() {
		return list.iterator();
	}

	public synchronized int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	public synchronized ListIterator listIterator() {
		return list.listIterator();
	}

	public synchronized ListIterator listIterator(int index) {
		return list.listIterator(index);
	}

	public synchronized boolean remove(Object o) {
		return list.remove(o);
	}

	public synchronized Object remove(int index) {
		return list.remove(index);
	}

	public synchronized boolean removeAll(Collection c) {
		return list.removeAll(c);
	}

	public synchronized boolean retainAll(Collection c) {
		return list.retainAll(c);
	}

	public synchronized Object set(int index, Object element) {
		return list.set(index, element);
	}

	public synchronized int size() {
		return list.size();
	}

	public synchronized List subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	public synchronized Object[] toArray() {
		return list.toArray();
	}

	public synchronized Object[] toArray(Object[] a) {
		return list.toArray(a);
	}

}
