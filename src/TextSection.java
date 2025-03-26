import java.lang.reflect.Array;
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
    private DataSection data;

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
            } else if(type.equals("pseudo_instructions")){
                //handle the Pseudo Instruction
                if (instruction.equals("la")) { // keep la
                    //machineCode.add("la " + regArray[0] + " " + regArray[1]);
                    List<String> la = resolveLabels("la " + regArray[0] + " " + regArray[1]);
                    machineCode.add(la.get(0)); // add lui instruction
                    machineCode.add(la.get(1)); // add ori instruction
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


        }
    }

    // Replace the label with the actual offset or address (beq, bne, j, la)
    public ArrayList<String> resolveLabels(String la) {
        // revised Instructions should handle beq, bne, j. Just la left?
        // la $a0, label
        // -> lui $at, upper 16 bits label address(remove lower 16: shift right 16, then back 16)
        // -> ori $at lower 16 bits label address (remove upper 16: shift left 16, then back 16)
        ArrayList<String> loadAddress = new ArrayList<>();
        List<String> labels = data.getLabels();
        int luiOffset = 0, oriOffset = 0;
        char split = la.charAt(' ');
        String registers = la.substring(split).trim(); //contains: $a0 label
        String [] regArray = registers.split(" ");//split into $a0 & label

        for(String l: labels) {
            if(regArray[1]==l){
                luiOffset = data.getLabelAddress(l);
                luiOffset = luiOffset>>16;//remove lower 16
                luiOffset = luiOffset<<16;
                regArray[1] = ""+luiOffset;
                loadAddress.add(Instructions.iFormatEncoding("lui", regArray));
                oriOffset = data.getLabelAddress(l);
                oriOffset = oriOffset<<16;//remove upper 16
                oriOffset = oriOffset>>16;
                regArray[1] = ""+oriOffset;
                loadAddress.add(Instructions.iFormatEncoding("ori", regArray));
            }
        }
    return loadAddress;
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
