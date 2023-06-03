package mobaprocessor.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Getter
@AllArgsConstructor
public class Composition {

    private final Set<String> champions;

    private final Map<String, Integer> synergies;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Composition that = (Composition) o;
        return champions.equals(that.champions) && synergies.equals(that.synergies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(champions, synergies);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("champions", champions)
                .append("synergies", synergies)
                .toString();
    }
}
