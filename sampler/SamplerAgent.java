package sampler;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import misc.FrequencySet;
import misc.MutableInt;

public class SamplerAgent {
	private static FrequencySet<String> samples = new FrequencySet<String>();

	public static void premain(String args, Instrumentation inst) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
		Sampler s = new Sampler();
		Thread t = new Thread(s);
		t.start();
	}

	public static class ShutdownHook implements Runnable {
		public void run() {
			System.out.println("\nFrequencies:");
			ArrayList<FreqElem> freqs = new ArrayList<FreqElem>();
			for (String s : samples.keySet()) {
				freqs.add(new FreqElem(s, samples.get(s)));
				FreqElem.count += samples.get(s).intValue();
			}
			Collections.sort(freqs);
			for (FreqElem f : freqs) {
				System.out.println(f);
			}
		}

	}

	public static class Sampler implements Runnable {
		@Override
		public void run() {
			Set<Thread> threads = Thread.getAllStackTraces().keySet();
			Thread main = null;
			for (Thread t : threads) {
				if (t.getName().equals("main")) {
					main = t;
					break;
				}
			}
			while (main.isAlive()) {
				StackTraceElement[] stackTraces = main.getStackTrace();
				StackTraceElement elem = null;
				boolean found = false;
				for (StackTraceElement s : stackTraces) {
					if (!s.getClassName().startsWith("java") && !s.getClassName().startsWith("sun.")
							&& !s.getClassName().startsWith("com.sun")) {
						found = true;
						elem = s;
						break;
					}
				}
				if (elem != null) {
					String MethodPath = elem.getClassName() + "." + elem.getMethodName();
					samples.add(MethodPath);
					try {
						Thread.sleep(1);
					} catch (Exception e) {
						System.out.println("this can never be printed");
					}
				} else {
					if (found == true) {
						break;
					}
				}
			}
		}
	}

	private static class FreqElem implements Comparable<FreqElem> {
		public static int count;
		private String MethodPath;
		private int freq;

		public FreqElem(String m, MutableInt mutableInt) {
			MethodPath = m;
			freq = mutableInt.intValue();
		}

		@Override
		public int compareTo(FreqElem f) {
			if (freq < f.freq) {
				return 1;
			} else if (freq > f.freq) {
				return -1;
			}
			return 0;
		}

		public String toString() {
			return MethodPath + ": " + freq + "(" + ((double)((int)((double)freq * 10000/count))/100) + "%)";
		}
	}

}
