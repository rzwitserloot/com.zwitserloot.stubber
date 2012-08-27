package com.zwitserloot.stubber;

import static org.junit.Assert.assertEquals;
import lombok.val;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.zwitserloot.stubber.reader.DependencySweeper;

public class TestSigParsing {
	@Test
	public void testInnerClassSigWithGenerics() {
		val in = DependencySweeper.typesInSignature("Lcom/intellij/refactoring/changeSignature/ChangeSignatureDialogBase<TP;TM;TD;>.UpdateSignatureListener;");
		assertEquals(ImmutableSet.of(
				"com/intellij/refactoring/changeSignature/ChangeSignatureDialogBase", 
				"com/intellij/refactoring/changeSignature/ChangeSignatureDialogBase$UpdateSignatureListener"), in);
	}
}
