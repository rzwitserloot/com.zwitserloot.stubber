package com.zwitserloot.stubber.model;

import java.util.Collection;

import com.zwitserloot.stubber.reader.DependencySweeper;

import lombok.experimental.Value;

@Value
public class FieldModel {
	private final String name, desc, signature;
	
	public void addTypeNamesOfSignature(Collection<String> types) {
		DependencySweeper.typesInSignature(types, desc);
		DependencySweeper.typesInSignature(types, signature);
	}
}
