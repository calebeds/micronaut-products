package me.calebeoliveira.product;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import me.calebeoliveira.InMemoryStore;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Get("/filter{?max,offset}")
    public List<Product> filteredProducts(@QueryValue Optional<Integer> max,
                                          @QueryValue Optional<Integer> offset) {
        return store.getProducts()
                .values()
                .stream()
                .skip(offset.orElse(0))
                .limit(max.orElse(0))
                .toList();
    }

}
