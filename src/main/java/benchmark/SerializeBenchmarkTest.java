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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
public class SerializeBenchmarkTest {

    JsonTrade jsonTrade;
    TradeProtos.Trade protoTrade;
    UnsafeBuffer sbeBuffer;
    Gson gson = new Gson();

    /**Test converting object --> bytebuffer
     * Setup prepare all necessary objects
     */
    @Setup(Level.Invocation)
    public void setup() throws InvalidProtocolBufferException {

        jsonTrade = new JsonTrade("USD/ETH", 1000, 100);

        protoTrade = TradeProtos.Trade
                .newBuilder()
                .setSym("USD/ETH")
                .setRate(2000)
                .setAmount(100)
                .build();


        final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        sbeBuffer = new UnsafeBuffer(byteBuffer);
    }

    @Benchmark
    public void jsonSerializeTest(Blackhole blackhole) {
        blackhole.consume(ByteBuffer.wrap(gson.toJson(jsonTrade).getBytes()));
    }

    @Benchmark
    public void protobufSerializeTest(Blackhole blackhole) {
        blackhole.consume(ByteBuffer.wrap(protoTrade.toByteArray()));
    }

    @Benchmark
    public void sbeSerializeTest(Blackhole blackhole) {
         MessageHeaderEncoder messageHeaderEncoder = new MessageHeaderEncoder();
         TradeEncoder tradeEncoder = new TradeEncoder();
         tradeEncoder.wrapAndApplyHeader(sbeBuffer, 0, messageHeaderEncoder);
         tradeEncoder.sym("USD/ETH");
         tradeEncoder.amount(1000);
         tradeEncoder.rate(100);
         blackhole.consume(sbeBuffer);
    }
}
