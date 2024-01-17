import java.util.ArrayList;
import java.util.TreeMap;

/**
 * 回文数相关
 */
public class LeeCode_9 {
    public static boolean isPalindrome(int x) {
        if (x < 0) {
            return false;
        }
        ArrayList<Integer> list = new ArrayList<>();
        while (x != 0) {
            list.add(x % 10);
            x = x / 10;
        }
        for (int start = 0, end = list.size() - 1; start < list.size(); start++, end--) {
            if (!list.get(start).equals(list.get(end))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 其实只需要看一半的数是否相等就行了
     *
     * @param x
     * @return
     */
    public static boolean isPalindrome2(int x) {
        if (x < 0 || x % 10 != 0) {
            return false;
        }
        int reserveNumber = 0;
        while (x > reserveNumber) {
            // 如果是偶数 翻转一半数字 那么 此时如果它是回文数 那么 X==reserveNumber
            // 如果是奇数的情况下，多计算一次，如果是 翻转的数 最低位去掉 跟 x相等 那么 就是回文数 因为最中间的数不影响它是否是回文数
            reserveNumber = reserveNumber * 10 + x % 10; // 翻转数字的关键代码
            x = x / 10;
        }

        return x == reserveNumber || x == reserveNumber / 10;


    }

    public static void main(String[] args) {
        TreeMap<Object, Object> objectObjectTreeMap = new TreeMap<>();


    }
}