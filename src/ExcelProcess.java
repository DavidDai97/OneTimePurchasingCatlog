import jxl.*;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelProcess {
    private static SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");
    private static WritableCellFormat titleFormat;
    private static WritableCellFormat goodFormat;
    private static WritableCellFormat expiredFormat;
    private static WritableCellFormat noneFormat;
    private static WritableCellFormat normalFormat;

    public static void processData(String postFix) throws Exception{
        String sourceFilePath = "../Source/PO Receiving " + postFix + ".xls";
        String copiedFilePath = "../Output/PO Receiving " + postFix + ".xls";
        try {
            copyFile(sourceFilePath, copiedFilePath);
        }
        catch(IOException e){
            JOptionPane.showMessageDialog(null, "Error: copy file failed.",
                    "Error message",JOptionPane.WARNING_MESSAGE);
        }
        File dataFile = new File(sourceFilePath);
        processDataHelper(dataFile);
    }

    private static Queue<Sheet> getSheetNum(Workbook wb){
        int sheet_size = wb.getNumberOfSheets();
        Queue<Sheet> results = new LinkedList<>();
        for (int index = 0; index < sheet_size; index++){
            Sheet dataSheet = wb.getSheet(index);
            if(dataSheet.getName().contains("Sheet 1")){
                results.add(dataSheet);
            }
        }
        return results;
    }

    private static void processDataHelper(File dataFile) throws Exception{
        InputStream is = new FileInputStream(dataFile.getAbsolutePath());
        Workbook wb = Workbook.getWorkbook(is);
        Queue<Sheet> dataSheets = getSheetNum(wb);
        int orderDateColIdx;
        int deliveryDateColIdx;
        int supplierColIdx;
        int orderTypeColIdx;
        int buyerColIdx;
        int itemDescriptionColIdx;
        int priceColIdx;
        int currencyColIdx;
        while(!dataSheets.isEmpty()){
            Sheet currDataSheet = dataSheets.poll();
            orderDateColIdx = currDataSheet.findCell("Po Line Creation Date").getColumn();
            deliveryDateColIdx = currDataSheet.findCell("Transaction Date").getColumn();
            supplierColIdx = currDataSheet.findCell("Supplier Name").getColumn();
            orderTypeColIdx = currDataSheet.findCell("Line Type").getColumn();
            buyerColIdx = currDataSheet.findCell("Buyer Name").getColumn();
            itemDescriptionColIdx = currDataSheet.findCell("Po Item Description").getColumn();
            priceColIdx = currDataSheet.findCell("Po Unit Price").getColumn();
            currencyColIdx = currDataSheet.findCell("Po Currency").getColumn();
            int rowNum = currDataSheet.getRows();
            for(int i = 1; i < rowNum; i++) {
                if (currDataSheet.getCell(orderTypeColIdx, i).getContents().equals("Goods")) {
                    continue;
                }
                String currSupplier = currDataSheet.getCell(supplierColIdx, i).getContents();
                if(MainGUI.suppliersNotConsider.contains(currSupplier)){
                    continue;
                }
                String currDescription = currDataSheet.getCell(itemDescriptionColIdx, i).getContents();
                boolean isNotConsider = false;
                for(int j = 0; j < MainGUI.suppliersNotConsider.size(); j++){
                    if(currSupplier.contains(MainGUI.suppliersNotConsider.get(j)) || currDescription.contains(MainGUI.suppliersNotConsider.get(j))){
                        isNotConsider = true;
                        break;
                    }
                }
                if(isNotConsider){
                    continue;
                }
                String currBuyer = currDataSheet.getCell(buyerColIdx, i).getContents();
                DateCell currOrderDateCell = (DateCell) currDataSheet.getCell(orderDateColIdx, i);
                Date currOrderDate_temp = currOrderDateCell.getDate();
                String currOrderDate = myFormat.format(currOrderDate_temp);
                DateCell currDeliveryDateCell = (DateCell) currDataSheet.getCell(deliveryDateColIdx, i);
                Date currDeliveryDate_temp = currDeliveryDateCell.getDate();
                String currDeliveryDate = myFormat.format(currDeliveryDate_temp);
                double currPrice = ((NumberCell) currDataSheet.getCell(priceColIdx, i)).getValue();
                String currCurrency = currDataSheet.getCell(currencyColIdx, i).getContents();
                System.out.println(currSupplier + ": " + currDescription);
                String[] definition = processDescription(currDescription);
                if(definition == null){
                    continue;
                }
                if(definition.length == 1) {
                    return;
                }
                TypeOrPartNum currPartNumNode;
                Brand currBrandNode;
                Supplier currSupplierNode;
                Item currItemNode;
                if(MainGUI.catlogNodes.containsKey(definition[0])){
                    currPartNumNode = MainGUI.catlogNodes.get(definition[0]);
                    if(currPartNumNode.contains(definition[2])){
                        currBrandNode = currPartNumNode.get(definition[2]);
                        if(currBrandNode.contains(definition[1])){
                            currSupplierNode = currBrandNode.get(definition[1]);
                            if(currSupplierNode.contains(currSupplier)){
                                currItemNode = currSupplierNode.get(currSupplier);
                                currItemNode.addPurchaseTime();
                                int currLeadTime = calcDateDifference(currDeliveryDate, currOrderDate);
                                currItemNode.setAverageLeadTime(currLeadTime);
                                currItemNode.setBuyer(currBuyer);
                                currItemNode.setPrice(currPrice);
                            }
                            else{
                                currItemNode = new Item(currBuyer, calcDateDifference(currDeliveryDate, currOrderDate), currCurrency, currPrice);
                                currSupplierNode.put(currSupplier, currItemNode);
                            }
                        }
                        else{
                            currSupplierNode = new Supplier();
                            currItemNode = new Item(currBuyer, calcDateDifference(currDeliveryDate, currOrderDate), currCurrency, currPrice);
                            currSupplierNode.put(currSupplier, currItemNode);
                            currBrandNode.put(definition[1], currSupplierNode);
                        }
                    }
                    else{
                        currBrandNode = new Brand();
                        currSupplierNode = new Supplier();
                        currItemNode = new Item(currBuyer, calcDateDifference(currDeliveryDate, currOrderDate), currCurrency, currPrice);
                        currSupplierNode.put(currSupplier, currItemNode);
                        currBrandNode.put(definition[1], currSupplierNode);
                        currPartNumNode.put(definition[2], currBrandNode);
                    }
                }
                else{
                    currPartNumNode = new TypeOrPartNum();
                    currBrandNode = new Brand();
                    currSupplierNode = new Supplier();
                    currItemNode = new Item(currBuyer, calcDateDifference(currDeliveryDate, currOrderDate), currCurrency, currPrice);
                    currSupplierNode.put(currSupplier, currItemNode);
                    currBrandNode.put(definition[1], currSupplierNode);
                    currPartNumNode.put(definition[2], currBrandNode);
                    MainGUI.catlogNodes.put(definition[0], currPartNumNode);
                    MainGUI.itemNameQueue.add(definition[0]);
                }
            }
            // Output to an excel sheet as a catlog.
        }
    }

    private static int writeCnt = 0;
    private static void outputCatlog(){
        int itemNameCol = 1;
        int itemTypeCol = 2;
        int itemBrandCol = 3;
        int itemSupplierCol = 4;
        int itemLeadTimeCol = 5;
        int itemPriceCol = 6;
        int itemPurchasedTimeCol = 7;
        int itemBuyerCol = 8;
        try {
            String outputFilePath = "../Output/Catlog" + ".xls";
            WritableWorkbook outputFile = Workbook.createWorkbook(new File(outputFilePath));
            WritableSheet catlogSheet = outputFile.createSheet("Catlog", 0);
            jxl.write.Label itemNameTitle = new jxl.write.Label(itemNameCol, 0, "Item Name", titleFormat);
            catlogSheet.addCell(itemNameTitle);
            catlogSheet.setColumnView(0, 22);
            jxl.write.Label partNumTitle = new jxl.write.Label(itemTypeCol, 0, "Item Type or Part #", titleFormat);
            catlogSheet.addCell(partNumTitle);
            catlogSheet.setColumnView(0, 30);
            jxl.write.Label brandTitle = new jxl.write.Label(itemBrandCol, 0, "Brand", titleFormat);
            catlogSheet.addCell(brandTitle);
            catlogSheet.setColumnView(0, 18);
            jxl.write.Label supplierTitle = new jxl.write.Label(itemSupplierCol, 0, "Supplier", titleFormat);
            catlogSheet.addCell(supplierTitle);
            catlogSheet.setColumnView(0, 18);
            jxl.write.Label leadTimeTitle = new jxl.write.Label(itemLeadTimeCol, 0, "Lead Time", titleFormat);
            catlogSheet.addCell(leadTimeTitle);
            catlogSheet.setColumnView(0, 18);
            jxl.write.Label priceTitle = new jxl.write.Label(itemPriceCol, itemPriceCol, "Item Price", titleFormat);
            catlogSheet.addCell(priceTitle);
            catlogSheet.setColumnView(0, 22);
            jxl.write.Label purchasedTimeTitle = new jxl.write.Label(itemPurchasedTimeCol, 0, "Item Purchased Time", titleFormat);
            catlogSheet.addCell(purchasedTimeTitle);
            catlogSheet.setColumnView(0, 30);
            jxl.write.Label buyerTitle = new jxl.write.Label(itemBuyerCol, 0, "Buyer", titleFormat);
            catlogSheet.addCell(buyerTitle);
            catlogSheet.setColumnView(0, 18);
            while(!MainGUI.itemNameQueue.isEmpty()){
                String currItemName = MainGUI.itemNameQueue.poll();
            }

            outputFile.write();
            outputFile.close();
            writeCnt = 0;
        }
        catch (Exception e){
            System.out.println(e.toString());
            writeCnt++;
            if(writeCnt < 5){
                initializeFormat();
                outputCatlog();
            }
        }
    }

    public static void initializeFormat(){
        try{
            WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD,false);
            titleFormat = new WritableCellFormat(titleFont);
            titleFormat.setAlignment(Alignment.CENTRE);
            titleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            WritableFont myFont = new WritableFont(WritableFont.ARIAL,10, WritableFont.NO_BOLD, false);
            goodFormat = new WritableCellFormat(myFont);
            expiredFormat = new WritableCellFormat(myFont);
            noneFormat = new WritableCellFormat(myFont);
            normalFormat = new WritableCellFormat(myFont);
            normalFormat.setAlignment(Alignment.CENTRE);
            normalFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            goodFormat.setBackground(Colour.LIGHT_GREEN);
            goodFormat.setAlignment(Alignment.CENTRE);
            goodFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            expiredFormat.setBackground(Colour.RED);
            expiredFormat.setAlignment(Alignment.CENTRE);
            expiredFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
            noneFormat.setBackground(Colour.YELLOW);
            noneFormat.setAlignment(Alignment.CENTRE);
            noneFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        }
        catch(WriteException e){
            System.out.println("Err: 5, Initialize Error.");
        }
    }

    private static String[] processDescription(String description){
        String itemName = null;
        boolean nameChanged = false;
        String itemBrand = null;
        boolean brandChanged = false;
        String itemPartNum = null;
        boolean partNumChanged = false;
        for(int i = 0; i < MainGUI.itemKnown.size(); i++){
            String currItem = MainGUI.itemKnown.get(i);
            if(description.contains(currItem)){
                itemName = currItem;
                nameChanged = true;
            }
        }
        for(int i = 0; i < MainGUI.brandKnown.size(); i++){
            String currBrand = MainGUI.brandKnown.get(i);
            ArrayList<String> currBrandsNames = MainGUI.brandGroups.get(currBrand);
            for(int j = 0; j < currBrandsNames.size(); j++){
                if(description.contains(currBrandsNames.get(j))){
                    itemBrand = currBrand;
                    brandChanged = true;
                }
            }
        }
        for(int i = 0; i < MainGUI.typeOrPartNum.size(); i++){
            String currPartNum = MainGUI.typeOrPartNum.get(i);
            if(description.contains(currPartNum)){
                itemPartNum = currPartNum;
                partNumChanged = true;
            }
        }
        if(!nameChanged || !brandChanged || ! partNumChanged){
            String[] definition = MainGUI.userDefineDescription(description, itemName, itemBrand, itemPartNum);
            if(definition != null) {
                System.out.println("Item Name: " + definition[0]);
                System.out.println("Item Brand: " + definition[1]);
                System.out.println("Item Type or Part #: " + definition[2]);
                return definition;
            }
            else{
                return null;
            }
        }
        return new String[]{itemName, itemBrand, itemPartNum};
    }

    private static String dateAddition(String date, int num, char type){
        Date dateToCal;
        try {
            dateToCal = myFormat.parse(date);
        }
        catch (Exception e){
            System.out.println("Format error");
            return "";
        }
        long finalDate = 0;
        if(type == 'd'){
            finalDate=(dateToCal.getTime()/1000) + 60*60*24*num;
        }
        else if(type == 'h'){
            finalDate=(dateToCal.getTime()/1000) + 60*60*num;
        }
        //finalDate=(dateToCal.getTime()/1000) + 60*60*24*num;
        dateToCal.setTime(finalDate*1000);
        return myFormat.format(dateToCal);
    }

    private static int calcDateDifference(String endDate, String startDate){
        Date start, end;
        try {
            end = myFormat.parse(endDate);
            start = myFormat.parse(startDate);
        }
        catch (Exception e){
            System.out.println("Format error");
            return -1;
        }
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(start);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(end);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);
        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

    private static void copyFile(String oldPath, String newPath) throws IOException {
        File oldFile = new File(oldPath);
        File file = new File(newPath);
        FileInputStream in = new FileInputStream(oldFile);
        FileOutputStream out = new FileOutputStream(file);
        byte[] buffer=new byte[2097152];
        while((in.read(buffer)) != -1){
            out.write(buffer);
        }
    }
}
