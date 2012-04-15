package tracer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import misc.FrequencySet;
import misc.MutableInt;

public class Profiler {

	public static HashMap<String, Stack<Long>> startTimes = new HashMap<String, Stack<Long>>();
	public static HashMap<String, Long> totalTimes = new HashMap<String, Long>();
	public static FrequencySet<String> invocations = new FrequencySet<String>();
	public static FrequencySet<String> instantiations = new FrequencySet<String>();

	public static void start(String className, String methodName) {
		String path = className + "." + methodName;
		Stack<Long> times = startTimes.get(path);
		if (times == null) {
			times = new Stack<Long>();
			startTimes.put(path, times);
		}
		times.push(System.nanoTime());
		invocations.add(path);
	}

	public static void end(String className, String methodName) {
		String path = className + "." + methodName;

		Long prev = totalTimes.get(path);
		if (prev == null) {
			prev = new Long(0);
		}
		Stack<Long> times = startTimes.get(path);
		long stime = times.pop();

		Long diff = System.nanoTime() - stime;
		long total = prev + diff;
		totalTimes.put(path, total);
	}

	public static void inc(String className) {
		instantiations.add(className);
	}

	public static void incArray() {
		instantiations.add("Array");
		
	}

	public static void printResults() {
		System.out.println("\nTimes:");
		ArrayList<TimeElem> times = new ArrayList<TimeElem>();
		for (String s : totalTimes.keySet()) {
			times.add(new TimeElem(s, (totalTimes.get(s) / 1000000), invocations.get(s)));
		}
		Collections.sort(times);
		for (TimeElem t : times) {
			System.out.println(t);
		}

		System.out.println("\nInstantiations:");
		ArrayList<FreqElem> freqs = new ArrayList<FreqElem>();
		for (String s : instantiations.keySet()) {
			freqs.add(new FreqElem(s, instantiations.get(s)));
		}
		Collections.sort(freqs);
		for (FreqElem f : freqs) {
			System.out.println(f);
		}

	}

	private static class FreqElem implements Comparable<FreqElem> {
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
			return MethodPath + " " + freq;
		}
	}

	private static class TimeElem implements Comparable<TimeElem> {
		private String MethodPath;
		private long time;
		private int num;

		public TimeElem(String m, Long t, MutableInt n) {
			MethodPath = m;
			time = t;
			num = n.intValue();
		}

		@Override
		public int compareTo(TimeElem t) {
			if (time < t.time) {
				return 1;
			} else if (time > t.time) {
				return -1;
			}
			return 0;
		}

		public String toString() {
			return MethodPath + " " + time + "ms " + num;
		}
	}
}
