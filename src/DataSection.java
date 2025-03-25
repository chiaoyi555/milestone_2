import java.util.ArrayList;
import java.util.List;

public class DataSection {
    //use myData: .asciiz "Hello" as an example

    //first piece of data in the .data section should be at address 0x10010000
    private static final int DATA_START_ADDRESS = 0x10010000;

    private final List<String> labels = new ArrayList<>(); // store the label we declared
    private final List<Integer> addresses = new ArrayList<>(); // store data's address
    private final List<String> encodedData = new ArrayList<>();
    private int currentAddress = DATA_START_ADDRESS;


    public void parseDataSection(ArrayList<String> dataLines) {
        for(String line: dataLines){
            if (line.isEmpty() || line.startsWith("#")) continue; // skip empty line or comment

            String[] parts = line.split(":");

            String label = parts[0].trim(); //myData
            String data = parts[1].trim();  //.asciiz "Hello"

            labels.add(label);
            addresses.add(currentAddress);

            if (data.startsWith(".asciiz")) {
                // store the string between "" into content (Hello)
                String content = data.substring(data.indexOf('"') + 1, data.lastIndexOf('"'));
                encodeString(content);
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

    private void encodeString(String content) {
        for (char c : content.toCharArray()) {
            encodedData.add(String.format("%02x", (int) c)); //change the char into hex format (ASCII)
        }
        encodedData.add("00"); // add null in the end
        currentAddress += content.length() + 1; // update address
    }

    public List<String> getEncodedData() {
        return encodedData;
    }

}
