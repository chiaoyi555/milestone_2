
public class Main {


    public static void main(String[] args) {
        // Scanner s = new Scanner(System.in);
        // String line = s.nextLine();
        // char c = '$';
        // line.substring(c);


        // tests
        System.out.println("add:"+determineInstructionType("add"));
        System.out.println("sub:"+determineInstructionType("sub"));
        System.out.println("ori:"+determineInstructionType("ori"));
        System.out.println("j:"+determineInstructionType("j"));
        System.out.println("andi:"+determineInstructionType("andi"));
        System.out.println("syscall:"+determineInstructionType("syscall"));
        
    }

    // method for determine whether it is r-format, i-format or j-format
    public static String determineInstructionType(String instruction) {
        String[] R_Format = { "add", "and", "or", "slt", "sub" };
        String[] I_Format = { "addi", "andi", "beq", "bne", "lui", "lu", "ori", "su" };
        String[] J_Format = { "j" };

        if (instruction == null) {
            return "Unknown Type"; 
        }
      
        for(String ins : R_Format){ // run the for loop to see whether the instruction is R format
            if (ins.equals(instruction))
            return "R_Format";
        }
        for(String ins : I_Format){ // run the for loop to see whether the instruction is I format
            if (ins.equals(instruction))
            return "I_Format";
        }
        for(String ins : J_Format){ // run the for loop to see whether the instruction is J format
            if (ins.equals(instruction))
            return "J_Format";
        }
        return "syscall";  // otherwise it would be syscall 
        
    }

}