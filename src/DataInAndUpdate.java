import java.io.*;
import java.util.*;

public class DataInAndUpdate {
    private static int dataChanged = 0;
    private static final String filePath = "../ReferenceData/reference.txt";

    public static int readData(char dataToRead){
        ArrayList<String> resultString;
        boolean isBrand = false;
        if(dataToRead == MainGUI.BRAND){
            resultString = MainGUI.brandKnown;
            isBrand = true;
        }
        else if(dataToRead == MainGUI.ITEM){
            resultString = MainGUI.itemKnown;
        }
        else if(dataToRead == MainGUI.TYPE){
            resultString = MainGUI.typeOrPartNum;
        }
        else if(dataToRead == MainGUI.USELESS){
            resultString = MainGUI.uselessData;
        }
        else if(dataToRead == MainGUI.NOTCONSIDER){
            resultString = MainGUI.suppliersNotConsider;
        }
        else{
            System.out.println("Err: data to read not specified or code not correct.");
            return 3;   // Error code 3, data to read not specified.
        }
        FileInputStream reader;
        File firstFile = new File(filePath);
        BufferedReader br;
        try{
            reader = new FileInputStream(firstFile);
            br = new BufferedReader(new InputStreamReader(reader, "gbk"));
        }
        catch(FileNotFoundException e){
            System.out.println("Err 1: " + e.toString());
            return 1; // Error code 1, file not found.
        }
        catch (UnsupportedEncodingException e){
            System.out.println("Err 2: " + e.toString());
            return 1;
        }
        String currString;
        try {
            while ((currString = br.readLine()) != null) {
                if(currString.charAt(0) == dataToRead && isBrand){
                    String[] brandsArray = currString.substring(1).split(" & ");
                    String brandKey = brandsArray[0];
                    ArrayList<String> brandsValue = new ArrayList<>();
                    for(int i = 0; i < brandsArray.length; i++){
                        brandsValue.add(brandsArray[i]);
                    }
                    MainGUI.brandGroups.put(brandKey, brandsValue);
                    resultString.add(brandKey);
                }
                else if(currString.charAt(0) == dataToRead){
                    resultString.add(currString.substring(1));
                }
            }
            br.close();
            reader.close();
        }
        catch(IOException e){
            System.out.println("Err: " + e.toString());
            return 2;   // Error code 2, data read error.
        }
        return 0;   // No Error exist, return 0.
    }

    public static int readAll(){
        int errCode;
        if((errCode = readData(MainGUI.BRAND)) != 0){
            return (10+errCode);
        }
        if((errCode = readData(MainGUI.ITEM)) != 0){
            return (20+errCode);
        }
        if((errCode = readData(MainGUI.TYPE)) != 0){
            return (30+errCode);
        }
        if((errCode = readData(MainGUI.USELESS)) != 0){
            return (40+errCode);
        }if((errCode = readData(MainGUI.NOTCONSIDER)) != 0){
            return (50+errCode);
        }
        return 0;
    }

    public static int updateData(char dataToWrite){
        if(dataChanged == 0){
            return 0;
        }
        ArrayList<String> currData;
        boolean isBrand = false;
        if(dataToWrite == MainGUI.BRAND){
            currData = MainGUI.brandKnown;
            isBrand = true;
        }
        else if(dataToWrite == MainGUI.ITEM){
            currData = MainGUI.itemKnown;
        }
        else if(dataToWrite == MainGUI.TYPE){
            currData = MainGUI.typeOrPartNum;
        }
        else if(dataToWrite == MainGUI.USELESS){
            currData = MainGUI.uselessData;
        }
        else if(dataToWrite == MainGUI.NOTCONSIDER){
            currData = MainGUI.suppliersNotConsider;
        }
        else{
            System.out.println("Err: data to read not specified or code not correct.");
            return 3;   // Error code 3, data to write not specified.
        }
        File secondFile=new File(filePath);
        try {
            FileOutputStream writer = new FileOutputStream(secondFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(writer,"gbk"));
            for(int i = 0; i < currData.size(); i++){
                if(!isBrand) {
                    bw.write("" + dataToWrite + currData.get(i) + "\r\n");
                }
                else{
                    bw.write("" + dataToWrite);
                    ArrayList<String> brands = new ArrayList<>(MainGUI.brandGroups.get(currData.get(i)));
                    for(int j = 0; j < brands.size()-1; j++){
                        bw.write(brands.get(j));
                        bw.write(" & ");
                    }
                    bw.write(brands.get(brands.size()) + "\r\n");
                }
            }
            bw.close();
            writer.close();
        }
        catch (IOException e){
            System.out.println("Err: " + e.toString());
            return 1;   // Error Code 3: File write error
        }
        return 0;
    }

    public static int updateAll(){
        int errCode;
        if((errCode = updateData(MainGUI.BRAND)) != 0){
            return (10+errCode);
        }
        if((errCode = updateData(MainGUI.ITEM)) != 0){
            return (20+errCode);
        }
        if((errCode = updateData(MainGUI.TYPE)) != 0){
            return (30+errCode);
        }
        if((errCode = updateData(MainGUI.USELESS)) != 0){
            return (40+errCode);
        }
        if((errCode = updateData(MainGUI.NOTCONSIDER)) != 0){
            return (50+errCode);
        }
        return 0;
    }

    public static void changeData(){
        dataChanged = 1;
    }
}
