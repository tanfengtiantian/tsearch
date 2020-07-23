package com.tf.search.utils.fst;



import com.tf.search.utils.fst.vm.Program;
import com.tf.search.utils.fst.vm.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;

public class FST {

    private final VirtualMachine vm = new VirtualMachine();

    private Program program;

    public FST(InputStream input) throws IOException {
        this.program = new Program();
        this.program.readProgramFromFile(input);
    }

    public int lookup(String input) {
        return vm.run(program, input);
    }
}
