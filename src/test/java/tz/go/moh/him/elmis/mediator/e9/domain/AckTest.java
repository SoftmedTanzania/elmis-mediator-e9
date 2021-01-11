package tz.go.moh.him.elmis.mediator.e9.domain;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AckTest {

    @Test
    public void testElmisAck() throws Exception {
        String jsonElmisAckPayload = "{\"imported\":1,\"updated\":0,\"ignored\":7382,\"status\":\"Success\",\"iL_TransactionIDNumber\":\"5ff2b29416a3c934156395d3\",\"il_TransactionIDNumber\":\"5ff2b29416a3c934156395d3\"}";
        Ack ack = new Gson().fromJson(jsonElmisAckPayload, Ack.class);

        assertEquals(1, ack.getImported());
        assertEquals(0, ack.getUpdated());
        assertEquals(7382, ack.getIgnored());
        assertEquals("Success", ack.getStatus());
        assertEquals("5ff2b29416a3c934156395d3", ack.getIlTransactionIDNumber());
        assertEquals("5ff2b29416a3c934156395d3", ack.getiLTransactionIDNumber());
    }

}