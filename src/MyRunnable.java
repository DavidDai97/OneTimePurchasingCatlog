import javax.swing.*;

public class MyRunnable implements Runnable{
    private String description;
    private String itemName;
    private String itemBrand;
    private String itemPartNum;
    private Thread parentThread;
    public static final Object LOCK = new Object();

    public MyRunnable(String description, String itemName, String itemBrand, String itemPartNum, Thread parentThread){
        this.description = description;
        this.itemName = itemName;
        this.itemBrand = itemBrand;
        this.itemPartNum = itemPartNum;
        this.parentThread = parentThread;
    }
    @Override
    public void run() {
        MainGUI.userDefineDescription(this.description, this.itemName, this.itemBrand, this.itemPartNum, this.parentThread);
    }
}