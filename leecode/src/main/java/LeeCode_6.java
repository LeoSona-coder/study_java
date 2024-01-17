import java.util.ArrayList;
import java.util.List;

public class LeeCode_6 {
    public String convert(String s, int r) {
        int n = s.length();

        if (r == 1 || r >= n) {
            return s;
        }
        int t = 2 * r - 2;

        int c = (n / t + 1) * (1 + r - 2);// 一个周期的列数 等于第一列 + r-2 列
        char[][] chars = new char[r][c];


        for (int i = 0, x = 0, y = 0; i < n; i++) {
            chars[x][y] = s.charAt(i);
            if (i % t < r - 1) {
                x++; // 向下移动
            } else {
                x--;
                y++; // 向右上移动
            }
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (chars[i][j] != 0) {
                    sb.append(chars[i][j]);
                }
            }
        }
        return sb.toString();
    }

    public String convert2(String s, int r) {
        List<StringBuilder> rows = new ArrayList<>();
        for (int i = 0; i < r; i++) rows.add(new StringBuilder());
        int i = 0;
        int flag = 1;
        for (char c : s.toCharArray()) {
            rows.get(i).append(c);
            if (i == 0 || i == r - 1) {
                flag = -flag;
            }
            i = i + flag;
        }
        StringBuilder result = new StringBuilder();
        for (int j = 0; j < r; j++) result.append(rows.get(j));
        return result.toString();

    }
}
