package net.dtl.citizens.trader;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.configuration.file.FileConfiguration;

public class LoggingManager {
	//config file separator
	private final static char PATH_SEPARATOR = '/';
	
	//settings and files
	protected boolean separateFiles;
	protected File logFile;
	
	
	//constructor
	public LoggingManager()
	{	
		//config file
		FileConfiguration config = CitizensTrader.getInstance().getConfig();
		
		
		//filename
		String fileName = null;
		
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("dd_MM_yyyy");
		
		// Default settings
		if ( fileName == null ) 
		{
			fileName = df.format(date) + "_log.txt";
		}

		String baseDir = config.getString("logging.basedir", "plugins\\DtlCitizensTrader\\logs" );
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

			writer.append(logString+"\n");
			writer.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	
}
