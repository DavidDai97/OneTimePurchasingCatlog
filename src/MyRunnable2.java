import javax.swing.*;
import java.io.FileNotFoundException;

public class MyRunnable2 implements Runnable{
    private String postFix;

    public MyRunnable2(String postFix){
        this.postFix = postFix;
    }

    @Override
    public void run() {
        try {
            ExcelProcess.processData(postFix);
            System.out.println("Update data return code: " + DataInAndUpdate.updateAll());
            JOptionPane.showMessageDialog(null, "Process finished", "Progress",
                    JOptionPane.WARNING_MESSAGE);
        }
        catch (FileNotFoundException e){
            System.out.println("Error: " + e.toString());
            JOptionPane.showMessageDialog(null, "Err: File not found", "Error message", JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception e){
            System.out.println(e.toString());
            JOptionPane.showMessageDialog(null, "Unknown Error", "Error message", JOptionPane.ERROR_MESSAGE);
        }
    }
}