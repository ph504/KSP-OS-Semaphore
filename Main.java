import java.util.*;
public class Main{

    public static void main (String [] args){
        initializeRestaurant();
        startRestaurant();
        for (Chef chef:Chef.chefs) chef.start();
        AssistantChef.chefAssistant.start();
        try {
            for (Chef chef : Chef.chefs) chef.join();
            AssistantChef.chefAssistant.join();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }


    }

    public static void initializeRestaurant(/*String [] args*/){
        /*if(args != null)
            if(args.length>0){
                // if there was to be input? I mean it can
            }
            else{

            }*/

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

    public static void startRestaurant(){
        Chef.startRestaurant();
    }
}








class AssistantChef extends Thread {

    // singleton pattern.
    static AssistantChef chefAssistant = new AssistantChef();

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

    static int time = 0;

    private long priorityTime; // last time the priority of the chefs was checked.
    private long lastIngChangeTime; // the last time when the ingredient changed.
    private long lastIngCreatedTime; // the last time when ingredients were made.

    private AssistantChef(){}

    public void changeCurrentIngredient() throws InterruptedException {
        List<Ingredient> neededIngredients = new ArrayList<>();
        List<Ingredient> requiredList = new ArrayList<>();
        // secondimp lock.
        for (Ingredient ing: tempRNI.keySet()){
            // mutex lock
            // lock is in getCount method.
            int count = ing.getCount();
            // mutex unlock
            if (count - tempRNI.get(ing) < 0) {
                neededIngredients.add(ing);
            }
        }
        Ingredient ingredient = null;
        // secondimp unlock.
        if(neededIngredients.size()>0) {

            do {
                int randIndex = (int) (Math.random() * neededIngredients.size()); // randomly choose a needed Ingredient.
                ingredient = neededIngredients.get(randIndex); // fetch the needed ingredient.
            } while (ingredient.equals(currentIngredient)); // check if the ingredient is not equal to the previous ingredient which was being made.
        }
        else {
            do {
                int randIndex = (int) (Math.random() * requiredList.size()); // randomly choose a needed Ingredient.
                ingredient = requiredList.get(randIndex); // fetch the needed ingredient.
            } while (ingredient.equals(currentIngredient)); // check if the ingredient is not equal to the previous ingredient which was being made.

        }

        currentIngredient = ingredient; // change the current ingredient which is being made to the ingName.

    }

    public void manageIngCreation() throws InterruptedException {

        if (System.currentTimeMillis() - priorityTime > 20000) { // 20 seconds for checking the priorities.

            priorityTime = System.currentTimeMillis();
            // mutex lock
            // lock is inside the getHigherPriorityChef method.
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
            ++time;
            /*System.out.println("time = "+ time);
            System.out.println("System time = " + System.currentTimeMillis());*/

        }



    }

    private void createIngredient() throws InterruptedException {
        // secondimp mutex lock.
        // lock is inside getCount method.
        if(!(currentIngredient.getCount()>=10)) {

            currentIngredient.updateIng(offset*makingSpeed);
            done = true;
        }
        // else take a break for the rest of remaining of the whole two seconds.
        // secondimp mutex unlock.
    }

    private void startRestaurant() throws InterruptedException {
        // mutex lock
        // lock is inside the getHigherPriorityChef method.
        Chef myPriorityChef = Chef.getHigherPriorityChef();
        // mutex unlock
        tempRNI = myPriorityChef.getReqNoIng();

        priorityTime = System.currentTimeMillis();
        lastIngChangeTime = System.currentTimeMillis();
        lastIngCreatedTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            startRestaurant();


            while (/*second implementation*/true) {

                if (!Chef.isThereCustomer()) break;
                manageIngCreation();

            }
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
    }
}


class Chef extends Thread /*implements Startable*/ {
    final static List<Chef> chefs = new ArrayList<>(); // list of chefs in the restaurant.
    private final static Map<String, Chef> giveNameGetObject = new HashMap<>(); // get the object of the chef by having the name.


    private final static Semaphore custNumMutex = new Semaphore(1);
    private static int customersNum;
    private static int N; // this number doesn't change after initialization.
    // public static Semaphore customersNum = new Semaphore(N); // secondimp
    private final Map<Ingredient, Integer> reqNoIng = new HashMap<>(); // required number of ingredients.
    private final Semaphore custMutex = new Semaphore(1);
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
        giveNameGetObject.put(name, this);
    }

    // checks if there are any costumers left.
    private boolean hasCustomer(){
        return customers.size() > 0;
    }

    // checks if there is enough ingredients for its own sandwich.
    private void checkSufficiency() throws InterruptedException {
        sufficientIngredients = true;
        // secondimp lock.
        for(Ingredient ing:reqNoIng.keySet()){
            // lock is in getCount method.
            int count = ing.getCount();
            if(count - reqNoIng.get(ing) < 0){
                sufficientIngredients = false;
                break;
            }
        }
        // secondimp unlock.

    }

    void createSandwich() throws InterruptedException {


        // mutex lock.
        // output here.
        custNumMutex.acquire();
        custMutex.acquire();
        // bug fix
        for(Ingredient ing:reqNoIng.keySet()){
            ing.updateIng(offset*reqNoIng.get(ing));
        }

        int index = N - (--customersNum);
        // cannot create customer because the condition is checked not to exceed the customers number.
        int id = customers.poll();
        System.out.println(index + "-" + id + "-" + name + "-" + AssistantChef.time);
        custMutex.release();
        custNumMutex.release();
        // mutex unlock.
        sufficientIngredients = false; // reset, should check again if it wants to create another sandwich.


    }

    @Override
    public void run() {
        while(hasCustomer()){
            // read from count if there is available consume. (createSandwich);

            try {
                checkSufficiency();
            } catch (InterruptedException e) {
                // e.printStackTrace();
            }
            if(sufficientIngredients) {
                try {
                    createSandwich();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    Map<Ingredient, Integer> getReqNoIng() {
        return reqNoIng;
    }

    static Chef getHigherPriorityChef() throws InterruptedException {
        Chef highestPriority = giveNameGetObject.get("Gordon Ramsay");
        // secondimp lock.
        for(Chef chef:chefs){
            //mutex lock chef and highPriority.
            highestPriority.custMutex.acquire();
            chef.custMutex.acquire();

            int csize = chef.customers.size();
            int hpsize = highestPriority.customers.size();

            chef.custMutex.release();
            highestPriority.custMutex.release();
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
        N = customersNum;
        for (int i = 0; i < customersNum; i++) {
            chefs.get(scanner.nextInt()-1) // take the id of the chef from the input.
                    .customers.add(i); // i is the id of the customer.
        }
    }

    public static boolean isThereCustomer() throws InterruptedException {
        // mutex lock.
        custNumMutex.acquire();
        int tempNum = customersNum;
        custNumMutex.release();
        return tempNum > 0;
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
    // name of the ingredient.
    String name;
    // default number of ingredients from the start is 2.
    private final static int DEFAULT_NO_INGREDIENTS = 2;
    // give ingredient's name and get the number of available ingredients.
    private final static Semaphore availIngMutex = new Semaphore(1);
    final static Map<Ingredient, Integer> giveIngGetCount = new HashMap<>(); // available ingredients. Critical Section !


    Ingredient(String name){
        this.name = name;
        giveIngGetCount.put(this,DEFAULT_NO_INGREDIENTS);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    void updateIng(int offset) throws InterruptedException {
        // mutex lock.
        availIngMutex.acquire();
        giveIngGetCount.put(this,giveIngGetCount.get(this)+offset);
        // for AssistantChef to take break.
        if(giveIngGetCount.get(this)>10){
            giveIngGetCount.put(this,10);
        }
        availIngMutex.release();
        // mutex unlock.
    }

    int getCount() throws InterruptedException {
        // mutex lock.
        availIngMutex.acquire();
        int count = giveIngGetCount.get(this);
        availIngMutex.release();
        //mutex unlock.
        return count;

    }
}


// temporarily unavailable.

/*interface Entity { 
    void initRestaurant();
    void startRestaurant();
}*/




