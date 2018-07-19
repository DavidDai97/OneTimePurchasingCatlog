import java.util.*;

public class TypeOrPartNum {
    private Map<String, Brand> brandNode;
    private ArrayList<String> brands;

    public TypeOrPartNum(){
        brandNode = new HashMap<>();
        brands = new ArrayList<>();
    }

    public boolean contains(String key){
        return this.brandNode.containsKey(key);
    }
    public Brand get(String key){
        return this.brandNode.get(key);
    }
    public Brand get(int idx){
        if(idx > brandNode.size()){
            return null;
        }
        return this.brandNode.get(this.brands.get(idx));
    }
    public String getKey(int idx){
        if(idx > brandNode.size()){
            return null;
        }
        return this.brands.get(idx);
    }
    public void put(String key, Brand value){
        this.brandNode.put(key, value);
        if(!brands.contains(key)){
            brands.add(key);
        }
    }
    public int getSize(){
        return this.brands.size();
    }
}
