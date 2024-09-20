package com.github.learntocode2013;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListWithFixedFirstElement<E> extends AbstractList<E> {
  private final List<E> backingList;

  public ListWithFixedFirstElement(E firstElement) {
    this.backingList = new ArrayList<>();
    backingList.add(firstElement);
  }

  @Override
  public E get(int index) {
    return backingList.get(index);
  }

  @Override
  public E set(int index, E element) {
    if (index != 0) {
      return backingList.set(index, element);
    }
    throw new IllegalArgumentException("Cannot set element at index 0");
  }

  public void add(int index, E element) {
    if (index != 0) {
      backingList.add(index, element);
    }
    throw new IllegalArgumentException("Cannot add element at index 0");
  }

  public E remove(int index) {
    if (index != 0) {
      return backingList.remove(index);
    }
    throw new UnsupportedOperationException();
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    if (index != 0) {
      return backingList.addAll(index, c);
    }
    throw new IllegalArgumentException("Cannot add element at index 0");
  }

  @Override
  public int size() {
    return backingList.size();
  }
}
