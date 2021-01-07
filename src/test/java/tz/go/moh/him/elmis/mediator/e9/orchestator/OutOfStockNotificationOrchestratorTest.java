package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.MockHTTPConnector;
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
import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.ELMIS_ORDER_NUMBER_IS_BLANK;
import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.INVOICE_NUMBER_IS_BLANK;
import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.MSD_ORDER_NUMBER_IS_BLANK;

public class OutOfStockNotificationOrchestratorTest extends BaseTest {
    private static final String jsonPayload = "{\"invoiceNumber\":\"598357\",\"zone\":\"Muleba\",\"soldTo\":\"MZ510046\",\"soldToCustomerName\":\"Kaigala Health Center\",\"shipTo\":\"MZ510046\",\"shipToCustomerName\":\"Kaigala Health Center\",\"msdOrderNumber\":\"364509\",\"elmisOrderNumber\":\"18L\",\"invoiceDate\":\"28-07-2018\",\"shipVia\":\"Company Truck\",\"salesCategory\":\"ILS Sales\",\"paymentTerms\":\"On Account\",\"salesPerson\":\"Michael John\",\"comment\":\"Some comments\",\"invoiceLineTotal\":\"7,102,300.00\",\"invoicelineDiscount\":\"0.00\",\"invoiceMiscellanousCharges\":\"0.00\",\"invoiceTotal\":\"0.00\",\"legalNumber\":\"INML-018991\",\"fullFilledItems\":[{\"itemCode\":\"10010002BE\",\"itemDescription\":\"AMOXICILLIN CAPS\",\"uom\":\"1000CP\",\"quantity\":\"5\",\"batchSerialNo\":\"170595\",\"batchQuantity\":\"5\",\"expiryDate\":\"31-05-2020\",\"unitPrice\":\"31,500.00\",\"amount\":\"157500.00\"}],\"stockOutItems\":[{\"itemCode\":\"10010003MD\",\"itemDescription\":\"ALBENDAZOLE\",\"uom\":\"100TB\",\"quantity\":\"5\",\"missingItemStatus\":\"Out of Stock\"}],\"inSufficientFundingItems\":[{\"itemCode\":\"10010031MD\",\"itemDescription\":\"QUININE\",\"uom\":\"500TB\",\"quantity\":\"10\",\"missingItemStatus\":\"Insufficient Funding\"}],\"rationingItems\":[{\"itemCode\":\"10060024MD\",\"itemDescription\":\"DIAZEPAM\",\"uom\":\"10AMP\",\"quantity\":\"20\",\"missingItemStatus\":\"Rationing due to low stock\"}],\"closeToExpireItems\":[{\"itemCode\":\"10060025MD\",\"itemDescription\":\"CETIRIZINE\",\"uom\":\"100TB\",\"quantity\":\"20\",\"missingItemStatus\":\"Close to expire\"}],\"phasedOutItems\":[{\"itemCode\":\"10020015MD\",\"itemDescription\":\"Amoxicillin Granules\",\"uom\":\"24TB\",\"quantity\":\"20\",\"missingItemStatus\":\"Item phased out\"}]}";
    private static final String elmisSampleResponse = "{\n" +
            "\"invoiceNumber\": \"598357\",\n" +
            "\"message\":\"Received Successful\"\n" +
            "}\n";

    @Override
    public void before() throws Exception {
        super.before();
        List<MockLauncher.ActorToLaunch> toLaunch = new LinkedList<>();
        toLaunch.add(new MockLauncher.ActorToLaunch("http-connector", MockElmis.class));
        TestingUtils.launchActors(system, testConfig.getName(), toLaunch);
    }


    @Test
    public void testSendingOfOutOfStockNotificationRequest() throws Exception {
        assertNotNull(testConfig);
        new JavaTestKit(system) {{
            final ActorRef serviceReceivedOrchestrator = system.actorOf(Props.create(OutOfStockNotificationOrchestrator.class, testConfig));
            Map<String, String> headers = new HashMap<>();
            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/elmis/out_of_stock_notification",
                    jsonPayload,
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


            int responseStatus = 0;
            boolean foundResponse = false;
            String responsePayload = "";

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    foundResponse = true;
                    responseStatus = ((FinishRequest) o).getResponseStatus();
                    responsePayload = ((FinishRequest) o).getResponse();
                    break;
                }
            }


            assertTrue("Must send FinishRequest", foundResponse);
            assertEquals(200, responseStatus);
            assertEquals(elmisSampleResponse, responsePayload);
        }};
    }

    @Test
    public void testSendingAnInvalidOutOfStockNotificationRequest() throws Exception {
        final String invalidOutOfStockNotifiation = "{\"invoiceNumber\":\"\",\"zone\":\"Muleba\",\"soldTo\":\"MZ510046\",\"soldToCustomerName\":\"Kaigala Health Center\",\"shipTo\":\"MZ510046\",\"shipToCustomerName\":\"Kaigala Health Center\",\"msdOrderNumber\":\"\",\"elmisOrderNumber\":\"\",\"invoiceDate\":\"28-07-2018\",\"shipVia\":\"Company Truck\",\"salesCategory\":\"ILS Sales\",\"paymentTerms\":\"On Account\",\"salesPerson\":\"Michael John\",\"comment\":\"Some comments\",\"invoiceLineTotal\":\"7,102,300.00\",\"invoicelineDiscount\":\"0.00\",\"invoiceMiscellanousCharges\":\"0.00\",\"invoiceTotal\":\"0.00\",\"legalNumber\":\"INML-018991\",\"fullFilledItems\":[{\"itemCode\":\"10010002BE\",\"itemDescription\":\"AMOXICILLIN CAPS\",\"uom\":\"\",\"quantity\":\"5\",\"batchSerialNo\":\"170595\",\"batchQuantity\":\"5\",\"expiryDate\":\"31-05-2020\",\"unitPrice\":\"31,500.00\",\"amount\":\"157500.00\"}],\"stockOutItems\":[{\"itemCode\":\"10010003MD\",\"itemDescription\":\"\",\"uom\":\"100TB\",\"quantity\":\"5\",\"missingItemStatus\":\"Out of Stock\"}],\"inSufficientFundingItems\":[{\"itemCode\":\"10010031MD\",\"itemDescription\":\"QUININE\",\"uom\":\"500TB\",\"quantity\":\"\",\"missingItemStatus\":\"Insufficient Funding\"}],\"rationingItems\":[{\"itemCode\":\"10060024MD\",\"itemDescription\":\"DIAZEPAM\",\"uom\":\"\",\"quantity\":\"20\",\"missingItemStatus\":\"Rationing due to low stock\"}],\"closeToExpireItems\":[{\"itemCode\":\"10060025MD\",\"itemDescription\":\"CETIRIZINE\",\"uom\":\"100TB\",\"quantity\":\"20\",\"missingItemStatus\":\"Close to expire\"}],\"phasedOutItems\":[{\"itemCode\":\"10020015MD\",\"itemDescription\":\"Amoxicillin Granules\",\"uom\":\"24TB\",\"quantity\":\"20\",\"missingItemStatus\":\"Item phased out\"}]}";
        assertNotNull(testConfig);
        new JavaTestKit(system) {{
            final ActorRef serviceReceivedOrchestrator = system.actorOf(Props.create(OutOfStockNotificationOrchestrator.class, testConfig));
            Map<String, String> headers = new HashMap<>();
            MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                    getRef(),
                    getRef(),
                    "unit-test",
                    "POST",
                    "http",
                    null,
                    null,
                    "/elmis/out_of_stock_notification",
                    invalidOutOfStockNotifiation,
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
            int responseStatus = 0;
            String responsePayload = "";

            for (Object o : out) {
                if (o instanceof FinishRequest) {
                    foundResponse = true;
                    responsePayload = ((FinishRequest) o).getResponse();
                    responseStatus = ((FinishRequest) o).getResponseStatus();
                    break;
                }
            }


            assertTrue("Must send FinishRequest", foundResponse);
            assertEquals(400, responseStatus);
            assertTrue(responsePayload.contains(INVOICE_NUMBER_IS_BLANK));
            assertTrue(responsePayload.contains(MSD_ORDER_NUMBER_IS_BLANK));
            assertTrue(responsePayload.contains(ELMIS_ORDER_NUMBER_IS_BLANK));
        }};
    }

    private static class MockElmis extends MockHTTPConnector {


        @Override
        public String getResponse() {
            return elmisSampleResponse;
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
            assertEquals(jsonPayload, msg.getBody());
        }
    }
}