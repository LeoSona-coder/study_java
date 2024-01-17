public class LeeCode_7 {
    /**
     * 给你一个 32 位的有符号整数 x ，返回将 x 中的数字部分反转后的结果。
     *
     * 如果反转后整数超过 32 位的有符号整数的范围 [−231,  231 − 1] ，就返回 0。
     * @param x
     * @return
     */
    public static int reverse(int x) {
        int res = 0;

        while (x != 0) {
            int num = x % 10;
            if (res > Integer.MAX_VALUE / 10 || (res == Integer.MAX_VALUE / 10 && num > Integer.MAX_VALUE % 10)) {
                return 0;
            }

            if (res < Integer.MIN_VALUE / 10 || (res == Integer.MIN_VALUE / 10 && num < Integer.MIN_VALUE % 10)) {
                return 0;
            }
            res = res * 10 + num;
            x /= 10;
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(reverse(900000));
    }
}
