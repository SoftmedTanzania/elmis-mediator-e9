package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.elmis.mediator.e9.domain.DailyStockStatus;
import tz.go.moh.him.mediator.core.adapter.CsvAdapterUtils;
import tz.go.moh.him.mediator.core.domain.ErrorMessage;
import tz.go.moh.him.mediator.core.domain.ResultDetail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyStockStatusOrchestrator extends UntypedActor {
    private final MediatorConfig config;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected MediatorHTTPRequest originalRequest;
    protected JSONObject errorMessageResource;
    protected List<ErrorMessage> errorMessages = new ArrayList<>();

    public DailyStockStatusOrchestrator(MediatorConfig config) {
        this.config = config;
        InputStream stream = getClass().getClassLoader().getResourceAsStream("error-messages.json");
        try {
            if (stream != null) {
                errorMessageResource = new JSONObject(IOUtils.toString(stream)).getJSONObject("DAILY_STOCK_STATUS_ERROR_MESSAGES");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate Daily Stock Status Required Fields
     *
     * @param dailyStockStatus to be validated
     * @return array list of validation results details incase of failed validations
     */
    public List<ResultDetail> validateRequiredFields(DailyStockStatus dailyStockStatus) {
        List<ResultDetail> resultDetailsList = new ArrayList<>();
        if (StringUtils.isBlank(dailyStockStatus.getPlant()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_PLANT_IS_BLANK"), null));

        if (StringUtils.isBlank(dailyStockStatus.getPartNum()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_PART_NUM_IS_BLANK"), dailyStockStatus.getPlant()), null));

        if (StringUtils.isBlank(dailyStockStatus.getOum()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_OUM_IS_BLANK"), dailyStockStatus.getPlant()), null));

        if (StringUtils.isBlank(dailyStockStatus.getPartDescription()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_PART_DESCRIPTION_IS_BLANK"), dailyStockStatus.getPlant()), null));

        if (StringUtils.isBlank(dailyStockStatus.getOnHandQty()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_ON_HAND_QTY_IS_BLANK"), dailyStockStatus.getPlant()), null));

        if (StringUtils.isBlank(dailyStockStatus.getDate()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_DATE_IS_BLANK"), dailyStockStatus.getPlant()), null));

        if (StringUtils.isBlank(dailyStockStatus.getMonthOfStock()))
            resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, String.format(errorMessageResource.getString("ERROR_MONTH_OF_STOCK_IS_BLANK"), dailyStockStatus.getPlant()), null));

        return resultDetailsList;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            //Converting the received request body to POJO List
            List<DailyStockStatus> dailyStockStatusList = new ArrayList<>();
            try {
                dailyStockStatusList = convertMessageBodyToPojoList(((MediatorHTTPRequest) msg).getBody());
            } catch (Exception e) {
                //In-case of an exception creating an error message with the stack trace
                ErrorMessage errorMessage = new ErrorMessage(
                        originalRequest.getBody(),
                        Arrays.asList(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, e.getMessage(), tz.go.moh.him.mediator.core.utils.StringUtils.writeStackTraceToString(e)))
                );
                errorMessages.add(errorMessage);
            }

            log.info("Received payload in JSON = " + new Gson().toJson(dailyStockStatusList));

            List<DailyStockStatus> validatedObjects;
            if (dailyStockStatusList.isEmpty()) {
                ErrorMessage errorMessage = new ErrorMessage(
                        originalRequest.getBody(),
                        Arrays.asList(
                                new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVALID_PAYLOAD"), null)
                        )
                );
                errorMessages.add(errorMessage);
                validatedObjects = new ArrayList<>();
            } else {
                validatedObjects = validateData(dailyStockStatusList);
            }

            updatePayloadListWithOpenHimTransactionId((originalRequest).getHeaders().get("x-openhim-transactionid"), validatedObjects);
            sendDataToElmis(new Gson().toJson(validatedObjects));
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from eLMIS");
            finalizeResponse((MediatorHTTPResponse) msg);
        } else {
            unhandled(msg);
        }
    }

    protected void updatePayloadListWithOpenHimTransactionId(String openHimTransactionId, List<DailyStockStatus> receivedList) {
        //update message to send to eLMIS
        for (DailyStockStatus dailyStockStatus : receivedList) {
            dailyStockStatus.setIlTransactionId(openHimTransactionId);
        }
    }

    protected List<DailyStockStatus> validateData(List<DailyStockStatus> receivedList) {
        List<DailyStockStatus> validReceivedList = new ArrayList<>();

        for (DailyStockStatus dailyStockStatus : receivedList) {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setSource(new Gson().toJson(dailyStockStatus));

            List<ResultDetail> resultDetailsList = new ArrayList<>();

            if (dailyStockStatus == null) {
                resultDetailsList.add(new ResultDetail(ResultDetail.ResultsDetailsType.ERROR, errorMessageResource.getString("ERROR_INVALID_PAYLOAD"), null));
            } else {
                resultDetailsList.addAll(validateRequiredFields(dailyStockStatus));
            }

            //TODO implement additional data validations checks
            if (resultDetailsList.size() == 0) {
                //No errors were found during data validation
                //adding the service received to the valid payload to be sent to HDR
                validReceivedList.add(dailyStockStatus);
            } else {
                //Adding the validation results to the Error message object
                errorMessage.setResultsDetails(resultDetailsList);
                errorMessages.add(errorMessage);
            }
        }
        return validReceivedList;
    }

    protected List<DailyStockStatus> convertMessageBodyToPojoList(String msg) throws IOException {
        List<DailyStockStatus> dailyStockStatusList;
        try {
            Type listType = new TypeToken<List<DailyStockStatus>>() {
            }.getType();
            dailyStockStatusList = new Gson().fromJson((originalRequest).getBody(), listType);
        } catch (com.google.gson.JsonSyntaxException ex) {
            dailyStockStatusList = (List<DailyStockStatus>) CsvAdapterUtils.csvToArrayList(msg, DailyStockStatus.class);
        }
        return dailyStockStatusList;
    }

    private void sendDataToElmis(String msg) throws IOException, XmlPullParserException {
        if (!errorMessages.isEmpty()) {
            FinishRequest finishRequest = new FinishRequest(new Gson().toJson(errorMessages), "text/json", HttpStatus.SC_BAD_REQUEST);
            (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
        } else {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            String scheme;
            if (config.getProperty("elmis.secure").equals("true")) {
                scheme = "https";
            } else {
                scheme = "http";
            }

            List<Pair<String, String>> params = new ArrayList<>();

            MediatorHTTPRequest forwardToElmisRequest = new MediatorHTTPRequest(
                    (originalRequest).getRequestHandler(), getSelf(), "Sending Diaily Stock Status to eLMIS", "POST", scheme,
                    config.getProperty("elmis.host"), Integer.parseInt(config.getProperty("elmis.api.port")), config.getProperty("elmis.api.daily_stock_status.path"),
                    msg, headers, params
            );

            ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
            httpConnector.tell(forwardToElmisRequest, getSelf());
        }
    }

    private void finalizeResponse(MediatorHTTPResponse response) {
        (originalRequest).getRequestHandler().tell(response.toFinishRequest(), getSelf());
    }
}
