package sort;

public class InsertSort {
    public void sort(int[] arr) {
        /**
         *  i 待插入元素的索引
         */
        for (int i = 1; i < arr.length; i++) {
            int temp = arr[i]; // 代表待插入的值
            int j = i - 1; // 已排序的区域的索引
            while (j > 0) {
                if (temp < arr[j]) { // 找到待插入的位置 也就是找到一个比temp小的值后面
                    arr[j + 1] = arr[j]; // 如果temp小于 a[j] 就把 a[j]往后移动一位
                } else {
                    break; //如果是temp 大于 arr[j] 那么不需要再比较了
                }
                j--;
            }
            // 如果一直找不到 那此时j为-1 那就插入到数组开头
            arr[j + 1] = temp;
        }
    }
}
