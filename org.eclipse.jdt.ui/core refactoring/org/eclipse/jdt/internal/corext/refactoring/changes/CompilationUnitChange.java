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
package org.eclipse.jdt.internal.corext.refactoring.changes;

import org.eclipse.text.edits.UndoEdit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jdt.core.ICompilationUnit;

import org.eclipse.jface.text.IDocument;

import org.eclipse.jdt.internal.corext.Assert;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.ltk.core.refactoring.Change;

public class CompilationUnitChange extends TextFileChange {

	private ICompilationUnit fCUnit;

	/**
	 * Creates a new <code>CompilationUnitChange</code>.
	 * 
	 * @param name the change's name mainly used to render the change in the UI
	 * @param cunit the compilation unit this text change works on
	 */
	public CompilationUnitChange(String name, ICompilationUnit cunit) {
		super(name, getFile(cunit));
		Assert.isNotNull(cunit);
		fCUnit= cunit;
		setTextType("java"); //$NON-NLS-1$
	}
	
	private static IFile getFile(ICompilationUnit cunit) {
		return (IFile) cunit.getResource();
	}
	
	/* non java-doc
	 * Method declared in IChange.
	 */
	public Object getModifiedElement(){
		return fCUnit;
	}
	
	/**
	 * Returns the compilation unit this change works on.
	 * 
	 * @return the compilation unit this change works on
	 */
	public ICompilationUnit getCompilationUnit() {
		return fCUnit;
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected IDocument aquireDocument(IProgressMonitor pm) throws CoreException {
		pm.beginTask("", 2); //$NON-NLS-1$
		fCUnit.becomeWorkingCopy(null, new SubProgressMonitor(pm, 1));
		return super.aquireDocument(new SubProgressMonitor(pm, 1));
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void releaseDocument(IDocument document, IProgressMonitor pm) throws CoreException {
		super.releaseDocument(document, pm);
		fCUnit.discardWorkingCopy();
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected Change createUndoChange(UndoEdit edit) throws CoreException {
		return new UndoCompilationUnitChange(getName(), fCUnit, edit, getSaveMode());
	}
}

