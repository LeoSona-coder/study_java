package array;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class Array<E> {
    private int size;

    private Object[] elementData = new Object[]{};

    /**
     * 将当前数组 array 从index开始的元素往后移动一位
     *
     * @param e 要添加的元素
     * @param index 添加的位置
     */

    public boolean add(E e, int index) {

        int s = this.size;
        if (elementData.length == size) {
            grow();
        }
        System.arraycopy(this.elementData, index, this.elementData, index + 1, s - index);
        this.elementData[index] = e;
        size++;
        return true;
    }
    public boolean add(E e) {

        add(e, size);
        return true;
    }
    private void grow() {
        grow(size + 1);
    }
    private void grow(int minCapacity) {
       this.elementData =  Arrays.copyOf(this.elementData, minCapacity);
    }

    public void sort() {
        int temp = 0;

        for (int i = 0; i < size - 1; i++) {
            for (int j = size - 1; j > i; j--) {
                


            }
        }
    }

    @Override
    public String toString() {
        return "Array{" +
                "elementData=" + Arrays.toString(elementData) +
                '}';
    }

    public static void main(String[] args) {
        Array<Integer> a = new Array<>();
        a.add(0);
        a.add(1);
        a.add(3);
        a.add(2, 2);
        System.out.println(a);

    }

}
