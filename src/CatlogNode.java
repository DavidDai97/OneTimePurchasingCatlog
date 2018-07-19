import java.util.*;

public class CatlogNode {
    private Map<String, TypeOrPartNum> partNumNode;

    public CatlogNode(){
        partNumNode = new HashMap<>();
    }

    public boolean contains(String key){
        return this.partNumNode.containsKey(key);
    }
    public TypeOrPartNum get(String key){
        return this.partNumNode.get(key);
    }
    public void put(String key, TypeOrPartNum value){
        this.partNumNode.put(key, value);
    }
}
