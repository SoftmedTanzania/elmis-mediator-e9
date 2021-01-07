package tz.go.moh.him.elmis.mediator.e9.domain;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.*;

public class OutOfStockNotificationTest {

    @Test
    public void testOutOfStockNotificationTest() throws Exception {
        String jsonOutOfStockNotificationPayload = "{\"invoiceNumber\":\"598357\",\"zone\":\"Muleba\",\"soldTo\":\"MZ510046\",\"soldToCustomerName\":\"Kaigala Health Center\",\"shipTo\":\"MZ510046\",\"shipToCustomerName\":\"Kaigala Health Center\",\"msdOrderNumber\":\"364509\",\"elmisOrderNumber\":\"18L\",\"invoiceDate\":\"28-07-2018\",\"shipVia\":\"Company Truck\",\"salesCategory\":\"ILS Sales\",\"paymentTerms\":\"On Account\",\"salesPerson\":\"Michael John\",\"comment\":\"Some comments\",\"invoiceLineTotal\":\"7,102,300.00\",\"invoicelineDiscount\":\"0.00\",\"invoiceMiscellanousCharges\":\"0.00\",\"invoiceTotal\":\"0.00\",\"legalNumber\":\"INML-018991\",\"fullFilledItems\":[{\"itemCode\":\"10010002BE\",\"itemDescription\":\"AMOXICILLIN CAPS\",\"uom\":\"1000CP\",\"quantity\":\"5\",\"batchSerialNo\":\"170595\",\"batchQuantity\":\"5\",\"expiryDate\":\"31-05-2020\",\"unitPrice\":\"31,500.00\",\"amount\":\"157500.00\"}],\"stockOutItems\":[{\"itemCode\":\"10010003MD\",\"itemDescription\":\"ALBENDAZOLE\",\"uom\":\"100TB\",\"quantity\":\"5\",\"missingItemStatus\":\"Out of Stock\"}],\"inSufficientFundingItems\":[{\"itemCode\":\"10010031MD\",\"itemDescription\":\"QUININE\",\"uom\":\"500TB\",\"quantity\":\"10\",\"missingItemStatus\":\"Insufficient Funding\"}],\"rationingItems\":[{\"itemCode\":\"10060024MD\",\"itemDescription\":\"DIAZEPAM\",\"uom\":\"10AMP\",\"quantity\":\"20\",\"missingItemStatus\":\"Rationing due to low stock\"}],\"closeToExpireItems\":[{\"itemCode\":\"10060025MD\",\"itemDescription\":\"CETIRIZINE\",\"uom\":\"100TB\",\"quantity\":\"20\",\"missingItemStatus\":\"Close to expire\"}],\"phasedOutItems\":[{\"itemCode\":\"10020015MD\",\"itemDescription\":\"Amoxicillin Granules\",\"uom\":\"24TB\",\"quantity\":\"20\",\"missingItemStatus\":\"Item phased out\"}]}";
        OutOfStockNotification outOfStockNotification = new Gson().fromJson(jsonOutOfStockNotificationPayload, OutOfStockNotification.class);

        assertEquals("598357", outOfStockNotification.getInvoiceNumber());
        assertEquals("Muleba", outOfStockNotification.getZone());
        assertEquals("MZ510046", outOfStockNotification.getSoldTo());
        assertEquals("Kaigala Health Center", outOfStockNotification.getSoldToCustomerName());
        assertEquals("MZ510046", outOfStockNotification.getShipTo());
        assertEquals("Kaigala Health Center", outOfStockNotification.getShipToCustomerName());
        assertEquals("364509", outOfStockNotification.getMsdOrderNumber());
        assertEquals("18L", outOfStockNotification.getElmisOrderNumber());
        assertEquals("28-07-2018", outOfStockNotification.getInvoiceDate());
        assertEquals("Company Truck", outOfStockNotification.getShipVia());
        assertEquals("ILS Sales", outOfStockNotification.getSalesCategory());
        assertEquals("On Account", outOfStockNotification.getPaymentTerms());
        assertEquals("Michael John", outOfStockNotification.getSalesPerson());
        assertEquals("Some comments", outOfStockNotification.getComment());
        assertEquals("7,102,300.00", outOfStockNotification.getInvoiceLineTotal());
        assertEquals("0.00", outOfStockNotification.getInvoicelineDiscount());
        assertEquals("0.00", outOfStockNotification.getInvoiceMiscellanousCharges());
        assertEquals("0.00", outOfStockNotification.getInvoiceTotal());
        assertEquals("INML-018991", outOfStockNotification.getLegalNumber());


        assertEquals("10010002BE", outOfStockNotification.getFullFilledItems().get(0).getItemCode());
        assertEquals("AMOXICILLIN CAPS", outOfStockNotification.getFullFilledItems().get(0).getItemDescription());
        assertEquals("1000CP", outOfStockNotification.getFullFilledItems().get(0).getUom());
        assertEquals("5", outOfStockNotification.getFullFilledItems().get(0).getQuantity());
        assertEquals("170595", outOfStockNotification.getFullFilledItems().get(0).getBatchSerialNo());
        assertEquals("5", outOfStockNotification.getFullFilledItems().get(0).getQuantity());
        assertEquals("31-05-2020", outOfStockNotification.getFullFilledItems().get(0).getExpiryDate());
        assertEquals("31,500.00", outOfStockNotification.getFullFilledItems().get(0).getUnitPrice());
        assertEquals("5", outOfStockNotification.getFullFilledItems().get(0).getBatchQuantity());
        assertEquals("157500.00", outOfStockNotification.getFullFilledItems().get(0).getAmount());

        assertEquals("10010003MD", outOfStockNotification.getStockOutItems().get(0).getItemCode());
        assertEquals("ALBENDAZOLE", outOfStockNotification.getStockOutItems().get(0).getItemDescription());
        assertEquals("100TB", outOfStockNotification.getStockOutItems().get(0).getUom());
        assertEquals("5", outOfStockNotification.getStockOutItems().get(0).getQuantity());
        assertEquals("Out of Stock", outOfStockNotification.getStockOutItems().get(0).getMissingItemStatus());

        assertEquals(1, outOfStockNotification.getInSufficientFundingItems().size());
        assertEquals(1, outOfStockNotification.getRationingItems().size());
        assertEquals(1, outOfStockNotification.getCloseToExpireItems().size());
        assertEquals(1, outOfStockNotification.getPhasedOutItems().size());

    }


}