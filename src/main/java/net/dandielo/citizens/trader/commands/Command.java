package net.dandielo.citizens.trader.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) 
public @interface Command {
	String name();
	String syntax() default "";
	String desc() default "";
	String usage() default "";
	String perm() default "";
	boolean npc() default true;
}
