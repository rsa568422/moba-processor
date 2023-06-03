package mobaprocessor.io.file;

import mobaprocessor.entities.Synergy;
import mobaprocessor.entities.Target;
import mobaprocessor.io.web.SynergyWebUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static mobaprocessor.Constants.*;

public class SynergyFileUtils {

    private SynergyFileUtils() {}

    public static List<Synergy> readSynergies(Target target) {
        try {
            StringBuilder builder = new StringBuilder();
            Files.readAllLines(getPath(target)).forEach(builder::append);
            return Arrays.asList(gson.fromJson(builder.toString(), Synergy[].class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSynergies(Target target) {
        List<Synergy> synergies = SynergyWebUtils.getSynergiesFromWeb(target);
        try {
            Path path = getPath(target);
            if (exist(target)) Files.delete(path);
            Files.writeString(path, gson.toJson(synergies));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean exist(Target target) {
        return Files.exists(Path.of(String.format("%s%s.json", ROOT, target)));
    }

    private static Path getPath(Target target) {
        return Path.of(String.format("%s%s.json", ROOT, target));
    }
}
