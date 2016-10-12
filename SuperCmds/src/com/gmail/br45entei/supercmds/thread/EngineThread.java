/**
 * 
 */
package com.gmail.br45entei.supercmds.thread;

import com.gmail.br45entei.supercmds.Main;
import com.gmail.br45entei.supercmds.util.CodeUtils;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/** @author Brian_Entei */
@SuppressWarnings("javadoc")
public abstract class EngineThread extends Thread implements Listener {
	public final JavaPlugin	parentPlugin;
	private boolean			isRunning	= false;
	protected final int		runRate;
	
	/** Allocates a new {@code Thread} object. This constructor has the same
	 * effect as {@link java.lang.Thread#Thread(ThreadGroup,Runnable,String)
	 * Thread} {@code (null, null, name)}.
	 * 
	 * @param name the name of the new thread
	 * @param runRate The rate at which this thread will run(per second ?),
	 *            default is 60. */
	public EngineThread(JavaPlugin plugin, String name, int runRate) {
		super(name);
		this.parentPlugin = plugin;
		this.runRate = runRate;
		Main.server.getPluginManager().registerEvents(this, this.parentPlugin);
	}
	
	@Override
	public final synchronized void start() {
		if(this.isRunning) {
			return;
		}
		this.isRunning = true;
		super.start();
	}
	
	/** Stops this thread. Recommended over interrupting the thread. */
	public final void stopThread() {
		this.isRunning = false;
	}
	
	/** @return Whether or not this thread is running */
	public final boolean isRunning() {
		return this.isRunning && this.isAlive();
	}
	
	@Override
	public final void run() {
		final double frameTime = 1.0D / this.runRate;
		double lastTime = Time.getTime();
		double unprocessedTime = 0;
		while(this.isRunning()) {
			boolean run = false;
			double startTime = Time.getTime();
			double passedTime = startTime - lastTime;
			lastTime = startTime;
			unprocessedTime += passedTime;
			while(unprocessedTime > frameTime) {
				run = true;
				unprocessedTime -= frameTime;
				if(!this.isRunning()) {
					break;
				}
			}
			if(!this.isRunning()) {
				break;
			}
			if(run && this.isRunning()) {
				this.execute();
			} else {
				CodeUtils.threadSleep(this.runRate, true);
			}
		}
		this.cleanup();
		this.stopThread();
	}
	
	/** The code to be executed at the rate of this thread. */
	public abstract void execute();
	
	/** The code to be executed when this thread is shutting down. */
	public abstract void cleanup();
	
	/** Time class for use in Engine Threads
	 * 
	 * @author Brian_Entei */
	private static final class Time {
		/** One game second(used to divide system nano time) */
		public static final long SECOND = 1000000000L;
		
		/** @return The current time */
		public static final double getTime() {
			return (double) System.nanoTime() / (double) Time.SECOND;
		}
	}
	
}
