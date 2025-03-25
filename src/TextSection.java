import java.util.ArrayList;
import java.util.List;

public class TextSection {
    /*example
    *   li $t1, 1
    *   li $t2, 3
    * loop:
    *   addi $t1, $t1, 1
    *   beq $t1, $t2, EXIT
    *   j loop
    * EXIT:
    *   li $v0, 10
    *   syscall
    * */

    //The first instruction in the .text section should be at 0x00400000
    private static final int TEXT_START_ADDRESS = 0x00400000;

    private final List<String> labels = new ArrayList<>(); // store the label we declared
    private final List<Integer> addresses = new ArrayList<>(); // store data's address
    private final List<String> machineCode = new ArrayList<>();
    private int currentAddress = TEXT_START_ADDRESS;

    public void parseTextSection(ArrayList<String> textLines) {
        for(String line: textLines){
            if (line.isEmpty() || line.startsWith("#")) continue; // skip empty line or comment

            if (line.contains(":")) { // if this line is label
                String label = line.replace(":", "").trim();
                labels.add(label);
                addresses.add(currentAddress);
                continue;
            }

            int splitInstruction = line.indexOf(" ");

            String instruction = line.substring(0, splitInstruction); //instruction substring
            String registers = line.substring(splitInstruction).trim(); // registers substring
            registers = registers.replace(")", "");
            registers = registers.replace("(", ",");
            String[] regArray = registers.split(","); // Arrays for registers and intermediates

            for (int i = 0; i < regArray.length; i++) {
                regArray[i] = regArray[i].trim();
            }

            String inst = null;
            String type = Instructions.determineInstructionType(instruction);
            if (type.equals("R_Format")) {
                // run R format encoding
                inst = Instructions.rFormatEncoding(instruction, regArray[0], regArray[1], regArray[2]);
            } else if (type.equals("I_Format")) {
                // run I format encoding
                inst = Instructions.iFormatEncoding(instruction, regArray);
            } else if (type.equals("J_Format")) {
                // run J format encoding
                inst = Instructions.jFormatEncoding(regArray[0]);
            } else if (instruction.equals("syscall")) {
                inst = Instructions.syscall();
            }

            if (inst != null) {
                machineCode.add(inst); // store machine code
                currentAddress += 4; // update address
            }

        }
    }

    // Replace the label with the actual offset or address (beq, bne, j, la)
    public void resolveLabels(DataSection dataSection) {
        
    }

    public int getLabelAddress(String label) {
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i).equals(label)) {
                return addresses.get(i);
            }
        }
        return -1; // there is no this label
    }

    public List<String> getMachineCode() {
        return machineCode;
    }
}
