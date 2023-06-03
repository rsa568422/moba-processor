package mobaprocessor.io.web;

import mobaprocessor.entities.Synergy;
import mobaprocessor.entities.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.stream.Collectors;

import static mobaprocessor.Constants.ROOT;

public class SynergyWebUtils {

    private static final String URL = "https://app.mobalytics.gg/tft/set9/synergies/";

    private SynergyWebUtils() {}

    public static List<Synergy> getSynergiesFromWeb(Target target) {
        System.setProperty("webdriver.chrome.driver", String.format("%schromedriver.exe", ROOT));
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(String.format("%s%s", URL, target));

        List<Synergy> synergies = getRows(getContainer(driver))
                .stream()
                .filter(row -> !Synergy.getName(row).isBlank())
                .map(Synergy::valueOf)
                .collect(Collectors.toList());

        driver.close();

        return synergies;
    }

    private static WebElement getContainer(WebDriver driver) {
        return driver.findElement(By.xpath("//*[@id=\"container\"]/div/main/div[2]"));
    }

    private static List<WebElement> getRows(WebElement container) {
        return container.findElements(By.xpath("div"));
    }
}
