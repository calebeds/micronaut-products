package me.calebeoliveira.admin.product;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import me.calebeoliveira.InMemoryStore;
import me.calebeoliveira.product.Product;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@MicronautTest
class AdminProductsControllerTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdminProductsControllerTest.class);
    public static final String PRODUCT_URL = "/admin/products";

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    InMemoryStore store;

    @Test
    void shouldReturnUnauthorized_whenCredentialsNotValid() {
        var productToAdd = new Product(1234, "my-test-product", Product.Type.OTHER);

        var response = assertThrows(HttpClientResponseException.class, () -> {
            httpClient.toBlocking().exchange(
                    HttpRequest.POST(PRODUCT_URL, productToAdd),
                    Product.class
            );
        });

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
    }

    @Test
    void shouldAddNewProduct_whenAdminEndpointIsCalled() {
        final Optional<BearerAccessRefreshToken> token = givenMyUserIsLoggedIn();

        var productToAdd = new Product(1234, "my-test-product", Product.Type.OTHER);
        store.getProducts().remove(productToAdd.id());
        assertNull(store.getProducts().get(productToAdd.id()));

        var response = httpClient.toBlocking().exchange(
                HttpRequest.POST(PRODUCT_URL, productToAdd)
                        .accept(MediaType.APPLICATION_JSON)
                        .bearerAuth(token.get().getAccessToken()),
                Product.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertTrue(response.getBody().isPresent());
        assertEquals(productToAdd.id(), response.getBody().get().id());
        assertEquals(productToAdd.name(), response.getBody().get().name());
    }



    @Test
    void shouldResultInConflict_whenAddProductTwice() {
        final Optional<BearerAccessRefreshToken> token = givenMyUserIsLoggedIn();

        var productToAdd = new Product(1234, "my-test-product", Product.Type.OTHER);

        store.getProducts().remove(productToAdd.id());
        assertNull(store.getProducts().get(productToAdd.id()));

        var response = httpClient.toBlocking().exchange(
                HttpRequest.POST(PRODUCT_URL, productToAdd)
                        .accept(MediaType.APPLICATION_JSON)
                        .bearerAuth(token.get().getAccessToken()),
                Product.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());

        var expectedConflict = assertThrows(HttpClientResponseException.class,
                () -> httpClient.toBlocking().exchange(HttpRequest.POST(PRODUCT_URL, productToAdd)
                        .accept(MediaType.APPLICATION_JSON)
                        .bearerAuth(token.get().getAccessToken())));

        assertEquals(HttpStatus.CONFLICT, expectedConflict.getStatus());
    }

    @Test
    void shouldUpdateProduct_whenCallingPutAdminProductEndpoint() {
        final Optional<BearerAccessRefreshToken> token = givenMyUserIsLoggedIn();

        Product productToUpdate = new Product(999, "old-value", Product.Type.OTHER);

        store.getProducts().put(productToUpdate.id(), productToUpdate);
        assertEquals(productToUpdate, store.getProducts().get(productToUpdate.id()));

        UpdateProductRequest updateRequest = new UpdateProductRequest("new-value", Product.Type.TEA);

        var response = httpClient.toBlocking().exchange(
                HttpRequest.PUT(PRODUCT_URL + "/" + productToUpdate.id(), updateRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .bearerAuth(token.get().getAccessToken()),
                Product.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        var productFromStore = store.getProducts().get(productToUpdate.id());
        assertEquals(updateRequest.name(), productFromStore.name());
        assertEquals(updateRequest.type(), productFromStore.type());
    }

    @Test
    void shouldAddNewProduct_whenNonExistentProductIsPassedInPutAdminProductEndpoint() {
        final Optional<BearerAccessRefreshToken> token = givenMyUserIsLoggedIn();

        final int productId = 999;

        store.getProducts().remove(productId);
        assertNull(store.getProducts().get(productId));

        UpdateProductRequest updateRequest = new UpdateProductRequest("new-value", Product.Type.TEA);

        var response = httpClient.toBlocking().exchange(
                HttpRequest.PUT(PRODUCT_URL + "/" + productId, updateRequest)
                        .accept(MediaType.APPLICATION_JSON)
                        .bearerAuth(token.get().getAccessToken()),
                Product.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        var productFromStore = store.getProducts().get(productId);
        assertEquals(productId, productFromStore.id());
        assertEquals(updateRequest.name(), productFromStore.name());
        assertEquals(updateRequest.type(), productFromStore.type());
    }

    @Test
    void shouldDeleteProduct_whenTheDeleteEndpointIsCalledWithId() {
        final Optional<BearerAccessRefreshToken> token = givenMyUserIsLoggedIn();

        Product productToDelete = new Product(987, "delete-me", Product.Type.OTHER);
        store.addProduct(productToDelete);
        assertTrue(store.getProducts().containsKey(productToDelete.id()));
        assertTrue(store.getProducts().containsValue(productToDelete));

        final HttpResponse<Product> response = httpClient.toBlocking().exchange(
                HttpRequest.DELETE(PRODUCT_URL + "/" + productToDelete.id())
                        .accept(MediaType.APPLICATION_JSON)
                        .bearerAuth(token.get().getAccessToken()),
                Argument.of(Product.class)
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.getBody().isPresent());
        assertEquals(productToDelete.id(), response.getBody().get().id());
        assertEquals(productToDelete.name(), response.getBody().get().name());
        assertEquals(productToDelete.type(), response.getBody().get().type());
    }

    @Test
    void shouldReturnNotFound_whenTheDeleteEndpointIsCalledWithIdNonExistent() {
       final Optional<BearerAccessRefreshToken> token = givenMyUserIsLoggedIn();

       final var productId = 987;
       store.removeProductById(productId);
       assertNull(store.getProducts().get(productId));

       var response = assertThrows(HttpClientResponseException.class,
               () -> httpClient.toBlocking().exchange(
                       HttpRequest.DELETE(PRODUCT_URL + "/" + productId)
                               .accept(MediaType.APPLICATION_JSON)
                               .bearerAuth(token.get().getAccessToken())
               ));
       assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    private Optional<BearerAccessRefreshToken> givenMyUserIsLoggedIn() {
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("my-user", "secret");
        var login = HttpRequest.POST("/login", credentials);
        var responseToken = httpClient.toBlocking().exchange(login, BearerAccessRefreshToken.class);

        assertEquals(HttpStatus.OK, responseToken.getStatus());
        final Optional<BearerAccessRefreshToken> token = responseToken.getBody();
        assertTrue(token.isPresent());
        assertEquals("my-user", token.get().getUsername());
        LOG.debug("Login Bearer Token: {} expires in {}", token.get().getAccessToken(), token.get().getExpiresIn());
        return token;
    }

}