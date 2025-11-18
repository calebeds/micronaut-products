package me.calebeoliveira.admin.product;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.calebeoliveira.InMemoryStore;
import me.calebeoliveira.product.Product;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "admin")
@Controller("/admin/products")
class AdminProductsController {

    private final InMemoryStore store;

    public AdminProductsController(InMemoryStore store) {
        this.store = store;
    }

    @Status(HttpStatus.CREATED)
    @Post(consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Product",
            description = "Accepts a new Product in the request body and persists it in the InMemoryStore.")
    @ApiResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Product.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Product does already exist. ID has to be unique!"
    )
    public Product addNewProduct(@Body Product product) {
        if(store.getProducts().containsKey(product.id())) {
            throw new HttpStatusException(HttpStatus.CONFLICT,
                    "Product with id " + product.id() + " already exists");
        }
        return store.addProduct(product);
    }

    @Put("{id}")
    public Product updateProduct(@PathVariable Integer id, @Body UpdateProductRequest request) {
        Product updatedProduct = new Product(id, request.name(), request.type());
        return store.addProduct(updatedProduct);
    }

    @Delete("{id}")
    public Product deleteProduct(@PathVariable Integer id) {
        return store.removeProductById(id);
    }
}
