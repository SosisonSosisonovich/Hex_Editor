package Test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import main.ByteArray;

import java.io.FileOutputStream;
import java.io.IOException;


public class ByteArrayTest {
    static byte[] bytes = {10, 20, 30, 40, 50};

    @Test
    public void getByteTest() throws IOException {
        File testFile = File.createTempFile("Test", ".txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(bytes);
        }
        ByteArray testByte = new ByteArray(testFile);

        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], testByte.getByte(i));
        }
        testFile.deleteOnExit();
    }

    @Test
    public void getSizeTest() throws IOException{
        File testFile = File.createTempFile("Test", ".txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(bytes);
        }
        ByteArray testByte = new ByteArray(testFile);

        assertEquals(bytes.length, testByte.getSize());

        testFile.deleteOnExit();
    }

}
