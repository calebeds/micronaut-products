package me.calebeoliveira.admin.product;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import me.calebeoliveira.InMemoryStore;
import me.calebeoliveira.product.Product;

@Controller("/admin/products")
class AdminProductsController {

    private final InMemoryStore store;

    public AdminProductsController(InMemoryStore store) {
        this.store = store;
    }

    @Status(HttpStatus.CREATED)
    @Post(consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
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
}
