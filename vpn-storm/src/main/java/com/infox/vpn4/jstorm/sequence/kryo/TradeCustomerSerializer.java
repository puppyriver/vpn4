package com.infox.vpn4.jstorm.sequence.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.infox.vpn4.jstorm.sequence.bean.Pair;
import com.infox.vpn4.jstorm.sequence.bean.TradeCustomer;

/**
 * @author JohnFang (xiaojian.fxj@alibaba-inc.com).
 */
public class TradeCustomerSerializer extends Serializer<TradeCustomer> {

    PairSerializer pairSerializer = new PairSerializer();

    @Override
    public TradeCustomer read(Kryo kryo, Input input, Class<TradeCustomer> arg2) {

        Pair custormer = kryo.readObject(input, Pair.class);
        Pair trade = kryo.readObject(input, Pair.class);

        long timeStamp = input.readLong();
        String buffer = input.readString();

        TradeCustomer inner = new TradeCustomer(timeStamp, trade, custormer, buffer);
        return inner;
    }

    @Override
    public void write(Kryo kryo, Output output, TradeCustomer inner) {

        kryo.writeObject(output, inner.getCustomer());
        kryo.writeObject(output, inner.getTrade());
        output.writeLong(inner.getTimestamp());
        output.writeString(inner.getBuffer());
    }

}