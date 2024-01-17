import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LeeCode_20 {
    /**
     *  leecode 第20 题 有效的括号
     *  碰到括号 记得跟栈关联
     * @param s
     * @return
     */
    public static boolean isValid(String s) {
        int length = s.length();
        if (length == 0) {
            return true;
        }
        if (length % 2 == 1) {
            return false;
        }
        Map<Character, Character> pairs = new HashMap<Character, Character>() {{
            put(')', '(');
            put(']', '[');
            put('}', '{');
        }};
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (pairs.containsKey(c)){
                if (stack.isEmpty() ||stack.peek() != pairs.get(c)) {
                    return false;
                }
                stack.pop();
            }else {
                stack.push(c);
            }
        }
        return stack.isEmpty();
    }
}
