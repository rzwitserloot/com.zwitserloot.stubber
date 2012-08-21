package com.zwitserloot.stubber.model;

import java.util.Collection;
import java.util.List;

import lombok.experimental.Value;

import com.zwitserloot.stubber.reader.DependencySweeper;

@Value
public class MethodModel {
	private final String name, desc, signature;
	private final List<String> exceptions;
	
	public void addTypeNamesOfSignature(Collection<String> types) {
		DependencySweeper.typesInSignature(types, desc);
		DependencySweeper.typesInSignature(types, signature);
		if (exceptions != null) types.addAll(exceptions);
	}
}
