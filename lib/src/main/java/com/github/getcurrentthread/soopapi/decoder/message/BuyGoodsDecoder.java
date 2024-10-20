package com.github.getcurrentthread.soopapi.decoder.message;

import java.util.HashMap;
import java.util.Map;

public class BuyGoodsDecoder implements IMessageDecoder {
    @Override
    public Map<String, Object> decode(String[] parts) {
        Map<String, Object> result = new HashMap<>();
        result.put("goodsType", Integer.parseInt(parts[1]));
        result.put("bjId", parts[2]);
        result.put("buyerId", parts[4]);
        result.put("buyerNickname", parts[5]);
        result.put("goodsName", parts[6]);
        result.put("goodsCount", Integer.parseInt(parts[7]));
        result.put("relay", 0);
        return result;
    }
}