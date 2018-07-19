import java.util.*;
public class Brand {
    private Map<String, Supplier> supplierNode;

    public Brand(){
        supplierNode = new HashMap<>();
    }

    public boolean contains(String key){
        return this.supplierNode.containsKey(key);
    }
    public Supplier get(String key){
        return this.supplierNode.get(key);
    }
    public void put(String key, Supplier value){
        this.supplierNode.put(key, value);
    }
}
