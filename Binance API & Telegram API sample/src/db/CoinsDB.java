package db;

import java.util.HashMap;

import Model.Coin;

public class CoinsDB {
    HashMap<String, Coin> myHashMap;

    public CoinsDB() {
        this.myHashMap = new HashMap<>();
    }

    public HashMap<String, Coin> getMyHashMap() {
        return this.myHashMap;
    }
}
