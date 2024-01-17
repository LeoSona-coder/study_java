public class LeeCode_35 {
    /**
     * 简单看一眼 应该是二分查找法来计算
     *
     * @param nums
     * @param target
     * @return
     */
    public static int searchInsert(int[] nums, int target) {
        int n = nums.length;
        int left = 0, right = n - 1, ans = n;
        while (left <= right) {
            int mid = (right + left) >> 1;
            if (target <= nums[mid]) {
                right = mid - 1;
                ans = mid;
            } else{
                left = mid + 1;
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        System.out.println(searchInsert(new int[]{0, 1, 2, 3, 4, 5}, 6));
    }
}
