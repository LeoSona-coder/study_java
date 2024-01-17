package sort;

public class SortUtils {
    /**
     * 将数组中的某两个元素交换
     * @param arr 带操作的数组
     * @param src 起始位置
     * @param dest  目标位置
     */
    public static void swap(int[] arr, int src, int dest) {
        int temp = arr[src];
        arr[src] = arr[dest];
        arr[dest] = temp;
    }
}
