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
    private final List<String> temp = new ArrayList<>();
    private int currentAddress = TEXT_START_ADDRESS;
    private DataSection data;

    public void parseTextSection(ArrayList<String> textLines) {
        for(String line: textLines){
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue; // skip empty line or comment
            if(line.contains("syscall")){
                machineCode.add(Instructions.syscall());
                currentAddress += 4;
            }
            else if (line.contains(":")) { // if this line is label
                int colon = line.indexOf(":");
                int instructionEnd = line.indexOf(" ");
                String label = line.substring(0, colon);
                String removeLabel = line.substring(colon).trim();
                String instruction = removeLabel.substring(0, instructionEnd);
                String registers = removeLabel.substring(instructionEnd);
                registers = registers.replace(")", "").replace("(", ",");
                String [] regArray = registers.split(",");
                for(String r: regArray){
                    r.trim();
                }
                String type = Instructions.determineInstructionType(instruction);
                if (type.equals("R_Format")) {
                    // run R format encoding
                    machineCode.add(Instructions.rFormatEncoding(instruction, regArray[0], regArray[1], regArray[2]));
                    currentAddress += 4;
                } else if (type.equals("I_Format")) {
                    // run I format encoding
                    machineCode.add(Instructions.iFormatEncoding(instruction, regArray));
                    currentAddress += 4;
                } else if (type.equals("J_Format")) {
                    // run J format encoding
                    machineCode.add(Instructions.jFormatEncoding(regArray[0]));
                    currentAddress += 4;
                } else if (type.equals("pseudo_instructions")) {
                    //handle the Pseudo Instruction
                    if (instruction.equals("la")) { // keep la
                        machineCode.add("la " + regArray[0] + " " + regArray[1]);
                        currentAddress += 8;  // lui + ori, leave 8 bytes for la
                    } else {
                        List<String> pseudoInstructions = Instructions.handlePseudoInstruction(instruction, regArray);
                        for (String pseudoInst : pseudoInstructions) {
                            machineCode.add(pseudoInst);
                            currentAddress += 4;
                        }
                    }
                } else if (instruction.equals("syscall")) {
                    machineCode.add(Instructions.syscall());
                    currentAddress += 4;
                }
                labels.add(label);
                addresses.add(currentAddress);
            }
            else {
                line = line.trim();
                int splitInstruction = line.indexOf(" ");

                String instruction = line.substring(0, splitInstruction); //instruction substring
                String registers = line.substring(splitInstruction).trim(); // registers substring
                registers = registers.replace(")", "").replace("(", ",");
                String[] regArray = registers.split(","); // Arrays for registers and intermediates

                for (int i = 0; i < regArray.length; i++) {
                    regArray[i] = regArray[i].trim();
                }

                String type = Instructions.determineInstructionType(instruction);
                if (type.equals("R_Format")) {
                    // run R format encoding
                    machineCode.add(Instructions.rFormatEncoding(instruction, regArray[0], regArray[1], regArray[2]));
                    currentAddress += 4;
                } else if (type.equals("I_Format")) {
                    // run I format encoding
                        switch(instruction) {
                            case "bne":
                                machineCode.add(instruction + " " + regArray);
                                break;
                            case "beq":
                                machineCode.add(instruction + " " + regArray);
                                break;
                            default:
                                machineCode.add(Instructions.iFormatEncoding(instruction, regArray));
                                break;
                        }
                    currentAddress += 4;
                } else if (type.equals("J_Format")) {
                    // run J format encoding
                    machineCode.add(regArray[0]);
                    currentAddress += 4;
                } else if (type.equals("pseudo_instructions")) {
                    //handle the Pseudo Instruction
                    if (instruction.equals("la")) { // keep la
                        machineCode.add("lalui " + regArray[0] + " " + regArray[1]);
                        machineCode.add("laori " + regArray[0] + " " + regArray[1]);
                        currentAddress += 8;  // lui + ori, leave 8 bytes for la
                    } else {
                        List<String> pseudoInstructions = Instructions.handlePseudoInstruction(instruction, regArray);
                        for (String pseudoInst : pseudoInstructions) {
                            machineCode.add(pseudoInst);
                            currentAddress += 4;
                        }
                    }
                }
            }
        }
    }

    // Replace the label with the actual offset or address (beq, bne, j, la)
    public void resolveLabels(DataSection data) {
        // la $a0, label
        // -> lui $at, upper 16 bits label address(remove lower 16: shift right 16, then back 16)
        // -> ori $at lower 16 bits label address (remove upper 16: shift left 16, then back 16)
        for(int i=0; i<machineCode.size(); ++i) {
            if (machineCode.get(i).contains("bne ")) {

            }
            else if (machineCode.get(i).contains("beq ")) {

            }
            else if (machineCode.get(i).contains("j ")) {
                String j = machineCode.get(i);
                machineCode.set(i, Instructions.jFormatEncoding(j));
            }
            else if (machineCode.get(i).contains("lalui ")) {
                String lui = machineCode.get(i);
                int splitAt = lui.indexOf(" ");
                String instruction = "lui";
                String registers = lui.substring(splitAt).trim();
                // rt int
                String [] array = registers.split(",");
                for(String s: array) {
                    s = s.trim();
                }
                machineCode.set(i, Instructions.iFormatEncoding(instruction, array));
            }
            else if (machineCode.get(i).contains("laori ")) {
                String ori = machineCode.get(i);
                int splitInstruction = ori.indexOf(" ");
                String instruction = "ori";
                String registers = ori.substring(splitInstruction);
                //rt rs int
                String [] array = registers.split(",");
                for(String s: array) {
                    s = s.trim();
                }
                machineCode.set(i, Instructions.iFormatEncoding(instruction, array));
            }
        }
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
