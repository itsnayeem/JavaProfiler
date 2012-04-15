package tracer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class TracerAgent {
	private static Instrumentation instrumentation;

	public static void premain(String args, Instrumentation inst) throws Exception {
		instrumentation = inst;
		instrumentation.addTransformer(new MyClassFileTransformer());
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
	}

	public static class MyClassFileTransformer implements ClassFileTransformer {
		public byte[] transform(ClassLoader l, String className, Class<?> c, ProtectionDomain pd, byte[] b)
				throws IllegalClassFormatException {
			if (className.startsWith("java") || className.startsWith("sun") || className.startsWith("com/sun")
					|| className.startsWith("com/apple") || className.startsWith("tracer")
					|| className.startsWith("misc") || className.startsWith("$")) {
				return b;
			}
			ClassReader cr = new ClassReader(b);
			ClassWriter cw = new ClassWriter(0);
			ClassVisitor cv = new MyClassVisitor(cw);
			cr.accept(cv, ClassReader.EXPAND_FRAMES);
			return cw.toByteArray();
		}
	}

	public static class ShutdownHook implements Runnable {
		public void run() {
			Profiler.printResults();
		}

	}

}