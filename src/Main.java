public class Main {

    public static void main(String[] args) {
        // Scanner s = new Scanner(System.in); // scanner
        String nextTest = ""; // s.nextLine(); // input scanner
        String line = " sw $s3, 26($k1) ";
        line = Instructions.removeComment(line); //removes comment
        System.out.println(line);
        line = line.trim(); // remove boarder spaces

        if (line.contains(" ") == false) {
            Instructions.syscall();
        }
        else {
            int splitInstruction = line.indexOf(" ");
            String instruction = line.substring(0, splitInstruction); //instruction substring
            System.out.println("contains_space?" + instruction + "?"); //instruction contains no additional space
            String registers = line.substring(splitInstruction); // registers substring
            registers = registers.replace(")", "");
            registers = registers.replace("(", ",");
            String[] regArray; // Arrays for registers and intermediates
            String splitAtComma = ",";
            regArray = registers.split(splitAtComma);
            for (int i = 0; i < regArray.length; ++i) {
                regArray[i] = regArray[i].trim();
            }
            for (String s : regArray) {
                System.out.println("contains_space?" + s + "?");
            }

            String inst = null;
            if (Instructions.determineInstructionType(instruction).equals("R_Format")) {
                // run R format encoding
                inst = Instructions.rFormatEncoding(instruction, regArray[0], regArray[1], regArray[2]);

            } else if (Instructions.determineInstructionType(instruction).equals("I_Format")) {
                // run I format encoding
                inst = Instructions.iFormatEncoding(instruction, regArray);

            } else if (Instructions.determineInstructionType(instruction).equals("J_Format")) {
                // run J format encoding
                inst = Instructions.jFormatEncoding(regArray[0]);

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
            System.out.println("add: " + Instructions.determineInstructionType("add")); // r
            System.out.println("sub: " + Instructions.determineInstructionType("sub")); // r
            System.out.println("ori: " + Instructions.determineInstructionType("ori")); // i
            System.out.println("j: " + Instructions.determineInstructionType("j")); // j
            System.out.println("andi: " + Instructions.determineInstructionType("andi")); // i
            System.out.println("syscall: " + Instructions.determineInstructionType("syscall")); // syscall
            System.out.println("xyz: " + Instructions.determineInstructionType("xyz")); // unknown type

            // tests for removeComment
            System.out.println("tests for removeComment");
            System.out.println(Instructions.removeComment("add $t0, $t1, $t2 # This is a comment")); // add $t0, $t1, $t2
            System.out.println(Instructions.removeComment("lw $a0, 0($sp) # load value")); // lw $a0, 0($sp)
            System.out.println(Instructions.removeComment("# Only a comment")); // empty
        }
    }
}