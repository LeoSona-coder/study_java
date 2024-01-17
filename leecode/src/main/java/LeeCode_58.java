public class LeeCode_58 {
    public static int lengthOfLastWord(String s) {
        int n = s.length();
        char[] charArray = s.toCharArray();
        int charNumber = 0;

        for (int i = n - 1; i >= 0; i--) {
            if (charArray[i] != ' '){
                charNumber++;
            } else if (charArray[i] == ' ' && charNumber != 0) {
                return charNumber;
            }
            if (i == 0) {
                return charNumber;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        System.out.println(lengthOfLastWord("a"));

    }
}
