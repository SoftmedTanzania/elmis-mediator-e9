package tz.go.moh.him.elmis.mediator.e9;

public class Constants {

    public interface ErrorMessages {
        String ERROR_REQUIRED_FIELDS_CHECK_FAILED = " - Required fields are empty;";
        String ERROR_INVALID_PAYLOAD = "Invalid payload;";
        String INVOICE_NUMBER_IS_BLANK = "invoiceNumber is blank;";
        String MSD_ORDER_NUMBER_IS_BLANK = "msdOrderNumber is blank;";
        String ELMIS_ORDER_NUMBER_IS_BLANK = "elmisOrderNumber is blank;";
        String STOCK_OUT_ITEM_WITH_ITEM_CODE = "Stock Out Item with itemCode ";
        String IN_SUFFICIENT_FUNDING_ITEM_WITH_ITEM_CODE = "InSufficient Funding Item with itemCode %s is missing required fields itemCode/itemDescription/uom/quantity;";
        String PHASED_OUT_ITEM_WITH_ITEM_CODE = "Phased out Item with itemCode %s is missing required fields itemCode/itemDescription/uom/quantity;";
        String CLOSE_TO_EXPIRE_ITEM_WITH_ITEM_CODE = "Close to Expire Item with itemCode %s is missing required fields itemCode/itemDescription/uom/quantity;";
        String RATIONING_ITEM_WITH_ITEM_CODE = "Rationing Item with itemCode %s is missing required fields itemCode/itemDescription/uom/quantity;";
        String FULL_FILLED_ITEM_WITH_ITEM_CODE = "Full Filled Item with itemCode %s is missing required fields itemCode/itemDescription/uom/quantity;";
    }
}
