package com.zwitserloot.stubber.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import lombok.Cleanup;
import lombok.val;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

/**
 * Creates a jar file containing just stubs for a given list of types. Only public/protected members
 * are included.
 */
public class StubJarWriter {
	private static boolean isVisible(int access) {
		return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0;
	}
	
	private final ClassLoader cl;
	
	public StubJarWriter() {
		this(ClassLoader.getSystemClassLoader());
	}
	
	public StubJarWriter(ClassLoader cl) {
		this.cl = cl;
	}
	
	private static class Dir {
		private final Map<String, Dir> entries = new HashMap<String, Dir>();
	}
	
	public void write(Collection<String> types, String fileName) throws IOException {
		Dir root = new Dir();
		for (String type : types) {
			String[] entries = type.split("/");
			int pos = 0;
			Dir cur = root;
			while (pos < entries.length - 1) {
				Dir child = cur.entries.get(entries[pos]);
				if (child == null) {
					child = new Dir();
					cur.entries.put(entries[pos], child);
				}
				cur = child;
				pos++;
			}
		}
		
		@Cleanup val fos = new FileOutputStream(fileName);
		@Cleanup val out = new JarOutputStream(fos);
		
		createAllDirs(out, "", root);
		
		for (String type : types) {
			@Cleanup val in = cl.getResourceAsStream(type + ".class");
			if (in == null) continue;
			byte[] classData = ByteStreams.toByteArray(in);
			in.close();
			out.putNextEntry(new ZipEntry(type + ".class"));
			writeStub(out, classData);
		}
	}
	
	private static final Map<Character, int[]> FOO = ImmutableMap.<Character, int[]>builder()
			.put('V', new int[] {Opcodes.NOP, Opcodes.RETURN})
			.put('[', new int[] {Opcodes.ACONST_NULL, Opcodes.ARETURN})
			.put('L', new int[] {Opcodes.ACONST_NULL, Opcodes.ARETURN})
			.put('I', new int[] {Opcodes.ICONST_0, Opcodes.IRETURN})
			.put('S', new int[] {Opcodes.ICONST_0, Opcodes.IRETURN})
			.put('B', new int[] {Opcodes.ICONST_0, Opcodes.IRETURN})
			.put('Z', new int[] {Opcodes.ICONST_0, Opcodes.IRETURN})
			.put('C', new int[] {Opcodes.ICONST_0, Opcodes.IRETURN})
			.put('J', new int[] {Opcodes.LCONST_0, Opcodes.LRETURN})
			.put('F', new int[] {Opcodes.FCONST_0, Opcodes.FRETURN})
			.put('D', new int[] {Opcodes.DCONST_0, Opcodes.DRETURN})
			.build();
	
	private void writeStub(JarOutputStream out, byte[] classData) throws IOException {
		ClassWriter cw = new ClassWriter(0);
		new ClassReader(classData).accept(new ClassVisitor(Opcodes.ASM4, cw) {
			@Override public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
				if (isVisible(access)) {
					return super.visitField(access, name, desc, signature, value);
				}
				return null;
			}
			
			@Override public MethodVisitor visitMethod(int access, String name, final String desc, String signature, String[] exceptions) {
				if (isVisible(access)) {
					return new MethodVisitor(Opcodes.ASM4, super.visitMethod(access, name, desc, signature, exceptions)) {
						public void visitEnd() {
							char returnType = desc.charAt(desc.indexOf(')') + 1);
							for (int code : FOO.get(returnType)) {
								if (code != Opcodes.NOP) visitInsn(code);
							}
							super.visitEnd();
						}
					};
				}
				return null;
			}
		}, ClassReader.SKIP_CODE);
		out.write(cw.toByteArray());
	}
	
	private void createAllDirs(JarOutputStream out, String prefix, Dir dir) throws IOException {
		for (val e : dir.entries.entrySet()) {
			String dirname = e.getKey();
			Dir subdirs = e.getValue();
			String fullname = prefix + (prefix.isEmpty() ? "" : "/") + dirname;
			out.putNextEntry(new ZipEntry(fullname));
			createAllDirs(out, fullname, subdirs);
		}
	}
}
