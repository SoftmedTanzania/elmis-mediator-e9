package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.ERROR_DATE_IS_OF_INVALID_FORMAT_IS_NOT_A_VALID_PAST_DATE;
import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.ERROR_INVALID_PAYLOAD;

public class DailyStockStatusOrchestratorTest extends BaseTest {
    private static final String csvPayload =
            "Plant,PartNum,UOM,PartDescription,OnHandQty,Date,MonthOfStock\n" +
                    "DM,10010001MD,1000TB,ACETYLSALICYLIC ACID (ASPIRIN)  TABLETS 300MG,0,20201201,1";

    @Override
    public void before() throws Exception {
        super.before();

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
    public void testInValidDate() throws Exception {
        assertNotNull(testConfig);

        new JavaTestKit(system) {{
            String invalidDate =
                    "[{\"IL_IDNumber\":\"123456789\",\"Plant\":\"DM\",\"PartNum\":\"10010001MD\",\"UOM\":\"1000TB\",\"PartDescription\":\"ACETYLSALICYLIC ACID (ASPIRIN)  TABLETS 300MG\",\"OnHandQty\":0,\"Date\":\"Feb  1 2018  4:00AM\",\"MonthOfStock\":\"1\"},{\"IL_IDNumber\":\"123456789\",\"Plant\":\"DR\",\"PartNum\":\"10010001MD\",\"UOM\":\"1000TB\",\"PartDescription\":\"ACETYLSALICYLIC ACID (ASPIRIN)  TABLETS 300MG\",\"OnHandQty\":2,\"Date\":\"Feb  1 2018  4:00AM\",\"MonthOfStock\":\"2\"}]";
            createActorAndSendRequest(system, testConfig, getRef(), invalidDate, DailyStockStatusOrchestrator.class, "/elmis/daily_stock_status");

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
            assertTrue(responseMessage.contains(ERROR_DATE_IS_OF_INVALID_FORMAT_IS_NOT_A_VALID_PAST_DATE));
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
            assertTrue(responseMessage.equals(ERROR_INVALID_PAYLOAD));
        }};
    }

    @Test
    public void validateRequiredFields() {
        DailyStockStatus dailyStockStatus = new DailyStockStatus();
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setPlant("DM");
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setPartNum("10010001MD");
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setOum("1000TB");
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setPartDescription("ACETYLSALICYLIC ACID (ASPIRIN)  TABLETS 300MG");
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setOnHandQty("0");
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setDate("20180101");
        assertFalse(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));

        dailyStockStatus.setMonthOfStock("1");

        //Valid payload
        assertTrue(DailyStockStatusOrchestrator.validateRequiredFields(dailyStockStatus));
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
