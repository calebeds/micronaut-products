package me.calebeoliveira.product;

public class ProductTypeNotFoundException extends RuntimeException {
    public ProductTypeNotFoundException(Throwable cause) {
        super(cause);
    }
}
