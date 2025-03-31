import java.util.ArrayList;
import java.util.List;

public class DataSection {
    //use myData: .asciiz "Hello" as an example

    //first piece of data in the .data section should be at address 0x10010000
    private static final int DATA_START_ADDRESS = 0x10010000;

    private final List<String> labels = new ArrayList<>(); // store the label we declared
    private final List<Integer> addresses = new ArrayList<>(); // store data's address
    private List<String> ascii = new ArrayList<>();
    private List<String> encodeData = new ArrayList<>();

    private int currentAddress = DATA_START_ADDRESS;


    public void parseDataSection(List<String> dataLines) {
        List<String> temp = new ArrayList<>();
        String s = "";
        for(String line: dataLines){

            if (line.isEmpty() || line.startsWith("#")) continue; // skip empty line or comment
            int split = line.indexOf(":");
            String label = line.substring(0,split).trim();
            String data = line.substring(split+1).trim();

            labels.add(label);
            addresses.add(currentAddress);

            if (data.startsWith(".asciiz")) {
                // store the string between "" into content (Hello)

                String content = data.substring(data.indexOf("\"")+1, data.lastIndexOf("\""));
                content += "\0";
                s+=content;
                currentAddress+=s.length();
                s = "";
            }
        }
        while(s.length()%4!=0){
            s+= "\0";
        }
        int i = 0, j = 4;
        while(j<=s.length()){
            String sub = s.substring(i,j);
            ascii.add(sub);
            i = j;
            j = i+4;
        }
        for(String l: ascii){
            temp.add(reverse(l));
        }
        ascii = temp;
        encodeString(ascii);
    }
    private String reverse(String l){
        String reverse = "";
        char ch;
        for (int i = 0; i < l.length(); i++) {
            ch = l.charAt(i); //get char at i
            reverse = ch + reverse; //add next char in front to reverse string
        }
        return reverse;
    }
    public List<String> getLabels(){
        return labels;
    }
    public int getLabelAddress(String label) {
        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i).equals(label)) {
                return addresses.get(i);
            }
        }
        return -1; // there is no this label
    }

    private void encodeString(List<String> ascii) {
        for(int i = 0; i<ascii.size(); ++i){
            String encode = "";
            for(int j = 0; j<ascii.get(i).length(); j++){
                encode += String.format("%02x", (int)ascii.get(i).charAt(j));
            }
            encodeData.add(encode);
        }
    }

    public List<String> getEncodedData() {
        return encodeData;
    }

}
