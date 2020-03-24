package org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.jboss.jbossts.star.util.TxLinkNames;
import org.jboss.jbossts.star.util.TxMediaType;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.narayana.quickstart.rest.bridge.inbound.jpa.jaxrs.TaskResource;

/**
 *
 * @author rmartinc
 *
 * <ul>
 *     <li>cp $JBOSS_HOME/docs/examples/configs/standalone-rts.xml $JBOSS_HOME/standalone/configuration/</li>
 *     <li>cp $JBOSS_HOME_2/docs/examples/configs/standalone-rts.xml $JBOSS_HOME_2/standalone/configuration/</li>
 *     <li>$JBOSS_HOME/bin/standalone.sh -c standalone-rts.xml -Djboss.tx.node.id=server1 -Djboss.node.name=server1</li>
 *     <li>$JBOSS_HOME_2/bin/standalone.sh -c standalone-rts.xml -Djboss.tx.node.id=server2 -Djboss.node.name=server2 -Djboss.socket.binding.port-offset=10000</li>
 * </ul>
 */
public class SimpleTwoServers {

    private static final String DEPLOYMENT_NAME = "restat-bridge-jpa-5.10.5.Final-SNAPSHOT";

    private static final String BASE_URL1 = "http://localhost:8080/";
    private static final String BASE_URL2 = "http://localhost:18080/";

    private static final String TRANSACTION_MANAGER_URL = BASE_URL1 + "rest-at-coordinator/tx/transaction-manager";

    private static final String DEPLOYMENT_URL1 = BASE_URL1 + DEPLOYMENT_NAME;
    private static final String DEPLOYMENT_URL2 = BASE_URL2 + DEPLOYMENT_NAME;

    private static final String TASKS_URL1 = DEPLOYMENT_URL1 + "/" + TaskResource.TASKS_PATH_SEGMENT;
    private static final String TASKS_URL2 = DEPLOYMENT_URL2 + "/" + TaskResource.TASKS_PATH_SEGMENT;

    private static final String USERS_URL1 = DEPLOYMENT_URL1 + "/" + TaskResource.USERS_PATH_SEGMENT;
    private static final String USERS_URL2 = DEPLOYMENT_URL2 + "/" + TaskResource.USERS_PATH_SEGMENT;

    private static final String TEST_USERNAME1 = "gytis1";

    private static final String TEST_TASK_TITLE1 = "task1";

    private static final String TEST_TASK_TITLE2 = "task2";

    private TxSupport txSupport;

    public SimpleTwoServers() {
        txSupport = new TxSupport(TRANSACTION_MANAGER_URL);
    }

    public void execute() throws Exception {
        System.out.println("TaskResourceTest.testCommit()");

        checkTasks(TASKS_URL1, TEST_USERNAME1);
        checkTasks(TASKS_URL2, TEST_USERNAME1);

        System.out.println("Starting REST-AT transaction...");
        txSupport.startTx();

        createTask(TASKS_URL1, TEST_USERNAME1, TEST_TASK_TITLE1);
        createTask(TASKS_URL2, TEST_USERNAME1, TEST_TASK_TITLE1);

        System.out.println("Commiting REST-AT transaction...");
        txSupport.commitTx(); // .rollbackTx();

        checkTasks(TASKS_URL1, TEST_USERNAME1);
        checkTasks(TASKS_URL2, TEST_USERNAME1);

        delete();
    }

    private void delete() {
        ClientBuilder.newClient().target(TASKS_URL1).request().delete();
        ClientBuilder.newClient().target(USERS_URL1).request().delete();
        ClientBuilder.newClient().target(TASKS_URL2).request().delete();
        ClientBuilder.newClient().target(USERS_URL2).request().delete();
    }

    private void checkTasks(String url, String username) throws Exception  {
        JSONArray jsonArray = getUserTasks(url, username);
        System.out.println("tasks=" + jsonArray.length());
        if (jsonArray.length() > 0) {
            System.out.println("owner=" + jsonArray.getJSONObject(0).getString("owner"));
            System.out.println("title=" + jsonArray.getJSONObject(0).getString("title"));
        }
    }

    private Response createTask(final String url, final String userName, final String title) throws Exception {
        System.out.println("Creating task " + title + " for user " + userName);
        System.out.println("getDurableParticipantEnlistmentURI " + txSupport.getDurableParticipantEnlistmentURI());

        final Link participantEnlistmentLink = Link.fromUri(txSupport.getDurableParticipantEnlistmentURI())
                .title(TxLinkNames.PARTICIPANT).rel(TxLinkNames.PARTICIPANT).type(TxMediaType.PLAIN_MEDIA_TYPE).build();

        Client client = ClientBuilder.newClient();

        Response response = client.target(url + "/" + userName + "/" + title).request()
                .header("Link", participantEnlistmentLink).post(null);
        if (response.getStatus() != 201) {
            throw new Exception("Error status " + response.getStatus());
        }

        return response;
    }

    private JSONArray getUserTasks(final String url, final String userName) throws Exception {
        System.out.println("Getting all tasks of " + userName + "...");
        final String response = ClientBuilder.newClient().target(url + "/" + userName).request().get(String.class);
        final JSONArray jsonArray = new JSONArray(response);

        System.out.println("Received tasks:");
        System.out.println(jsonArray);

        return jsonArray;
    }

    public static void main(String... args) throws Exception {
        new SimpleTwoServers().execute();
    }
}
