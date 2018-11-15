import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryTire {
    String now;
    Map<Character, List<DictionaryTire>> next;
    List<String> subTire;

    int computeIndex(char charNeededCompute){
        return charNeededCompute - 'a' + 1;
    }

    DictionaryTire(){
        now = "";
        next = new HashMap<>();
        subTire = new ArrayList<>();
    }
    

    List<String> findPrefix(String input){
        return this.subTire;
    }


}
