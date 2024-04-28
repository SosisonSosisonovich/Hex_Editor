import java.io.*;

public class ByteArray {
    private byte[] byteArray;

    public ByteArray(File file) throws IOException {
        int size = (int) file.length();
        int offset = 0;
        byteArray = new byte[size];

        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[4096];
            int index = 0;
            int count = 0;
            // count принимает значение кол-ва прочитанных байт или -1, если конец файла.
            while (index < size && (count = input.read(buffer, 0, 4096)) > -1){
                System.arraycopy(buffer, 0, byteArray, index, count);
                index += count;
            }
        }
    }

    public byte getByte(int offset) {
        return byteArray[offset];
    }

    public int getSize() {
        return byteArray.length;
    }
}
