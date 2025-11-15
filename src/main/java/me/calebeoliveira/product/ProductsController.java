package me.calebeoliveira.product;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import me.calebeoliveira.InMemoryStore;

import java.util.ArrayList;
import java.util.List;

@Controller("/products")
class ProductsController {

    private final InMemoryStore store;

    public ProductsController(InMemoryStore store) {
        this.store = store;
    }

    @Get
    public List<Product> listAllProducts() {
        return new ArrayList<>(store.getProducts().values());
    }
}
