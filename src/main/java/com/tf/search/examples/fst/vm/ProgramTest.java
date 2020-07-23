package com.tf.search.examples.fst.vm;


import com.tf.search.examples.fst.FSTTestHelper;
import com.tf.search.utils.fst.FSTBuilder;
import com.tf.search.utils.fst.vm.Program;
import com.tf.search.utils.fst.vm.VirtualMachine;

public class ProgramTest {


    public void testReadProgramFromFile() throws Exception {

        String resource = "ipadic-allwords_uniq_sorted.csv";
//        String resource = "ipadic-allwords_uniqHead5000.csv";

        FSTTestHelper fstTestHelper = new FSTTestHelper();
        FSTBuilder fstBuilder = fstTestHelper.readIncremental(resource);

        Program program = fstBuilder.getFstCompiler().getProgram();
        program.outputProgramToFile("fstbytebuffer");


        VirtualMachine vm = new VirtualMachine();
        fstTestHelper.checkOutputWordByWord(resource, program, vm);

        Program readProgram = new Program();
        readProgram.readProgramFromFile("fstbytebuffer");

        fstTestHelper.checkOutputWordByWord(resource, readProgram, vm);
    }
}
