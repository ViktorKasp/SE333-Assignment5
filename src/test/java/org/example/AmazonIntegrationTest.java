package org.example;

import org.example.Amazon.*;
import org.example.Amazon.Cost.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AmazonIntegrationTest {

    private static Database database;
    private ShoppingCartAdaptor cart;
    private Amazon amazon;

    @BeforeAll
    static void initDatabase() {
        database = new Database();
    }

    @BeforeEach
    void setUp() {
        database.resetDatabase();
        cart = new ShoppingCartAdaptor(database);
    }

    @AfterAll
    static void tearDown() {
        database.close();
    }

    // ── SPECIFICATION-BASED ──────────────────────────────────────

    @Test
    @DisplayName("specification-based")
    void emptyCartReturnsZeroTotal() {
        amazon = new Amazon(cart, List.of(new RegularCost(), new DeliveryPrice(), new ExtraCostForElectronics()));
        assertThat(amazon.calculate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("specification-based")
    void singleNonElectronicItemCalculatesCorrectTotal() {
        // RegularCost: 2 * 10.0 = 20.0  |  Delivery (1 row): 5.0  |  Electronics: 0.0  → 25.0
        amazon = new Amazon(cart, List.of(new RegularCost(), new DeliveryPrice(), new ExtraCostForElectronics()));
        amazon.addToCart(new Item(ItemType.OTHER, "Java Book", 2, 10.0));
        assertThat(amazon.calculate()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("specification-based")
    void singleElectronicItemIncludesElectronicsSurcharge() {
        // RegularCost: 100.0  |  Delivery: 5.0  |  Electronics: 7.50  → 112.50
        amazon = new Amazon(cart, List.of(new RegularCost(), new DeliveryPrice(), new ExtraCostForElectronics()));
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "Laptop", 1, 100.0));
        assertThat(amazon.calculate()).isEqualTo(112.50);
    }

    @Test
    @DisplayName("specification-based")
    void mixedCartChargesElectronicsSurchargeOnce() {
        // book 20 + phone 200 = 220  |  Delivery (2 rows): 5.0  |  Electronics: 7.50  → 232.50
        amazon = new Amazon(cart, List.of(new RegularCost(), new DeliveryPrice(), new ExtraCostForElectronics()));
        amazon.addToCart(new Item(ItemType.OTHER, "Book", 1, 20.0));
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "Phone", 1, 200.0));
        assertThat(amazon.calculate()).isEqualTo(232.50);
    }

    @Test
    @DisplayName("specification-based")
    void cartPersistsItemsAcrossMultipleAdds() {
        amazon = new Amazon(cart, List.of(new RegularCost()));
        amazon.addToCart(new Item(ItemType.OTHER, "Pen", 1, 1.0));
        amazon.addToCart(new Item(ItemType.OTHER, "Notebook", 1, 5.0));
        assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("specification-based")
    void deliveryPriceIsFiveForOneToThreeItems() {
        amazon = new Amazon(cart, List.of(new DeliveryPrice()));
        amazon.addToCart(new Item(ItemType.OTHER, "A", 1, 0.0));
        amazon.addToCart(new Item(ItemType.OTHER, "B", 1, 0.0));
        amazon.addToCart(new Item(ItemType.OTHER, "C", 1, 0.0));
        assertThat(amazon.calculate()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("specification-based")
    void deliveryPriceIsTwelvePointFiveForFourItems() {
        amazon = new Amazon(cart, List.of(new DeliveryPrice()));
        for (int i = 0; i < 4; i++) {
            amazon.addToCart(new Item(ItemType.OTHER, "Item" + i, 1, 0.0));
        }
        assertThat(amazon.calculate()).isEqualTo(12.5);
    }

    @Test
    @DisplayName("specification-based")
    void deliveryPriceIsTwentyForMoreThanTenItems() {
        amazon = new Amazon(cart, List.of(new DeliveryPrice()));
        for (int i = 0; i < 11; i++) {
            amazon.addToCart(new Item(ItemType.OTHER, "Item" + i, 1, 0.0));
        }
        assertThat(amazon.calculate()).isEqualTo(20.0);
    }

    // ── STRUCTURAL-BASED ─────────────────────────────────────────

    @Test
    @DisplayName("structural-based")
    void databaseResetClearsAllItems() {
        cart.add(new Item(ItemType.OTHER, "Eraser", 1, 0.5));
        assertThat(cart.getItems()).hasSize(1);

        database.resetDatabase();
        ShoppingCartAdaptor freshCart = new ShoppingCartAdaptor(database);
        assertThat(freshCart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("structural-based")
    void addToCartPersistsItemInDatabase() {
        amazon = new Amazon(cart, List.of(new RegularCost()));
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "Headphones", 2, 50.0));

        List<Item> items = cart.getItems();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Headphones");
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
        assertThat(items.get(0).getPricePerUnit()).isEqualTo(50.0);
        assertThat(items.get(0).getType()).isEqualTo(ItemType.ELECTRONIC);
    }

    @Test
    @DisplayName("structural-based")
    void multipleRulesAreAllApplied() {
        // No RegularCost — only Delivery + Electronics
        amazon = new Amazon(cart, List.of(new DeliveryPrice(), new ExtraCostForElectronics()));
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "TV", 1, 0.0));
        assertThat(amazon.calculate()).isEqualTo(12.50);
    }

    @Test
    @DisplayName("structural-based")
    void onlyRegularCostRuleReturnsCorrectPrice() {
        amazon = new Amazon(cart, List.of(new RegularCost()));
        amazon.addToCart(new Item(ItemType.OTHER, "Desk", 3, 50.0));
        assertThat(amazon.calculate()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("structural-based")
    void getItemsReturnsAllPersistedItems() {
        amazon = new Amazon(cart, List.of(new RegularCost()));
        amazon.addToCart(new Item(ItemType.OTHER, "Chair", 1, 30.0));
        amazon.addToCart(new Item(ItemType.ELECTRONIC, "Monitor", 1, 200.0));
        amazon.addToCart(new Item(ItemType.OTHER, "Lamp", 2, 15.0));
        assertThat(cart.getItems()).hasSize(3);
    }

    @Test
    @DisplayName("structural-based")
    void electronicsSurchargeNotAppliedWhenNoElectronics() {
        amazon = new Amazon(cart, List.of(new ExtraCostForElectronics()));
        amazon.addToCart(new Item(ItemType.OTHER, "Novel", 1, 10.0));
        assertThat(amazon.calculate()).isEqualTo(0.0);
    }
}