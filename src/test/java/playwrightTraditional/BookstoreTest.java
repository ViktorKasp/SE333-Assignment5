package playwrightTraditional;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookstoreTest {

    static Playwright playwright;
    static Browser browser;
    static BrowserContext context;
    static Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
        context = browser.newContext(new Browser.NewContextOptions()
                .setRecordVideoDir(Paths.get("videos/"))
                .setRecordVideoSize(1280, 720));
        page = context.newPage();
        page.setDefaultTimeout(60_000);
    }

    @AfterAll
    static void closeBrowser() {
        context.close();
        browser.close();
        playwright.close();
    }

    // ── TestCase 1: Search, filter, and add to cart ───────────────

    @Test
    @Order(1)
    @DisplayName("TestCase Bookstore")
    void testBookstoreSearchAndAddToCart() throws InterruptedException {
        page.navigate("https://depaul.bncollege.com/");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        Thread.sleep(3000);

        // Dismiss banner if present
        try {
            page.locator(".bned-close, button:has-text('×'), [aria-label='Close']")
                    .first().click(new Locator.ClickOptions().setTimeout(3000));
            Thread.sleep(500);
        } catch (Exception ignored) {}

        // Search for earbuds
        Locator searchBox = page.locator("input[placeholder*='product title']");
        searchBox.click();
        Thread.sleep(500);
        searchBox.fill("earbuds");
        page.keyboard().press("Enter");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Scroll down so filters are visible
        page.mouse().wheel(0, 500);
        Thread.sleep(1000);

        // ── Brand filter ──
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("brand")).click();
        Thread.sleep(1000);
        page.locator("#facet-brand")
                .getByRole(AriaRole.LIST)
                .locator("label")
                .filter(new Locator.FilterOptions().setHasText("JBL"))
                .click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(1500);

        // ── Color filter ──
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Color")).click();
        Thread.sleep(1000);
        page.locator("#facet-Color")
                .getByRole(AriaRole.LIST)
                .locator("label")
                .filter(new Locator.FilterOptions().setHasText("Black"))
                .click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(1500);

        // ── Price filter ──
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Price")).click();
        Thread.sleep(1000);
        page.locator("label")
                .filter(new Locator.FilterOptions().setHasText("Over $50"))
                .click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(1500);

        // Click JBL Quantum product
        page.locator("a:has-text('JBL Quantum True Wireless')").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Assert product name, SKU, price, description
        assertThat(page.getByText("668972707").nth(1)).isVisible();
        assertThat(page.getByText("$164.98")).isVisible();
        assertThat(page.getByText("Adaptive noise cancelling")).isVisible();
        assertThat(page.locator("text=JBL Quantum True Wireless").first()).isVisible();

        // Add to cart
        page.getByLabel("Add to cart").click();
        Thread.sleep(4000);

        // Assert cart shows 1 item
        assertThat(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Cart 1 items"))).isVisible();

        // Click Cart link
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Cart 1 items")).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);
    }

    // ── TestCase 2: Shopping Cart page ───────────────────────────
    @Test
    @Order(2)
    @DisplayName("TestCase Your Shopping Cart Page")
    void testShoppingCartPage() throws InterruptedException {
        // Wait for page to fully load
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Assert cart heading
        assertThat(page.getByText("Your Shopping Cart (1 Item)").nth(1)).isVisible();

        // Assert product link is visible
        assertThat(page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("JBL Quantum True Wireless"))).isVisible();

        // Scroll down to make sidebar and promo code visible
        //page.mouse().wheel(0, 800);
        //Thread.sleep(2000);

        // Assert FAST In-Store Pickup
        assertThat(page.locator("text=FAST In-Store Pickup").first()).isVisible();

        // Assert sidebar totals
        assertThat(page.locator("text=164.98").first()).isVisible();
        assertThat(page.locator("text=TBD").first()).isVisible();

        // Scroll down more to promo code
        page.mouse().wheel(0, 500);
        Thread.sleep(2000);

        // Fill promo code using exact ID
        page.locator("#js-voucher-code-text").waitFor();
        page.locator("#js-voucher-code-text").click();
        Thread.sleep(500);
        page.locator("#js-voucher-code-text").fill("TEST");
        Thread.sleep(500);

        // Click Apply
        page.locator("#js-voucher-code-text").press("Enter");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Scroll down to see promo rejection message
        page.mouse().wheel(0, 200);
        Thread.sleep(1000);

        // Assert promo rejection message
        assertThat(page.getByText("The coupon code entered is")).isVisible();

        // Scroll back to top
        page.evaluate("window.scrollTo(0, 0)");
        Thread.sleep(1000);

        // Click Proceed to Checkout
        page.locator("text=PROCEED TO CHECKOUT").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);
    }
    // ── TestCase 3: Create Account page ──────────────────────────

    @Test
    @Order(3)
    @DisplayName("TestCase Create Account Page")
    void testCreateAccountPage() throws InterruptedException {
        // Assert Create Account heading
        assertThat(page.getByRole(AriaRole.HEADING,
                new Page.GetByRoleOptions().setName("Create Account"))).isVisible();

        // Proceed as Guest
        page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Proceed As Guest")).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);
    }

    // ── TestCase 4: Contact Information page ─────────────────────
    @Test
    @Order(4)
    @DisplayName("TestCase Contact Information Page")
    void testContactInformationPage() throws InterruptedException {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // First name
        page.locator("#contactInfo\\.firstName").click();
        Thread.sleep(300);
        page.locator("#contactInfo\\.firstName").fill("John");
        Thread.sleep(300);

        // Last name
        page.locator("#contactInfo\\.lastName").click();
        Thread.sleep(300);
        page.locator("#contactInfo\\.lastName").fill("Doe");
        Thread.sleep(300);

        // Email
        page.locator("#contactInfo\\.emailAddress").click();
        Thread.sleep(300);
        page.locator("#contactInfo\\.emailAddress").fill("john.doe@example.com");
        Thread.sleep(300);

        // Phone
        page.locator("#phone1").click();
        Thread.sleep(300);
        page.locator("#phone1").fill("3125550123");
        Thread.sleep(300);

        // Scroll down to make Continue button visible
        page.mouse().wheel(0, 400);
        Thread.sleep(1000);

        // Click Continue
        page.locator("button.btn.btn-primary").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);
    }
    // ── TestCase 5: Pickup Information page ──────────────────────
    @Test
    @Order(5)
    @DisplayName("TestCase Pickup Information")
    void testPickupInformationPage() throws InterruptedException {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Scroll down to the Continue button
        page.mouse().wheel(0, 200);
        Thread.sleep(1500);

        // Click the second Continue button (index 1) which is for pickup section
        page.locator("button.btn-primary").nth(1).click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);
    }
    // ── TestCase 6: Payment Information page ─────────────────────
    @Test
    @Order(6)
    @DisplayName("TestCase Payment Information")
    void testPaymentInformationPage() throws InterruptedException {
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Click Back to Cart using exact class from HTML
        page.locator(".bned-go-cart-text").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);
    }
    // ── TestCase 7: Delete from cart ─────────────────────────────

    @Test
    @Order(7)
    @DisplayName("TestCase Your Shopping Cart - Delete")
    void testDeleteFromCart() throws InterruptedException {
        page.getByLabel("Remove product JBL Quantum").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        Thread.sleep(2000);

        // Assert cart is empty
        assertThat(page.locator("text=Your cart is empty").first()).isVisible();

        page.close();
    }
}