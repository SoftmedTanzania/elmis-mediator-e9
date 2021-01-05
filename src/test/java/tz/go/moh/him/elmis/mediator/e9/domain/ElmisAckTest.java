package tz.go.moh.him.elmis.mediator.e9.domain;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ElmisAckTest {

    @Test
    public void testElmisAck() throws Exception {
        String jsonElmisAckPayload = "{\"imported\":1,\"updated\":0,\"ignored\":7382,\"status\":\"Success\",\"iL_TransactionIDNumber\":\"5ff2b29416a3c934156395d3\",\"il_TransactionIDNumber\":\"5ff2b29416a3c934156395d3\"}";
        ElmisAck elmisAck = new Gson().fromJson(jsonElmisAckPayload, ElmisAck.class);

        assertEquals(1, elmisAck.getImported());
        assertEquals(0, elmisAck.getUpdated());
        assertEquals(7382, elmisAck.getIgnored());
        assertEquals("Success", elmisAck.getStatus());
        assertEquals("5ff2b29416a3c934156395d3", elmisAck.getIlTransactionIDNumber());
        assertEquals("5ff2b29416a3c934156395d3", elmisAck.getiLTransactionIDNumber());
    }

}