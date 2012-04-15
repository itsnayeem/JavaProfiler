package tracer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

public class MyClassVisitor extends ClassVisitor {
	private String className;

	public MyClassVisitor(ClassVisitor cv) {
		super(Opcodes.ASM4, cv);
	}

	public void visit(final int version, final int access, final String name, final String signature,
			final String superName, final String[] interfaces) {

		super.visit(version, access, name, signature, superName, interfaces);

		this.className = name.replace("/", ".");
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

		if (mv == null) {
			return null;
		}
		return new Timer(mv, access, name, desc);
	}

	private class Timer extends AdviceAdapter {
		private String methodName;

		public Timer(final MethodVisitor mv, final int access, final String name, final String desc) {
			super(ASM4, mv, access, name, desc);
			this.methodName = name;
		}

		@Override
		protected void onMethodEnter() {
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methodName);
			mv.visitMethodInsn(INVOKESTATIC, "tracer/Profiler", "start", "(Ljava/lang/String;Ljava/lang/String;)V");
			super.onMethodEnter();
		}

		protected void onMethodExit(int opcode) {
			mv.visitLdcInsn(className);
			mv.visitLdcInsn(methodName);
			mv.visitMethodInsn(INVOKESTATIC, "tracer/Profiler", "end", "(Ljava/lang/String;Ljava/lang/String;)V");
			super.onMethodExit(opcode);
		}

		@Override
		public void visitMaxs(int stack, int locals) {
			super.visitMaxs(stack + 2, locals);
		}

		public void visitTypeInsn(int opcode, String type) {
			if (opcode == Opcodes.NEW) {
				mv.visitLdcInsn(type);
				mv.visitMethodInsn(INVOKESTATIC, "tracer/Profiler", "inc", "(Ljava/lang/String;)V");
			}
			super.visitTypeInsn(opcode, type);
		}
	}
}
