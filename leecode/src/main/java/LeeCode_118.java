import java.util.ArrayList;
import java.util.List;

/**
 * 杨辉三角构建 给出层数
 * 动态规划法：使用前一层 构建后面一层
 */
public class LeeCode_118 {
    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> triangle = new ArrayList<List<Integer>>();

        for (int i = 0; i < numRows; i++) {
            List<Integer> row = new ArrayList<Integer>();
            for (int j = 0; j <= i; j++) { // 每一行有 n + 1个数
                if (j == 0 || j == i) {
                    row.add(1); // 第一个和最后一个 都是 1
                } else {
                    row.add(triangle.get(i-1).get(j-1)+triangle.get(i-1).get(j));
                }

            }
            triangle.add(row);
        }
        return triangle;
    }
}
