package sort;

import java.util.Arrays;

import static sort.SortUtils.swap;

public class SingleSideQuickSort {
    public static final int[] unSortArray = new int[]{1, 2, 8, 7, 6, 3, 4, 5};

    /**
     * 单边循环快排（lomuto 分区方案）
     * 1. 选择最右元素作为基准点元素
     * 2. j指针负责找到比基准点小的元素，一旦找到则与i 进行交换
     * 3. i指针维护小于基准点元素的边界，也是每次交换的目标索引
     * 4. 最后基准点与i交换，i即为分区位置
     *
     * @param arr   待排序的数组
     * @param left  左边界
     * @param right 右边界
     * @return 基准点元素所在的正确索引 可以用来确定下一轮分区的边界
     */

    public static int partition(int[] arr, int left, int right) {
        int pv = arr[right]; // 选去最右边的元素为基准
        int i = left; // i是维护比基准元素小的范围的边界
        for (int j = left; j < right; j++) {
            if (arr[j] < pv) {
                if (i != j) {  // 如果i == j 的时候  没必要交换
                    swap(arr, i, j);
                }
                i++;
            }
        }
        if (i != right) { // 如果i == right 的时候  没必要交换 也就是区域内的值全都比基准值小的情况下
            swap(arr, i, right);
        }
        System.out.println("排序后的结果：" + Arrays.toString(arr) + "i = " + i);
        return i;
    }


    public static void sort(int[] arr, int left, int right) {
        if (left >= right) {
            return;
        }
        int i = partition(arr, left, right);
        sort(arr, left, i - 1);
        sort(arr, i + 1, right);
    }

}
