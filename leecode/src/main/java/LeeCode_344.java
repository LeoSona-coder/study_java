public class LeeCode_344 {
    /**
     *  力扣 344 反转字符串
     *  双指针 交换值
     * @param s
     */
    public void reverseString(char[] s) {
        for (int i = 0, j = s.length - 1; i < j; i++, j--) {
            char a;
            a = s[i];
            s[i] = s[j];
            s[j] = a;
        }
    }
}
