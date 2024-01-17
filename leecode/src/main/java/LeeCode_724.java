import java.util.Arrays;

public class LeeCode_724 {
    public int pivotIndex(int[] nums) {
        int n = nums.length;
        int total = Arrays.stream(nums).sum();
        int sum = 0;

        for (int i = 0; i < n; i++) {
            if (2 * sum + nums[i] == total) {
                return i;
            }
            sum += nums[i];
        }

        return -1;
    }
}
