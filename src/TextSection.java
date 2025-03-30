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
    private static final int MAX_16BIT = 0xFFFF;
    private final List<String> labels = new ArrayList<>(); // store the label we declared
    private final List<Integer> addresses = new ArrayList<>(); // store data's address
    private final List<String> machineCode = new ArrayList<>();
    private int currentAddress = TEXT_START_ADDRESS;
    private DataSection data;

    public void parseTextSection(List<String> textLines) {
        for(String line: textLines) {
            line = line.replace("\t", "");
            if (line.isEmpty() || line.contains("#")) continue; // skip empty line or comment
            if (line.contains(":")) { // if this line is label
                int findColon = line.indexOf(":");
                String label = line.substring(0,findColon);
                labels.add(label);
                addresses.add(currentAddress);
                continue;
            }
            if (line.contains("syscall")) {
                machineCode.add(Instructions.syscall());
                currentAddress += 4;
                continue;
            }
             if (line.contains("j ")) {
                machineCode.add(line);
                currentAddress += 4;
                continue;
            }
            int splitInstruction = line.indexOf(" ");

            String instruction = line.substring(0, splitInstruction); //instruction substring
            String registers = line.substring(splitInstruction).trim(); // registers substring
            registers = registers.replace(")", "");
            registers = registers.replace("(", ",");
            String[] regArray = registers.split(","); // Arrays for registers and intermediates
            int i = 0;
            while(i<regArray.length){
                regArray[i] = regArray[i].trim();
                ++i;
            }
            for(String l: regArray) l = l.trim();

            String type = Instructions.determineInstructionType(instruction);
            if (type.equals("R_Format")) {
                // run R format encoding
                machineCode.add(Instructions.rFormatEncoding(instruction, regArray[0], regArray[1], regArray[2]));
                currentAddress += 4;
            } else if (type.equals("I_Format")) {
                // run I format encoding
                if(instruction.equals("beq")){
                    //rs rt label
                    machineCode.add(line);
                }
                else if(instruction.equals("bne")){
                    machineCode.add(line);
                }
                else{
                    machineCode.add(Instructions.iFormatEncoding(instruction, regArray));
                }
                currentAddress += 4;
            } else if (type.equals("pseudo_instructions")) {
                //handle the Pseudo Instruction
                    switch(instruction){
                        case "li":
                            int value = Integer.parseInt(regArray[1]);
                            if(value > MAX_16BIT){ // lui + ori
                                machineCode.add("lilui "+regArray);
                                machineCode.add("liori "+regArray);
                                currentAddress+=8;
                            }else {
                                machineCode.add(Instructions.iFormatEncoding("ori", new String[]{regArray[0], "$zero", regArray[1]}));
                                currentAddress+=4;
                            }
                            break;
                        case "la":
                            machineCode.add("lalui " + regArray[0] + "," + regArray[1]);
                            machineCode.add("laori " + regArray[0] + ",$at," + regArray[1]);
                            //ori $a0, $at, label
                            currentAddress+=8;
                            // deal with this in TextSection
                            break;
                        case "blt": // slt $at, $rs, $rt + bne $at, $zero, label
                            machineCode.add("slt "+regArray[0]+regArray[1]+"$at");
                            machineCode.add("bne $at, $zero "+regArray[2]);
                            currentAddress+=8;
                            break;
                        case "move": // add $d, $s, $zero
                            machineCode.add(Instructions.rFormatEncoding("add", regArray[0], regArray[1], "$zero"));
                            currentAddress+=4;
                            break;
                    }
                /*} else {
                    List<String> pseudoInstructions = Instructions.handlePseudoInstruction(instruction, regArray);
                    for (String pseudoInst : pseudoInstructions) {
                        machineCode.add(pseudoInst);
                        currentAddress += 4;
                    }
                }*/
            }
        }
    }

        // Replace the label with the actual offset or address (beq, bne, j, la)
        public void resolveLabels (DataSection data){
            // la $a0, label
            // -> lui $at, upper 16 bits label address(remove lower 16: shift right 16, then back 16)
            // -> ori $at lower 16 bits label address (remove upper 16: shift left 16, then back 16)
            for (int i = 0; i < machineCode.size(); ++i) {
                if (machineCode.get(i).contains("bne ")) {
                    String bne = machineCode.get(i);
                    String[] array = bne.split(" ");
                    String label = array[2];
                    int labelAddress = this.getLabelAddress(label);
                    int pcPlus4 = this.getLabelAddress(machineCode.get(i+1));
                    int offset = labelAddress-pcPlus4;
                    bne = "bne ";
                    array[2] = ""+offset;
                    for(int index=0; i<array.length; ++i){
                        array[index] = array[index].trim();
                    }
                    machineCode.set(i, Instructions.iFormatEncoding(bne, array));
                } else if (machineCode.get(i).contains("beq ")) {
                    String beq = machineCode.get(i);
                    String[] array = beq.split(" ");
                    String label = array[2];
                    int labelAddress = data.getLabelAddress(label);
                    int pcPlus4 = data.getLabelAddress(machineCode.get(i+1));
                    int offset = labelAddress-pcPlus4;
                    beq = "beq ";
                    array[2] = ""+offset;
                    for(int index=0; i<array.length; ++i){
                        array[index] = array[index].trim();
                    }
                    machineCode.set(i, Instructions.iFormatEncoding(beq, array));
                } else if (machineCode.get(i).contains("j ")) {
                    String j = machineCode.get(i);
                    String[] array = j.split(" ");
                    String label = array[1];
                    int offset = data.getLabelAddress(label);
                    j = "j " + offset;
                    machineCode.set(i, Instructions.jFormatEncoding(j));
                } else if (machineCode.get(i).contains("lalui ")) {
                    String lui = machineCode.get(i);
                    int splitAt = lui.indexOf(" ");
                    String instruction = "lui";
                    String registers = lui.substring(splitAt).trim();
                    // rt int
                    String[] array = registers.split(",");
                    for(int index=0; index<array.length; ++index){
                        array[index] = array[index].trim();
                    }
                    String label = array[1];
                    int offset = data.getLabelAddress(label);
                    offset = offset >> 16;
                    array[1] = "" + offset;
                    machineCode.set(i, Instructions.iFormatEncoding(instruction, array));
                } else if (machineCode.get(i).contains("laori ")) {
                    //ori $a0, $at, label
                    String ori = machineCode.get(i);
                    int splitInstruction = ori.indexOf(" ");
                    String instruction = "ori";
                    String registers = ori.substring(splitInstruction);
                    //rt rs int
                    String[] array = registers.split(",");
                    for(int index=0; index<array.length; ++index){
                        array[index] = array[index].trim();
                    }
                    String label = array[2];
                    int offset = data.getLabelAddress(label);
                    offset = offset << 16;
                    offset = offset >> 16;
                    array[2] = "" + offset;
                    machineCode.set(i, Instructions.iFormatEncoding(instruction, array));
                }
            }
        }

        public int getLabelAddress (String label){
            for (int i = 0; i < labels.size(); i++) {
                if (labels.get(i).equals(label)) {
                    return addresses.get(i);
                }
            }
            return -1; // there is no this label
        }

        public List<String> getMachineCode () {
            return machineCode;
        }
}
