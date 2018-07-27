import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.locks.LockSupport;

public class MainGUI {
    public static final char BRAND = '@';
    public static final char ITEM = '!';
    public static final char TYPE = '?';
    public static final char USELESS = '~';
    public static final char NOTCONSIDER = '$';
    public static ArrayList<String> brandKnown = new ArrayList<>();     // Brand use char '@' to represent
    public static ArrayList<String> itemKnown = new ArrayList<>();      // Item description use char '!' to represent
    public static ArrayList<String> typeOrPartNum = new ArrayList<>();  // Type or part# use char '?' to represent
    public static ArrayList<String> uselessData = new ArrayList<>();    // Useless data use char '~' to represent (NULL item)
    public static ArrayList<String> suppliersNotConsider = new ArrayList<>();
    public static HashMap<String, ArrayList<String>> brandGroups = new HashMap<>();
    public static Map<String, TypeOrPartNum> catlogNodes = new HashMap<>();
    public static Map<String, String> namePartNumPair = new HashMap<>();
    public static Queue<String> itemNameQueue = new LinkedList<>();
    private static JTextField resultText;
    public static String[] definitionResult = new String[10];
    public static JFrame mainFrame;
    public static JLabel processingLabel;
//    public static boolean isFinishDefinition = false;

    public static void main(String[] args){
        DataInAndUpdate.readAll();
        ExcelProcess.initializeFormat();
        createGUI();
//        System.out.println("Brand:");
//        for(int i = 0; i < brandKnown.size(); i++){
//            Queue<String> brands = new LinkedList<>(brandGroups.get(brandKnown.get(i)));
//            while(brands.size() > 1){
//                System.out.print(brands.poll() + ", ");
//            }
//            System.out.println(brands.poll());
//        }
//        System.out.println("Item:");
//        for(int i = 0; i < itemKnown.size();i++){
//            System.out.println(itemKnown.get(i));
//        }
//        System.out.println("Type or Part Num:");
//        for(int i = 0; i < typeOrPartNum.size();i++){
//            System.out.println(typeOrPartNum.get(i));
//        }
//        System.out.println("Useless Data:");
//        for(int i = 0; i < uselessData.size();i++){
//            System.out.println(uselessData.get(i));
//        }
//        DataInAndUpdate.changeData();
//        DataInAndUpdate.updateData(BRAND);
    }

    private static JFrame createFrame(int x, int y, int width, int height, java.awt.Color colourUse, String title, LayoutManager layoutUse){
        JFrame resultFrame = new JFrame(title);
        resultFrame.setBounds(x, y, width,height);
        resultFrame.setBackground(colourUse);
        resultFrame.setResizable(false);
        resultFrame.setLayout(layoutUse);
        resultFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        return resultFrame;
    }

    private static JButton createButton(String textUse, int x, int y, int width, int height, Font fontUse, java.awt.Color colourUse){
        JButton resultButton = new JButton(textUse);
        resultButton.setBounds(x, y, width, height);
        resultButton.setFont(fontUse);
        if(colourUse == null) return resultButton;
        resultButton.setBackground(colourUse);
        return resultButton;
    }

    private static JPanel createTextInput(String text, int textFieldWidth, int x, int y, int width, int height){
        JLabel resultLabel = new JLabel(text);
        resultText = new JTextField(textFieldWidth);
        JPanel resultPanel = new JPanel();
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        resultPanel.add(resultLabel);
        resultPanel.add(resultText);
        resultPanel.setLayout(new GridLayout(2, 1));
        resultPanel.setBounds(x, y, width, height);
        return resultPanel;
    }

    private static JPanel createPanel(String text, JTextField textFieldUse){
        JLabel resultLabel = new JLabel(text, JLabel.CENTER);
        JPanel resultPanel = new JPanel();
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultPanel.add(resultLabel);
        resultPanel.add(textFieldUse);
        resultPanel.setLayout(new GridLayout(2, 1));
        return resultPanel;
    }

    public static void userDefineDescription(String description, String itemName, String itemBrand, String itemPartNum, Thread parentThread){
//        String[] definitionResult = new String[3];
//        if(itemName == null) {
//            definitionResult[0] = JOptionPane.showInputDialog(null, "Item Name of: \n" + description, "Item Name", JOptionPane.PLAIN_MESSAGE);
//        }
//        else{
//            int option = JOptionPane.showOptionDialog(null, "Is the name of this item: " + itemName,"Confirm Name",
//                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
//            System.out.println(option);
//            if(option == 0) {
//                definitionResult[0] = itemName;
//            }
//            else{
//                definitionResult[0] = JOptionPane.showInputDialog(null, "Item Name of: \n" + description, "Item Name", JOptionPane.PLAIN_MESSAGE);
//            }
//        }
//        if(itemBrand == null) {
//            definitionResult[1] = JOptionPane.showInputDialog(null, "Item Brand of: \n" + description, "Item Brand", JOptionPane.PLAIN_MESSAGE);
//        }
//        else{
//            int option = JOptionPane.showOptionDialog(null, "Is the brand of this item: " + itemBrand,"Confirm Brand",
//                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
//            System.out.println(option);
//            if(option == 0) {
//                definitionResult[1] = itemBrand;
//            }
//            else{
//                definitionResult[1] = JOptionPane.showInputDialog(null, "Item Brand of: \n" + description, "Item Brand", JOptionPane.PLAIN_MESSAGE);
//            }
//        }
//        if(itemPartNum == null) {
//            definitionResult[2] = JOptionPane.showInputDialog(null, "Item Type or Part Num of: \n" + description, "Type #", JOptionPane.PLAIN_MESSAGE);
//        }
//        else{
//            int option = JOptionPane.showOptionDialog(null, "Is the partNum of this item: " + itemPartNum,"Confirm Brand",
//                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
//            System.out.println(option);
//            if(option == 0) {
//                definitionResult[2] = itemPartNum;
//            }
//            else{
//                definitionResult[2] = JOptionPane.showInputDialog(null, "Item Type or Part Num of: \n" + description, "Type #", JOptionPane.PLAIN_MESSAGE);
//            }
//        }
//        if(definitionResult[0] == null || definitionResult[1] == null || definitionResult[2] == null ||
//                definitionResult[0].equals("") || definitionResult[1].equals("") || definitionResult[2].equals("")){
//            int option = JOptionPane.showOptionDialog(null, "If you want to stop this process press OK, if you want to skip the PO line press cancel!","Exit",
//                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
//            System.out.println(option);
//            if(option == 0) {
//                return new String[1];
//            }
//            else{
//                return null;
//            }
//        }
//        return definitionResult;
        try {
            synchronized (MyRunnable.LOCK) {
                JFrame seperationFrame = createFrame(200, 200, 1000, 350, Color.LIGHT_GRAY,
                        "User Define Item Description", new GridLayout(3, 1, 0, 10));
                ((JPanel) seperationFrame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
                JTextField descriptionLabel = new JTextField(description);
                descriptionLabel.setEditable(false);
                descriptionLabel.setHorizontalAlignment(JTextField.CENTER);
                descriptionLabel.setFont(new Font("宋体", Font.BOLD, 22));
                seperationFrame.add(descriptionLabel);
                JTextField itemNameField = new JTextField(30);
                itemNameField.setFont(new Font("宋体", Font.PLAIN, 18));
                if (itemName != null) {
                    itemNameField.setText(itemName);
                }
                JPanel itemNamePanel = createPanel("Item Name:", itemNameField);
                JTextField itemBrandField = new JTextField(30);
                itemBrandField.setFont(new Font("宋体", Font.PLAIN, 18));
                if (itemBrand != null) {
                    itemBrandField.setText(itemBrand);
                }
                JPanel itemBrandPanel = createPanel("Item Brand:", itemBrandField);
                JTextField itemPartNumField = new JTextField(30);
                itemPartNumField.setFont(new Font("宋体", Font.PLAIN, 18));
                if (itemPartNum != null) {
                    itemPartNumField.setText(itemPartNum);
                }
                JPanel itemPartNumPanel = createPanel("Item Type or Part #:", itemPartNumField);
                JPanel inputPanel = new JPanel();
                inputPanel.setLayout(new GridLayout(1, 3, 30, 0));
                inputPanel.add(itemNamePanel);
                inputPanel.add(itemBrandPanel);
                inputPanel.add(itemPartNumPanel);
                seperationFrame.add(inputPanel);
                JButton confirmationButton = new JButton("Finish!");
                JButton cancelButton = new JButton("Cancel!");
                confirmationButton.setFont(new Font("Arial", Font.BOLD, 30));
                cancelButton.setFont(new Font("Arial", Font.BOLD, 30));
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new GridLayout(1, 2, 100, 0));
                buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
                buttonPanel.add(confirmationButton);
                buttonPanel.add(cancelButton);
                seperationFrame.add(buttonPanel);
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Cancel");
                        int option = JOptionPane.showOptionDialog(null, "Do you want to skip this PO line or Stop the current process", "Cancel",
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"Skip", "STOP!", "Cancel"}, null);
                        System.out.println(option);
                        if (option == 0) {
                            try{
                                synchronized (MyRunnable.LOCK) {
                                    definitionResult = null;
                                    MyRunnable.LOCK.notifyAll();
                                }
                            }
                            catch (Exception el){}
                            seperationFrame.dispose();
                        }
                        else if(option == 1){
                            try{
                                synchronized (MyRunnable.LOCK) {
                                    definitionResult = new String[1];
                                    MyRunnable.LOCK.notifyAll();
                                }
                            }
                            catch (Exception el){}
                            seperationFrame.dispose();
                        }
                    }
                });
                confirmationButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Confirmation");
                        try{
                            synchronized (MyRunnable.LOCK) {
                                definitionResult = new String[3];
                                definitionResult[0] = itemNameField.getText();
                                definitionResult[1] = itemBrandField.getText();
                                definitionResult[2] = itemPartNumField.getText();
                                MyRunnable.LOCK.notifyAll();
                            }
                        }
                        catch (Exception el){}
                        seperationFrame.dispose();
                    }
                });
                seperationFrame.setVisible(true);
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
//        JOptionPane.showOptionDialog(null, "Press OK once finish definition, or NO if you want to skip this PO Line, or Cancel if you want to stop the current process",
//                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[]{"Continue", "Skip", "Cancel"}, null);
//        return definitionResult;
    }

    private static void createGUI(){
        mainFrame = createFrame(400, 100, 550, 350, Color.LIGHT_GRAY,
                "Performance Evaluator Version 2.0", null);
        JPanel fileInput = createTextInput("File Name Postfix (YYYYMMDD-YYYYMMDD): ", 10, 50, 50, 450, 50);
        JButton exitB = createButton("Exit!", 275, 125, 175, 75,
                new Font("Arial", Font.BOLD, 25), Color.RED);
        JButton processData = createButton("Process Data", 50, 125, 175, 75,
                new Font("Arial", Font.BOLD, 20), null);
        processingLabel = new JLabel("Current processing line: " + ExcelProcess.currentProcess, JLabel.CENTER);
        processingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        processingLabel.setBounds(50, 250, 400,50);
        mainFrame.add(fileInput);
        mainFrame.add(exitB);
        mainFrame.add(processData);
        mainFrame.addWindowListener(new MyWin());
        mainFrame.add(processingLabel);
        mainFrame.setVisible(true);
        exitB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Exit Program");
                int option = JOptionPane.showOptionDialog(null, "Are you sure to exit the program?","Exit",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,null,null,null);
                System.out.println(option);
                if(option == 0) {
                    System.exit(0);
                }
            }
        });
        processData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyRunnable2 processRunnable = new MyRunnable2(resultText.getText());
                Thread processThread = new Thread(processRunnable);
                processThread.start();
//                    ExcelProcess.processData(resultText.getText());
            }
        });
    }

}
