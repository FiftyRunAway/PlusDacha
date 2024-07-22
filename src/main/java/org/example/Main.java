package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class Main {

    private static final String[] exclusionsForBuying = new String[]{
            "Богиня шопинга",
            "Йога с Алисой ПРО",
            "Шахматы",
            "Плюс Детям",
            "House Dance с Kerry",
            "Балансборд",
            "Беговелы"
    };

    public static final String afishaURL = "https://afisha.yandex.ru/moscow/other/places/plus-dacha-msk/schedule?schedule-filter-tickets=true";
    private static final String dachaURL = "https://plus.yandex.ru/dacha";
    private static final String recipient = "sir.runaway@yandex.ru";
    private static int updateTime = 100; // в секундах


    private static final Properties MAIL_PROPERTIES = new Properties();
    static {
        MAIL_PROPERTIES.setProperty("mail.transport.protocol", "smtps");
        MAIL_PROPERTIES.setProperty("mail.smtp.host", "smtp.yandex.ru");
        MAIL_PROPERTIES.setProperty("mail.smtp.port", "465");
        MAIL_PROPERTIES.setProperty("mail.smtp.auth", "true");
        MAIL_PROPERTIES.setProperty("mail.smtp.ssl.enable", "true");
        MAIL_PROPERTIES.setProperty("mail.debug", "false");
    }
    static final Authenticator authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(
                    "sir.runaway@yandex.ru",
                    "txxbthqazfwknxor"
            );
        }
    };
    static final Session session = Session.getInstance(MAIL_PROPERTIES, authenticator);

    protected static List<Event> allEvents = new ArrayList<>();

    public static boolean enabled = false;
    public static boolean progressTimerEnabled = false;
    private static boolean firstAfterFast = false;
    public static String active_events = "Ожидание запуска...";
    public static String lastEvents = "";
    private static int progressNow;
    private static MutableAttributeSet attributes1;
    private static MutableAttributeSet attributes2;

    public static MenuBar menuBar;
    public static Menu buyTickets;
    public static Menu buyTickets2;
    private static Menu accounts;

    public static GUI frame;
    public static WebDriver driver;
    private static Timer progressTimer, updateTimer;

    private static boolean firstStart;

    public static void main(String[] args) {
        startNewDriver();
        frame = new GUI();
        frame.getSiteButton().addActionListener(Main::actionPerformed);
        frame.getOnButton().addActionListener(e -> startAction());
        frame.getLoginButton().addActionListener(e -> BuyTicket.login(BuyTicket.selectedAccount));
        frame.getEventsArea().setText(active_events);

        setMenuBar();
        checkCurrentTime();

        attributes1 = new SimpleAttributeSet(frame.getEventsArea().getInputAttributes());
        StyleConstants.setForeground(attributes1, new Color(111, 81, 116));
        attributes2 = new SimpleAttributeSet(frame.getEventsArea().getInputAttributes());
        StyleConstants.setForeground(attributes2, Color.BLACK);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> driver.quit(), "Shutdown-thread"));
    }

    public static void start() {
        start(false);
    }

    public static void start(boolean skipNavigation) {
        frame.getOnButton().setText("Остановить");
        frame.getEventsArea().setText("Обновление...");
        frame.getUpdateArea().setText("Обновление...");
        if (!skipNavigation) {
            try {
                driver.navigate().to(afishaURL);
            } catch (Exception e) {
                startNewDriver();
                driver.navigate().to(afishaURL);
            }
        }
        firstStart = true;

        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() throws RuntimeException {
                frame.getEventsArea().setText("Обновление...");
                try {
                    driver.navigate().to(afishaURL);
                } catch (Exception e) {
                    frame.getEventsArea().setText("Браузер закрыт, откройте его снова, нажав на запуск");
                    disable();
                    return;
                }
                frame.getUpdateArea().setText("Обновление...");

                WebElement events;
                try {
                    events = driver.findElement(By.xpath("//div[@class='schedule-stage-list__inner']"));
                } catch (Exception ex) {
                    captchaOnUpdate();
                    return;
                }
                enabled = true;

                StringBuilder res = new StringBuilder();
                String[] s = events.getText().split("\n");
                StringBuilder resWithDates = new StringBuilder();

                String actualDate = "";
                int eventsToShowThisDate = 0;
                boolean isDate;
                for (int i = 0; i < s.length; i++) {
                    isDate = false;
                    try {
                        Integer.parseInt(s[i]);
                        if (eventsToShowThisDate == 0 && !actualDate.isEmpty()) {
                            resWithDates.delete(resWithDates.length() - (actualDate.length() + 2), resWithDates.length());
                        }
                        eventsToShowThisDate = 0;
                        isDate = true;
                        String date = s[i] + " " + s[i + 2];
                        resWithDates.append(date).append(":\n");
                        actualDate = date;
                    } catch (IllegalArgumentException ignored) {}

                    if (s[i].toLowerCase().contains("бесплатно")) {
                        String name = s[i - 1];
                        boolean isExclusion = false;
                        for (String str : exclusionsForBuying) {
                            if (name.contains(str)) {
                                isExclusion = true;
                                break;
                            }
                        }
                        if (!isExclusion) {
                            resWithDates.append("    ").append(s[i - 1]).append("\n");
                            eventsToShowThisDate++;
                        }
                        res.append(s[i - 1]).append("\n");

                        int index = (res.toString().split("\n").length - 1);
                        allEvents.clear();
                        allEvents.add(new Event(name, actualDate, index));

                        if (isDate) i += 2;
                    }
                }
                if (eventsToShowThisDate == 0) {
                    resWithDates.delete(resWithDates.length() - (actualDate.length() + 2), resWithDates.length());
                }
                active_events = resWithDates.toString();

                List<String> last = new ArrayList<>(Arrays.asList(lastEvents.split("\n")));
                List<String> newe = new ArrayList<>(Arrays.asList(res.toString().split("\n")));
                if (BuyTicket.loggedIn) updateMenuBar(newe);
                boolean newsOrNot = false;
                if (!last.toString().equals(newe.toString()) && SystemTray.isSupported()) {
                    StringBuilder news = new StringBuilder();
                    if (last.size() > newe.size()) {
                        for (String w : last) {
                            if (!newe.contains(w)) {
                                news.append(w).append("\n");
                            }
                        }
                    } else {
                        newsOrNot = true;
                        for (String w : newe) {
                            if (!last.contains(w)) {
                                news.append(w).append("\n");
                            }
                        }
                    }
                    try {
                        newsTray(firstStart, news.toString(), newsOrNot);
                        if (frame.getCheckBox1().isSelected() && newsOrNot && !firstStart) {
                            sendEmailNews(news.toString());
                        }
                        lastEvents = res.toString();
                        firstStart = false;
                    } catch (AWTException | MessagingException e) {
                        throw new RuntimeException(e);
                    }
                }
                frame.getEventsArea().setText("");
                formatEvents(active_events);

                ZonedDateTime now = ZonedDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                frame.getUpdateArea().setText(dtf.format(now));

                progressTimer = new Timer();
                progressNow = 0;
                progressTimerEnabled = true;
                progressTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        progressNow += Math.round((float) frame.getProgressBar1().getMaximum() / updateTime);
                        if (progressNow >= frame.getProgressBar1().getMaximum()) {
                            progressTimer.cancel();
                            progressTimerEnabled = false;
                            progressNow = 0;
                            frame.getProgressBar1().setValue(progressNow);
                        } else {
                            frame.getProgressBar1().setValue(progressNow);
                        }
                    }
                }, 0, 1000);

                if (!BuyTicket.loggedIn) frame.getLoginButton().setEnabled(true);
                accounts.setEnabled(true);

                frame.setSize(GUI.WIDTH, active_events.split("\n").length * 15 + 400);
            }
        }, 0, updateTime * 1000L);
    }

    private static void captchaOnUpdate() {
        disable();
        frame.getEventsArea().setText("Ошибка..\n\nПроверьте окно браузера Chrome, возможно там вас\nподжидает капча!");
        try {
            driver.manage().window().setSize(new Dimension(500, 700 ));
        } catch (Exception ignored) {}
        frame.getUpdateArea().setText("...");
        try {
            captchaTray();
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static void disable() {
        updateTimer.cancel();
        enabled = false;
        frame.getOnButton().setText("Запустить снова");

        if (progressTimerEnabled) {
            progressTimer.cancel();
            progressNow = 0;
            frame.getProgressBar1().setValue(progressNow);
            progressTimerEnabled = false;
        }
    }

    public static void newsTray(boolean firstStart, String news, boolean newsBoolean) throws AWTException {
        if (firstStart) return;
        customTray("Обновление", (newsBoolean? "Пополнение билетов: \n" : "Закончились билеты: \n") + news, TrayIcon.MessageType.INFO);
    }

    public static void captchaTray() throws AWTException {
        customTray("КАПЧА", "Зайдите в браузер, чтобы пройти капчу", TrayIcon.MessageType.WARNING);
    }

    public static void customTray(String caption, String text, TrayIcon.MessageType messageType) throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("icon.webp");
        TrayIcon trayIcon = new TrayIcon(image, "Dacha");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Dacha");
        tray.add(trayIcon);

        trayIcon.displayMessage(caption, text, messageType);
        tray.remove(trayIcon);
    }

    private static void actionPerformed(ActionEvent e) {
        try {
            openBrowser(dachaURL);
            openBrowser(afishaURL);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void startAction() {
        if (!enabled || frame.getOnButton().getText().contains("Прошел капчу")) {
            try {
                driver.manage().window().setSize(new Dimension(5,5));
                driver.manage().window().minimize();
            } catch (Exception ignored) {}
            start();
        } else {
            disable();
        }
    }

    public static void openBrowser(String url) throws URISyntaxException {
        Desktop desktop;
        try {
            desktop = Desktop.getDesktop();
        } catch (Exception ex) {
            System.err.println("Класс Desktop не поддерживается.");
            return;
        }
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            System.err.println("BROWSE: операция не поддерживается..");
            return;
        }
        try {
            desktop.browse(new URL(url).toURI());
        } catch (IOException ex) {
            System.err.println("Failed to browse. " + ex.getLocalizedMessage());
        }
    }

    private static void startNewDriver() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(5,5));
        driver.manage().window().minimize();
    }

    private static void setMenuBar() {
        menuBar = new MenuBar();
        Menu exclusions = new Menu("Исключения");
        menuBar.setHelpMenu(exclusions);

        for (String exclusion : exclusionsForBuying) {
            MenuItem item = new MenuItem(exclusion);
            item.setEnabled(false);
            exclusions.add(item);
        }

        accounts = new Menu("Выбрать аккаунт для логина");
        accounts.setEnabled(false);
        menuBar.add(accounts);

        MenuItem first = new MenuItem(getUsername(0));
        MenuItem second = new MenuItem(getUsername(1));
        second.setEnabled(false);
        BuyTicket.selectedAccount = 1;
        MenuItem third = new MenuItem(getUsername(2));
        first.addActionListener(e -> {
            BuyTicket.selectedAccount = 0;
            first.setEnabled(false);
            second.setEnabled(true);
            third.setEnabled(true);

        });
        accounts.add(first);

        second.addActionListener(e -> {
            BuyTicket.selectedAccount = 1;
            first.setEnabled(true);
            second.setEnabled(false);
            third.setEnabled(true);
        });
        accounts.add(second);

        third.addActionListener(e -> {
            BuyTicket.selectedAccount = 2;
            first.setEnabled(true);
            second.setEnabled(true);
            third.setEnabled(false);
        });
        accounts.add(third);

        frame.setMenuBar(menuBar);
    }

    public static void sendEmailNews(String news) throws MessagingException {
        StringBuilder text = new StringBuilder("Пополнение:\n");
        text.append(news);

        text.append("\n\n").append("Афиша: " + afishaURL).append("\nДача: " + dachaURL);
        sendEmail(text.toString());
    }

    private static void sendEmail(String text) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setFrom(new InternetAddress("sir.runaway@yandex.ru"));
        message.setSubject("Плюс Дача: обновления!");
        message.setText(text);

        Transport.send(message);
    }

    public static void updateMenuBar(List<String> activeEvents) {
        if (buyTickets == null) {
            buyTickets = new Menu("Купить билет x1");
        } else {
            frame.getMenuBar().remove(buyTickets);
            buyTickets = new Menu("Купить билет x1");
        }
        if (buyTickets2 == null) {
            buyTickets2 = new Menu("Купить билеты x2");
        } else {
            frame.getMenuBar().remove(buyTickets2);
            buyTickets2 = new Menu("Купить билеты x2");
        }
        for (int i = 0; i < activeEvents.size(); i++) {
            boolean toSkip = false;
            for (String s : exclusionsForBuying) {
                if (activeEvents.get(i).contains(s)) {
                    toSkip = true;
                    break;
                }
            }
            if (toSkip) continue;

            MenuItem item = new MenuItem(activeEvents.get(i));
            int finalI = i;
            item.addActionListener(e -> {
                try {
                    BuyTicket.buy(finalI, false);
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
            });
            buyTickets.add(item);

            MenuItem twoTickets = new MenuItem(activeEvents.get(i));
            twoTickets.addActionListener(e -> {
                try {
                    BuyTicket.buy(finalI, true);
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
            });
            buyTickets2.add(twoTickets);

        }
        menuBar.add(buyTickets);
        menuBar.add(buyTickets2);
        frame.setMenuBar(menuBar);
    }

    public static WebDriver getDriver() {
        return driver;
    }

    public static String getUsername(int index) {
        switch (index) {
            case 0: {
                return "sir.runaway";
            }
            case 1: {
                return "runaway.sasha";
            }
            case 2: {
                return "runaway.netf";
            }
            default:
                throw new IllegalStateException("Unexpected value: " + index);
        }
    }

    private static void checkCurrentTime() {
        int fastUpdate = updateTime / 5;
        int defaultUpdate = updateTime;
        Color defaultColor = frame.getProgressBar1().getBackground();

        String defaultTitle = frame.getTitle();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                ZonedDateTime now = ZonedDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                frame.setTitle(defaultTitle + " " + dtf.format(now));

                if (hour == 11 && minute > 48 && minute < 54) {
                    if (updateTime != fastUpdate) {
                        boolean toEnable = false;
                        if (enabled) {
                            disable();
                            toEnable = true;
                        }
                        updateTime = fastUpdate;
                        frame.getProgressBar1().setBackground(new Color(213, 62, 7));
                        firstAfterFast = true;
                        if (toEnable) start();
                    }
                } else {
                    if (firstAfterFast) {
                        updateTime = defaultUpdate;
                        frame.getProgressBar1().setBackground(defaultColor);
                        firstAfterFast = false;
                        if (enabled) {
                            disable();
                            start();
                        }
                    }
                }
            }
        }, 0L, 2000L);
    }

    private static void formatEvents(String msg) {
        String[] lines = msg.split("\n");
        JTextPane tp = frame.getEventsArea();
        for (String line : lines) {
            try {
                Integer.parseInt(String.valueOf(line.charAt(0)));
                tp.getStyledDocument().insertString(tp.getDocument().getLength(), line + "\n", attributes1);
                continue;
            } catch (Exception ignored) {}
            try {
                tp.getStyledDocument().insertString(tp.getDocument().getLength(), line + "\n", attributes2);
            } catch (BadLocationException  ignored) {}
        }
    }
}