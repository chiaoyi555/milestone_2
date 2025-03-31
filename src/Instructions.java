import java.util.ArrayList;
import java.util.List;

public class Instructions {

    private static final int MAX_16BIT = 0xFFFF;
    // method for determine whether it is r-format, i-format or j-format
    public static String determineInstructionType(String instruction) {
        String[] R_Format = { "add", "and", "or", "slt", "sub" };
        String[] I_Format = { "addiu", "andi", "beq", "bne", "lui", "lw", "ori", "sw" };
        String[] J_Format = { "j" };
        String[] pseudo_instructions = {"li", "la", "blt", "move"};


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
        for (String ins : pseudo_instructions) { // run the for loop to see whether the instruction is pseudo instructions
            if (ins.equals(instruction))
                return "pseudo_instructions";
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
        // instruction format: op<<26, rs<<21, rt<<16, int ends at 15
        int inst = 0;
        int opcode = 0;
        switch(instruction){
            case "addiu": // addiu array: [rt], [rs], [int]
                opcode = 0b001001; //pass in as addiu $rt, rs, int
                inst = RTRSIntermediate(opcode, regArray); // rt rs int method
                break;
            case "andi": // andi array: [rt], [rs], [int]
                opcode = 0b001100;
                inst = RTRSIntermediate(opcode, regArray); // rt rs int method
                break;
            case "beq": // beq array: [rs], [rt], [int]
                opcode = 0b000100;
                inst = RSRTIntermediate(opcode, regArray); // rs rt int method
                break;
            case "bne": // bne array: [rs], [rt], [int]
                opcode = 0b000101;
                inst = RSRTIntermediate(opcode, regArray); //rs rt int method
                break;
            case "lui":
                opcode = 0b001111;
                inst = RTIntermediate(opcode, regArray); // rt int method
                break;
            case "lw":
                opcode = 0b100011;
                inst = RTOffsetBase(opcode, regArray); // rt offset base method
                break;
            case "ori":
                opcode = 0b001101;
                inst = RTRSIntermediate(opcode, regArray); // rt rs int method
                break;
            case "sw":
                opcode = 0b101011;
                inst = RTOffsetBase(opcode, regArray); // rt offset base method
                break;
        }
        return String.format("%08x", inst); // return hexidecimal string of instruction;
    }


    // method for run J format encoding
    public static String jFormatEncoding(String instr_index) {
        String[] split = instr_index.split(" ");
        String label = split[1];
        int target = Integer.parseInt(split[1]);
        int j = 0b000010;
        //int offset = 0b00010000000000000001001100;
        //offset = offset >> 2;
        //0000 | 10 - 00 0100 0000 0000 0000 0100 1100
        // shift right 2 0000 1000 0001 0000 0000 0000 0001 0011
        // 08100013
        // 0840004b
        int inst = 0;
        inst = inst | (target);
        inst = inst | (j << 26);
        // 0000 | 10
        // j = 0x0800000
        // target =
        return String.format("%08x", inst);
    }

    public static List<String> handlePseudoInstruction(String instruction, String[] regArray) {
        List<String> instructions = new ArrayList<>();

        switch(instruction){
            case "li":
                int value = Integer.parseInt(regArray[1]);
                if(value > MAX_16BIT){ // lui + ori
                    instructions.add(iFormatEncoding("lui", new String[]{regArray[0], String.valueOf(value >>> 16)}));
                    instructions.add(iFormatEncoding("ori", new String[]{regArray[0], regArray[0], String.valueOf(value & 0xFFFF)}));
                }else {
                    instructions.add(iFormatEncoding("ori", new String[]{regArray[0], "$zero", regArray[1]}));
                }
                break;
            case "la":
                // deal with this in TextSection
                break;
            case "blt": // slt $at, $rs, $rt + bne $at, $zero, label
                instructions.add(rFormatEncoding("slt", regArray[0], regArray[1], "$at")); // slt $at, $rs, $rt
                instructions.add(iFormatEncoding("bne", new String[]{"$at", "$zero", regArray[2]})); // bne $at, $zero, label
                break;
            case "move": // add $d, $s, $zero
                instructions.add(rFormatEncoding("add", regArray[0], regArray[1], "$zero"));
                break;
        }
        return instructions;
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
        twosComplement = -Integer.parseInt(num[1]); //adding sign after parse
        twosComplement = ~(~twosComplement); // 2's complement of a negative number
        return twosComplement;
    }
    public static int trimIntermediate(int intermediate){

            intermediate =  intermediate << 16;
            intermediate =  intermediate >> 16;

        return intermediate;
    }
    //pass in as addiu rt, rs, int op: 0b001001
    //li $v0, 4 -> addiu, $v0, $zero, 4
    //lui + ori
    public static int RTRSIntermediate(int opcode, String []regArray){
        int inst = 0;
        int intermediate = 0;
        Integer rs, rt;
        if(regArray[2].contains("-")) {
            intermediate = parseNegative(regArray[2]); // convert to 2's complement
        }
        else if(regArray[2].contains("0x")){
            String target = regArray[2].toString();
            String[] split = target.split("x");
            intermediate = Integer.parseInt(split[1], 16);
        }
        else{
            intermediate = Integer.parseInt(regArray[2]); // parse string (int)
        }

        rs = Register.getRegisterNumber(regArray[1]); // rs register number
        rt = Register.getRegisterNumber(regArray[0]); // rt register number
        if (rs == null || rt == null) {
            throw new IllegalArgumentException("Invalid register name");
        }
        intermediate = trimIntermediate(intermediate);
        Integer.toBinaryString(rs); // covert rs to number
        Integer.toBinaryString(rt); // covert rt to number
        inst |= (opcode << 26);
        inst |= (rs << 21);
        inst |= (rt << 16);
        inst |= intermediate;
        return inst;
    }
    // bne & beq: rs rt label
    public static int RSRTIntermediate(int opcode, String [] regArray){
        int inst = 0;
        float intermediate = 0.0F; // int >> 2 rounds down, we want to round up so need float/double(cant use shift on this)
        Integer rs, rt;
        if(regArray[2].contains("-")) {
            intermediate = parseNegative(regArray[2]); // convert to 2's complement
        }
        else if(regArray[2].contains("0x")){
            String target = regArray[2];
            String[] split = target.split("x");
            intermediate = (float)Integer.parseInt(split[1], 16);
        }
        else{
            intermediate = (float)Integer.parseInt(regArray[2]); // parse string (int)
        }

        intermediate /= 4; // can't >> 2 a float number
        int offset = (int) (intermediate); // convert back to int
        rs = Register.getRegisterNumber(regArray[0]); // rs register number
        rt = Register.getRegisterNumber(regArray[1]); // rt register number
        if (rs == null || rt == null) {
            throw new IllegalArgumentException("Invalid register name");
        }
        offset = trimIntermediate(offset);
        Integer.toBinaryString(rs); // covert rs to number
        Integer.toBinaryString(rt); // covert rt to number
        inst |= (opcode << 26);
        inst |= (rs << 21);
        inst |= (rt << 16);
        inst |= offset;
        return inst;
    }
    // lui
    public static int RTIntermediate(int opcode, String [] regArray){
        int inst = 0;
        int intermediate = 0;
        Integer rt;
        if(regArray[1].contains("-")) {
            intermediate = parseNegative(regArray[1]); // convert to 2's complement
        }
        else if(regArray[1].contains("0x")){
            String target = regArray[1].toString();
            String[] split = target.split("x");
            intermediate = Integer.parseInt(split[1], 16);
        }
        else{
            intermediate = Integer.parseInt(regArray[1]); // parse string (int)
        }
        rt = Register.getRegisterNumber(regArray[0]); // rt register number
        if (rt == null) {
            throw new IllegalArgumentException("Invalid register name");
        }
        Integer.toBinaryString(rt); // covert rt to number
        inst |= (opcode << 26);
        inst |= (rt <<16);
        inst |= intermediate;
        return inst;
    }
    public static int RTOffsetBase(int opcode, String [] regArray){
        int inst = 0;
        int intermediate = 0;
        Integer rt, base;
        if(regArray[1].contains("-")) {
            intermediate = parseNegative(regArray[1]); // convert to 2's complement
        }
        else if(regArray[1].contains("0x")){
            String target = regArray[1].toString();
            String[] split = target.split("x");
            intermediate = Integer.parseInt(split[1], 16);
        }
        else if(regArray[1].equals("")){
            intermediate = 0;
        }
        else{
            intermediate = Integer.parseInt(regArray[1]); // parse string (int)
        }

        intermediate = trimIntermediate(intermediate); // trim sign-bit to 16 bits
        rt = Register.getRegisterNumber(regArray[0]); // rt register number
        base = Register.getRegisterNumber(regArray[2]);
        if (rt == null) {
            throw new IllegalArgumentException("Invalid register name");
        }
        Integer.toBinaryString(rt); // covert rt to number
        inst |= (opcode << 26);
        inst |= (base<<21);
        inst |= (rt << 16);
        inst |= intermediate;
        return inst;
    }
}
