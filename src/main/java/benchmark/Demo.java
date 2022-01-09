package benchmark;

import com.google.gson.Gson;
import json.JsonTrade;

import java.nio.ByteBuffer;

public class Demo {

    public static void main(String[] args) {
        // Serialize
        JsonTrade trade = new JsonTrade("USD/ETH", 1000, 100);
        System.out.println("" + new Gson().toJson(trade));
        String json = new Gson().toJson(trade);
        ByteBuffer buffer = ByteBuffer.wrap(json.getBytes());

        //Deserialize
        String json2 = new String(buffer.array());
        JsonTrade trade2 = new Gson().fromJson(json2, JsonTrade.class);
        System.out.println("" + new Gson().toJson(trade2));
    }
}
