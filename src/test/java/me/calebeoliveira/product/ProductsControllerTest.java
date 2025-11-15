package me.calebeoliveira.product;

import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@MicronautTest
class ProductsControllerTest {
    private static final Logger LOG = LoggerFactory.getLogger(ProductsControllerTest.class);

    @Inject
    @Client("/products")
    private HttpClient client;

    @Inject
    private JsonMapper jsonMapper;

    @Test
    void shouldReturnTenProducts_whenCallingGetOnProductsEndpoint() throws IOException {
        JsonNode response = client.toBlocking().retrieve("/", JsonNode.class);
        LOG.debug("Retrieved products: {}" + logProducts(response));
        assertEquals(10, response.size());
    }

    @Test
    void shouldFetchProduct_whenCallingGetOnProductsWithId() {
        Product response = client.toBlocking().retrieve("/0", Product.class);
        assertEquals(0, response.id());
        assertEquals(Product.Type.COFFEE, response.type());
        assertNotNull(response.name());
    }

    private String logProducts(JsonNode response) throws IOException {
        return jsonMapper.writeValueAsString(response);
    }
}