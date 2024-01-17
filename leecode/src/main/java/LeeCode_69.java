public class LeeCode_69 {
    public static int mySqrt(int x) {
        int half = 0;
        while (half * half <= x) {
            half++;
        }
        return half - 1;
    }

    public static void main(String[] args) {
        int i = mySqrt(1000000000);
        System.out.println(i);
    }
}
 n