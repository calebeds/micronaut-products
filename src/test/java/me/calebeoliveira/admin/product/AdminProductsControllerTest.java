package me.calebeoliveira.admin.product;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import me.calebeoliveira.InMemoryStore;
import me.calebeoliveira.product.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@MicronautTest
class AdminProductsControllerTest {
    @Inject
    @Client("/admin/products")
    HttpClient httpClient;

    @Inject
    InMemoryStore store;

    @Test
    void shouldAddNewProduct_whenAdminEndpointIsCalled() {
        var productToAdd = new Product(1234, "my-test-product", Product.Type.OTHER);

        store.getProducts().remove(productToAdd.id());
        assertNull(store.getProducts().get(productToAdd.id()));

        var response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/", productToAdd),
                Product.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertTrue(response.getBody().isPresent());
        assertEquals(productToAdd.id(), response.getBody().get().id());
        assertEquals(productToAdd.name(), response.getBody().get().name());
    }

    @Test
    void shouldResultInConflict_whenAddProductTwice() {
        var productToAdd = new Product(1234, "my-test-product", Product.Type.OTHER);

        store.getProducts().remove(productToAdd.id());
        assertNull(store.getProducts().get(productToAdd.id()));

        var response = httpClient.toBlocking().exchange(
                HttpRequest.POST("/", productToAdd),
                Product.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());

        var expectedConflict = assertThrows(HttpClientResponseException.class,
                () -> httpClient.toBlocking().exchange(HttpRequest.POST("/", productToAdd)));

        assertEquals(HttpStatus.CONFLICT, expectedConflict.getStatus());
    }

    @Test
    void shouldUpdateProduct_whenCallingPutAdminProductEndpoint() {
        Product productToUpdate = new Product(999, "old-value", Product.Type.OTHER);

        store.getProducts().put(productToUpdate.id(), productToUpdate);
        assertEquals(productToUpdate, store.getProducts().get(productToUpdate.id()));

        UpdateProductRequest updateRequest = new UpdateProductRequest("new-value", Product.Type.TEA);

        var response = httpClient.toBlocking().exchange(
                HttpRequest.PUT("/" + productToUpdate.id(), updateRequest),
                Product.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        var productFromStore = store.getProducts().get(productToUpdate.id());
        assertEquals(updateRequest.name(), productFromStore.name());
        assertEquals(updateRequest.type(), productFromStore.type());
    }

    @Test
    void shouldAddNewProduct_whenNonExistentProductIsPassedInPutAdminProductEndpoint() {
        final int productId = 999;

        store.getProducts().remove(productId);
        assertNull(store.getProducts().get(productId));

        UpdateProductRequest updateRequest = new UpdateProductRequest("new-value", Product.Type.TEA);

        var response = httpClient.toBlocking().exchange(
                HttpRequest.PUT("/" + productId, updateRequest),
                Product.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        var productFromStore = store.getProducts().get(productId);
        assertEquals(productId, productFromStore.id());
        assertEquals(updateRequest.name(), productFromStore.name());
        assertEquals(updateRequest.type(), productFromStore.type());
    }
}