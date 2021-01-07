package tz.go.moh.him.elmis.mediator.e9.domain;

import java.util.List;

public class OutOfStockNotification {

    private String invoiceNumber;
    private String zone;
    private String soldTo;
    private String soldToCustomerName;
    private String shipTo;
    private String shipToCustomerName;
    private String msdOrderNumber;
    private String elmisOrderNumber;
    private String invoiceDate;
    private String shipVia;
    private String salesCategory;
    private String paymentTerms;
    private String salesPerson;
    private String comment;
    private String invoiceLineTotal;
    private String invoicelineDiscount;
    private String invoiceMiscellanousCharges;
    private String invoiceTotal;
    private String legalNumber;

    private List<FullFilledItem> fullFilledItems;
    private List<Item> stockOutItems;
    private List<Item> inSufficientFundingItems;
    private List<Item> rationingItems;
    private List<Item> closeToExpireItems;
    private List<Item> phasedOutItems;

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getZone() {
        return zone;
    }

    public String getSoldTo() {
        return soldTo;
    }

    public String getSoldToCustomerName() {
        return soldToCustomerName;
    }

    public String getShipTo() {
        return shipTo;
    }

    public String getShipToCustomerName() {
        return shipToCustomerName;
    }

    public String getMsdOrderNumber() {
        return msdOrderNumber;
    }

    public String getElmisOrderNumber() {
        return elmisOrderNumber;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public String getShipVia() {
        return shipVia;
    }

    public String getSalesCategory() {
        return salesCategory;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public String getSalesPerson() {
        return salesPerson;
    }

    public String getComment() {
        return comment;
    }

    public String getInvoiceLineTotal() {
        return invoiceLineTotal;
    }

    public String getInvoicelineDiscount() {
        return invoicelineDiscount;
    }

    public String getInvoiceMiscellanousCharges() {
        return invoiceMiscellanousCharges;
    }

    public String getInvoiceTotal() {
        return invoiceTotal;
    }

    public String getLegalNumber() {
        return legalNumber;
    }

    public List<FullFilledItem> getFullFilledItems() {
        return fullFilledItems;
    }

    public List<Item> getStockOutItems() {
        return stockOutItems;
    }

    public List<Item> getInSufficientFundingItems() {
        return inSufficientFundingItems;
    }

    public List<Item> getRationingItems() {
        return rationingItems;
    }

    public List<Item> getCloseToExpireItems() {
        return closeToExpireItems;
    }

    public List<Item> getPhasedOutItems() {
        return phasedOutItems;
    }

    public static class FullFilledItem {
        private String itemCode;
        private String itemDescription;
        private String uom;
        private String quantity;
        private String batchSerialNo;
        private String batchQuantity;
        private String expiryDate;
        private String unitPrice;
        private String amount;

        public String getItemCode() {
            return itemCode;
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public String getUom() {
            return uom;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getBatchSerialNo() {
            return batchSerialNo;
        }

        public String getBatchQuantity() {
            return batchQuantity;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public String getUnitPrice() {
            return unitPrice;
        }

        public String getAmount() {
            return amount;
        }
    }

    public static class Item {
        private String itemCode;
        private String itemDescription;
        private String uom;
        private String quantity;
        private String missingItemStatus;

        public String getItemCode() {
            return itemCode;
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public String getUom() {
            return uom;
        }

        public String getQuantity() {
            return quantity;
        }

        public String getMissingItemStatus() {
            return missingItemStatus;
        }
    }
}
