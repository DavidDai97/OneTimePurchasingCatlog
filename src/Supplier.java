import java.util.*;

public class Supplier {
    private Map<String, Item> itemNode;

    public Supplier(){
        itemNode = new HashMap<>();
    }

    public boolean contains(String key){
        return this.itemNode.containsKey(key);
    }
    public Item get(String key){
        return this.itemNode.get(key);
    }
    public void put(String key, Item value){
        this.itemNode.put(key, value);
    }
}
