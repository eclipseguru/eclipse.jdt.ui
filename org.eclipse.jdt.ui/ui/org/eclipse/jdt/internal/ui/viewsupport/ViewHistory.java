/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.viewsupport;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;


/**
 * 
 */
public abstract class ViewHistory {

	/**
	 * Configure the history List action.
	 * Clients typically want to set a text and an image.
	 * 
	 * @param action the action
	 */
	public abstract void configureHistoryListAction(IAction action);
	
	/**
	 * Configure the history drop down action.
	 * Clients typically want to set a tooltip and an image.
	 * 
	 * @param action the action
	 */
	public abstract void configureHistoryDropDownAction(IAction action);
	
	public abstract String getHistoryListDialogTitle();

	public abstract String getHistoryListDialogMessage();

	public abstract Shell getShell();
	
	
	/**
	 * @return an unmodifiable list of history entries, can be empty
	 */
	public abstract List getHistoryEntries();
	
	/**
	 * @return the active entry from the history
	 */
	public abstract Object getCurrentEntry();
	
	/**
	 * @param entry the entry to activate, or <code>null</code> if none should be active
	 */
	public abstract void setActiveEntry(Object entry);
	
	/**
	 * @param remainingEntries all the remaining history entries, can be empty
	 * @param activeEntry the entry to activate, or <code>null</code> if none should be active
	 */
	public abstract void setHistoryEntries(List remainingEntries, Object activeEntry);
	
	/**
	 * @param element the element to render
	 * @return the image descriptor for the given element, or <code>null</code>
	 */
	public abstract ImageDescriptor getImageDescriptor(Object element);
	
	/**
	 * @param element the element to render
	 * @return the label text for the given element
	 */
	public abstract String getText(Object element);
	
	public final IAction createHistoryDropDownAction() {
		return new HistoryDropDownAction(this);
	}

	public abstract String getMaxEntriesMessage();
	public abstract int getMaxEntries();
	public abstract void setMaxEntries(int maxEntries);

}
