import java.io.*;

public class ByteArray {

    private byte[] byteArray;

    public ByteArray(int size){
        byteArray = new byte [size];
    }

    public ByteArray(String file) throws IOException {
        this(new File(file));
    }

    //считывает данные из файла и превращает в массив
     public ByteArray(File file) throws IOException{
        int size = (int) file.length();
        int offset = 0;
        byteArray = new byte[size];

        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            int index = 0;
            int count = 0;
            // count принимает значение кол-ва прочитанных байт или -1, если конец файла.
            while (index < size && (count = input.read(byteArray, index, byteArray.length - index)) > -1) {
                index += count;
            }
        }
    }

    //превращает считанные из потока данные в массив байтов
    public ByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byteArray = new byte[4096]; // создается временный массив для чтения файла блоками по 4 кбайт
        int count = 0;
        // count принимает значение кол-ва прочитанных байт или -1, если конец файла.
        while ((count=in.read(byteArray, 0,byteArray.length))>-1) {
            baos.write(byteArray, 0,count);
        }
        byteArray = baos.toByteArray();
    }

    public byte getByte(int offset) {
        return byteArray[offset];
    }


    public int getSize() {
        return byteArray.length;
    }
}
