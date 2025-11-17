package me.calebeoliveira.admin.product;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import me.calebeoliveira.InMemoryStore;
import me.calebeoliveira.product.Product;

@Controller("/admin/products")
class AdminProductsController {

    private final InMemoryStore store;

    public AdminProductsController(InMemoryStore store) {
        this.store = store;
    }

    @Post(consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public Product addNewProduct(@Body Product product) {
        return store.addProduct(product);
    }
}
