import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import Model.Coin;
import db.CoinsDB;
import info.Info;

public class App {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        int i = 0, j = 0;
        ArrayList<String> symbolList = new ArrayList<>(); 
        String[] blackListWeek = {"","1000XECUSDT", "ETHUSDT_220624", "ANCUSDT", "WOOUSDT", "DEFIUSDT", "1000BTTCUSDT", "GMTUSDT", "PEOPLEUSDT", "APEUSDT", "DYDXUSDT", "ENSUSDT", "BTCUSDT_220624", "IMXUSDT", "BNXUSDT", "BTCDOMUSDT", "API3USDT", "1000SHIBUSDT"};
        // String[] blackListDay = {"","1000XECUSDT", "ETHUSDT_220624", "DEFIUSDT", "1000BTTCUSDT", "GMTUSDT", "APEUSDT", "BTCUSDT_220624", "BTCDOMUSDT", "API3USDT", "1000SHIBUSDT", "UNIUSDT"};
        CoinsDB myDatabase = new CoinsDB();
        Info myInfo = new Info();
        
        setSymbols(symbolList);

        for(i=0; i < symbolList.size(); i++) {
            for(j=0; j < blackListWeek.length; j++) {
                if(symbolList.get(i).equals(blackListWeek[j])) {
                    symbolList.remove(i);
                }
            }
        }
        
        for(i=0; i < symbolList.size(); i++) {
            setCoinInfo(myDatabase.getMyHashMap(), symbolList.get(i));
            /*
            System.out.println(myDatabase.getMyHashMap().get(symbolList.get(i)).getCoinName());
            System.out.println("HIGH = "+myDatabase.getMyHashMap().get(symbolList.get(i)).getHigh());
            System.out.println("LOW = "+myDatabase.getMyHashMap().get(symbolList.get(i)).getLow());
            */
            if(myDatabase.getMyHashMap().get(symbolList.get(i)) == null) {
                myDatabase.getMyHashMap().remove(symbolList.get(i));
            }
        }

        System.out.println("Number of Coins : "+symbolList.size());
        Thread.sleep(1000 * 60 * 5);
        
        while(true) {
            for(i=0; i < symbolList.size(); i++) {
                compare(myDatabase.getMyHashMap().get(symbolList.get(i)), myInfo);
            }

            Thread.sleep(1000 * 60 * 10);
        }
    }

    public static void compare(Coin coin, Info info) throws IOException, InterruptedException {
        Thread.sleep(1000 * 2);
        int status;
        double high, low;
        BufferedReader reader;
        String line, message;
        StringBuffer responseContent = new StringBuffer();

        if(coin == null) {
            System.out.println("Error!");
            return;
        }
        
        String address = "https://www.binance.com/api/v3/klines?symbol="+coin.getCoinName()+"&interval=1d&limit=1";
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        status = connection.getResponseCode();

        if(status == 200) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            for(; (line = reader.readLine()) != null;) {
                responseContent.append(line);
            }
            reader.close();
            
            Object obj = JSONValue.parse(responseContent.toString());
            JSONArray array = (JSONArray) obj;
            
            array = (JSONArray) array.get(0);
            high = Double.parseDouble(array.get(2).toString());
            low = Double.parseDouble(array.get(3).toString());
            // System.out.println(high);
            // System.out.println(low);
            
            if(coin.getHigh() <= high && coin.getHigh() > low && coin.isState() == true) {
                message = coin.getCoinName()+"%20is%20New%20High!";
                url = new URL("https://api.telegram.org/bot"+info.getBotToken()+"/sendMessage?chat_id="+info.getChatID()+"&text="+message);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                
                status = connection.getResponseCode();
                System.out.println("Telegram Request: "+status);
                
                if(status == 200) {
                    coin.setState(false);
                }
                
                else if(status == 400) {
                    System.out.println("New High Message Error!");
                    System.out.println(url.toString()+"\n");
                }
            }
            
            if(coin.getLow() >= low && coin.getLow() < high && coin.isState() == true) {
                message = coin.getCoinName()+"%20is%20New%20Low!";
                url = new URL("https://api.telegram.org/bot"+info.getBotToken()+"/sendMessage?chat_id="+info.getChatID()+"&text="+message);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                
                status = connection.getResponseCode();
                System.out.println("Telegram Request: "+status);
                
                if(status == 200) {
                    coin.setState(false);
                }
                
                else if(status == 400) {
                    System.out.println("New Low Message Error!");
                    System.out.println(url.toString());
                }
            }
        }
    }

    public static void setSymbols(ArrayList<String> symbols) throws IOException, ParseException {
        int status, i=0;
        String address = "https://www.binance.com/fapi/v1/ticker/price";
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        JSONObject jo;

        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        status = connection.getResponseCode();
        
        if(status == 200) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            for(; (line = reader.readLine()) != null;) {
                responseContent.append(line);
            }
            reader.close();
        }

        Object obj = JSONValue.parse(responseContent.toString());
        JSONArray array = (JSONArray) obj;

        for(i=0; i < array.size(); i++) {
            jo = (JSONObject) array.get(i);
            symbols.add(jo.get("symbol").toString());
        }
    }

    public static void setCoinInfo(HashMap<String, Coin> hashMap, String symbol) throws IOException {
        int status;
        String address = "https://www.binance.com/api/v3/klines?symbol="+symbol+"&interval=1d&limit=51";
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        status = connection.getResponseCode();
        
        if(status == 200) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            for(; (line = reader.readLine()) != null;) {
                responseContent.append(line);
            }
            reader.close();
            setHighAndLow(responseContent.toString(), symbol, hashMap);
        }

    }

    public static void setHighAndLow(String responseBody, String symbol, HashMap<String, Coin> hashMap) {
        int i = 0, j = 0;
        String[] candles = responseBody.split("]");
        String[] candleBar;
        boolean permHigh, permLow, isSetHigh = false, isSetLow = false;

        double high = -1.0, low = -1.0;
        double candleHigh = -1.0, candleLow = -1.0;

        for(i=candles.length-5; i > 4; i--) {
            permHigh = true;
            permLow = true;

            candles[i] = candles[i].replace("[", "");
            candles[i] = candles[i].replace("\"", "");          
            candleBar = candles[i].split(",");

            candleHigh = Double.parseDouble(candleBar[3]);
            candleLow = Double.parseDouble(candleBar[4]);

            for(j = i+4; j > i-5; j--) {
                candleBar = candles[j].split(",");
                candleBar[3] = candleBar[3].replace("\"", "");
                if(candleHigh < Double.parseDouble(candleBar[3])) {
                    permHigh = false;
                    break;
                }
            }

            if(permHigh == true && isSetHigh == false) {
                high = candleHigh;
                isSetHigh = true;
            }

            for(j = i+4; j > i-5; j--) {
                candleBar = candles[j].split(",");
                candleBar[4] = candleBar[4].replace("\"", "");
                if(candleLow > Double.parseDouble(candleBar[4])) {
                    permLow = false;
                    break;
                }
            }

            if(permLow == true && isSetLow == false) {
                low = candleLow;
                isSetLow = true;
            }

            if(high != -1.0 && low != -1.0) {
                Coin coin = new Coin(symbol, high, low, true);
                hashMap.put(coin.getCoinName(), coin);
                break;
            }
        }
    }
}
