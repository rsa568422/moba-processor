package mobaprocessor;

import mobaprocessor.entities.Champion;
import mobaprocessor.entities.Synergy;
import mobaprocessor.entities.Target;
import mobaprocessor.io.file.SynergyFileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        if (!SynergyFileUtils.exist(Target.ORIGINS)) SynergyFileUtils.saveSynergies(Target.ORIGINS);
        if (!SynergyFileUtils.exist(Target.CLASSES)) SynergyFileUtils.saveSynergies(Target.CLASSES);
        processSynergies(3);
    }

    private static void processSynergies(Integer size) {
        List<Synergy> classes = SynergyFileUtils.readSynergies(Target.CLASSES);
        List<Synergy> origins = SynergyFileUtils.readSynergies(Target.ORIGINS);
        List<Champion> champions = Champion.getChampions(classes, origins);
        Map<String, List<Integer>> synergyLevels = getSynergyLevels(classes, origins);
        Map<String, List<String>> synergyChampions = getSynergyChampions(classes, origins);
        champions.stream().map(Objects::toString).forEach(System.out::println);
        synergyLevels.entrySet()
                .stream()
                .map(entry -> String.format("%s -> levels : %s", entry.getKey(), entry.getValue()))
                .forEach(System.out::println);
        synergyChampions.entrySet()
                .stream()
                .map(entry -> String.format("%s -> champions : %s", entry.getKey(), entry.getValue()))
                .forEach(System.out::println);
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
}
