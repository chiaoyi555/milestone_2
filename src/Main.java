// import java.util.Scanner;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String inputFile = args[0];//"/Users/brucethao/Desktop/TestFileCreation/EvenOrOdd.asm";
        try {
            List<String> lines = new ArrayList<>(Files.readAllLines(Paths.get(inputFile))); //read the file
            List<String> dataSection = new ArrayList<>();
            List<String> textSection = new ArrayList<>();
            boolean isData = false;
            boolean isText = false;

            for (String line : lines) { //separate the file into two parts(data and text)
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
            data.parseDataSection(dataSection);
            List<String> dataOutput = data.getEncodedData();

            // Process the .text section: parse instructions and generate machine code
            TextSection text = new TextSection();
            text.parseTextSection(textSection);
            text.resolveLabels(data); // Resolve labels for jump and branch instructions like j, beq, la, etc.
            List<String> textOutput = text.getMachineCode();


            // TODO get directory of input file
            // create a new files with filename.text and filename.data
            // write to files using dataOutput & textOutput
            int removePeriod = inputFile.lastIndexOf(".");
            int removeDirectory = inputFile.lastIndexOf("/");
            String fileName = inputFile.substring(removeDirectory+1, removePeriod);
            String fileNameASM =inputFile.substring(removeDirectory);
            String textFileName = fileName+".text";
            String dataFileName = fileName+".data";
            try{
                File fileDirectory = new File(inputFile);
                String parent = fileDirectory.getParent();


                File dataFile = new File(parent, dataFileName);
                dataFile.createNewFile();
                BufferedWriter writeData = new BufferedWriter(new FileWriter(dataFile));
                for(String l: dataOutput){
                    writeData.write(l);
                    writeData.newLine();
                }
                writeData.close();
                File textFile = new File(parent, textFileName);
                textFile.createNewFile();
                BufferedWriter writeText = new BufferedWriter(new FileWriter(textFile));
                for(String l: textOutput){
                    writeText.write(l);
                    writeText.newLine();
                }
                writeText.close();
                System.out.println("Success!");
            }
            catch(IOException e){
                System.out.println(" Failed to read or write file: "+e.getMessage());
            }
        }catch(Exception e){
            System.err.println( e.getMessage());
        }




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