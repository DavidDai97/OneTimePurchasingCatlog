import jxl.*;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;

import javax.swing.*;
import java.io.*;
import java.lang.Number;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelProcess {
    private static SimpleDateFormat myFormat = new SimpleDateFormat("yyyyMMdd");
    private static WritableCellFormat titleFormat;
    private static WritableCellFormat normalFormat;
    public static int currentProcess = 0;

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
        outputCatalog();
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
                if (currDataSheet.getCell(orderTypeColIdx, i).getContents().equals("Goods")) continue;
                String currSupplier = currDataSheet.getCell(supplierColIdx, i).getContents();
                if(MainGUI.suppliersNotConsider.contains(currSupplier)) continue;
                String currDescription = replaceChinese(currDataSheet.getCell(itemDescriptionColIdx, i).getContents().toUpperCase());
                boolean isNotConsider = false;
                for(int j = 0; j < MainGUI.suppliersNotConsider.size(); j++){
                    if(currSupplier.contains(MainGUI.suppliersNotConsider.get(j))){
                        isNotConsider = true;
                        break;
                    }
                }
                if(!isNotConsider && isDescriptionUseless(currDescription)) isNotConsider = true;
                if(isNotConsider) continue;
                currentProcess = i;
                MainGUI.processingLabel.setText("Current processing line: " + ExcelProcess.currentProcess);
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
                // If skip this item, return null; if stop process return String[1]; if normal, return String[3]
                if(definition == null){
                    MainGUI.suppliersNotConsider.add(currDescription);
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
        }
    }
    private static boolean isDescriptionUseless(String description){
        for(int i = 0; i < MainGUI.suppliersNotConsider.size(); i++){
            String currUseless = MainGUI.suppliersNotConsider.get(i);
            if(description.contains(currUseless)){
                return true;
            }
            if(getSimilarityRatio(description, currUseless) > 0.5){
                return true;
            }
        }
        return false;
    }

    private static int compare(String str, String target){
        int d[][];
        int n = str.length();
        int m = target.length();
        int i;
        int j;
        char ch1;
        char ch2;
        int temp;
        if(n == 0){ return m; }
        if(m == 0){ return n; }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++){
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++){
            d[0][j] = j;
        }
        for (i = 1; i <= n; i++){
            ch1 = str.charAt(i - 1);
            for (j = 1; j <= m; j++){
                ch2 = target.charAt(j - 1);
                if (ch1 == ch2 || ch1 == ch2+32 || ch1+32 == ch2){
                    temp = 0;
                }
                else{
                    temp = 1;
                }
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }
    private static int min(int one, int two, int three) {
        return (one = one < two ? one : two) < three ? one : three;
    }
    public static float getSimilarityRatio(String str, String target) {
        return 1 - (float) compare(str, target) / Math.max(str.length(), target.length());
    }

    private static int writeCnt = 0;
    private static void outputCatalog(){
        int itemNameCol = 0;
        int itemTypeCol = 1;
        int itemBrandCol = 2;
        int itemSupplierCol = 3;
        int itemLeadTimeCol = 4;
        int currencyCol = 5;
        int itemPriceCol = 6;
        int itemPurchasedTimeCol = 7;
        int itemBuyerCol = 8;
        try {
            String outputFilePath = "../Output/Catalog" + ".xls";
            WritableWorkbook outputFile = Workbook.createWorkbook(new File(outputFilePath));
            WritableSheet catlogSheet = outputFile.createSheet("Catalog", 0);
            jxl.write.Label itemNameTitle = new jxl.write.Label(itemNameCol, 0, "Item Name", titleFormat);
            catlogSheet.addCell(itemNameTitle);
            catlogSheet.setColumnView(itemNameCol, 22);
            jxl.write.Label partNumTitle = new jxl.write.Label(itemTypeCol, 0, "Item Type or Part #", titleFormat);
            catlogSheet.addCell(partNumTitle);
            catlogSheet.setColumnView(itemTypeCol, 30);
            jxl.write.Label brandTitle = new jxl.write.Label(itemBrandCol, 0, "Brand", titleFormat);
            catlogSheet.addCell(brandTitle);
            catlogSheet.setColumnView(itemBrandCol, 18);
            jxl.write.Label supplierTitle = new jxl.write.Label(itemSupplierCol, 0, "Supplier", titleFormat);
            catlogSheet.addCell(supplierTitle);
            catlogSheet.setColumnView(itemSupplierCol, 18);
            jxl.write.Label leadTimeTitle = new jxl.write.Label(itemLeadTimeCol, 0, "Lead Time", titleFormat);
            catlogSheet.addCell(leadTimeTitle);
            catlogSheet.setColumnView(itemLeadTimeCol, 18);
            jxl.write.Label currencyTitle = new jxl.write.Label(currencyCol, 0, "Currency", titleFormat);
            catlogSheet.addCell(currencyTitle);
            catlogSheet.setColumnView(currencyCol, 22);
            jxl.write.Label priceTitle = new jxl.write.Label(itemPriceCol, 0, "Item Price", titleFormat);
            catlogSheet.addCell(priceTitle);
            catlogSheet.setColumnView(itemPriceCol, 22);
            jxl.write.Label purchasedTimeTitle = new jxl.write.Label(itemPurchasedTimeCol, 0, "Item Purchased Time", titleFormat);
            catlogSheet.addCell(purchasedTimeTitle);
            catlogSheet.setColumnView(itemPurchasedTimeCol, 30);
            jxl.write.Label buyerTitle = new jxl.write.Label(itemBuyerCol, 0, "Buyer", titleFormat);
            catlogSheet.addCell(buyerTitle);
            catlogSheet.setColumnView(itemBuyerCol, 18);
            int rowCnt = 1;
            while(!MainGUI.itemNameQueue.isEmpty()){
                String currItemName = MainGUI.itemNameQueue.poll();
                Label itemNameLabel = new Label(itemNameCol, rowCnt, currItemName, normalFormat);
                catlogSheet.addCell(itemNameLabel);
                TypeOrPartNum currPartNumNode = MainGUI.catlogNodes.get(currItemName);
                for(int i = 0; i < currPartNumNode.getSize(); i++){
                    String currItemPartNum = currPartNumNode.getKey(i);
                    Label itemPartNumLabel = new Label(itemTypeCol, rowCnt, currItemPartNum, normalFormat);
                    catlogSheet.addCell(itemPartNumLabel);
                    Brand currBrandNode = currPartNumNode.get(i);
                    for(int j = 0; j < currBrandNode.getSize(); j++){
                        String currBrand = currBrandNode.getKey(j);
                        Label itemBrandLabel = new Label(itemBrandCol, rowCnt, currBrand, normalFormat);
                        catlogSheet.addCell(itemBrandLabel);
                        Supplier currSupplierNode = currBrandNode.get(j);
                        System.out.println("Size: " + currSupplierNode.getSize());
                        for(int k = 0; k < currSupplierNode.getSize(); k++){
                            String currSupplier = currSupplierNode.getKey(k);
                            Label itemSupplierLabel = new Label(itemSupplierCol, rowCnt, currSupplier, normalFormat);
                            catlogSheet.addCell(itemSupplierLabel);
                            //Lead time, Price, Purchase times, Buyer
                            Item currItemNode = currSupplierNode.get(k);
                            jxl.write.Number currLeadTimeLabel = new jxl.write.Number(itemLeadTimeCol, rowCnt, currItemNode.getAverageLeadTime(), normalFormat);
                            catlogSheet.addCell(currLeadTimeLabel);
                            jxl.write.Label currCurrencyLabel = new jxl.write.Label(currencyCol, rowCnt, currItemNode.getCurrency(), normalFormat);
                            catlogSheet.addCell(currCurrencyLabel);
                            jxl.write.Number currPriceLabel = new jxl.write.Number(itemPriceCol, rowCnt, currItemNode.getPrice(), normalFormat);
                            catlogSheet.addCell(currPriceLabel);
                            jxl.write.Number currPurchaseTimesLabel = new jxl.write.Number(itemPurchasedTimeCol, rowCnt, currItemNode.getPurchaseTime(), normalFormat);
                            catlogSheet.addCell(currPurchaseTimesLabel);
                            Label currBuyerLabel = new Label(itemBuyerCol, rowCnt, currItemNode.getBuyer(), normalFormat);
                            catlogSheet.addCell(currBuyerLabel);
                            rowCnt++;
                        }
                    }
                }
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
                outputCatalog();
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
            normalFormat = new WritableCellFormat(myFont);
            normalFormat.setAlignment(Alignment.CENTRE);
            normalFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
        }
        catch(WriteException e){
            System.out.println("Err: 5, Initialize Error.");
        }
    }
    public static void main(String[] args){
        processDescription("ÍÏÁ´,25.57.R  SYSW17016");
//        System.out.println(getSimilarityRatio("Rnd2017001-PROT	2017/2/7	Peicong.Qi	2000XC-102	MAIN SUPPORT Ö÷Ö§¼Ü", "Rnd2017001-PROT 2017/2/7    Peicong.Qi  2000XC-103  UPER BOX COVER ÉÏ¿ò¸Ç°å"));
    }

    private static boolean isBrandSure = false;
    private static boolean isItemSure = false;
    private static boolean isPartNumSure = false;
    private static String[] processDescription(String description){
        isBrandSure = false;
        isItemSure = false;
        isPartNumSure = false;
        String itemBrand = findPossibleBrand(description);
        String itemPartNum = findPossiblePartNum(description);
        String itemName = findPossibleItemName(description);
        if(itemName == null && itemPartNum != null){
            itemName = MainGUI.namePartNumPair.get(itemPartNum);
            if(itemName != null){
                isItemSure = true;
            }
        }
        if(!isBrandSure || !isItemSure || !isPartNumSure){
            MyRunnable definitionRunnable = new MyRunnable(description, itemName, itemBrand, itemPartNum, Thread.currentThread());
            Thread definitionThread = new Thread(definitionRunnable);
            definitionThread.start();
            try{
                synchronized(MyRunnable.LOCK) {
                    MyRunnable.LOCK.wait();
                }
            }
            catch (Exception e){
                System.out.println(e.toString());
            }
            String[] definition = null;
            try{
                synchronized (MyRunnable.LOCK) {
                    definition = MainGUI.definitionResult;
                    MyRunnable.LOCK.notifyAll();
                }
            }
            catch (Exception e){}
            if(definition != null && definition.length != 1) {
                System.out.println(definition[0]);
                System.out.println(definition[1]);
                System.out.println(definition[2]);
                add2Dict(definition);
                return definition;
            }
            else if(definition != null){
                return definition;
            }
            else{
                return null;
            }
        }
        String[] definition = new String[]{itemName, itemBrand, itemPartNum};
        add2Dict(definition);
        return definition;
    }
    private static void add2Dict(String[] definition){
        MainGUI.namePartNumPair.putIfAbsent(definition[2], definition[0]);
        if(!MainGUI.brandKnown.contains(definition[1].toUpperCase())){
            DataInAndUpdate.changeData();
            MainGUI.brandKnown.add(definition[1].toUpperCase());
            ArrayList<String> temp = new ArrayList<>();
            temp.add(definition[1].toUpperCase());
            MainGUI.brandGroups.put(definition[1].toUpperCase(), temp);
        }
        if(!MainGUI.itemKnown.contains(definition[0].toUpperCase())){
            DataInAndUpdate.changeData();
            MainGUI.itemKnown.add(definition[0].toUpperCase());
        }
        if(!MainGUI.typeOrPartNum.contains(definition[2].toUpperCase())){
            DataInAndUpdate.changeData();
            MainGUI.typeOrPartNum.add(definition[2].toUpperCase());
        }
    }
    private static String replaceChinese(String original){
        original = original.replace('£¬', ',');
        original = original.replace('£¨', '(');
        original = original.replace('£©', ')');
        original = original.replace('\t', ' ');
        return original;
    }
    private static String findPossibleItemName(String description){
        String itemName = null;
        for(int i = 0; i < MainGUI.itemKnown.size(); i++){
            String currItem = MainGUI.itemKnown.get(i);
            if(description.contains(currItem.toUpperCase())){
                itemName = currItem;
                isItemSure = true;
                break;
            }
        }
        if(itemName != null){
            return itemName;
        }
        Pattern p = Pattern.compile("(^|,|\\s+)[\u4e00-\u9fa5]*(,|\\s+)");
        Matcher m = p.matcher(description);
        if(m.find()){
            Pattern p1 = Pattern.compile("[\u4e00-\u9fa5]+");
            Matcher m1 = p1.matcher(description);
            if(m1.find()){
                itemName = m1.group(0);
            }
        }
        return itemName;
    }
    private static String findPossibleBrand(String description){
        String itemBrand = null;
        for(int i = 0; i < MainGUI.brandKnown.size(); i++){
            String currBrand = MainGUI.brandKnown.get(i);
            ArrayList<String> currBrandsNames = MainGUI.brandGroups.get(currBrand);
            for(int j = 0; j < currBrandsNames.size(); j++){
                if(description.contains(currBrandsNames.get(j))){
                    itemBrand = currBrand;
                    isBrandSure = true;
                    break;
                }
            }
        }
        if(itemBrand == null){
            if(description.contains("(") && description.contains(")")){
                itemBrand = description.substring(description.indexOf('(')+1, description.indexOf(')'));
            }
        }
        return itemBrand;
    }
    private static String findPossiblePartNum(String description){
        String itemPartNum = null;
        for(int i = 0; i < MainGUI.typeOrPartNum.size(); i++){
            String currPartNum = MainGUI.typeOrPartNum.get(i);
            if(description.toUpperCase().contains(currPartNum.toUpperCase())){
                itemPartNum = currPartNum;
                isPartNumSure = true;
                break;
            }
        }
        if(itemPartNum != null){
            return itemPartNum;
        }
        int patternNum = findCharacterNum(description, "[+-]");
        if(patternNum != 0) {
            String temp = "\\w+";
            String regex = "";
            for (int i = 0; i < patternNum; i++) {
                regex = regex + temp + "[-+]";
            }
            regex = regex + temp;
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(description);
            if (m.find()) {
                itemPartNum = m.group(0);
            }
        }
        return itemPartNum;
    }
    private static int findCharacterNum(String text, String regex){
        return text.split(""+regex).length-1;
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
