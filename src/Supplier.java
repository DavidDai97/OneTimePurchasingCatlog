import java.util.*;

public class Supplier {
    private Map<String, Item> itemNode;
    private ArrayList<String> items;

    public Supplier(){
        itemNode = new HashMap<>();
        items = new ArrayList<>();
    }

    public boolean contains(String key){
        return this.itemNode.containsKey(key);
    }
    public Item get(String key){
        return this.itemNode.get(key);
    }
    public Item get(int idx){
        if(idx > itemNode.size()){
            return null;
        }
        return this.itemNode.get(this.items.get(idx));
    }
    public String getKey(int idx){
        if(idx > itemNode.size()){
            return null;
        }
        return this.items.get(idx);
    }
    public void put(String key, Item value){
        this.itemNode.put(key, value);
        if(!items.contains(key)){
            items.add(key);
        }
    }
    public int getSize(){
        return this.items.size();
    }
}
