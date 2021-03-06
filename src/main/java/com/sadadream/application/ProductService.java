package com.sadadream.application;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.github.dozermapper.core.Mapper;
import com.sadadream.domain.Product;
import com.sadadream.domain.ProductRepository;
import com.sadadream.domain.User;
import com.sadadream.domain.UserRepository;
import com.sadadream.dto.ProductData;
import com.sadadream.errors.ProductNotFoundException;
import com.sadadream.errors.UserNotFoundException;

@Service
@Transactional
public class ProductService {
    private final Mapper mapper;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    public ProductService(
            Mapper dozerMapper,
            ProductRepository productRepository,
             UserRepository userRepository
    ) {
        this.mapper = dozerMapper;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return findProduct(id);
    }

    public Product createProduct(ProductData productData, Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Product product = mapper.map(productData, Product.class);
        product.setUser(user);

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductData productData) {
        Product product = findProduct(id);

        product.changeWith(mapper.map(productData, Product.class));

        return product;
    }

    public Product deleteProduct(Long id) {
        Product product = findProduct(id);

        productRepository.delete(product);

        return product;
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
