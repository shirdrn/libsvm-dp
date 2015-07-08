package org.shirdrn.document.preprocessing.utils;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	private String poolName = "POOL";
	private DecimalFormat formatter = new DecimalFormat("000");

	public NamedThreadFactory(String poolName) {
		if(poolName != null && !poolName.trim().isEmpty()) {
			this.poolName = poolName;
		}
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
	}

	public NamedThreadFactory() {
		this("POOL");
	}

	@Override
	public Thread newThread(Runnable r) {
		String threadName = poolName + "-" + formatter.format(threadNumber.getAndIncrement());
		Thread t = new Thread(group, r, threadName, 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

}