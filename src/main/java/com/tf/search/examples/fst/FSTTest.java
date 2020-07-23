package com.tf.search.examples.fst;

import com.tf.search.utils.fst.FST;
import com.tf.search.utils.fst.FSTBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FSTTest {

    private static FST fst;

    public static void main(String[] args) throws Exception {
        setUp();
        testFST();
        testMiss();
    }
    public static void setUp() throws IOException {
        // 写
        File output = new File("/Users/zxcs/Desktop/jafka-data/fst/fst-1.fst");
        //File output = File.createTempFile("fst-", ".fst");
        //output.deleteOnExit();

        String[] keys = new String[]{"cat", "cats", "dog"};
        int[] values = new int[]{1, 2, 4};

        FSTBuilder builder = new FSTBuilder();

        builder.createDictionary(keys, values);
        builder.getFstCompiler().getProgram().outputProgramToStream(
            new FileOutputStream(output)
        );

        // 读
        fst = new FST(new FileInputStream(output));
    }

    public static void testFST() throws IOException {
        System.out.println(fst.lookup("dog"));
        System.out.println(fst.lookup("cat"));
        System.out.println(fst.lookup("cats"));

    }

    public static void testMiss() throws Exception {
        System.out.println(fst.lookup("mouse"));
    }
}
