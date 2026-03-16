package org.example;

import org.example.Amazon.*;
import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AmazonUnitTest {

    // ── SPECIFICATION-BASED ──────────────────────────────────────

    @Test
    @DisplayName("specification-based")
    void calculateReturnsZeroWhenNoRules() {
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(mockCart.getItems()).thenReturn(Collections.emptyList());

        Amazon amazon = new Amazon(mockCart, Collections.emptyList());
        assertThat(amazon.calculate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("specification-based")
    void calculateSumsAllPriceRulesCorrectly() {
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        List<Item> items = List.of(new Item(ItemType.OTHER, "Book", 1, 20.0));
        when(mockCart.getItems()).thenReturn(items);

        PriceRule rule1 = Mockito.mock(PriceRule.class);
        PriceRule rule2 = Mockito.mock(PriceRule.class);
        when(rule1.priceToAggregate(items)).thenReturn(20.0);
        when(rule2.priceToAggregate(items)).thenReturn(5.0);

        Amazon amazon = new Amazon(mockCart, List.of(rule1, rule2));
        assertThat(amazon.calculate()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("specification-based")
    void addToCartDelegatesToShoppingCart() {
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        Amazon amazon = new Amazon(mockCart, Collections.emptyList());
        Item item = new Item(ItemType.ELECTRONIC, "Mouse", 1, 25.0);

        amazon.addToCart(item);

        verify(mockCart, times(1)).add(item);
    }

    @Test
    @DisplayName("specification-based")
    void regularCostReturnsZeroForEmptyCart() {
        RegularCost rule = new RegularCost();
        assertThat(rule.priceToAggregate(Collections.emptyList())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("specification-based")
    void regularCostMultipliesQuantityByPrice() {
        RegularCost rule = new RegularCost();
        List<Item> items = List.of(
                new Item(ItemType.OTHER, "Pen", 3, 2.0),   // 6.0
                new Item(ItemType.OTHER, "Book", 2, 15.0)  // 30.0
        );
        assertThat(rule.priceToAggregate(items)).isEqualTo(36.0);
    }

    @Test
    @DisplayName("specification-based")
    void deliveryPriceReturnsZeroForEmptyCart() {
        DeliveryPrice rule = new DeliveryPrice();
        assertThat(rule.priceToAggregate(Collections.emptyList())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("specification-based")
    void deliveryPriceReturnsCorrectTierForOneItem() {
        DeliveryPrice rule = new DeliveryPrice();
        assertThat(rule.priceToAggregate(
                List.of(new Item(ItemType.OTHER, "X", 1, 0.0))
        )).isEqualTo(5.0);
    }

    @Test
    @DisplayName("specification-based")
    void electronicsSurchargeAppliedWhenCartHasElectronic() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        assertThat(rule.priceToAggregate(
                List.of(new Item(ItemType.ELECTRONIC, "Tablet", 1, 300.0))
        )).isEqualTo(7.50);
    }

    @Test
    @DisplayName("specification-based")
    void electronicsSurchargeNotAppliedWithNoElectronic() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        assertThat(rule.priceToAggregate(
                List.of(new Item(ItemType.OTHER, "Bag", 1, 30.0))
        )).isEqualTo(0.0);
    }

    // ── STRUCTURAL-BASED ─────────────────────────────────────────

    @Test
    @DisplayName("structural-based")
    void calculateCallsGetItemsOnce() {
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        when(mockCart.getItems()).thenReturn(Collections.emptyList());

        PriceRule mockRule = Mockito.mock(PriceRule.class);
        when(mockRule.priceToAggregate(anyList())).thenReturn(0.0);

        new Amazon(mockCart, List.of(mockRule)).calculate();

        verify(mockCart, times(1)).getItems();
    }

    @Test
    @DisplayName("structural-based")
    void eachPriceRuleReceivesTheSameItemList() {
        ShoppingCart mockCart = Mockito.mock(ShoppingCart.class);
        List<Item> items = List.of(new Item(ItemType.OTHER, "Chair", 1, 50.0));
        when(mockCart.getItems()).thenReturn(items);

        PriceRule rule1 = Mockito.mock(PriceRule.class);
        PriceRule rule2 = Mockito.mock(PriceRule.class);
        when(rule1.priceToAggregate(items)).thenReturn(50.0);
        when(rule2.priceToAggregate(items)).thenReturn(5.0);

        new Amazon(mockCart, List.of(rule1, rule2)).calculate();

        verify(rule1).priceToAggregate(items);
        verify(rule2).priceToAggregate(items);
    }

    @Test
    @DisplayName("structural-based")
    void regularCostLoopsOverAllItems() {
        RegularCost rule = new RegularCost();
        List<Item> items = List.of(
                new Item(ItemType.OTHER, "A", 1, 10.0),
                new Item(ItemType.OTHER, "B", 1, 20.0),
                new Item(ItemType.OTHER, "C", 1, 30.0)
        );
        assertThat(rule.priceToAggregate(items)).isEqualTo(60.0);
    }

    @Test
    @DisplayName("structural-based")
    void deliveryPriceBoundaryAtExactlyFourItems() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = List.of(
                new Item(ItemType.OTHER, "A", 1, 0.0),
                new Item(ItemType.OTHER, "B", 1, 0.0),
                new Item(ItemType.OTHER, "C", 1, 0.0),
                new Item(ItemType.OTHER, "D", 1, 0.0)
        );
        assertThat(rule.priceToAggregate(items)).isEqualTo(12.5);
    }

    @Test
    @DisplayName("structural-based")
    void deliveryPriceBoundaryAtExactlyTenItems() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) items.add(new Item(ItemType.OTHER, "I" + i, 1, 0.0));
        assertThat(rule.priceToAggregate(items)).isEqualTo(12.5);
    }

    @Test
    @DisplayName("structural-based")
    void deliveryPriceBoundaryAtElevenItems() {
        DeliveryPrice rule = new DeliveryPrice();
        List<Item> items = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) items.add(new Item(ItemType.OTHER, "I" + i, 1, 0.0));
        assertThat(rule.priceToAggregate(items)).isEqualTo(20.0);
    }

    @Test
    @DisplayName("structural-based")
    void electronicsSurchargeIsExactly7Point50WhenMultipleElectronics() {
        ExtraCostForElectronics rule = new ExtraCostForElectronics();
        List<Item> items = List.of(
                new Item(ItemType.OTHER, "Book", 1, 10.0),
                new Item(ItemType.ELECTRONIC, "Camera", 1, 500.0),
                new Item(ItemType.ELECTRONIC, "Drone", 1, 999.0)
        );
        // anyMatch means surcharge fires only ONCE regardless of how many electronics
        assertThat(rule.priceToAggregate(items)).isEqualTo(7.50);
    }

    @Test
    @DisplayName("structural-based")
    void itemGettersReturnConstructorValues() {
        Item item = new Item(ItemType.ELECTRONIC, "Keyboard", 3, 45.99);
        assertThat(item.getType()).isEqualTo(ItemType.ELECTRONIC);
        assertThat(item.getName()).isEqualTo("Keyboard");
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getPricePerUnit()).isEqualTo(45.99);
    }
}
