public class LeeCode_14 {
    /**
     * 14. 最长公共前缀
     * <p>
     * 编写一个函数来查找字符串数组中的最长公共前缀。
     * 如果不存在公共前缀，返回空字符串 ""。
     *
     * @param strs
     * @return
     */
    public static String longestCommonPrefix(String[] strs) {
        if (strs.length == 0) {
            return "";
        }
        String ans = strs[0];
        for (int i = 1; i < strs.length; i++) {
            int j = 0;

            for (; j < ans.length() && j < strs[i].length(); j++) {
                if (ans.charAt(j) != strs[i].charAt(j)) {
                    break;
                }
            }
            ans = ans.substring(0, j); // 每次不管出没出现不等的情况 都需要截取一次，不然{"ab", "a"} 这种情况结果就是 ab
            if (ans.equals("")) {
                return ans;
            }

        }
        return ans;
    }

    public static String longestCommonPrefix2(String[] strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }
        int count = strs.length;

        int length = strs[0].length();

        for (int i = 0; i < length; i++) {
            char c = strs[0].charAt(i);
            for (String str : strs) {
                if (i == str.length() || c != str.charAt(i)) {
                    return str.substring(0, i);
                }
            }
        }
        return strs[0];

    }



    public static void main(String[] args) {
        System.out.println(longestCommonPrefix(new String[]{"ab", "a"}));
    }
}
