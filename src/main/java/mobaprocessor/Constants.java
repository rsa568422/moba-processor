package mobaprocessor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.FileSystems;

public class Constants {
    public static final String RESOURCES = "\\src\\main\\resources\\";

    public static final String ROOT = FileSystems.getDefault()
            .getPath("")
            .normalize()
            .toAbsolutePath()
            .toString()
            .concat(RESOURCES);

    public static final Gson gson = new GsonBuilder().create();
}
