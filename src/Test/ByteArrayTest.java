package Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import App.ByteArray;

import java.io.FileOutputStream;
import java.io.IOException;


public class ByteArrayTest {
    static byte[] bytes = {10, 20, 30, 40, 50};
    private static ByteArray testByte;

    @BeforeEach
    public void setUp() throws IOException {
        File testFile = File.createTempFile("test",".txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(bytes);
        }
        testByte = new ByteArray(testFile);
    }
    @Test
    private void getByteTest() throws IOException {
        File testFile = File.createTempFile("test", ".txt");
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            fos.write(bytes);
        }
        ByteArray testByte = new ByteArray(testFile);

        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], testByte.getByte(i));
        }
    }

    @Test
    public void getSizeTest() throws IOException{
        assertEquals(bytes.length, testByte.getSize());
    }
}
