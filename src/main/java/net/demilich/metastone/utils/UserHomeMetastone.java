package net.demilich.metastone.utils;

import net.demilich.metastone.BuildConfig;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

/**
 * Singleton data class that holds the platform specific path to the metastone user home dir.
 */
public class UserHomeMetastone {

    private static UserHomeMetastone INSTANCE;
    private static String ENV_VAR_NAME = "USER_HOME_METASTONE";

    static {
        String metastoneHomeDirPath = System.getenv(ENV_VAR_NAME);
        
        // if we dont have an ENV variable set for USER_HOME_METASTONE, then use the default user directory
        if ((metastoneHomeDirPath == null || metastoneHomeDirPath.isEmpty())) {
            metastoneHomeDirPath = (FileSystemView.getFileSystemView().getDefaultDirectory().getPath()
                    + File.separator + BuildConfig.NAME).replace("\\", "\\\\");
        }

    	UserHomeMetastone.init(metastoneHomeDirPath);
    }
    
    
    private String dirPath;

    private UserHomeMetastone(String path) {
        dirPath = path;
    }

    private static void init(String path) {
        if(path == null) {
            throw new NullPointerException("UserHomeMetastone.init(path) cannot be initialized with null!");
        }

        if (INSTANCE == null) {
            INSTANCE = new UserHomeMetastone(path);
        } else {
            INSTANCE.dirPath = path;
        }
    }

    public static String getPath() {
        if (INSTANCE == null) {
            throw new RuntimeException("UserHomeMetastone must first be initialized!");
        }

        return INSTANCE.dirPath;
    }
}