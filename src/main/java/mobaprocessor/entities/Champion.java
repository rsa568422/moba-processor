package mobaprocessor.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import mobaprocessor.Constants;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class Champion {
    private final String name;
    private final List<String> classes;
    private final List<String> origins;

    @Override
    public String toString() {
        return Constants.gson.toJson(this);
    }

    public static List<Champion> getChampions(List<Synergy> classes, List<Synergy> origins) {
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
}
