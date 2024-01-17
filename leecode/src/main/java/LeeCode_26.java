import java.util.HashMap;

public class LeeCode_26 {
    public int removeDuplicates(int[] nums) {

        int n = nums.length;
        if (n == 0) {
            return 0;
        }
        int fast = 1;
        int slow = 1;
        while (fast < n) {
            if (nums[fast] != nums[fast - 1])
                nums[slow] = nums[fast];
            else
                slow++;
            fast++;
        }
        return slow;
    }
}
