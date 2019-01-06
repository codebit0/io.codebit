package io.codebit.support.ignite;

import io.codebit.support.util.Config;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

/**
 * Created by bootcode on 2018-07-04.
 */
public class IgniteProvider {

    static {
        Config config = Config.of("classpath://config/config.yaml");
        String file = config.getString("support.ignite.config.file", null);
        if(file != null) {
            IGNITE = Ignition.start(file);
        }else {
            IGNITE = Ignition.start();
        }
    }

    private static Ignite IGNITE;

    private IgniteProvider() {
    }

    public static Ignite connect() {
        return IGNITE;
    }

    public static boolean close() {
        return Ignition.stop(true);
    }

}
