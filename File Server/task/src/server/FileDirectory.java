package server;

import java.io.File;
import java.util.Objects;

public class FileDirectory {

    //public static final String FILES_DIR = "data/";
   /* public static final String FILES_CANONICAL_PATH =
            Objects.requireNonNull(FileDirectory.class.getResource("")).getFile()
                    .replaceAll("out/production/classes/server/", "src/server/data/")
                    .replaceAll("%20", " ");*/

    public static final String filesPath = System.getProperty("user.dir") +
            File.separator + "src" + File.separator + "server" + File.separator + "data" + File.separator;

    public static final String mapPath = filesPath + "map.bin";

}
