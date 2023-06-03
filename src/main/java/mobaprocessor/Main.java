package mobaprocessor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Gson gson = new GsonBuilder().create();

    public static void main(String[] args) {
        saveSynergies(Target.ORIGINS);
        saveSynergies(Target.CLASSES);
        processSynergies(3);
    }

    private static void processSynergies(Integer size) {
        List<Synergy> classes = readSynergies(Target.CLASSES);
        List<Synergy> origins = readSynergies(Target.ORIGINS);
        List<Champion> champions = getChampions(classes, origins);
        Map<String, List<Integer>> synergyLevels = getSynergyLevels(classes, origins);
        Map<String, List<String>> synergyChampions = getSynergyChampions(classes, origins);
        champions.forEach(System.out::println);
        synergyLevels.forEach((synergy, levels) -> System.out.printf("%s -> levels : %s%n", synergy, levels));
        synergyChampions.forEach((synergy, championNames) -> System.out.printf("%s -> champions : %s%n", synergy, championNames));
    }

    private static List<Champion> getChampions(List<Synergy> classes, List<Synergy> origins) {
        List<String> names = origins.stream()
                .map(Synergy::getChampions)
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return names.stream().map(name -> {
            List<String> championClasses = classes.stream()
                    .filter(c -> c.getChampions().contains(name))
                    .map(Synergy::getName)
                    .collect(Collectors.toList());
            List<String> championOrigins = origins.stream()
                    .filter(c -> c.getChampions().contains(name))
                    .map(Synergy::getName)
                    .collect(Collectors.toList());
            return new Champion(name, championClasses, championOrigins);
        }).collect(Collectors.toList());
    }

    private static Map<String, List<Integer>> getSynergyLevels(List<Synergy> classes, List<Synergy> origins) {
        Map<String, List<Integer>> classLevels = classes.stream()
                .collect(Collectors.groupingBy(
                        Synergy::getName,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0).getLevels())
                ));
        Map<String, List<Integer>> originLevels = origins.stream()
                .collect(Collectors.groupingBy(
                        Synergy::getName,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0).getLevels())
                ));
        Map<String, List<Integer>> synergyLevels = new HashMap<>();
        synergyLevels.putAll(classLevels);
        synergyLevels.putAll(originLevels);
        return synergyLevels;
    }

    private static Map<String, List<String>> getSynergyChampions(List<Synergy> classes, List<Synergy> origins) {
        Map<String, List<String>> classChampions = classes.stream()
                .collect(Collectors.groupingBy(
                        Synergy::getName,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0).getChampions())
                ));
        Map<String, List<String>> originChampions = origins.stream()
                .collect(Collectors.groupingBy(
                        Synergy::getName,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0).getChampions())
                ));
        Map<String, List<String>> synergyLevels = new HashMap<>();
        synergyLevels.putAll(classChampions);
        synergyLevels.putAll(originChampions);
        return synergyLevels;
    }

    private static List<Synergy> readSynergies(Target target) {
        try {
            StringBuilder builder = new StringBuilder();
            Files.readAllLines(Path.of(String.format("%s%s.json", ROOT, target))).forEach(builder::append);
            return Arrays.asList(gson.fromJson(builder.toString(), Synergy[].class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveSynergies(Target target) {
        List<Synergy> synergies = getSynergiesFromWeb(target);
        try {
            Path path = Path.of(String.format("%s%s.json", ROOT, target));
            if (Files.exists(path)) Files.delete(path);
            Files.writeString(path, gson.toJson(synergies));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Synergy> getSynergiesFromWeb(Target target) {
        System.setProperty("webdriver.chrome.driver", String.format("%schromedriver.exe", ROOT));
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(String.format("https://app.mobalytics.gg/tft/set9/synergies/%s", target));

        List<Synergy> synergies = getRows(getContainer(driver))
                .stream()
                .filter(row -> !getName(row).isBlank())
                .map(Main::toSynergy)
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

    private static String getName(WebElement row) {
        return row.findElement(By.xpath("div[1]/div[1]/div/p/span")).getText();
    }

    private static List<WebElement> getLevels(WebElement row) {
        return row.findElements(By.xpath("div[1]/div[2]/div/div"));
    }

    private static List<WebElement> getChampions(WebElement row) {
        return row.findElements(By.xpath("div[2]/div"));
    }

    private static Integer getLevel(WebElement level) {
        return Integer.valueOf(level.findElement(By.xpath("p[1]")).getText());
    }

    private static String getChampionName(WebElement champion) {
        return champion.findElement(By.xpath("a/div[2]")).getText();
    }

    private static Synergy toSynergy(WebElement row) {
        String name = getName(row);
        List<Integer> levels = getLevels(row)
                .stream()
                .map(Main::getLevel)
                .collect(Collectors.toList());
        List<String> champions = getChampions(row)
                .stream()
                .map(Main::getChampionName)
                .collect(Collectors.toList());
        return new Synergy(name, levels, champions);
    }

    @Getter
    @AllArgsConstructor
    static class Synergy {
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
    }

    @Getter
    @AllArgsConstructor
    static class Champion {
        private final String name;
        private final List<String> classes;
        private final List<String> origins;

        @Override
        public String toString() {
            return gson.toJson(this);
        }
    }

    private enum Target {
        CLASSES ("classes"),
        ORIGINS ("origins");

        private final String postfix;

        Target(String postfix) {
            this.postfix = postfix;
        }

        @Override
        public String toString() {
            return postfix;
        }
    }

    private static final String RESOURCES = "\\src\\main\\resources\\";

    private static final String ROOT = FileSystems.getDefault()
            .getPath("")
            .normalize()
            .toAbsolutePath()
            .toString()
            .concat(RESOURCES);
}
