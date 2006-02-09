/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation 
 * 			(report 36180: Callers/Callees view)
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.viewsupport;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.util.Assert;

import org.eclipse.jdt.internal.ui.JavaPluginImages;

/*package*/ class HistoryDropDownAction extends Action {

	private class HistoryAction extends Action {
		private final Object fElement;

		public HistoryAction(Object element) {
			Assert.isNotNull(element);
			fElement= element;

			setText(fHistory.getText(element));
			setImageDescriptor(fHistory.getImageDescriptor(element));
		}

		public void run() {
			fHistory.setActiveEntry(fElement);
		}
	}

	private class ClearAction extends Action {
		public ClearAction() {
			setText("&Clear History");
			JavaPluginImages.setLocalImageDescriptors(this, "removea_exc.gif"); //$NON-NLS-1$
		}
		
		public void run() {
			fHistory.setHistoryEntries(Collections.EMPTY_LIST, null);
		}
	}
	
	private class HistoryMenuCreator implements IMenuCreator {

		public Menu getMenu(Menu parent) {
			return null;
		}

		public Menu getMenu(Control parent) {
			if (fMenu != null) {
				fMenu.dispose();
			}
			fMenu= new Menu(parent);
			
			List entries= fHistory.getHistoryEntries();
			boolean checkOthers= addEntryMenuItems(entries);
			
			if (entries.size() > 0) {
				new MenuItem(fMenu, SWT.SEPARATOR);
			}
			
			if (entries.size() > 0) {
				Action clear= new ClearAction();
				addActionToMenu(fMenu, clear);
			}
			Action others= new HistoryListAction(fHistory);
			others.setChecked(checkOthers);
			addActionToMenu(fMenu, others);
		
			return fMenu;
		}

		private boolean addEntryMenuItems(List entries) {
			boolean checkOthers= true;
			int min= Math.min(entries.size(), RESULTS_IN_DROP_DOWN);
			for (int i= min - 1; i > 0; i--) { // reverse order: youngest first
				Object entry= entries.get(i);
				HistoryAction action= new HistoryAction(entry);
				boolean check= entry.equals(fHistory.getCurrentEntry());
				action.setChecked(check);
				if (action.isChecked())
					checkOthers= false;
				addActionToMenu(fMenu, action);
			}
			return checkOthers;
		}

		private void addActionToMenu(Menu parent, Action action) {
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(parent, -1);
		}

		public void dispose() {
			fHistory= null;
		
			if (fMenu != null) {
				fMenu.dispose();
				fMenu= null;
			}
		}
	}

	public static final int RESULTS_IN_DROP_DOWN= 10;

	private ViewHistory fHistory;
	private Menu fMenu;

	public HistoryDropDownAction(ViewHistory history) {
		fHistory= history;
		fMenu= null;
		setMenuCreator(new HistoryMenuCreator());
		fHistory.configureHistoryDropDownAction(this);
	}

	public void run() {
		new HistoryListAction(fHistory).run();
	}
}
