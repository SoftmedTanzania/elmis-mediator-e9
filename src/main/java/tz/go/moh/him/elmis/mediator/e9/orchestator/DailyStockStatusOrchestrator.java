package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.elmis.mediator.e9.domain.DailyStockStatus;
import tz.go.moh.him.mediator.core.adapter.CsvAdapterUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.ERROR_INVALID_PAYLOAD;
import static tz.go.moh.him.elmis.mediator.e9.Constants.ErrorMessages.ERROR_REQUIRED_FIELDS_CHECK_FAILED;

public class DailyStockStatusOrchestrator extends UntypedActor {
    private final MediatorConfig config;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected String errorMessage = "";
    protected MediatorHTTPRequest originalRequest;

    public DailyStockStatusOrchestrator(MediatorConfig config) {
        this.config = config;
    }

    public static boolean validateRequiredFields(DailyStockStatus dailyStockStatus) {
        if (StringUtils.isBlank(dailyStockStatus.getPlant()))
            return false;
        if (StringUtils.isBlank(dailyStockStatus.getPartNum()))
            return false;
        if (StringUtils.isBlank(dailyStockStatus.getOum()))
            return false;
        if (StringUtils.isBlank(dailyStockStatus.getPartDescription()))
            return false;
        if (StringUtils.isBlank(dailyStockStatus.getOnHandQty()))
            return false;
        if (StringUtils.isBlank(dailyStockStatus.getDate()))
            return false;
        return !StringUtils.isBlank(dailyStockStatus.getMonthOfStock());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;

            List<DailyStockStatus> objects = convertMessageBodyToPojoList(((MediatorHTTPRequest) msg).getBody());
            log.info("Received payload in JSON = " + new Gson().toJson(objects));

            List<DailyStockStatus> validatedObjects = validateData(objects);
            parseMessage((originalRequest).getHeaders().get("x-openhim-transactionid"), validatedObjects);
            sendDataToElmis(new Gson().toJson(validatedObjects));
        } else if (msg instanceof MediatorHTTPResponse) { //respond
            log.info("Received response from eLMIS");
            finalizeResponse((MediatorHTTPResponse) msg);
        } else {
            unhandled(msg);
        }
    }

    protected void parseMessage(String openHimTransactionId, List<DailyStockStatus> receivedList) throws IOException, XmlPullParserException {
        //update message to send to eLMIS
        for (DailyStockStatus dailyStockStatus : receivedList) {
            dailyStockStatus.setIlTransactionId(openHimTransactionId);
        }
    }

    protected List<DailyStockStatus> validateData(List<DailyStockStatus> receivedList) {
        List<DailyStockStatus> validReceivedList = new ArrayList<>();

        if (receivedList == null || receivedList.size() == 0) {
            errorMessage += ERROR_INVALID_PAYLOAD;
            return validReceivedList;
        }

        for (DailyStockStatus dailyStockStatus : receivedList) {

            if (dailyStockStatus == null) {
                errorMessage += ERROR_INVALID_PAYLOAD;
                continue;
            }

            if (!validateRequiredFields(dailyStockStatus)) {
                errorMessage += dailyStockStatus.getPartNum() + ERROR_REQUIRED_FIELDS_CHECK_FAILED;
                continue;
            }

            //TODO implement additional data validations checks
            validReceivedList.add(dailyStockStatus);
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
        if (!errorMessage.isEmpty()) {
            String errorMessageTobeSent = "";
            if (errorMessage.equals(ERROR_INVALID_PAYLOAD)) {
                errorMessageTobeSent = ERROR_INVALID_PAYLOAD;
            } else {
                errorMessageTobeSent = "Failed to process the following entries with PartNum: " + errorMessage;
            }
            FinishRequest finishRequest = new FinishRequest(errorMessageTobeSent, "text/plain", HttpStatus.SC_BAD_REQUEST);
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
