package com.tf.search.examples.fst.vm;


import com.tf.search.utils.fst.vm.Instruction;
import com.tf.search.utils.fst.vm.Program;
import com.tf.search.utils.fst.vm.VirtualMachine;

import java.util.Arrays;
import java.util.List;

public class VirtualMachineTest {

    public void testHelloVM()
    {
        VirtualMachine vm = new VirtualMachine(false);
        Program program = new Program();

        Instruction instruction = new Instruction();
        instruction.setOpcode(Program.HELLO);

        Instruction instructionFail = new Instruction();
        instructionFail.setOpcode(Program.FAIL);

        program.addInstruction(instruction);
        program.addInstruction(instructionFail);

        vm.run(program, "");

    }

    public void testMatch() throws Exception {
        // testing the input string "a" being accepted or not
        VirtualMachine vm = new VirtualMachine(false);
        Program program = new Program();
        Instruction instructionMatch = new Instruction();
        instructionMatch.setOpcode(Program.MATCH);
        instructionMatch.setArg1('a'); // transition string
        instructionMatch.setArg2(1);  // target address, delta coded
        instructionMatch.setArg3(1); // output, value to be accumulated;

        Instruction instructionAccept = new Instruction();
        instructionAccept.setOpcode(Program.ACCEPT);

        program.addInstructions(
                Arrays.asList(instructionAccept, instructionMatch)
        );

        //assertEquals(1, vm.run(program, "a"));
    }
}