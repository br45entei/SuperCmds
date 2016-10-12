package com.gmail.br45entei.supercmds.util;

/** Utility class used for reflection and NMS
 * 
 * @author Brian_Entei */
public class VersionUtil {
	
	/** Finds the first package which contains the NMS version, and returns the
	 * version found.
	 * 
	 * @return The current NMS version */
	public static final String getNMSVersion() {
		//String version = null;
		for(Package p : Package.getPackages()) {
			String name = p.getName();
			if(name.startsWith("net.minecraft.server.v")) {
				return name.replace("net.minecraft.server.", "");//version = name.replace("net.minecraft.server.", "");
				//break;
			}
		}
		return null;//version;
		/*String version = null;
		Pattern pat = Pattern.compile("net\\.minecraft\\.(?:server)?\\.(v(?:\\d_)+R\\d)");
		for(Package p : Package.getPackages()) {
			String name = p.getName();
			Matcher m = pat.matcher(name);
			if(m.matches()) version = m.group(1);
		}
		return version;*/
	}
	
	/** Retrieves the class from the argument by the fully qualified
	 * name given the NMS version.
	 * 
	 * @param nmsClass The class to retrieve
	 * @return The class if found, {@code null} otherwise */
	public static final Class<?> getNMSClass(String nmsClass) {
		final String version = getNMSVersion();
		try {
			return version != null ? Class.forName(String.format(nmsClass, version)) : null;
		} catch(ClassNotFoundException e) {
			return null;
		}
	}
	
}
