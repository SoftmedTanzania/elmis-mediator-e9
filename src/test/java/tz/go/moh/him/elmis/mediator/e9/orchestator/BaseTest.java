package tz.go.moh.him.elmis.mediator.e9.orchestator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.testing.TestingUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseTest {

    protected static ActorSystem system;
    protected MediatorConfig testConfig;
    protected JSONObject errorMessageResource;

    @Before
    public void before() throws Exception {
        system = ActorSystem.create();

        testConfig = new MediatorConfig();
        testConfig.setName("elmis-mediator-emr-tests");
        testConfig.setProperties("mediator-unit-test.properties");

        InputStream stream = getClass().getClassLoader().getResourceAsStream("error-messages.json");
        if (stream != null) {
            errorMessageResource = new JSONObject(IOUtils.toString(stream));
        }
    }

    @After
    public void after() {
        TestingUtils.clearRootContext(system, testConfig.getName());
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    public void createActorAndSendRequest(ActorSystem system, MediatorConfig testConfig, ActorRef sender, String csvPayload, Class<?> type, String path) {
        final ActorRef orchestratorActor = system.actorOf(Props.create(type, testConfig));
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");
        headers.put("x-openhim-clientid", "csv-sync-service");
        MediatorHTTPRequest POST_Request = new MediatorHTTPRequest(
                sender,
                sender,
                "unit-test",
                "POST",
                "http",
                null,
                null,
                path,
                csvPayload,
                headers,
                Collections.<Pair<String, String>>emptyList()
        );

        orchestratorActor.tell(POST_Request, sender);
    }
}
