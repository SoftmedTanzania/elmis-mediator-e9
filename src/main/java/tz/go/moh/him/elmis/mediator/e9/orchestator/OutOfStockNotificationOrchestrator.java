package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.codehaus.plexus.util.StringUtils;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.elmis.mediator.e9.domain.OutOfStockNotification;
import tz.go.moh.him.mediator.core.validator.DateValidatorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.*;

public class OutOfStockNotificationOrchestrator extends UntypedActor {
    private final MediatorConfig config;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected MediatorHTTPRequest originalRequest;
    protected String errorMessage = "";

    public OutOfStockNotificationOrchestrator(MediatorConfig config) {
        this.config = config;
    }

    public static boolean validateItemRequiredFields(OutOfStockNotification.Item item) {
        if (StringUtils.isBlank(item.getItemCode()))
            return false;
        if (StringUtils.isBlank(item.getItemDescription()))
            return false;
        if (StringUtils.isBlank(item.getUom()))
            return false;
        try {
            Long.parseLong(item.getQuantity());
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean validateFullFilledItemRequiredFields(OutOfStockNotification.FullFilledItems fullFilledItems) {
        if (StringUtils.isBlank(fullFilledItems.getItemDescription()))
            return false;
        if (StringUtils.isBlank(fullFilledItems.getItemCode()))
            return false;
        if (StringUtils.isBlank(fullFilledItems.getUom()))
            return false;
        try {
            Long.parseLong(fullFilledItems.getQuantity());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void onReceive(Object msg) {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            log.info("Received payload in JSON = " + ((MediatorHTTPRequest) msg).getBody());

            OutOfStockNotification outOfStockNotification = new Gson().fromJson(((MediatorHTTPRequest) msg).getBody(), OutOfStockNotification.class);

            if (validateOutOfStockNotification(outOfStockNotification)) {
                sendDataToElmis(((MediatorHTTPRequest) msg).getBody());
            } else {
                FinishRequest finishRequest = new FinishRequest(errorMessage, "text/plain", HttpStatus.SC_BAD_REQUEST);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
            }
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from eLMIS");
            finalizeResponse((MediatorHTTPResponse) msg);
        } else {
            unhandled(msg);
        }
    }

    protected boolean validateOutOfStockNotification(OutOfStockNotification outOfStockNotification) {
        boolean validationStatus = true;

        if (StringUtils.isBlank(outOfStockNotification.getInvoiceNumber())) {
            errorMessage += INVOICE_NUMBER_IS_BLANK;
            validationStatus = false;
        }

        if (StringUtils.isBlank(outOfStockNotification.getMsdOrderNumber())) {
            errorMessage += MSD_ORDER_NUMBER_IS_BLANK;
            validationStatus = false;
        }

        if (StringUtils.isBlank(outOfStockNotification.getElmisOrderNumber())) {
            errorMessage += ELMIS_ORDER_NUMBER_IS_BLANK;
            validationStatus = false;
        }

        if (outOfStockNotification.getFullFilledItems() != null) {
            for (OutOfStockNotification.FullFilledItems fullFilledItems : outOfStockNotification.getFullFilledItems()) {
                if (!validateFullFilledItemRequiredFields(fullFilledItems)) {
                    errorMessage += FULL_FILLED_ITEM_WITH_ITEM_CODE + fullFilledItems.getItemCode() + IS_MISSING_REQUIRED_FIELDS_ITEM_CODE_ITEM_DESCRIPTION_UOM_QUANTITY;
                    validationStatus = false;
                }
            }
        }

        if (outOfStockNotification.getStockOutItems() != null) {
            for (OutOfStockNotification.Item item : outOfStockNotification.getStockOutItems()) {
                if (!validateItemRequiredFields(item)) {
                    errorMessage += STOCK_OUT_ITEM_WITH_ITEM_CODE + item.getItemCode() + IS_MISSING_REQUIRED_FIELDS_ITEM_CODE_ITEM_DESCRIPTION_UOM_QUANTITY;
                    validationStatus = false;
                }
            }
        }

        if (outOfStockNotification.getInSufficientFundingItems() != null) {
            for (OutOfStockNotification.Item item : outOfStockNotification.getInSufficientFundingItems()) {
                if (!validateItemRequiredFields(item)) {
                    errorMessage += IN_SUFFICIENT_FUNDING_ITEM_WITH_ITEM_CODE + item.getItemCode() + IS_MISSING_REQUIRED_FIELDS_ITEM_CODE_ITEM_DESCRIPTION_UOM_QUANTITY;
                    validationStatus = false;
                }
            }
        }

        if (outOfStockNotification.getRationingItems() != null) {
            for (OutOfStockNotification.Item item : outOfStockNotification.getRationingItems()) {
                if (!validateItemRequiredFields(item)) {
                    errorMessage += RATIONING_ITEM_WITH_ITEM_CODE + item.getItemCode() + IS_MISSING_REQUIRED_FIELDS_ITEM_CODE_ITEM_DESCRIPTION_UOM_QUANTITY;
                    validationStatus = false;
                }
            }
        }

        if (outOfStockNotification.getCloseToExpireItems() != null) {
            for (OutOfStockNotification.Item item : outOfStockNotification.getCloseToExpireItems()) {
                if (!validateItemRequiredFields(item)) {
                    errorMessage += CLOSE_TO_EXPIRE_ITEM_WITH_ITEM_CODE + item.getItemCode() + IS_MISSING_REQUIRED_FIELDS_ITEM_CODE_ITEM_DESCRIPTION_UOM_QUANTITY;
                    validationStatus = false;
                }
            }
        }

        if (outOfStockNotification.getPhasedOutItems() != null) {
            for (OutOfStockNotification.Item item : outOfStockNotification.getPhasedOutItems()) {
                if (!validateItemRequiredFields(item)) {
                    errorMessage += PHASED_OUT_ITEM_WITH_ITEM_CODE + item.getItemCode() + IS_MISSING_REQUIRED_FIELDS_ITEM_CODE_ITEM_DESCRIPTION_UOM_QUANTITY;
                    validationStatus = false;
                }
            }
        }


        if (!DateValidatorUtils.isValidPastDate(outOfStockNotification.getInvoiceDate(), "dd-mm-yyyy")) {
            errorMessage += "Invoice data is invalid format;";
            validationStatus = false;
        }
        return validationStatus;
    }

    private void sendDataToElmis(String msg) {
        String scheme;
        if (config.getProperty("elmis.secure").equals("true")) {
            scheme = "https";
        } else {
            scheme = "http";
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        List<Pair<String, String>> params = new ArrayList<>();

        MediatorHTTPRequest forwardToElmisRequest = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Sending Out of Stock Notification to eLMIS", "POST", scheme,
                config.getProperty("elmis.host"), Integer.parseInt(config.getProperty("elmis.api.port")), config.getProperty("elmis.api.out_of_stock_notification.path"),
                msg, headers, params
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(forwardToElmisRequest, getSelf());
    }

    private void finalizeResponse(MediatorHTTPResponse response) {
        (originalRequest).getRequestHandler().tell(response.toFinishRequest(), getSelf());
    }
}
