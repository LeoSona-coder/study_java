package sort;

import java.util.Arrays;

import static sort.SortUtils.swap;

public class DoubleSideQuickSort {

        /**
         * 双边循环快排(并不完全等价于 hoare 霍尔分区方案)
         * 1. 选择最左元素作为基准点元素
         * 2. j指针负责从右向左找比基准点小的元素，i指针负责从左向右找比基准点大的元素，一旦找到二者交换，直至i,i 相交
         * 3. 最后基准点与i(此时i与相等)交换，i即为分区位置
         *
         * @param arr   待排序的数组
         * @param left  左边界
         * @param right 右边界
         */
        public static int partition(int[] arr, int left, int right) {
        int i = left;
        int j = right;
        int pv = arr[left];

        while (i < j) {
            /**
             * 为什么这两步不能反过来写，因为如果反过来写，
             * 则如果最后一个元素是大的，左指针就指向这个大的元素 再执行 swap方法时，
             * 这个大的元素跟基准元素对调，那么顺序就错了，
             * 所以最后一步调换的时候 一定是跟一个比基准元素小的元素调换
             */
            while (arr[j] > pv && i < j) j--;  //从右边找小的
            while (arr[i] <= pv && i < j) i++; // 从左边找大的
            swap(arr, i, j);

        }
        swap(arr, left, j);
        System.out.println("排序后的结果：" + Arrays.toString(arr) + "i = " + i);
        return j;
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
