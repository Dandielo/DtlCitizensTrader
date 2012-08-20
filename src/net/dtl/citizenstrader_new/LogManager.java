package net.dtl.citizenstrader_new;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.configuration.ConfigurationSection;

public class LogManager {
	private final static char PATH_SEPARATOR = '/';
	
	protected boolean separateFiles;

	protected File logFile;

//	private int resetTimeout;
//	private String keepOldFiles;
//	private String singleFile;
	
	public LogManager(ConfigurationSection config) {	

		String fileName = null;
		
//		resetTimeout = config.getInt("general.logs.reset",30);
//		keepOldFiles = config.getString("general.logs.keep-old");
//		singleFile = config.getString("general.logs.single-file");
		
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("dd_MM_yyyy");
		
		// Default settings
		if ( fileName == null ) 
		{
			fileName = df.format(date) + "_log.txt";
			//config.set("general.logs.file", "log.txt");
		}

		String baseDir = config.getString("general.logs.basedir", "dtlTrader\\logs" );// "plugins/PermissionsEx");

		if ( baseDir.contains("\\") && !"\\".equals(File.separator) ) 
		{
			baseDir = baseDir.replace("\\", File.separator);
		}

		File baseDirectory = new File(baseDir);
		if ( !baseDirectory.exists() ) 
		{
			baseDirectory.mkdirs();
		}

		this.logFile = new File(baseDir, fileName);

		if ( !logFile.exists() )
		{
			try 
			{
				logFile.createNewFile();

			} 
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String buildPath(String... path) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		char separator = PATH_SEPARATOR; //permissions.options().pathSeparator();

		for ( String node : path ) 
		{
			if ( !first ) 
			{
				builder.append(separator);
			}

			builder.append(node);

			first = false;
		}

		return builder.toString();
	}
	
	public void log(String logString)
	{
		FileWriter writer = null;
		try {
			writer = new FileWriter(logFile,true);

			if ( writer == null )
				return;
			
			writer.append(logString);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	
}
