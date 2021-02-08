package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.elmis.mediator.e9.domain.OutOfStockNotification;
import tz.go.moh.him.elmis.mediator.e9.domain.OutOfStockNotificationErrorMessage;
import tz.go.moh.him.mediator.core.domain.ErrorMessage;
import tz.go.moh.him.mediator.core.domain.ResultDetail;
import tz.go.moh.him.mediator.core.validator.DateValidatorUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents Out Of Stock Notification orchestrator.
 */
public class OutOfStockNotificationOrchestrator extends UntypedActor {
    /**
     * The mediator configuration.
     */
    private final MediatorConfig config;

    /**
     * The logger instance.
     */
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    /**
     * Represents a mediator request.
     */
    protected MediatorHTTPRequest originalRequest;

    /**
     * Represents an Error Messages Definition Resource Object defined in <a href="file:../resources/error-messages.json">/resources/error-messages.json</a>.
     */
    protected JSONObject errorMessageResource;

    /**
     * Represents a list of error messages, if any,that have been caught during payload data validation to be returned to the source system as response.
     */
    protected OutOfStockNotificationErrorMessage errors = new OutOfStockNotificationErrorMessage();

    /**
     * Initializes a new instance of the {@link OutOfStockNotificationOrchestrator} class.
     *
     * @param config The mediator configuration.
     */
    public OutOfStockNotificationOrchestrator(MediatorConfig config) {
        this.config = config;
        InputStream stream = getClass().getClassLoader().getResourceAsStream("error-messages.json");
        try {
            if (stream != null) {
                errorMessageResource = new JSONObject(IOUtils.toString(stream)).getJSONObject("OUT_OF_STOCK_NOTIFICATIONS_ERROR_MESSAGES");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for validating OutOfStockNotification full filled item
     *
     * @param fullFilledItem to be validated
     * @return array list of validation results details incase of failed validations
     */
    public List<ResultDetail> validateFullFilledItemRequiredFields(OutOfStockNotification.FullFilledItem fullFilledItem) {
        List<ResultDetail> resultDetailsList = new ArrayList<>();
        if (StringUtils.isBlank(fullFilledItem.getItemDescription()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_ITEM_DESCRIPTION_IS_BLANK"), fullFilledItem.getItemCode()), null));

        if (StringUtils.isBlank(fullFilledItem.getItemCode()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_ITEM_CODE_IS_BLANK"), null));

        if (StringUtils.isBlank(fullFilledItem.getUom()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_UOM_IS_BLANK"), fullFilledItem.getItemCode()), null));

        try {
            Long.parseLong(fullFilledItem.getQuantity());
        } catch (Exception e) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_QUANTITY_IS_BLANK"), fullFilledItem.getItemCode()), null));
        }
        return resultDetailsList;
    }

    /**
     * Method for validating item
     *
     * @param item to be validated
     * @return array list of validation results details in case of failed validations
     */
    public List<ResultDetail> validateItemRequiredFields(OutOfStockNotification.Item item) {
        List<ResultDetail> resultDetailsList = new ArrayList<>();

        if (StringUtils.isBlank(item.getItemCode()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_ITEM_CODE_IS_BLANK"), null));

        if (StringUtils.isBlank(item.getUom()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_UOM_IS_BLANK"), item.getItemCode()), null));

        if (StringUtils.isBlank(item.getItemDescription()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_ITEM_DESCRIPTION_IS_BLANK"), item.getItemCode()), null));

        try {
            Long.parseLong(item.getQuantity());
        } catch (Exception e) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_QUANTITY_IS_BLANK"), item.getItemCode()), null));
        }
        return resultDetailsList;
    }


    /**
     * Method for validating a list of items list
     *
     * @param items to be validated
     * @return array list of validation results details incase of failed validations
     */
    public List<ErrorMessage> validateItemsListRequiredFields(List<OutOfStockNotification.Item> items) {
        List<ErrorMessage> errorMessagesList = new ArrayList<>();
        for (OutOfStockNotification.Item item : items) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setSource(new Gson().toJson(item));
            List<ResultDetail> resultDetailsList = validateItemRequiredFields(item);

            if (!resultDetailsList.isEmpty()) {
                errorMessage.setResultsDetails(resultDetailsList);
                errorMessagesList.add(errorMessage);
            }
        }
        return errorMessagesList;
    }

    /**
     * Method for validating OutOfStockNotification full filled items list
     *
     * @param fullFilledItems to be validated
     * @return array list of validation results details incase of failed validations
     */
    public List<ErrorMessage> validateFullFilledItemListRequired(List<OutOfStockNotification.FullFilledItem> fullFilledItems) {
        List<ErrorMessage> errorMessagesList = new ArrayList<>();
        for (OutOfStockNotification.FullFilledItem fullFilledItem : fullFilledItems) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setSource(new Gson().toJson(fullFilledItem));
            List<ResultDetail> resultDetailsList = validateFullFilledItemRequiredFields(fullFilledItem);

            if (!resultDetailsList.isEmpty()) {
                errorMessage.setResultsDetails(resultDetailsList);
                errorMessagesList.add(errorMessage);
            }
        }
        return errorMessagesList;
    }

    /**
     * Handles the received message.
     *
     * @param msg The received message.
     */
    @Override
    public void onReceive(Object msg) {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            log.info("Received payload in JSON = " + ((MediatorHTTPRequest) msg).getBody());

            OutOfStockNotification outOfStockNotification = new Gson().fromJson(((MediatorHTTPRequest) msg).getBody(), OutOfStockNotification.class);

            if (validateOutOfStockNotification(outOfStockNotification)) {
                sendDataToElmis(((MediatorHTTPRequest) msg).getBody());
            } else {
                FinishRequest finishRequest = new FinishRequest(new Gson().toJson(errors), "text/plain", HttpStatus.SC_BAD_REQUEST);
                (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
            }
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from eLMIS");
            (originalRequest).getRequestHandler().tell(((MediatorHTTPResponse) msg).toFinishRequest(), getSelf());
        } else {
            unhandled(msg);
        }
    }

    /**
     * Handles data validations
     *
     * @param outOfStockNotification object to be validated
     * @return boolean true if all data validations passed, False in case of failures
     */
    protected boolean validateOutOfStockNotification(OutOfStockNotification outOfStockNotification) {
        boolean validationStatus = true;

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setSource(new Gson().toJson(outOfStockNotification));

        List<ResultDetail> resultDetailsList = new ArrayList<>();

        if (StringUtils.isBlank(outOfStockNotification.getInvoiceNumber())) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVOICE_NUMBER_IS_BLANK"), null));
        }

        if (StringUtils.isBlank(outOfStockNotification.getMsdOrderNumber())) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_MSD_ORDER_NUMBER_IS_BLANK"), null));
        }

        if (StringUtils.isBlank(outOfStockNotification.getElmisOrderNumber())) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_ELMIS_ORDER_NUMBER_IS_BLANK"), null));
        }

        //Validating invoice date
        try {
            if (!DateValidatorUtils.isValidPastDate(outOfStockNotification.getInvoiceDate(), "dd-mm-yyyy")) {
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVOICE_DATE_IS_NOT_A_VALID_PAST_DATE"), null));
            }
        } catch (ParseException e) {
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVOICE_DATE_FORMAT"), tz.go.moh.him.mediator.core.utils.StringUtils.writeStackTraceToString(e)));
        }


        //Validating full filled items
        Map<String, List<ErrorMessage>> errorMessagesMap = new HashMap<>();

        List<ErrorMessage> fullFilledItemsErrors = validateFullFilledItemListRequired(outOfStockNotification.getFullFilledItems());
        if (!fullFilledItemsErrors.isEmpty())
            errorMessagesMap.put("fullFilledItems", fullFilledItemsErrors);

        List<ErrorMessage> stockOutItemsErrors = validateItemsListRequiredFields(outOfStockNotification.getStockOutItems());
        if (!stockOutItemsErrors.isEmpty())
            errorMessagesMap.put("stockOutItems", stockOutItemsErrors);

        List<ErrorMessage> inSufficientFundingItemsErrors = validateItemsListRequiredFields(outOfStockNotification.getInSufficientFundingItems());
        if (!inSufficientFundingItemsErrors.isEmpty())
            errorMessagesMap.put("inSufficientFundingItems", inSufficientFundingItemsErrors);

        List<ErrorMessage> rationingItemsErrors = validateItemsListRequiredFields(outOfStockNotification.getRationingItems());
        if (!rationingItemsErrors.isEmpty())
            errorMessagesMap.put("rationingItems", rationingItemsErrors);

        List<ErrorMessage> closeToExpireItemsErrors = validateItemsListRequiredFields(outOfStockNotification.getCloseToExpireItems());
        if (!closeToExpireItemsErrors.isEmpty())
            errorMessagesMap.put("closeToExpireItems", closeToExpireItemsErrors);

        List<ErrorMessage> phasedOutItemsItemsErrors = validateItemsListRequiredFields(outOfStockNotification.getPhasedOutItems());
        if (!phasedOutItemsItemsErrors.isEmpty())
            errorMessagesMap.put("phasedOutItems", phasedOutItemsItemsErrors);

        if (resultDetailsList.size() > 0) {
            validationStatus = false;
            errorMessage.setResultsDetails(resultDetailsList);
            errors.setErrorMessages(Arrays.asList(errorMessage));
        }

        if (errorMessagesMap.size() > 0) {
            validationStatus = false;
            errors.setItemsErrorMessages(errorMessagesMap);
        }

        return validationStatus;
    }

    /**
     * Method for sending data to ELMIS
     *
     * @param msg body to be sent
     */
    private void sendDataToElmis(String msg) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String path;
        String host;
        String scheme;
        int portNumber;

        if (config.getDynamicConfig().isEmpty()) {
            if (config.getProperty("elmis.secure").equals("true")) {
                scheme = "https";
            } else {
                scheme = "http";
            }

            host = config.getProperty("elmis.host");
            portNumber = Integer.parseInt(config.getProperty("elmis.api.port"));
            path = config.getProperty("elmis.api.out_of_stock_notification.path");
        } else {
            JSONObject connectionProperties = new JSONObject(config.getDynamicConfig()).getJSONObject("elmisConnectionProperties");

            if (!connectionProperties.getString("elmisUsername").isEmpty() && !connectionProperties.getString("elmisPassword").isEmpty()) {
                String auth = connectionProperties.getString("elmisUsername") + ":" + connectionProperties.getString("elmisPassword");
                byte[] encodedAuth = Base64.encodeBase64(
                        auth.getBytes(StandardCharsets.ISO_8859_1));
                String authHeader = "Basic " + new String(encodedAuth);
                headers.put(HttpHeaders.AUTHORIZATION, authHeader);
            }

            host = connectionProperties.getString("elmisHost");
            portNumber = connectionProperties.getInt("elmisPort");
            path = connectionProperties.getString("elmisOutOfStockNotificationPath");
            scheme = connectionProperties.getString("elmisScheme");
        }

        List<Pair<String, String>> params = new ArrayList<>();

        MediatorHTTPRequest forwardToElmisRequest = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Sending Out of Stock Notification to eLMIS", "POST", scheme,
                host, portNumber, path, msg, headers, params
        );

        ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
        httpConnector.tell(forwardToElmisRequest, getSelf());
    }
}
