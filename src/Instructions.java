public class Instructions {

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
    public static String iFormatEncoding(String instruction, String [] regArray) {
        int opcode = 0;
        switch(instruction){
            case "addiu":
                opcode = 0b001001;
                break;
            case "addi":
                // instruction format: op<<26, rs<<21, rt<<16, int ends at 15
                // array format: [rt], [rs], [int]
                opcode = 0b001100;
                int num;
                if(regArray[2].contains("-")) {
                    String value = regArray[2].substring(1); // removing - sign
                    num = Integer.parseInt(value);
                    num = ~(~num); // 2's complement of a negative number
                }
                if(regArray[2].contains("x")){
                    num = Integer.parseInt(regArray[2], 16);
                }
                break;
            case "beq":
                opcode = 0b0;
                break;
            case "bne":
                opcode = 0b0;
                break;
            case "lui":
                opcode = 0b0;
                break;
            case "lw":
                opcode = 0b0;
                break;
            case "ori":
                opcode = 0b0;
                break;
            case "sw":
                opcode = 0b0;
                break;
        }
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
