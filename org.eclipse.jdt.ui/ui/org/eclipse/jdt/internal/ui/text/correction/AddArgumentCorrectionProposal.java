/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.text.correction;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTRewrite;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.dom.TypeRules;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

public class AddArgumentCorrectionProposal extends ASTRewriteCorrectionProposal {

	private List fArguments;
	private int[] fInsertIndexes;
	private ITypeBinding[] fParamTypes;
	private ASTNode fNameNode;

	public AddArgumentCorrectionProposal(String label, ICompilationUnit cu, ASTNode nameNode, List arguments, int[] insertIdx, ITypeBinding[] expectedTypes, int relevance) {
		super(label, cu, null, relevance, JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE)); //$NON-NLS-1$
		fArguments= arguments;
		fNameNode= nameNode;
		fInsertIndexes= insertIdx;
		fParamTypes= expectedTypes;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.correction.ASTRewriteCorrectionProposal#getRewrite()
	 */
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast= fNameNode.getAST();
		ASTRewrite rewrite= new ASTRewrite(fNameNode.getParent());

		for (int i= 0; i < fInsertIndexes.length; i++) {
			int idx= fInsertIndexes[i];
			Expression newArg= getArgumentExpresion(ast, fParamTypes[idx]);
			rewrite.markAsInserted(newArg);
			fArguments.add(idx, newArg);
		}
		return rewrite;
	}
	
	private Expression getArgumentExpresion(AST ast, ITypeBinding requiredType) {
		CompilationUnit root= (CompilationUnit) fNameNode.getRoot();

		int offset= fNameNode.getStartPosition();
		
		ScopeAnalyzer analyzer= new ScopeAnalyzer(root);
		IBinding[] bindings= analyzer.getDeclarationsInScope(offset, ScopeAnalyzer.VARIABLES);
		for (int i= 0; i < bindings.length; i++) {
			IVariableBinding curr= (IVariableBinding) bindings[i];
			ITypeBinding type= curr.getType();
			if (type != null && TypeRules.canAssign(type, requiredType) && testModifier(curr)) {
				return ast.newSimpleName(curr.getName());
			}
		}
		return ASTNodeFactory.newDefaultExpression(ast, requiredType);
	}
	
	private boolean testModifier(IVariableBinding curr) {
		int modifiers= curr.getModifiers();
		int staticFinal= Modifier.STATIC | Modifier.FINAL;
		if ((modifiers & staticFinal) == staticFinal) {
			return false;
		}
		if (Modifier.isStatic(modifiers) && !ASTResolving.isInStaticContext(fNameNode)) {
			return false;
		}
		return true;
	}	
}
