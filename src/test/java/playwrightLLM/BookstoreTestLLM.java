package playwrightLLM;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class BookstoreTestLLM {
    private Playwright playwright;
    private Browser browser;
    private Page page;

    @BeforeEach
    public void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        page = browser.newPage();
    }

    @AfterEach
    public void tearDown() {
        if (page != null) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    public void testBookstoreFlow() {
        // Navigate to the bookstore
        page.navigate("https://depaul.bncollege.com");

        // Open search
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("search")).click();

        // Search for earbuds
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Search")).fill("earbuds");
        page.locator("form[name=\"search_form_SearchBox\"]").getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("search")).click();

        // Filter by Brand JBL
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("brand")).click();
        page.locator("label").filter(new Locator.FilterOptions().setHasText("JBL (10)")).first().click();

        // Filter by Color Black
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Color")).click();
        page.locator("label").filter(new Locator.FilterOptions().setHasText("Color Black (5)")).click();

        // Filter by Price Over $50
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Price")).click();
        page.locator("label").filter(new Locator.FilterOptions().setHasText("Price Over $50 (5)")).click();

        // Click the JBL Quantum product
        page.getByTitle("JBL Quantum True Wireless").first().click();

        // Add to cart
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add to cart")).click();

        // Verify cart shows 1 item
        assert page.locator("text=Cart 1 items").isVisible();

        // Go to cart
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("1")).click();

        // Proceed to checkout
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Proceed To Checkout")).nth(1).click();

        // Proceed as guest
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Proceed As Guest")).click();

        // Fill contact info
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("First Name (required)")).fill("John");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Last Name (required)")).fill("Doe");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email address (required)")).fill("john.doe@example.com");
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Phone Number (required)")).fill("3125550123");

        // Continue to pickup
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).first().click();

        // Continue to payment
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Continue")).click();

        // Go back to cart
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Back to cart")).click();

        // Delete the item from cart
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Remove product JBL Quantum True Wireless Noise Cancelling Gaming Earbuds- Black from cart")).click();
    }
}