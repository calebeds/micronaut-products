package me.calebeoliveira.product;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
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

    @Get("{id}")
    public Product getProduct(@PathVariable Integer id) {
        return store.getProducts().get(id);
    }
}
