package sort;

import java.util.Arrays;

public class BubbleSort {
    /**
     * 冒泡排序，
     *
     * @param arr
     */
    public static void bubbleSort(int[] arr) {
        int temp;
        // 添加一个信号量 优化一下
        for (int i = 0; i < arr.length - 1; i++) {
            boolean flag = true;
            for (int j = arr.length - 1; j > i; j--) {
                if (arr[j] < arr[j - 1]) {
                    swap(arr, j, j - 1);
                    flag = false; // 如果哪一次的排序中没有发生过调换 说明已经是排好序了
                }
            }
            if (flag) {
                break;
            }
            System.out.println("第" + (i + 1) + "次排序：" + Arrays.toString(arr));
        }
    }

    private static void swap(int[] arr, int src,int dest) {
        int temp;
        temp = arr[src];
        arr[src] = arr[dest];
        arr[dest] = temp;
    }
}
