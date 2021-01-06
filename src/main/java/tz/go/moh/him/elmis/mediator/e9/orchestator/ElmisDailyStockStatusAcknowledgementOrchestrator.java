package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;
import tz.go.moh.him.elmis.mediator.e9.domain.ElmisAck;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElmisDailyStockStatusAcknowledgementOrchestrator extends UntypedActor {
    private final MediatorConfig config;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected MediatorHTTPRequest originalRequest;
    protected ElmisAck elmisAck;

    public ElmisDailyStockStatusAcknowledgementOrchestrator(MediatorConfig config) {
        this.config = config;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof MediatorHTTPRequest) {
            originalRequest = (MediatorHTTPRequest) msg;
            elmisAck = new Gson().fromJson(((MediatorHTTPRequest) msg).getBody(), ElmisAck.class);
            obtainOpenHIMTransactionByTransactionId(elmisAck.getiLTransactionIDNumber());
        } else if (msg instanceof MediatorHTTPResponse) {
            log.info("Received feedback from core");
            log.info("Core Response code = " + ((MediatorHTTPResponse) msg).getStatusCode());
            log.info("Core Response body = " + ((MediatorHTTPResponse) msg).getBody());
            updateOpenHIMTransactionByTransactionId(new JSONObject(((MediatorHTTPResponse) msg).getBody()));

            FinishRequest finishRequest = new FinishRequest("", "text/plain", HttpStatus.SC_OK);
            (originalRequest).getRequestHandler().tell(finishRequest, getSelf());
        }
    }

    private void obtainOpenHIMTransactionByTransactionId(String transactionId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String scheme = "https";
        List<Pair<String, String>> params = new ArrayList<>();

        MediatorHTTPRequest obtainOpenHIMTransactionRequest = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Obtaining OpenHIM Transaction by transactionId", "GET", scheme,
                config.getProperty("core.host"), Integer.parseInt(config.getProperty("core.api.port")), "/transactions/" + transactionId,
                null, headers, params
        );

        ActorSelection coreApiConnector = getContext().actorSelection(config.userPathFor("core-api-connector"));
        coreApiConnector.tell(obtainOpenHIMTransactionRequest, getSelf());
    }


    private void updateOpenHIMTransactionByTransactionId(JSONObject transaction) {
        log.info("Updating OpenHIM Transaction with ELMIS ACK");
        if (elmisAck.getStatus().equals("Success")) {
            transaction.getJSONObject("response").put("status", HttpStatus.SC_OK);
            transaction.put("status", "Successful");
        } else {
            transaction.getJSONObject("response").put("status", HttpStatus.SC_BAD_REQUEST);
            transaction.put("status", "Failed");
        }
        transaction.getJSONObject("response").put("body", new Gson().toJson(elmisAck));
        transaction.getJSONObject("response").put("timestamp", new Timestamp(System.currentTimeMillis()));


        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        String scheme = "https";
        List<Pair<String, String>> params = new ArrayList<>();

        MediatorHTTPRequest obtainOpenHIMTransactionRequest = new MediatorHTTPRequest(
                (originalRequest).getRequestHandler(), getSelf(), "Updating OpenHIM Transaction by transactionId", "PUT", scheme,
                config.getProperty("core.host"), Integer.parseInt(config.getProperty("core.api.port")), "/transactions/" + elmisAck.getiLTransactionIDNumber(),
                transaction.toString(), headers, params
        );

        ActorSelection coreApiConnector = getContext().actorSelection(config.userPathFor("core-api-connector"));
        coreApiConnector.tell(obtainOpenHIMTransactionRequest, getSelf());
    }

}
