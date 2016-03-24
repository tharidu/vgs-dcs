package dcs.group8.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

	public static Properties getProperties(String clsName, String propertiesType) throws ClassNotFoundException {
		Properties prop = new Properties();
		try {

			File jarPath = new File(
					Class.forName(clsName).getProtectionDomain().getCodeSource().getLocation().getPath());
			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			System.out.println(" propertiesPath-" + propertiesPath);
			prop.load(new FileInputStream(propertiesPath + "/resources/" + propertiesType));

			return prop;
		} catch (IOException e1) {
			System.out.println("The requested properties file does not exist or could not be opened!");
			e1.printStackTrace();
			return null;
		}
	}
}
