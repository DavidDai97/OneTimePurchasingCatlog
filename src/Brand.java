import java.util.*;
public class Brand {
    private Map<String, Supplier> supplierNode;
    private ArrayList<String> suppliers;

    public Brand(){
        supplierNode = new HashMap<>();
        suppliers = new ArrayList<>();
    }

    public boolean contains(String key){
        return this.supplierNode.containsKey(key);
    }
    public Supplier get(String key){
        return this.supplierNode.get(key);
    }
    public Supplier get(int idx){
        if(idx > suppliers.size()){
            return null;
        }
        return this.supplierNode.get(this.suppliers.get(idx));
    }
    public String getKey(int idx){
        if(idx > supplierNode.size()){
            return null;
        }
        return this.suppliers.get(idx);
    }
    public void put(String key, Supplier value){
        this.supplierNode.put(key, value);
        if(!suppliers.contains(key)){
            suppliers.add(key);
        }
    }
    public int getSize(){
        return this.suppliers.size();
    }
}
