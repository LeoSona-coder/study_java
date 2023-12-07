package org.reol.study.collection.list;

public class LinkedList<E> {
    private Node<E> first;
    private Node<E> last;

    private int size;
    private int modCount;

    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    public E set(int index,E element) {
        checkElementIndex(index);
        Node<E> node = node(index);
        E oldVal = node.item;
        node.item = element;
        return oldVal;
    }
    private void checkElementIndex(int index) {
        if (!isElementIndex(index)) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    public boolean add(E e, int index) {
        checkPositionIndex(index);
        if (index == size)
            linkLast(e);
        else
            linkBefore(e, node(index));
        return true;
    }

    private Node<E> node(int index) {
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++) {
                x = x.next;
            }
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--) {
                x = x.prev;
            }
            return x;
        }
    }

    private void linkBefore(E e, Node<E> succ) {
        Node<E> prev = succ.prev;
        Node<E> newNode = new Node<>(prev, e, succ);
        succ.prev = newNode;
        if (prev == null) {
            first = newNode;
        }else {
            prev.next = newNode;
        }
        size++;
        modCount ++;

    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }
    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void linkLast(E e) {
        Node<E> newNode = new Node<>(last, e, null);
        Node<E> l = last;
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
        modCount++;
    }

    public String toString() {
        if (size == 0)
            return "[]";
        Node<E> cursor = first;
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (cursor != null) {
            sb.append(cursor.item);
            cursor = cursor.next;
            if (cursor == null) {
                sb.append(']');
                return sb.toString();
            }
            sb.append(',').append(' ');
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<>();

        list.add(1);
        list.add(2);
        list.set(1, 3);
        System.out.println(list);
    }


    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
}
