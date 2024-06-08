package mouride.dev.aws.lambda.apis;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mouride.dev.aws.lambda.dtos.Order;

/**
 * This class represents a Lambda function that retrieves all orders from a DynamoDB table.
 */
public class CreateOrderLambda {

    /**
     * The Amazon DynamoDB client used to interact with the DynamoDB service.
     */
    private final DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
    ;
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
    public APIGatewayProxyResponseEvent createOrder(final APIGatewayProxyRequestEvent requestEvent) {
        final Order order;
        try {
            order = objectMapper.readValue(requestEvent.getBody(), Order.class);
        } catch (final JsonProcessingException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(e.getMessage());
        }

        this.persistData(order);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(String.format("Order ID: %s", order.getId()));
    }

    /**
     * This method persists the given order data into the DynamoDB table.
     *
     * @param order The order object to be persisted.
     * @throws ConditionalCheckFailedException If a conditional check fails during the operation.
     */

    private void persistData(final Order order) throws ConditionalCheckFailedException {
        final Table table = this.dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        final Item item = new Item()
                .withPrimaryKey("id", order.getId())
                .withString("itemName", order.getItemName())
                .withInt("quantity", order.getQuantity());
        table.putItem(item);
    }

}
