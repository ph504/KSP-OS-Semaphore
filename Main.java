import java.util.*;
public class Main{
    public static void main (String [] args){
        System.out.println("Hello world!");
    }


    public static void initializeRestaurant(String [] args){
        if(args != null)
            if(args.length>0){
                // if there was to be input? I mean it can
            }
            else{


                Map<Ingredient, Integer>reqIng = new HashMap<>();

                reqIng.put(new Ingredient("Goosht"),1);
                reqIng.put(new Ingredient("Goje"), 2);
                reqIng.put(new Ingredient("Piaz"), 1);
                reqIng.put(new Ingredient("Ketchup"),2);
                Chef ramsy = new Chef("Gordon Ramsy", reqIng);

                reqIng.clear();
                reqIng.put(new Ingredient("Goosht"), 2);
                reqIng.put(new Ingredient("Piaz"), 3);
                reqIng.put(new Ingredient("Ketchup"), 2);
                reqIng.put(new Ingredient("Mustard"), 2);
                Chef oliver = new Chef("Jamie Oliver", reqIng);



            }
    }
}

class AssistantChef{
	String currentIngredient;
	static chefAssistant = new AssistantChef();
	private int makingSpeed = 5; // number of ingredients per second
	private AssistantChef(){}
	
    createIngredient(){

    }


}

class Chef{
    String name;
    // required number of ingredients.
    private final Map<Ingredient, Integer> reqNoIng = new HashMap<>();

    // the name of the chef and required number of ingredients.
    Chef(String name, Map<Ingredient, Integer> ingredients){
        this.name = name;
        reqNoIng.putAll(ingredients);
    }


    @Override
    public String toString() {
        return name;
    }
}

class Ingredient{ // CS
    String name;
    // give ingredient's name and get the number of available ingredients.
    final static Map<String, Integer> giveIngGetCount = new HashMap<String, Integer>();

    Ingredient(String name){
        this.name = name;
        giveIngGetCount.put(name,0);
    }


    @Override
    public String toString() {
        return name;
    }
}