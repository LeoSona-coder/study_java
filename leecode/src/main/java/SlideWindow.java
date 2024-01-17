import javax.print.DocFlavor;
import java.util.*;

/**
 * 滑动窗口相关算法
 */

public class SlideWindow {
    /**
     * 无重复字符的最长子串
     *
     * @param s 输入字符串
     * @return max 最大无重复字符的字串长度
     */
    public static int lengthOfLongestSubstring(String s) {
        int length = s.length();

        if (length == 0) {
            return 0;
        }
        int max = 0;

        HashMap<Character, Integer> map = new HashMap<>();
        // 双游标
        for (int start = 0, end = 0; end < length; end++) {
            char currentElement = s.charAt(end);
            // 如果当前元素已经有了，那么获取上一次该元素的位置 +１，就是新的窗口开始位置
            if (map.containsKey(currentElement)) {
                start = Math.max(map.get(currentElement) + 1, start);
            }
            max = Math.max(max, end - start + 1);
            map.put(currentElement, end);
        }
        return max;
    }



    public static void main(String[] args) {

    }
}
