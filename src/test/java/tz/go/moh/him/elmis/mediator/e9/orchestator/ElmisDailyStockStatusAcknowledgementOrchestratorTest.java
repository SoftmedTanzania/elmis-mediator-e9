package tz.go.moh.him.elmis.mediator.e9.orchestator;


import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.Test;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.connectors.CoreAPIConnector;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import org.openhim.mediator.engine.testing.MockLauncher;
import org.openhim.mediator.engine.testing.TestingUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ElmisDailyStockStatusAcknowledgementOrchestratorTest extends BaseTest {

    private static final String jsonElmisAckPayload = "{\"imported\":0,\"updated\":0,\"ignored\":7382,\"status\":\"Success\",\"iL_TransactionIDNumber\":\"5ff2b29416a3c934156395d3\",\"il_TransactionIDNumber\":\"5ff2b29416a3c934156395d3\"}";
    private static MediatorConfig myTestConfig;

    @Override
    public void before() throws Exception {
        super.before();
        myTestConfig = testConfig;
        List<MockLauncher.ActorToLaunch> toLaunch = new LinkedList<>();
        toLaunch.add(new MockLauncher.ActorToLaunch("core-api-connector", MockOpenHIM.class));
        TestingUtils.launchActors(system, testConfig.getName(), toLaunch);
    }

    @Test
    public void testMediatorHTTPRequest() throws Exception {
        assertNotNull(testConfig);
        new JavaTestKit(system) {{
            final ActorRef serviceReceivedOrchestrator = system.actorOf(Props.create(ElmisDailyStockStatusAcknowledgementOrchestrator.class, testConfig));
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "text/plain");
            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/elmis/daily_stock_status",
                    jsonElmisAckPayload,
                    headers,
                    Collections.<Pair<String, String>>emptyList()
            );

            serviceReceivedOrchestrator.tell(POST_Request, getRef());

            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("5 seconds")) {
                        @Override
                        protected Object match(Object msg) throws Exception {
                            if (msg instanceof FinishRequest) {
                                return msg;
                            }
                            throw noMatch();
                        }
                    }.get();

            boolean foundResponse = false;

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    foundResponse = true;
                    break;
                }
            }

            assertTrue("Must send FinishRequest", foundResponse);
        }};
    }

    private static class MockOpenHIM extends CoreAPIConnector {
        private final String openHIMSampleTransaction;

        public MockOpenHIM() {
            super(myTestConfig);
            openHIMSampleTransaction = "{\"request\":{\"host\":\"127.0.0.1\",\"port\":\"5001\",\"path\":\"/services_received\",\"headers\":{\"content-type\":\"text/csv\"},\"querystring\":\"\",\"body\":\"Plant,PartNum,UOM\\r\\nDM,10010001MD,1000TB\",\"method\":\"POST\",\"timestamp\":\"2021-01-04T06:15:48.662Z\"},\"response\":{\"status\":202,\"headers\":{\"x-openhim-transactionid\":\"5ff2b29416a3c934156395d3\"},\"body\":\"\",\"timestamp\":\"2021-01-04T06:15:48.746Z\"},\"error\":null,\"childIDs\":[],\"canRerun\":true,\"autoRetry\":false,\"wasRerun\":false,\"_id\":\"5ff2b29416a3c934156395d3\",\"status\":\"Successful\",\"clientID\":\"5fec702016a3c93415637002\",\"channelID\":\"5fec6e2416a3c93415636f2d\",\"clientIP\":\"197.250.198.31\",\"routes\":[],\"orchestrations\":[],\"__v\":0}";
        }

        @Override
        public void onReceive(Object msg) {
            if (msg instanceof MediatorHTTPRequest) {
                if (((MediatorHTTPRequest) msg).getMethod().equals("GET")) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "text/application/json");

                    MediatorHTTPResponse mediatorHTTPResponse = new MediatorHTTPResponse((MediatorHTTPRequest) msg, openHIMSampleTransaction, 200, headers);//new FinishRequest(response, "text/plain", HttpStatus.SC_OK);
                    ((MediatorHTTPRequest) msg).getRespondTo().tell(mediatorHTTPResponse, getSelf());
                } else if (((MediatorHTTPRequest) msg).getMethod().equals("PUT")) {
                    JSONObject receivedUpdatedTransaction = new JSONObject(((MediatorHTTPRequest) msg).getBody());

                    assertEquals(200, receivedUpdatedTransaction.getJSONObject("response").getInt("status"));
                    assertEquals(jsonElmisAckPayload, receivedUpdatedTransaction.getJSONObject("response").getString("body"));
                }
            }
        }
    }

}