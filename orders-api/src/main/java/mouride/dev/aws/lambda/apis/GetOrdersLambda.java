package mouride.dev.aws.lambda.apis;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mouride.dev.aws.lambda.dtos.Order;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a Lambda function that retrieves all orders from a DynamoDB table.
 */
public class GetOrdersLambda {

    /**
     * The Amazon DynamoDB client used to interact with the DynamoDB service.
     */
    private final AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient();

    /**
     * The ObjectMapper used to convert Java objects to JSON and vice versa.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This method is the entry point for the Lambda function.
     * It retrieves all orders from the DynamoDB table and returns them as a JSON response.
     *
     * @param requestEvent The API Gateway proxy request event.
     * @return The API Gateway proxy response event with the retrieved orders as the body.
     */
    public APIGatewayProxyResponseEvent getOrders(final APIGatewayProxyRequestEvent requestEvent) {
        final String jsonOutput;
        final List<Order> orders = getOrdersTable();
        try {
            jsonOutput = this.objectMapper.writeValueAsString(orders);
        } catch (final JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(e.getMessage());
        }
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(jsonOutput);
    }

    /**
     * This method retrieves all orders from the DynamoDB table.
     *
     * @return A list of Order objects representing the retrieved orders.
     */

    private List<Order> getOrdersTable() {
        final ScanResult scanResult = this.amazonDynamoDB.scan(new ScanRequest().withTableName(System.getenv("ORDERS_TABLE")));
        return scanResult.getItems()
                .stream()
                .map(item -> new Order(Integer.parseInt(item.get("id").getN()),
                        item.get("itemName").getS(),
                        Integer.parseInt(item.get("quantity").getN())))
                .collect(Collectors.toList());

    }
}
