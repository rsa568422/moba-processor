package mobaprocessor.utils;

import mobaprocessor.entities.Champion;
import mobaprocessor.entities.Composition;
import mobaprocessor.entities.Synergy;
import mobaprocessor.entities.Target;
import mobaprocessor.io.file.SynergyFileUtils;
import org.paukov.combinatorics3.Generator;

import java.util.*;
import java.util.stream.Collectors;

public class SynergyUtils {

    private SynergyUtils() {}

    public static void processSynergies(Integer size) {
        if (!SynergyFileUtils.exist(Target.ORIGINS)) SynergyFileUtils.saveSynergies(Target.ORIGINS);
        if (!SynergyFileUtils.exist(Target.CLASSES)) SynergyFileUtils.saveSynergies(Target.CLASSES);

        List<Synergy> classes = SynergyFileUtils.readSynergies(Target.CLASSES);
        List<Synergy> origins = SynergyFileUtils.readSynergies(Target.ORIGINS);

        List<Champion> champions = Champion.getChampions(classes, origins);
        Map<String, List<Integer>> synergyLevels = SynergyUtils.getSynergyLevels(classes, origins);
        Map<String, List<String>> synergyChampions = SynergyUtils.getSynergyChampions(classes, origins);

        List<Composition> top = Generator.combination(champions)
                .simple(size)
                .stream()
                .map(championNames -> getComposition(championNames, synergyChampions, synergyLevels))
                .sorted((c1, c2) -> value(c2).compareTo(value(c1)))
                .limit(10)
                .collect(Collectors.toList());

        top.forEach(System.out::println);
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

    private static Composition getComposition(List<Champion> champions,
                                              Map<String, List<String>> synergyChampions,
                                              Map<String, List<Integer>> synergyLevels) {
        Set<String> set = champions.stream()
                .map(Champion::getName)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Integer> synergies = new LinkedHashMap<>();
        champions.forEach(champion -> {
            champion.getClasses().forEach(synergy -> increment(champion.getName(), synergy, synergies, synergyChampions));
            champion.getOrigins().forEach(synergy -> increment(champion.getName(), synergy, synergies, synergyChampions));
        });
        List<String> toRemove = new ArrayList<>();
        synergies.forEach((synergy, quantity) -> {
            List<Integer> levels = synergyLevels.get(synergy);
            if (quantity >= levels.stream().min(Integer::compareTo).orElse(1)) {
                int finalValue = 1;
                for (Integer level : levels) {
                    if (quantity >= level) finalValue = level;
                }
                synergies.replace(synergy, finalValue);
            } else {
                toRemove.add(synergy);
            }
        });
        toRemove.forEach(synergies::remove);
        return new Composition(set, synergies);
    }

    private static void increment(String champion, String synergy,
                                  Map<String, Integer> synergies,
                                  Map<String, List<String>> synergyChampions) {
        if (synergyChampions.get(synergy).contains(champion)) {
            if (synergies.containsKey(synergy)) {
                synergies.replace(synergy, synergies.get(synergy) + 1);
            } else {
                synergies.put(synergy, 1);
            }
        }
    }

    private static Integer value(Composition composition) {
        return composition.getSynergies().values().stream().reduce(0, Integer::sum);
    }
}
