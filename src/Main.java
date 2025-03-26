// import java.util.Scanner;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String inputFile = args[0];
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(inputFile))); //read the file
            List<String> dataSection = new ArrayList<>();
            List<String> textSection = new ArrayList<>();
            boolean isData = false;
            boolean isText = false;

            for (String line : lines) { //separate the file into two parts(data and text)
                line = line.trim();
                if (line.startsWith(".data")) {
                    isData = true;
                    isText = false;
                    continue;
                } else if (line.startsWith(".text")) {
                    isText = true;
                    isData = false;
                    continue;
                }
                if (isData)
                    dataSection.add(line);
                if (isText)
                    textSection.add(line);
            }

            // Process the .data section: parse data and generate encoded machine code
            DataSection data = new DataSection();
            data.parseDataSection(new ArrayList<>(dataSection));
            List<String> dataOutput = data.getEncodedData();

            // Process the .text section: parse instructions and generate machine code
            TextSection text = new TextSection();
            text.parseTextSection(new ArrayList<>(textSection));
            //text.resolveLabels(data); // Resolve labels for jump and branch instructions like j, beq, la, etc.
            List<String> textOutput = text.getMachineCode();

        }catch(IOException e){
            System.err.println(" Failed to read or write file: " + e.getMessage());
        }

        // TODO get directory of input file
        // create a new files with output filename.text and filename.data
        // write to files using dataOutput & textOutput
        String nameSplit = ".";
        String [] file = inputFile.split(nameSplit);
        String fileName = file[0];

//        String line = args[0];
//        //System.out.println(line);
//        line = Instructions.removeComment(line); //removes comment
//        line = line.trim(); // remove boarder spaces
//        if (Instructions.determineInstructionType(line).equals("syscall")) {
//            System.out.println(Instructions.syscall());
//        }
//        else {
//            int splitInstruction = line.indexOf(" ");
//            String instruction = line.substring(0, splitInstruction); //instruction substring
//            //System.out.println("contains_space?" + instruction + "?"); //instruction contains no additional space
//            String registers = line.substring(splitInstruction); // registers substring
//            registers = registers.replace(")", "");
//            registers = registers.replace("(", ",");
//            String[] regArray; // Arrays for registers and intermediates
//            String splitAtComma = ",";
//            regArray = registers.split(splitAtComma);
//            for (int i = 0; i < regArray.length; ++i) {
//                regArray[i] = regArray[i].trim();
//                System.out.println(regArray[i]);
//            }
//            String inst = null;
//            if (Instructions.determineInstructionType(instruction).equals("R_Format")) {
//                // run R format encoding
//                inst = Instructions.rFormatEncoding(instruction, regArray[0], regArray[1], regArray[2]);
//
//            } else if (Instructions.determineInstructionType(instruction).equals("I_Format")) {
//                // run I format encoding
//                inst = Instructions.iFormatEncoding(instruction, regArray);
//
//            } else if (Instructions.determineInstructionType(instruction).equals("J_Format")) {
//                // run J format encoding
//                inst = Instructions.jFormatEncoding(regArray[0]);
//            }
//            System.out.println(inst);



    }
}