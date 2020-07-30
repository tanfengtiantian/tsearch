package com.tf.search.examples.fst;


import com.tf.search.lucene.fst.*;
import com.tf.search.lucene.utils.BytesRef;
import com.tf.search.lucene.utils.BytesRefBuilder;
import com.tf.search.lucene.utils.IntsRef;
import com.tf.search.lucene.utils.IntsRefBuilder;
import java.io.IOException;
import java.nio.file.Paths;

public class TestLuceneFST {


    public static void main1(String[] args) throws IOException {
        //write();
        read();
    }

    private static void write() throws IOException {

        //String inputValues[] = {"cat", "dog", "dogs", "dogs1"}; // in order
        String inputValues[] = {"谭枫", "谭", "彭华丽", "黄国玉"};
        long outputValues[] = {5, 7, 12, 28}; // must be positive
        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        Builder<Long> builder = new Builder<Long>(FST.INPUT_TYPE.BYTE1, outputs);
        BytesRefBuilder bytesRefBuilder = new BytesRefBuilder();
        IntsRefBuilder scratchInts = new IntsRefBuilder();
        for (int i = 0; i < inputValues.length; i++) {
            bytesRefBuilder.copyChars(inputValues[i]);
            builder.add(Util.toIntsRef(bytesRefBuilder.get(), scratchInts), outputValues[i]);
        }
        FST<Long> fst = builder.finish();
        fst.save(Paths.get("/Users/zxcs/Desktop/tf/Index-Data/index.tip"));

    }
    private static void read() throws IOException {
        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        BytesRefBuilder bytesRefBuilder = new BytesRefBuilder();
        FST<Long> fst =  FST.read(Paths.get("/Users/zxcs/Desktop/tf/Index-Data/index.tip"),outputs);
        // retrieval by key
        Long value = Util.get(fst, new BytesRef("dog"));
        System.out.println(value);

        // retrieval by value
        IntsRef ref = Util.getByOutput(fst, 12);
        String input = Util.toBytesRef(ref, bytesRefBuilder).utf8ToString();
        System.out.println(input);

        // scanning
        IntsRefFSTEnum<Long> fstEnum = new IntsRefFSTEnum<>(fst);
        IntsRefFSTEnum.InputOutput<Long> inputOutput;
        while ((inputOutput = fstEnum.next()) != null) {
            input = Util.toBytesRef(inputOutput.input, bytesRefBuilder).utf8ToString();
            Long output = inputOutput.output;
            System.out.println(input + "\t" + output);
        }

    }

    public static void main3(String[] args) throws IOException {
        String[] inputValues = {"谭枫", "谭", "彭华丽", "黄国玉"}; // in order
        String[] outputValues = {"ab", "abc", "abd", "bcddppppppp"}; // must be positive
        ByteSequenceOutputs outputs = ByteSequenceOutputs.getSingleton();

        Builder<BytesRef> builder = new Builder<>(FST.INPUT_TYPE.BYTE1, outputs);
        BytesRefBuilder bytesRefBuilder = new BytesRefBuilder();
        IntsRefBuilder scratchInts = new IntsRefBuilder();
        for (int i = 0; i < inputValues.length; i++) {
            bytesRefBuilder.copyChars(inputValues[i]);
            //builder.add(Util.toIntsRef(bytesRefBuilder.get(), scratchInts), outputValues[i]);
            scratchInts.copyUTF8Bytes(bytesRefBuilder.get());
            builder.add(scratchInts.get(), new BytesRef(outputValues[i]));
        }
        FST<BytesRef> fst = builder.finish();

        // retrieval by key
        BytesRef value = Util.get(fst, new BytesRef("谭枫"));
        // scanning
        IntsRefFSTEnum<BytesRef> fstEnum = new IntsRefFSTEnum<>(fst);
        IntsRefFSTEnum.InputOutput<BytesRef> inputOutput;
        while ((inputOutput = fstEnum.next()) != null) {
            String input = Util.toBytesRef(inputOutput.input, bytesRefBuilder).utf8ToString();
            BytesRef output = inputOutput.output;
            String out = output.utf8ToString();
            System.out.println(input + "\t" + out);
        }

        // Deduction

    }



    public static void main4(String[] args) throws IOException {
        String inputValues[] = {"谭枫", "谭枫枫", "彭华丽", "黄国玉"};
        long outputValues[] = {5, 7, 12, 28}; // must be positive
        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        Builder<Long> builder = new Builder<Long>(FST.INPUT_TYPE.BYTE1, outputs);
        BytesRefBuilder bytesRefBuilder = new BytesRefBuilder();
        IntsRefBuilder scratchInts = new IntsRefBuilder();
        for (int i = 0; i < inputValues.length; i++) {
            bytesRefBuilder.copyChars(inputValues[i]);
            builder.add(Util.toIntsRef(bytesRefBuilder.get(), scratchInts), outputValues[i]);
        }
        FST<Long> fst = builder.finish();

        // retrieval by key
        Long value = Util.get(fst, new BytesRef("谭枫1"));
        System.out.println(value);

        // retrieval by value
        IntsRef ref = Util.getByOutput(fst, 12);
        String input = Util.toBytesRef(ref, bytesRefBuilder).utf8ToString();
        System.out.println(input);

        // scanning
        IntsRefFSTEnum<Long> fstEnum = new IntsRefFSTEnum<>(fst);
        IntsRefFSTEnum.InputOutput<Long> inputOutput;
        while ((inputOutput = fstEnum.next()) != null) {
            input = Util.toBytesRef(inputOutput.input, bytesRefBuilder).utf8ToString();
            Long output = inputOutput.output;
            System.out.println(input + "\t" + output);
        }
    }


    public static void main(String[] args) throws IOException {
        String inputValues[] = {"谭枫", "谭枫枫", "彭华丽"};
        byte[] outputValues[] = {{1,2}, {3,4}, {5,6}};
        Outputs<BytesRef> outputs = ByteSequenceOutputs.getSingleton();
        Builder<BytesRef> builder = new Builder<>(FST.INPUT_TYPE.BYTE1, outputs);
        BytesRefBuilder scratchBytes  = new BytesRefBuilder();
        IntsRefBuilder scratchInts = new IntsRefBuilder();

        for (int i = 0; i <inputValues.length ; i++) {
            scratchBytes.copyChars(inputValues[i]);
            builder.add(Util.toIntsRef(scratchBytes.get(), scratchInts), new BytesRef(outputValues[i]));

        }
        FST<BytesRef> fst = builder.finish();

        BytesRef value = Util.get(fst,new BytesRef("彭华丽"));

        System.out.println(value);


    }
}
