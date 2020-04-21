package guru.springframework.brewery.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.BeerDto;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderLineDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import guru.springframework.brewery.web.model.BeerStyleEnum;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerDto validBeer;
    BeerOrderDto beerOrder;
    BeerOrderPagedList beerOrderPagedList;
    
	@Captor
	ArgumentCaptor<Integer> pageSizeCaptor;
	
	@Captor
	ArgumentCaptor<Integer> pageNumberCaptor;


    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder().id(UUID.randomUUID())
                .version(1)
                .beerName("Beer1")
                .beerStyle(BeerStyleEnum.PALE_ALE)
                .price(new BigDecimal("12.99"))
                .quantityOnHand(4)
                .upc(123456789012L)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .build();

        beerOrder = BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .customerRef("1234")
                .beerOrderLines(Arrays.asList(new BeerOrderLineDto[] {BeerOrderLineDto .builder()
       				 .beerId(validBeer.getId()) .build()} ))
                .build();

        beerOrderPagedList = new BeerOrderPagedList(Arrays.asList(new BeerOrderDto[] {beerOrder}),
                PageRequest.of(1, 1), 1L);
    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @Test
    void listOrders() throws Exception {
        given(beerOrderService.listOrders(any(), any())).willReturn(beerOrderPagedList);

        mockMvc.perform(get("/api/v1/customers/85d4506-e7dd-446e-a092-5f30b98e7b26/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        
		then(beerOrderService).should().listOrders(any(UUID.class),PageRequest.of(pageNumberCaptor.capture(), pageSizeCaptor.capture()));
		assertThat(pageSizeCaptor.getValue()).isEqualTo(25);
    }

    @Test
    void getOrder() throws Exception {
        given(beerOrderService.getOrderById(any(), any())).willReturn(beerOrder);

        mockMvc.perform(get("/api/v1/customers/85d4506-e7dd-446e-a092-5f30b98e7b26/orders/f25767d9-342a-48ac-a788-0a7a38ae6fb3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}