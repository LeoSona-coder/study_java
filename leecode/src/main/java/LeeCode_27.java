public class LeeCode_27 {
    public static int removeElement(int[] nums, int val) {
        int n = nums.length;
        int leftMoveCount = 0;
        for (int i = 0; i < n; i++) {
            if (nums[i] == val) {
                leftMoveCount++;
            } else {
                nums[i - leftMoveCount] = nums[i];
            }
        }

        return n-leftMoveCount;

    }

    public static void main(String[] args) {
        System.out.println(removeElement(new int[]{0,1,2,2,3,0,4,2},2));
    }
}
