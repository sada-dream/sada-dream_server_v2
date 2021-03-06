package com.sadadream.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.sadadream.application.AuthenticationService;
import com.sadadream.application.ProductService;
import com.sadadream.domain.Product;
import com.sadadream.domain.Role;
import com.sadadream.dto.ProductData;
import com.sadadream.errors.InvalidTokenException;
import com.sadadream.errors.ProductNotFoundException;

@WebMvcTest(ProductController.class)
@MockBean(JpaMetamodelMappingContext.class)
class ProductControllerTest {
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaDk";
    private static final String INVALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJ1c2VySWQiOjF9.ZZ3CUl0jxeLGvQ1Js5nG2Ty5qGTlqai5ubDMXZOdaD0";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {

        Product product = Product.builder()
                .id(1L)
                .brand("????????????")
                .category("??????")
                .description("???????????????.")
                .currency("KRW")
                .name("????????????")
                .price("50000")
                .build();

        given(productService.getProducts()).willReturn(List.of(product));

        given(productService.getProduct(1L)).willReturn(product);

        given(productService.getProduct(1000L))
                .willThrow(new ProductNotFoundException(1000L));

        given(productService.createProduct(any(ProductData.class), any(Long.class)))
                .willReturn(product);

        given(productService.updateProduct(eq(1L), any(ProductData.class)))
                .will(invocation -> {
                    Long id = invocation.getArgument(0);
                    ProductData productData = invocation.getArgument(1);
                    return Product.builder()
                            .id(id)
                            .name(productData.getName())
                            .brand(productData.getBrand())
                            .price(productData.getPrice())
                            .currency(productData.getCurrency())
                            .imageLink(productData.getImageLink())
                            .description(productData.getDescription())
                            .category(productData.getCategory())
                            .build();
                });

        given(productService.updateProduct(eq(1000L), any(ProductData.class)))
                .willThrow(new ProductNotFoundException(1000L));

        given(productService.deleteProduct(1000L))
                .willThrow(new ProductNotFoundException(1000L));

        given(authenticationService.parseToken(VALID_TOKEN)).willReturn(1L);

        given(authenticationService.parseToken(INVALID_TOKEN))
                .willThrow(new InvalidTokenException(INVALID_TOKEN));

        given(authenticationService.roles(1L))
            .willReturn(Arrays.asList(new Role("USER")));
    }

    @DisplayName("?????? ???????????? ??????????????? ??? ??????????????? ????????? ???????????????.")
    @Test
    void list() throws Exception {
        mockMvc.perform(
            get("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                containsString("\"brand\":\"????????????\"")
            ))
            .andExpect(content().string(
                containsString("\"name\":\"????????????\"")
            ))
            .andExpect(content().string(
                containsString("\"currency\":\"KRW\"")
            ));
        verify(productService).getProducts();

    }

    @DisplayName("???????????? ????????? ??????????????? ?????? ??????????????? ????????? ???????????????.")
    @Test
    void detailWithExistedProduct() throws Exception {
        mockMvc.perform(
            get("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                containsString("\"brand\":\"????????????\"")
            ))
            .andExpect(content().string(
                containsString("\"name\":\"????????????\"")
            ))
            .andExpect(content().string(
                containsString("\"currency\":\"KRW\"")
            ));

        verify(productService).getProduct(any(Long.class));
    }

    @DisplayName("???????????? ?????? ????????? ???????????? ???????????? ????????? ????????????.")
    @Test
    void detailWithNotExistedProduct() throws Exception {
        mockMvc.perform(get("/products/1000"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("????????? ?????? ??? ???????????? ?????? ????????? ????????????, ??????????????? ????????????.")
    @Test
    void createWithValidAttributes() throws Exception {
        mockMvc.perform(
            post("/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isCreated())
            .andExpect(content().string(
                containsString("\"brand\":\"????????????\"")
            ))
            .andExpect(content().string(
                containsString("\"name\":\"????????????\"")
            ))
            .andExpect(content().string(
                containsString("\"currency\":\"KRW\"")
            ));

        verify(productService).createProduct(any(ProductData.class), any(Long.class));
    }

    @DisplayName("???????????? ?????? ???????????? ?????? ????????? ????????????, ????????? ????????? ????????????.")
    @Test
    void createWithInvalidAttributes() throws Exception {
        mockMvc.perform(
            post("/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"\",\n"
                    + "  \"category\": \"\",\n"
                    + "  \"currency\": \"\",\n"
                    + "  \"description\": \"\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"\"\n"
                    + "  ],\n"
                    + "  \"name\": \"\",\n"
                    + "  \"price\": \"\"\n"
                    + "}")
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isBadRequest());
    }

    @DisplayName("????????? ?????? ?????? ?????? ????????? ????????????, ???????????? ?????????.")
    @Test
    void createWithoutAccessToken() throws Exception {
        mockMvc.perform(
            post("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
        )
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("???????????? ?????? ????????? ????????????, ?????? ?????? ????????? ?????? ????????????.")
    @Test
    void createWithWrongAccessToken() throws Exception {
        mockMvc.perform(
            post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
        )
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("???????????? ????????? ????????? ???????????? ????????? ?????? ??????????????? ????????????.")
    @Test
    void updateWithExistedProduct() throws Exception {
        mockMvc.perform(
            patch("/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isOk())
            .andExpect(content().string(
                containsString("\"brand\":\"????????????\"")
            ));

        verify(productService).updateProduct(eq(1L), any(ProductData.class));
    }

    @DisplayName("???????????? ?????? ???????????? ??????????????? ?????? ???????????? ????????? ????????????.")
    @Test
    void updateWithNotExistedProduct() throws Exception {
        mockMvc.perform(
            patch("/products/1000")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isNotFound());

        verify(productService).updateProduct(eq(1000L), any(ProductData.class));
    }

    @DisplayName("???????????? ?????? ???????????? ?????? ????????? ???????????? ????????? ????????? ????????????.")
    @Test
    void updateWithInvalidAttributes() throws Exception {
        mockMvc.perform(
            patch("/products/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"\",\n"
                    + "  \"category\": \"\",\n"
                    + "  \"currency\": \"\",\n"
                    + "  \"description\": \"\",\n"
                    + "  \"name\": \"\",\n"
                    + "  \"price\": \"\"\n"
                    + "}")
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isBadRequest());
    }

    @DisplayName("????????? ?????? ?????? ?????? ????????? ???????????? ???????????? ?????????. (401)")
    @Test
    void updateWithoutAccessToken() throws Exception {
        mockMvc.perform(
            patch("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
        )
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("???????????? ?????? ????????? ???????????? ?????? ????????? ???????????? ???????????? ?????????. (401)")
    @Test
    void updateWithInvalidAccessToken() throws Exception {
        mockMvc.perform(
            patch("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\n"
                    + "  \"brand\": \"????????????\",\n"
                    + "  \"category\": \"??????\",\n"
                    + "  \"currency\": \"KRW\",\n"
                    + "  \"description\": \"???????????????.\",\n"
                    + "  \"image_link\": [\n"
                    + "    \"https://aws.s3.abc.jpg\"\n"
                    + "  ],\n"
                    + "  \"name\": \"????????????\",\n"
                    + "  \"price\": \"50000\"\n"
                    + "}")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
        )
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("???????????? ????????? ??????????????? ??? ??????????????? ????????????.")
    @Test
    void destroyWithExistedProduct() throws Exception {
        mockMvc.perform(
            delete("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @DisplayName("?????? ?????? ?????? ????????? ???????????? ?????? ?????? ?????????. (404)")
    @Test
    void destroyWithNotExistedProduct() throws Exception {
        mockMvc.perform(
            delete("/products/1000")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + VALID_TOKEN)
        )
            .andExpect(status().isNotFound());

        verify(productService).deleteProduct(1000L);
    }

    @DisplayName("???????????? ?????? ???????????? ?????? ????????? ??????, ?????? ??????(401) ??? ????????????.")
    @Test
    void destroyWithInvalidAccessToken() throws Exception {
        mockMvc.perform(
            delete("/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + INVALID_TOKEN)
        )
            .andExpect(status().isUnauthorized());
    }
}
