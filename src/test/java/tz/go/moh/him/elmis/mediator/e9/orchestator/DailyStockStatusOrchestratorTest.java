package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;
import org.openhim.mediator.engine.testing.MockLauncher;
import org.openhim.mediator.engine.testing.TestingUtils;
import tz.go.moh.him.elmis.mediator.e9.domain.DailyStockStatus;
import tz.go.moh.him.mediator.core.adapter.CsvAdapterUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DailyStockStatusOrchestratorTest extends BaseTest {
    protected JSONObject dailyStockStatusErrorMessageResource;
    private static final String csvPayload =
            "Plant,PartNum,UOM,PartDescription,OnHandQty,Date,MonthOfStock\n" +
                    "DM,10010001MD,1000TB,ACETYLSALICYLIC ACID (ASPIRIN)  TABLETS 300MG,0,20201201,1";

    @Override
    public void before() throws Exception {
        super.before();

        dailyStockStatusErrorMessageResource = errorMessageResource.getJSONObject("DAILY_STOCK_STATUS_ERROR_MESSAGES");
        List<MockLauncher.ActorToLaunch> toLaunch = new LinkedList<>();
        toLaunch.add(new MockLauncher.ActorToLaunch("http-connector", MockElmis.class));
        TestingUtils.launchActors(system, testConfig.getName(), toLaunch);
    }

    @Test
    public void testMediatorHTTPRequest() throws Exception {
        assertNotNull(testConfig);
        new JavaTestKit(system) {{
            final ActorRef serviceReceivedOrchestrator = system.actorOf(Props.create(DailyStockStatusOrchestrator.class, testConfig));
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "text/plain");
            headers.put("x-openhim-transactionid", "1112212");
            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/elmis/daily_stock_status",
                    csvPayload,
                    headers,
                    Collections.<Pair<String, String>>emptyList()
            );

            serviceReceivedOrchestrator.tell(POST_Request, getRef());

            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("1 second")) {
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

    @Test
    public void testInValidPayload() throws Exception {
        assertNotNull(testConfig);

        new JavaTestKit(system) {{
            String invalidPayload = "Message Type";
            createActorAndSendRequest(system, testConfig, getRef(), invalidPayload, DailyStockStatusOrchestrator.class, "/elmis/daily_stock_status");

            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("1 second")) {
                        @Override
                        protected Object match(Object msg) throws Exception {
                            if (msg instanceof FinishRequest) {
                                return msg;
                            }
                            throw noMatch();
                        }
                    }.get();

            int responseStatus = 0;
            String responseMessage = "";

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    responseStatus = ((FinishRequest) o).getResponseStatus();
                    responseMessage = ((FinishRequest) o).getResponse();
                    break;
                }
            }

            assertEquals(400, responseStatus);
            assertTrue(responseMessage.contains(dailyStockStatusErrorMessageResource.getString("ERROR_INVALID_PAYLOAD")));

        }};
    }

    @Test
    public void validateRequiredFields() {
        assertNotNull(testConfig);

        new JavaTestKit(system) {{
            String invalidPayload = "Plant,PartNum,UOM,PartDescription,OnHandQty,Date,MonthOfStock\n" +
                    ",,,,,,";
            createActorAndSendRequest(system, testConfig, getRef(), invalidPayload, DailyStockStatusOrchestrator.class, "/bed_occupancy");

            final Object[] out =
                    new ReceiveWhile<Object>(Object.class, duration("1 second")) {
                        @Override
                        protected Object match(Object msg) throws Exception {
                            if (msg instanceof FinishRequest) {
                                return msg;
                            }
                            throw noMatch();
                        }
                    }.get();

            String responseMessage = "";
            int responseStatus = 0;

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    responseStatus = ((FinishRequest) o).getResponseStatus();
                    responseMessage = ((FinishRequest) o).getResponse();
                    break;
                }
            }

            assertEquals(400, responseStatus);
            assertTrue(responseMessage.contains(dailyStockStatusErrorMessageResource.getString("ERROR_PLANT_IS_BLANK")));
            assertTrue(responseMessage.contains(String.format(dailyStockStatusErrorMessageResource.getString("ERROR_PART_NUM_IS_BLANK"),"")));
            assertTrue(responseMessage.contains(String.format(dailyStockStatusErrorMessageResource.getString("ERROR_OUM_IS_BLANK"),"")));
            assertTrue(responseMessage.contains(String.format(dailyStockStatusErrorMessageResource.getString("ERROR_PART_DESCRIPTION_IS_BLANK"),"")));
            assertTrue(responseMessage.contains(String.format(dailyStockStatusErrorMessageResource.getString("ERROR_ON_HAND_QTY_IS_BLANK"),"")));
            assertTrue(responseMessage.contains(String.format(dailyStockStatusErrorMessageResource.getString("ERROR_MONTH_OF_STOCK_IS_BLANK"),"")));
            assertTrue(responseMessage.contains(String.format(dailyStockStatusErrorMessageResource.getString("ERROR_DATE_IS_BLANK"),"")));
        }};
    }


    private static class MockElmis extends MockHTTPConnector {
        private final String response;
        private final List<DailyStockStatus> payloadConvertedIntoArrayList;

        public MockElmis() throws IOException {
            response = "successful test response";
            payloadConvertedIntoArrayList = (List<DailyStockStatus>) CsvAdapterUtils.csvToArrayList(csvPayload, DailyStockStatus.class);

        }

        @Override
        public String getResponse() {
            return response;
        }

        @Override
        public Integer getStatus() {
            return 200;
        }

        @Override
        public Map<String, String> getHeaders() {
            return Collections.emptyMap();
        }

        @Override
        public void executeOnReceive(MediatorHTTPRequest msg) {
            System.out.println(msg.getBody());
            JSONArray messageJsonArray = new JSONArray(msg.getBody());

            DailyStockStatus expectedPayload = payloadConvertedIntoArrayList.get(0);

            DailyStockStatus dailyStockStatus = new Gson().fromJson(String.valueOf(messageJsonArray.getJSONObject(0)), DailyStockStatus.class);

            assertEquals(expectedPayload.getPartNum(), dailyStockStatus.getPartNum());
            assertEquals(expectedPayload.getDate(), dailyStockStatus.getDate());
            assertEquals(expectedPayload.getMonthOfStock(), dailyStockStatus.getMonthOfStock());
            assertEquals(expectedPayload.getOnHandQty(), dailyStockStatus.getOnHandQty());
            assertEquals(expectedPayload.getOum(), dailyStockStatus.getOum());
            assertEquals(expectedPayload.getPartDescription(), dailyStockStatus.getPartDescription());
            assertEquals(expectedPayload.getPlant(), dailyStockStatus.getPlant());
        }
    }
}
