package net.dandielo.citizens.trader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dandielo.citizens.trader.commands.Command;
import net.dandielo.citizens.trader.commands.TradersExecutor;
import net.dandielo.citizens.trader.commands.core.GeneralCommands;
import net.dandielo.citizens.trader.locale.LocaleManager;
import net.dandielo.citizens.trader.types.tNPC;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandManager {
	private static CitizensTrader plugin = CitizensTrader.getInstance();
	private static LocaleManager locale = CitizensTrader.getLocaleManager();
	
	private TradersExecutor executor;
	
	private Map<CommandSyntax, CommandBinding> commands;
	private Map<Class<?>, Object> objects = new HashMap<Class<?>, Object>();
	
	public CommandManager()
	{
		commands = new HashMap<CommandSyntax, CommandBinding>();
		executor = new TradersExecutor(this);
		
		plugin.getCommand("trader").setExecutor(executor);
		plugin.getCommand("banker").setExecutor(executor);
	}
	
	protected void newInstance(Class<?> clazz)
	{
		try
		{
			objects.put(clazz, clazz.newInstance());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	void registerCommands(Class<?> clazz)
	{
		if ( objects.containsKey(clazz) ) 
			return;
		
		newInstance(clazz);
		
		for ( Method method : clazz.getMethods() )
		{
			Command annotation = method.getAnnotation(Command.class);
			
			if ( annotation != null )
			{
				GeneralCommands.registerCommandInfo(annotation.name(), annotation);
				
			//	CitizensTrader.info("Added new command method");
				CommandSyntax syntax = new CommandSyntax(annotation.name(), annotation.syntax());
				
				commands.put(syntax, new CommandBinding(clazz, method, syntax, annotation));
			}
		}
	}
	
	public boolean execute(String name, CommandSender sender, tNPC tNPC, String[] args)
	{
		for ( Map.Entry<CommandSyntax, CommandBinding> command : commands.entrySet() )
			if ( new CommandSyntax(name, args).equals(command.getKey()) )
			{
				if ( command.getValue().requiresNpc() && tNPC == null )
				{
					locale.sendMessage(sender, "error-npc-not-selected");
					return true;
				}
				else
					return command.getValue().execute(sender, tNPC, args);
			}
		locale.sendMessage(sender, "error-command-invalid");
		return true;
	}
	
	private static class CommandSyntax
	{
		private static final Pattern commandPattern = Pattern.compile("(<([^<>]*)>)|([ ]*\\(([^\\(\\)]*)\\))|([ ]*\\{([^\\{\\}]*)\\})");
		
		private List<String> argumentNames = new ArrayList<String>();
		private String name;
		private String originalSyntax;
		private Pattern syntax;
				
		public CommandSyntax(String name, String[] args) 
		{
			this.name = name;
			originalSyntax = name + " " + toString(args);
		}

		public CommandSyntax(String name, String args) 
		{
			this.name = name;
			originalSyntax = args;
			String syntax = name + " " + originalSyntax;
			
			Matcher matcher = commandPattern.matcher(originalSyntax);
			while(matcher.find())
			{
				if ( matcher.group(1) != null )
				{
					argumentNames.add(matcher.group(2));
					syntax = syntax.replace(matcher.group(1), "(\\S+)");
				}
				if ( matcher.group(3) != null )
				{
					argumentNames.add(matcher.group(4));
					syntax = syntax.replace(matcher.group(3), "( [\\S]*){0,1}");
				}
				if ( matcher.group(5) != null )
				{
					argumentNames.add(matcher.group(6));
					syntax = syntax.replace(matcher.group(5), "( [\\S\\s]*){0,}");
				}
			}
			this.syntax = Pattern.compile(syntax);
		}
		
		public Map<String, String> listArgs(String group)
		{
			Map<String, String> map = new HashMap<String, String>();
			
			String[] args = group.split(" ", 2);
			
			String free = "";
			for ( String arg : args )
				if ( arg.contains(":") )
					map.put(arg.split(":")[0], arg.split(":")[1]);
				else if ( arg.startsWith("--" ))
					map.put(arg.substring(2), "");
				else
					free += " " + arg;
			
			if ( !free.isEmpty() )
				map.put("free", free.trim());
			
			return map;
		}
		
		public Map<String, String> commandArgs(String[] args)
		{
			Map<String, String> map = new HashMap<String, String>();
			Matcher matcher = syntax.matcher(name + " " + toString(args));
			int max = matcher.groupCount();
			
			matcher.find();
			for ( int i = 0 ; i < max ; ++i )
				if ( matcher.group(i+1) != null && !matcher.group(i+1).trim().isEmpty() )
					if ( argumentNames.get(i).equals("args") )
						map.putAll(listArgs(matcher.group(i+1).trim()));
					else
						map.put(argumentNames.get(i), matcher.group(i+1).trim());
			
			return map;
		}
		
		@Override
		public int hashCode()
		{
			return originalSyntax.hashCode();
		}
		
		@Override 
		public boolean equals(Object o)
		{
			if ( !(o instanceof CommandSyntax) )
				return false;
			return Pattern.matches(((CommandSyntax)o).syntax.pattern(), originalSyntax);
		}
		
		//Utils
		public static String toString(String[] args)
		{
			if ( args.length < 1 )
				return "";
			
			String res = args[0];
			for ( int i = 1 ; i < args.length ; ++i )
				res += " " + args[i];
			return res;
		}
	}
	
	private class CommandBinding
	{
		private Method method; 
		private CommandSyntax syntax;
		private Class<?> clazz;
		private String perm;
		private boolean req;
		
		public CommandBinding(Class<?> clazz, Method method, CommandSyntax syntax, Command cmd) 
		{
			this.clazz = clazz;
			this.method = method;
			this.syntax = syntax;
			this.perm = cmd.perm();
			this.req = cmd.npc(); 
		}
		
		public boolean requiresNpc() {
			return req;
		}
		
		public Boolean execute(CommandSender sender, tNPC tNPC, String[] args)
		{
			if ( !CitizensTrader.getPermissionsManager().has(sender, perm) )
			{
				sender.sendMessage(ChatColor.RED + "You don't have permissions to use this command");
				return true;
			}
			try 
			{
				Object result = method.invoke(objects.get(clazz), plugin, sender, tNPC, syntax.commandArgs(args));
				if ( result instanceof Boolean )
					return (Boolean) result;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return true;
		}
	}
	
	final class ObjectEntry<K, V> implements Map.Entry<K, V> {
	    private final K key;
	    private V value;

	    public ObjectEntry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }

	    @Override
	    public K getKey() {
	        return key;
	    }

	    @Override
	    public V getValue() {
	        return value;
	    }

	    @Override
	    public V setValue(V value) {
	        V old = this.value;
	        this.value = value;
	        return old;
	    }
	}
}
