package com.zwitserloot.stubber.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.zwitserloot.stubber.reader.DependencySweeper;

import lombok.experimental.Value;

@Value
public class ClassFile {
	private final Collection<String> parents;
	private final String name;
	private final Collection<FieldModel> fields;
	private final Collection<MethodModel> methods;
	private final String signature;
	
	public Set<String> getTypeNamesInSignatures() {
		Set<String> set = new HashSet<String>();
		addTypeNamesInSignatures(set);
		return set;
	}
	
	public void addTypeNamesInSignatures(Collection<String> types) {
		DependencySweeper.typesInSignature(types, signature);
		types.addAll(parents);
		for (FieldModel fm : fields) fm.addTypeNamesOfSignature(types);
		for (MethodModel mm : methods) mm.addTypeNamesOfSignature(types);
	}
}
