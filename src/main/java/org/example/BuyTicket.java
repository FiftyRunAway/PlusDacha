package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.WheelInput;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BuyTicket {

    public static boolean loggedIn = false;
    public static int selectedAccount = 0;

    private static final String session_id =
            "3:1720426866.5.1.1714062692076:6zAi1A:4.1.2:1|924743107.831958.2.2:831958.3:1714894650|331316575.5622291.2.2:5622291.3:1719684983|1019195689.5676143.2.2:5676143.3:1719738835|2000140462.6360582.2.2:6360582.3:1720423274|3:10291189.803353.r8_v0YCgjNj3iy0Ge6w_HQZxgV8";
    private static final String sessionid2 =
            "3:1720426866.5.1.1714062692076:6zAi1A:4.1.2:1|924743107.831958.2.2:831958.3:1714894650|331316575.5622291.2.2:5622291.3:1719684983|1019195689.5676143.2.2:5676143.3:1719738835|2000140462.6360582.2.2:6360582.3:1720423274|3:10291189.803353.fakesign0000000000000000000";

    private static WebDriverWait wait;

    public static void buy(int index, boolean twoTickets) throws AWTException {
        Main.disable();
        Main.frame.getEventsArea().setText("Происходит покупка билетов...\n\nДля продолжения работы необходимо\nнажать 'Запустить снова'");
        Main.frame.getMenuBar().remove(Main.buyTickets);
        Main.frame.getMenuBar().remove(Main.buyTickets2);
        WebDriver driver = Main.getDriver();
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {}

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        WebElement textElement = driver.findElements(By.xpath("//a[contains(@class, 'yaticket i-stat__click i-bem yaticket_js_inited')]")).get(index);
        Main.customTray("ВНИМАНИЕ", "Крутани колесико мыши!", TrayIcon.MessageType.WARNING);
        driver.get(textElement.getAttribute("href"));

        String eventType = driver.findElement(By.xpath("/html/body/div[2]/div[1]/div[3]/div/div/div[2]/div[3]/div/div[1]/div/div/div/div[1]/ul/li[1]")).getText();
        boolean spam = true;
        if (eventType.equalsIgnoreCase("кино")) {
            WebElement film = driver.findElement(By.xpath("//a[contains(@class,'ButtonWrapper-d2ik01-0 eUyUCQ afisha-common-session-list_button')]"));
            driver.navigate().to(film.getAttribute("href"));
            spam = false;
        } else {
            new Actions(driver).pause(1500).scrollByAmount(0, 250).perform();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[2]/div[1]/div[3]/div/div/div[6]/div/div[2]/div[2]/div[1]/iframe")));
            WebElement actualEventURL = driver.findElement(By.xpath("/html/body/div[2]/div[1]/div[3]/div/div/div[6]/div/div[2]/div[2]/div[1]/iframe"));
            driver.navigate().to(actualEventURL.getAttribute("src"));
        }

        WebElement next = driver.findElement(By.xpath("//button[contains(@class, 'PresalePlus__Button-sc-16wywu1-7 fibWYq')]"));
        wait.until(ExpectedConditions.elementToBeClickable(next));
        next.click();

        if (twoTickets) {
            WebElement plus = driver.findElement(By.xpath("//span[contains(@class, 'Action__ActionButton-sc-1iie49r-0-span amounter__Button-sc-1obkbj5-0 hiGksz gSrgAs amounter_button amounter_button__increase')]"));
            wait.until(ExpectedConditions.elementToBeClickable(plus));
            plus.click();
        }

        WebElement preLast = driver.findElement(By.xpath("//button[contains(@data-test, 'session-scheme_continue')]"));
        wait.until(ExpectedConditions.elementToBeClickable(preLast));
        preLast.click();

        if (spam) {
            WebElement mailCheckbox = driver.findElement(By.xpath("//div[@class='SessionCheckoutFormCheckboxItem__Inner-sc-17uur22-1 fxAFAf']"));
            mailCheckbox.click();
        }

        if (Main.frame.getCheckBox2().isSelected()) {
            WebElement purchase = driver.findElement(By.xpath("//button[contains(@class, 'Button-sc')]"));
            purchase.click();
        }
    }

    public static void login(int index) {
        Main.disable();
        Main.frame.getLoginButton().setEnabled(false);
        Main.frame.getEventsArea().setText("Вход в аккаунт Яндекс.Афиши...");
        Main.frame.getUpdateArea().setText("...");
        WebDriver driver = Main.getDriver();
        try {
            driver.navigate().to("https://passport.yandex.ru/auth?retpath=https%3A%2F%2Fid.yandex.ru%2F&noreturn=1");
            driver.manage().addCookie(new Cookie("Session_id", session_id));
            driver.manage().addCookie(new Cookie("sessionid2", sessionid2));
            driver.navigate().refresh();
            driver.findElements(By.xpath("//a[contains(@class, 'AuthAccountListItem')]")).get(index).click();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    driver.navigate().to(Main.afishaURL);
                    String actualAccount = driver.findElement(By.xpath("//button[@data-testid='header-user-button']")).getAttribute("aria-label");
                    if (!actualAccount.equals(Main.getUsername(index))) {
                        System.out.println("ERROR");
                        errorOnLogin();
                        return;
                    }
                    Main.menuBar.remove(1);
                    Main.start(true);
                    Main.frame.getLoginButton().setText(actualAccount);
                    Main.frame.getLoginButton().setBackground(new Color(53, 116, 32));
                    loggedIn = true;
                }
            }, 2500L);
        } catch (Exception e) {
            errorOnLogin();
        }
    }

    private static void errorOnLogin() {
        Main.frame.getLoginButton().setText("Попробовать снова");
        try {
            Main.captchaTray();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        Main.frame.getEventsArea().setText("Ошибка..\n\nПроверьте окно браузера Chrome,\nвозможно там капча или устарели куки");
        Main.frame.getLoginButton().setEnabled(true);
        Main.getDriver().manage().window().setSize(new Dimension(500, 700 ));
    }
}