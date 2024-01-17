import java.util.HashMap;

public class RomanNumber {
    public static int romanToInt(String s) {
        int length = s.length();

        HashMap<Character, Integer> romanMap = new HashMap<>();
        romanMap.put('I', 1);
        romanMap.put('V', 5);
        romanMap.put('X', 10);
        romanMap.put('L', 50);
        romanMap.put('C', 100);
        romanMap.put('D', 500);
        romanMap.put('M', 1000);

        int result = 0;

        for (int current = 0; current < length; current++) {
            char currentChar = s.charAt(current);
            Integer currentNumber = romanMap.get(currentChar);
            if (current != length - 1) {
                char nextChar = s.charAt(current + 1);
                Integer nextNumber = romanMap.get(nextChar);

                if (nextNumber <= currentNumber) {
                    result = result + currentNumber;
                } else {
                    result = result - currentNumber;
                }
            } else {
                result = result + currentNumber;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(romanToInt("III"));
    }
}
