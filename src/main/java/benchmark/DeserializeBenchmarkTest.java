package benchmark;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import json.JsonTrade;
import org.agrona.concurrent.UnsafeBuffer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import protobuf.TradeProtos;
import sbe.MessageHeaderDecoder;
import sbe.MessageHeaderEncoder;
import sbe.TradeDecoder;
import sbe.TradeEncoder;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class DeserializeBenchmarkTest {

    ByteBuffer jsonByteBuffer;
    ByteBuffer protoByteBuffer;
    UnsafeBuffer sbeByteBuffer;
    Gson gson;

    /** Test converting bytebuffer --> object
     * Setup prepare all kinds of bytebuffer
     */

    @Setup(Level.Invocation)
    public void setup() {
        //Gson
        gson = new Gson();
        JsonTrade jsonTrade = new JsonTrade("USD/ETH", 1000, 10);
        jsonByteBuffer = ByteBuffer.wrap(gson.toJson(jsonTrade).getBytes());

        //Protobuffer
        TradeProtos.Trade protoTrade = TradeProtos.Trade
                .newBuilder()
                .setSym("USD/ETH")
                .setRate(2000)
                .setAmount(100)
                .build();
        protoByteBuffer = ByteBuffer.wrap(protoTrade.toByteArray());

        //SBE bytebuffer
        final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        sbeByteBuffer = new UnsafeBuffer(byteBuffer);
        MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
        TradeEncoder tradeEncoder = new TradeEncoder();
        tradeEncoder.wrapAndApplyHeader(sbeByteBuffer, 0, messageHeaderEncoder);
        tradeEncoder.sym("USD/ETH");
        tradeEncoder.amount(1000);
        tradeEncoder.rate(100);
    }

    @Benchmark
    public void jsonDeserializeTest (Blackhole blackhole) {
        JsonTrade trade = gson.fromJson(new String(jsonByteBuffer.array()), JsonTrade.class);
        blackhole.consume(trade);
    }

    @Benchmark
    public void protobufDeserializeTest(Blackhole blackhole) throws InvalidProtocolBufferException {
        TradeProtos.Trade protoTrade = TradeProtos.Trade.parseFrom(protoByteBuffer.array());
        blackhole.consume(protoTrade);
    }

    @Benchmark
    public void sbeDeserializeTest(Blackhole blackhole) {
        MessageHeaderDecoder messageHeaderDecoder = new MessageHeaderDecoder();
        TradeDecoder tradeDecoder = new TradeDecoder();
        tradeDecoder.wrapAndApplyHeader(sbeByteBuffer, 0, messageHeaderDecoder);
        JsonTrade jsonTrade = new JsonTrade(tradeDecoder.sym(), tradeDecoder.rate(), tradeDecoder.amount());
        blackhole.consume(jsonTrade);
    }

}
