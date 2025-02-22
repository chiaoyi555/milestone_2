// import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Scanner s = new Scanner(System.in); // scanner
        String line = "add $a1, $t8, $t9"; // s.nextLine(); // input scanner
        line = line.replace(",", ""); // replace , into space
        System.out.println(line);
        String trim = line.trim(); // remove spaces
        String remove = " "; // places to split the string

        String[] seperate = line.split(remove); // array to contain split strings
        for (int i = 0; i < seperate.length; i++) {
            System.out.print("[" + i + "]");
            System.out.println(seperate[i]);
        }

        String inst = null;
        if (determineInstructionType(seperate[0]) == "R_Format") {
            // run R format encoding
            inst = rFormatEncoding(seperate[0], seperate[1], seperate[2], seperate[3]);

        } else if (determineInstructionType(seperate[0]) == "I_Format") {
            // run I format encoding
            inst = iFormatEncoding();

        } else if (determineInstructionType(seperate[0]) == "J_Format") {
            // run J format encoding
            inst = jFormatEncoding(seperate[1]);

        } else if (determineInstructionType(seperate[0]) == "syscall") {
            // run syscall encoding
            inst = syscall();

        }

        System.out.println(inst);

        // tests for Register.java
        System.out.println(Register.getRegisterNumber("$t0")); // 8
        System.out.println(Register.getRegisterNumber("$s7")); // 23
        System.out.println(Register.getRegisterNumber("$ra")); // 31
        System.out.println(Register.getRegisterNumber("$xyz")); // null
        System.out.println(Register.getRegisterNumber("$s0")); // 16
        System.out.println(Register.getRegisterNumber("$a1")); // 5
        System.out.println(Register.getRegisterNumber("$fp")); // 30
        System.out.println(Register.getRegisterNumber(" ")); // null

        // tests for determineInstructionType method
        System.out.println("add: " + determineInstructionType("add")); // r
        System.out.println("sub: " + determineInstructionType("sub")); // r
        System.out.println("ori: " + determineInstructionType("ori")); // i
        System.out.println("j: " + determineInstructionType("j")); // j
        System.out.println("andi: " + determineInstructionType("andi")); // i
        System.out.println("syscall: " + determineInstructionType("syscall")); // syscall
        System.out.println("xyz: " + determineInstructionType("xyz")); // unknown type
        
        // tests for removeComment
        System.out.println("tests for removeComment");
        System.out.println(removeComment("add $t0, $t1, $t2 # This is a comment")); // add $t0, $t1, $t2
        System.out.println(removeComment("lw $a0, 0($sp) # load value")); // lw $a0, 0($sp)
        System.out.println(removeComment("# Only a comment")); // empty
    
    }

    // method for determine whether it is r-format, i-format or j-format
    public static String determineInstructionType(String instruction) {
        String[] R_Format = { "add", "and", "or", "slt", "sub" };
        String[] I_Format = { "addi", "andi", "beq", "bne", "lui", "lu", "ori", "su" };
        String[] J_Format = { "j" };

        if (instruction == null) {
            return "Unknown Type";
        }

        for (String ins : R_Format) { // run the for loop to see whether the instruction is R format
            if (ins.equals(instruction))
                return "R_Format";
        }
        for (String ins : I_Format) { // run the for loop to see whether the instruction is I format
            if (ins.equals(instruction))
                return "I_Format";
        }
        for (String ins : J_Format) { // run the for loop to see whether the instruction is J format
            if (ins.equals(instruction))
                return "J_Format";
        }
        if (instruction.equals("syscall"))
            return "syscall"; // otherwise it would be syscall
        return "Unknown Type";

    }

    // method for run R format encoding
    public static String rFormatEncoding(String instruction, String registerRd, String registerRs, String registerRt) {
        int opcode = 0;
        int shamt = 0;
        int func = 0;

        // to see what kind of instruction we get
        switch (instruction) {
            case "add":
                func = 0b100000;
                break;
            case "sub":
                func = 0b100010;
                break;
            case "and":
                func = 0b100100;
                break;
            case "or":
                func = 0b100101;
                break;
            case "slt":
                func = 0b101010;
                break;
            default:
                throw new IllegalArgumentException("Invalid R-type instruction: " + instruction);
        }

        
        // change rd, rs,rt iinto number
        Integer rd = Register.getRegisterNumber(registerRd);
        Integer rs = Register.getRegisterNumber(registerRs);
        Integer rt = Register.getRegisterNumber(registerRt);

        //check
        if (rd == null || rs == null || rt == null) {
            throw new IllegalArgumentException("Invalid register name");
        }

        // turn rd, rs, rt into binary
        Integer.toBinaryString(rd);
        Integer.toBinaryString(rs);
        Integer.toBinaryString(rt);

        int inst = 0;
        inst |= (opcode << 26);
        inst |= (rs << 21);
        inst |= (rt << 16);
        inst |= (rd << 11);
        inst |= (shamt << 6);
        inst |= func;

        return String.format("%08x", inst);
    }

    // method for run I format encoding
    public static String iFormatEncoding() {
        return null;
    }

    // method for run J format encoding
    public static String jFormatEncoding(String instr_index) {
        String[] split = instr_index.split("x");
        int target = Integer.parseInt(split[1], 16);
        int j = 0b000010; 
        
        int inst = 0;
        inst = inst | (target << 0);
        inst = inst | (j << 26);
        return String.format("%08x", inst);
    }

    // method for run syscall encoding
    public static String syscall() {
        String inst = "0000000c";
        return inst;
    }

    // remove comment
    public static String removeComment(String input) {
        return input.split("#")[0].trim(); 
    }
}

