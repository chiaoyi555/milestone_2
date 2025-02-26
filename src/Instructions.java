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
        int inst = 0, opcode = 0, intermediate = 0;
        Integer rs, rt;
        // instruction format: op<<26, rs<<21, rt<<16, int ends at 15
        switch(instruction){
            case "addiu":
                opcode = 0b001001;
                break;
            case "andi":
                // array order: [rt], [rs], [int]
                opcode = 0b001100;
                if(regArray[2].contains("-")) {
                    intermediate = parseNegative(regArray[2]); // convert to 2's complement
                }
                else if(regArray[2].contains("0x")){
                    intermediate = Integer.parseInt(regArray[2], 16); // parse string (hexi)
                }
                else{
                    intermediate = Integer.parseInt(regArray[2]); // parse string (int)
                }
                intermediate = trimIntermediate(intermediate); // trim sign-bit to 16 bits
                rs = Register.getRegisterNumber(regArray[1]); // rs register number
                rt = Register.getRegisterNumber(regArray[0]); // rt register number
                if (rs == null || rt == null) {
                    throw new IllegalArgumentException("Invalid register name");
                }
                Integer.toBinaryString(rs); // covert rs to number
                Integer.toBinaryString(rt); // covert rt to number
                inst |= (opcode << 26);
                inst |= (rs << 21);
                inst |= (rt << 16);
                inst |= intermediate;
                return String.format("%08x", inst); // return hexidecimal string of instruction
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
    // parse negative number
    public static int parseNegative(String negativeNum){
        int twosComplement;
        String splitAt = "-";
        String []num = negativeNum.split(splitAt); // removing - sign
        System.out.println(twosComplement = -Integer.parseInt(num[1])); //adding sign after parse
        twosComplement = ~(~twosComplement); // 2's complement of a negative number
        return twosComplement;
    }
    public static int trimIntermediate(int intermediate){
        int shift = (int)(Math.pow(2,16)-1);
        int trim = intermediate & shift;
        System.out.println(trim);
        return trim;
    }
}
