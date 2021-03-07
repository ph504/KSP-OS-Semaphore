import java.util.*;
public class Main{

    public static void main (String [] args){
        // thread create chef
        // thread create chef
        // thread create ACHEF
        // thread start chef
        // thread start chef
        // thread start ACHEF


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
                Chef ramsy = new Chef("Gordon Ramsay", reqIng);

                reqIng.clear();
                reqIng.put(new Ingredient("Goosht"), 2);
                reqIng.put(new Ingredient("Piaz"), 3);
                reqIng.put(new Ingredient("Ketchup"), 2);
                reqIng.put(new Ingredient("Mustard"), 2);
                Chef oliver = new Chef("Jamie Oliver", reqIng);



            }
    }

    public static void startRestaurant(){
        Scanner scanner = new Scanner(System.in);
        scanner.nextInt();
    }
}

import java.util.*;

class AssistantChef extends Thread {

    static AssistantChef chefAssistant = new AssistantChef(); // singleton pattern.

    // taking the reference for ingredients needed for the high priority sandwich.
    private Map<Ingredient,Integer> tempRNI;
    // the current ingredient which is being made under 2 seconds.
    private Ingredient currentIngredient;
    // check if there is any customers left.
    private boolean allCustomersServed = false;
    // offset is for creating the ingredients.
    private int offset = 1;
    // the rate of making ingredients (number of ingredients made per second).
    private int makingSpeed = 5;
    // time which it takes to switch ingredients.
    private int switchIngTime = 2;
    // check if it has done the making for the second. (basically a control signal).
    private boolean done = false;

    private long priorityTime; // last time the priority of the chefs was checked.
    private long lastIngChangeTime; // the last time when the ingredient changed.
    private long lastIngCreatedTime; // the last time when ingredients were made.

    private AssistantChef(){}

    public void changeCurrentIngredient(){
        List<Ingredient> neededIngredients = new ArrayList<>();
        for (Ingredient ing: tempRNI.keySet()){
            // mutex lock
            int count = Ingredient.giveIngGetCount.get(ing);
            // mutex unlock
            if (count - tempRNI.get(ing) < 0) {
                neededIngredients.add(ing);
            }
        }
        Ingredient ingredient = null;
        do{

            int randIndex = (int)(Math.random()*neededIngredients.size()); // randomly choose a needed Ingredient.
            ingredient = neededIngredients.get(randIndex); // fetch the needed ingredient.
        }while(ingredient.equals(currentIngredient)); // check if the ingredient is not equal to the previous ingredient which was being made.

        currentIngredient = ingredient; // change the current ingredient which is being made to the ingName.

    }

    public void manageIngCreation(){

        if (System.currentTimeMillis() - priorityTime > 20000) { // 20 seconds for checking the priorities.

            priorityTime = System.currentTimeMillis();
            // mutex lock
            Chef myPriorityChef = Chef.getHigherPriorityChef();
            // mutex unlock
            tempRNI = myPriorityChef.getReqNoIng();

        }
        if (System.currentTimeMillis() - lastIngChangeTime > switchIngTime*1000) { // 2 seconds for changing the ingredient which is needed.
            lastIngChangeTime = System.currentTimeMillis();
            changeCurrentIngredient();
        }
        if (System.currentTimeMillis() - lastIngCreatedTime < 1000) { // 1 second for each 5(makingSpeed) ingredients made.
            // second implementation for 5 ing/sec
            if(!done)
                createIngredient();
        }
        else {
            lastIngCreatedTime = System.currentTimeMillis();
            done = false; // reset
        }



    }

    private void createIngredient(){
        // mutex lock.
        int temp = Ingredient.giveIngGetCount.get(currentIngredient)+makingSpeed;
        // the break after creating 10 ingredients.
        if(temp >= 10){
            temp = 10;
        }
        Ingredient.giveIngGetCount.put(currentIngredient, temp);
        done = true;
        // mutex unlock.
    }

    private void startRestaurant(){
        // mutex lock
        Chef myPriorityChef = Chef.getHigherPriorityChef();
        // mutex unlock
        tempRNI = myPriorityChef.getReqNoIng();

        priorityTime = System.currentTimeMillis();
        lastIngChangeTime = System.currentTimeMillis();
        lastIngCreatedTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        startRestaurant();
        // Chef.custMutex.acquire(); mutex lock
        while(/*second implementation*/Chef.isThereCustomer()){/*Chef.customersNum > 0 first implementation.*/
            // Chef.custMutex.release(); mutex unlock.
            manageIngCreation();
        }
    }
}


public class Chef extends Thread /*implements Startable*/ {
    private final static Map<String, Chef> giveNameGetObject = new HashMap<>(); // get the object of the chef by having the name.
    private final static List<Chef> chefs = new ArrayList<>(); // list of chefs in the restaurant.

    private static Semaphore custMutex;

    private static int customersNum;
    private final Map<Ingredient, Integer> reqNoIng = new HashMap<>(); // required number of ingredients.
    private final Queue<Integer> customers = new LinkedList<>(); // id of each customer is held.
    private String name;
    private int id;
    private int offset = -1;
    private boolean sufficientIngredients = true;




    // the name of the chef and required number of ingredients.
    Chef(String name, Map<Ingredient, Integer> ingredients){
        this.name = name;
        reqNoIng.putAll(ingredients);
        id = chefs.size();
        chefs.add(this);
    }

    // checks if there are any costumers left.
    private boolean hasCustomer(){
        return customers.size() > 0;
    }

    // checks if there is enough ingredients for its own sandwich.
    private void checkSufficiency(){
        sufficientIngredients = true;
        // secondimp lock.
        for(Ingredient ing:reqNoIng.keySet()){
            // mutex lock
            int count = Ingredient.giveIngGetCount.get(ing);
            // mutex unlock.
            if(count - reqNoIng.get(ing) < 0){
                sufficientIngredients = false;
                break;
            }
        }
        // secondimp unlock.

    }

    void createSandwich() {


        // mutex lock.
        // output here.
        --customersNum;
        // cannot create customer because the condition is checked not to exceed the customers number.
        int id = customers.poll();
        // mutex unlock.
        sufficientIngredients = false; // reset, should check again if it wants to create another sandwich.


    }

    @Override
    public void run() {
        while(hasCustomer()){
            // read from count if there is available consume. (createSandwich);

            checkSufficiency();
            if(sufficientIngredients) createSandwich();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    Map<Ingredient, Integer> getReqNoIng() {
        return reqNoIng;
    }

    static Chef getHigherPriorityChef(){
        Chef highestPriority = giveNameGetObject.get("Gordon Ramsay");
        // secondimp unlock.
        for(Chef chef:chefs){
            //mutex lock chef and highPriority.
            int csize = chef.customers.size();
            int hpsize = highestPriority.customers.size();
            // mutex unlock.
            if(csize > hpsize){
                highestPriority = chef;
            }
        }
        // secondimp unlock.
        return highestPriority;
    }

    // @Override // from Entity interface. // deleted temporarily.
    public static void startRestaurant() {
        Scanner scanner = new Scanner(System.in);
        customersNum = scanner.nextInt();
        for (int i = 0; i < customersNum; i++) {
            chefs.get(scanner.nextInt()-1) // take the id of the chef from the input.
                    .customers.add(i); // i is the id of the customer.
        }
    }

    public static boolean isThereCustomer(){
        // mutex lock.
        return customersNum > 0;
        // mutex unlock.
    }
    /*public static boolean isThereCustomer(){
        for(Chef chef: chefs){
            if(chef.hasCustomer())
                return true; // there is at least a customer.
        }
        return false; // there is no customer left.
    }*/
}


class Ingredient{
    String name;
    // give ingredient's name and get the number of available ingredients.
    final static Map<Ingredient, Integer> giveIngGetCount = new HashMap<>(); // Critical Section

    Ingredient(String name){
        this.name = name;
        giveIngGetCount.put(this,2);
    }

    @Override
    public String toString() {
        return name;
    }

    void updateIng(Ingredient ingredient, int offset){
        giveIngGetCount.put(ingredient,giveIngGetCount.get(ingredient)+offset);
    }
}


// temporarily unavailable.

/*interface Entity { 
    void initRestaurant();
    void startRestaurant();
}*/




