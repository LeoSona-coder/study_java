public class LeeCode_28 {
    public int strStr(String haystack, String needle) {
        char[] c1 = haystack.toCharArray();
        char[] c2 = needle.toCharArray();

        for (int i = 0; i < c1.length; i++) {
            if (c1[i] == c2[0]) {
                if (i <=  c1.length-needle.length() && haystack.substring(i, i+needle.length()).equals(needle)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
