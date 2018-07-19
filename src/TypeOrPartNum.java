import java.util.*;

public class TypeOrPartNum {
    private Map<String, Brand> brandNode;

    public TypeOrPartNum(){
        brandNode = new HashMap<>();
    }

    public boolean contains(String key){
        return this.brandNode.containsKey(key);
    }
    public Brand get(String key){
        return this.brandNode.get(key);
    }
    public void put(String key, Brand value){
        this.brandNode.put(key, value);
    }
}
