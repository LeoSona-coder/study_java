package InputStream;

import java.io.*;

public class InputStreamSample {
    public static void main(String[] args) {
        File file = new File("file1/a.txt");
        File file2 = new File("file2/b.txt");
        try (
                FileWriter fw = new FileWriter(file);
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(file2)
        ) {
            fw.write("刘佳慧是一个大笨蛋！");
            fw.flush();
            int length;
            byte[] buffer = new byte[4096];
            while ((length = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
                //
            }
        } catch (IOException e) {
            System.out.println("文件写入异常！");
        }



    }
}
