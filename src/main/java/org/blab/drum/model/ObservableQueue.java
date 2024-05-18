package org.blab.drum.model;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableListBase;

import java.util.*;

public class ObservableQueue<E> extends ObservableListBase<E> implements Queue<E> {
  private final LinkedList<E> queue;
  private final int capacity;

  private ObservableQueue<E> shadow;

  public ObservableQueue(int capacity) {
    this.queue = new LinkedList<>();
    this.capacity = capacity;
  }

  public boolean isFull() {
    return queue.size() == capacity;
  }

  @Override
  public E get(int i) {
    return queue.get(i);
  }

  @Override
  public int size() {
    return queue.size();
  }

  @Override
  public boolean offer(E e) {
    beginChange();

    if (isFull()) nextRemove(0, queue.poll());

    queue.offer(e);
    nextAdd(queue.size() - 1, queue.size());

    endChange();
    return true;
  }

  @Override
  public E remove() {
    beginChange();

    try {
      E e = queue.remove();
      nextRemove(0, e);
      return e;
    } finally {
      endChange();
    }
  }

  @Override
  public E poll() {
    beginChange();

    E e = queue.poll();
    if (e != null) nextRemove(0, e);

    endChange();
    return e;
  }

  @Override
  public E element() {
    return queue.element();
  }

  @Override
  public E peek() {
    return queue.peek();
  }

  @Override
  public E getFirst() {
    return queue.getFirst();
  }

  @Override
  public E getLast() {
    return queue.getLast();
  }

  public ObservableQueue<E> getShadow() {
    if (shadow == null) createShadow();
    return shadow;
  }

  private void createShadow() {
    shadow = new ObservableQueue<E>(capacity);
    shadow.queue.addAll(this);

    this.addListener(
        (ListChangeListener<E>)
            c ->
                Platform.runLater(
                    () -> {
                      while (c.next()) {
                        if (c.wasRemoved())
                          for (int i = 0; i < c.getRemovedSize(); ++i) shadow.poll();
                        if (c.wasAdded()) c.getAddedSubList().forEach(shadow::offer);
                      }
                    }));
  }
}
