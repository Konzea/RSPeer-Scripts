package Chin_Hunter.Helpers;

import org.rspeer.ui.Log;

import java.util.Arrays;
import java.util.stream.Stream;

public class RequiredItem {

    private static final int DEFAULT_PRICE_MULTIPLIER = 5;
    private static final int DEFAULT_OFFER_TIMEOUT = 15;

    private String Name;
    private int AmountRequired;
    private int AmountToBuy = -1;
    private int Price = - 1;
    private long offerPlacedTime = -1;

    private RequiredItem(){
        //Private default constructor
    }

    public RequiredItem(String name, int amount, int price){
        Name = name;
        AmountRequired = amount;
        Price = price;
    }

    public RequiredItem(String name, int amount){
        this(name, amount, -1);
    }

    public RequiredItem(String name){
        this(name, 1);
    }


    public String getName() {
        return Name;
    }

    public int getAmountRequired() {
        return AmountRequired;
    }

    public int getAmountToBuy(){
        if (AmountToBuy == -1)
            return AmountRequired;
        return AmountToBuy;
    }

    public void setAmountToBuy(int amountToBuy){
        AmountToBuy = amountToBuy;
    }

    public int getPrice() {
        return Price;
    }

    public long getSecondsSinceOfferPlaced(){
        return offerPlacedTime==-1?-1:(System.currentTimeMillis() - offerPlacedTime)/1000;
    }

    public void restOfferPlacedTime(){
        offerPlacedTime = -1;
    }

    public void setOfferPlacedTime(){
        offerPlacedTime = System.currentTimeMillis();
    }

    public void increasePriceTo(int newPrice){
        if (newPrice < Price){
            Log.severe("Tried increasing the price of " + Name + ", but inputted a reduced price.");
            return;
        }
        Price = newPrice;
    }

    // Function to merge two arrays of same type
    public static RequiredItem[] concat(RequiredItem[] a, RequiredItem[] b){
        return Stream.concat(Arrays.stream(a), Arrays.stream(b))
                .toArray(RequiredItem[]::new);
    }

    public static int getDefaultPriceMultiplier(){
        return DEFAULT_PRICE_MULTIPLIER;
    }

    public static int getDefaultOfferTimeout(){
        return DEFAULT_OFFER_TIMEOUT;
    }

    public static RequiredItem getByName(String name, RequiredItem[] array){
        for (RequiredItem requiredItem : array){
            if (requiredItem.getName().equalsIgnoreCase(name))
                return requiredItem;
        }
        return null;
    }

    public static void logAll(RequiredItem[] requiredItems){
        for (RequiredItem requiredItem: requiredItems)
            Log.info(requiredItem.getName() + " x" + requiredItem.getAmountToBuy());
    }
}
