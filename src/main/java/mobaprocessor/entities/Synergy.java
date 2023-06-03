package mobaprocessor.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class Synergy {

    private final String name;

    private final List<Integer> levels;

    private final List<String> champions;

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Synergy synergy = (Synergy) o;
        return name.equals(synergy.name) && levels.equals(synergy.levels) && champions.equals(synergy.champions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, levels, champions);
    }

    public static Synergy valueOf(WebElement row) {
        String name = getName(row);
        List<Integer> levels = getLevels(row)
                .stream()
                .map(Synergy::getLevel)
                .collect(Collectors.toList());
        List<String> champions = getChampions(row)
                .stream()
                .map(Synergy::getChampionName)
                .collect(Collectors.toList());
        return new Synergy(name, levels.isEmpty() ? List.of(1) : levels, champions);
    }

    public static String getName(WebElement row) {
        return row.findElement(By.xpath("div[1]/div[1]/div/p/span")).getText();
    }

    private static List<WebElement> getLevels(WebElement row) {
        return row.findElements(By.xpath("div[1]/div[2]/div/div"));
    }

    private static Integer getLevel(WebElement level) {
        return Integer.valueOf(level.findElement(By.xpath("p[1]")).getText());
    }

    private static List<WebElement> getChampions(WebElement row) {
        return row.findElements(By.xpath("div[2]/div"));
    }

    private static String getChampionName(WebElement champion) {
        return champion.findElement(By.xpath("a/div[2]")).getText();
    }

    private static final Gson gson = new GsonBuilder().create();
}
