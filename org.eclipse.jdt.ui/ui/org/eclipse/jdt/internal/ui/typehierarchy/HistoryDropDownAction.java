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
package org.eclipse.jdt.internal.ui.typehierarchy;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;

import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.jdt.core.IJavaElement;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;

public class HistoryDropDownAction extends Action implements IMenuCreator {


	public static final int RESULTS_IN_DROP_DOWN= 10;

	private TypeHierarchyViewPart fHierarchyView;
	private Menu fMenu;
	
	public HistoryDropDownAction(TypeHierarchyViewPart view) {
		fHierarchyView= view;
		fMenu= null;
		setToolTipText(TypeHierarchyMessages.getString("HistoryDropDownAction.tooltip")); //$NON-NLS-1$
		JavaPluginImages.setLocalImageDescriptors(this, "history_list.gif"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IJavaHelpContextIds.TYPEHIERARCHY_HISTORY_ACTION);
		setMenuCreator(this);
	}

	public void dispose() {
		fHierarchyView= null;
		if (fMenu != null) {
			fMenu.dispose();
			fMenu= null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu= new Menu(parent);
		IJavaElement[] elements= fHierarchyView.getHistoryEntries();
		boolean checked= addEntries(fMenu, elements);
		if (elements.length > RESULTS_IN_DROP_DOWN) {
			new MenuItem(fMenu, SWT.SEPARATOR);
			Action others= new HistoryListAction(fHierarchyView);
			others.setChecked(checked);
			addActionToMenu(fMenu, others);
		}
		return fMenu;
	}
	
	private boolean addEntries(Menu menu, IJavaElement[] elements) {
		boolean checked= false;
		
		int min= Math.min(elements.length, RESULTS_IN_DROP_DOWN);
		for (int i= 0; i < min; i++) {
			HistoryAction action= new HistoryAction(fHierarchyView, elements[i]);
			action.setChecked(elements[i].equals(fHierarchyView.getInputElement()));
			checked= checked || action.isChecked();
			addActionToMenu(menu, action);
		}	
		return checked;
	}
	

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	public void run() {
		(new HistoryListAction(fHierarchyView)).run();
	}
}
