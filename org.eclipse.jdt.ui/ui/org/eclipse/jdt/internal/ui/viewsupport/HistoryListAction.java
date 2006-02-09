/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jesper Kamstrup Linnet (eclipse@kamstrup-linnet.dk) - initial API and implementation 
 *          (report 36180: Callers/Callees view)
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.viewsupport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.PlatformUI;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;

/*package*/ class HistoryListAction extends Action {
	
	private class HistoryListDialog extends StatusDialog {
		private ListDialogField fHistoryList;
		private StringDialogField fMaxEntriesField;
		private int fMaxEntries;
		
		private Object fResult;
		
		private HistoryListDialog() {
			super(fHistory.getShell());
			setTitle(fHistory.getHistoryListDialogTitle()); 
			
			createHistoryList();
			createMaxEntriesField();
		}
		
		private void createHistoryList() {
			IListAdapter adapter= new IListAdapter() {
				public void customButtonPressed(ListDialogField field, int index) {
					doCustomButtonPressed();
				}
				public void selectionChanged(ListDialogField field) {
					doSelectionChanged();
				}
				
				public void doubleClicked(ListDialogField field) {
					doDoubleClicked();
				}				
			};
			String[] buttonLabels= new String[] { "&Remove" };
			LabelProvider labelProvider= new TestRunLabelProvider();
			fHistoryList= new ListDialogField(adapter, buttonLabels, labelProvider);
			fHistoryList.setLabelText(fHistory.getHistoryListDialogMessage());
			
			List historyEntries= fHistory.getHistoryEntries();
			fHistoryList.setElements(historyEntries);
			
			Object currentEntry= fHistory.getCurrentEntry();
			ISelection sel;
			if (currentEntry != null) {
				sel= new StructuredSelection(currentEntry);
			} else {
				sel= new StructuredSelection();
			}
			fHistoryList.selectElements(sel);
		}

		private void createMaxEntriesField() {
			fMaxEntriesField= new StringDialogField();
			fMaxEntriesField.setLabelText(fHistory.getMaxEntriesMessage());
			fMaxEntriesField.setDialogFieldListener(new IDialogFieldListener() {
				public void dialogFieldChanged(DialogField field) {
					String maxString= fMaxEntriesField.getText();
					boolean valid;
					try {
						fMaxEntries= Integer.parseInt(maxString);
						valid= fMaxEntries > 0 && fMaxEntries < 100;
					} catch (NumberFormatException e) {
						valid= false;
					}
					if (valid)
						updateStatus(new StatusInfo());
					else
						updateStatus(new StatusInfo(StatusInfo.ERROR, "Please enter a positive integer smaller than 100"));
				}
			});
			fMaxEntriesField.setText(Integer.toString(fHistory.getMaxEntries()));
		}

		/*
		 * @see Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			initializeDialogUnits(parent);
			
			Composite composite= (Composite) super.createDialogArea(parent);
			
			Composite inner= new Composite(composite, SWT.NONE);
			inner.setLayoutData(new GridData(GridData.FILL_BOTH));
			inner.setFont(composite.getFont());

			LayoutUtil.doDefaultLayout(inner, new DialogField[] { fHistoryList, new Separator() }, true);
			LayoutUtil.setHeightHint(fHistoryList.getListControl(null), convertHeightInCharsToPixels(12));
			LayoutUtil.setHorizontalGrabbing(fHistoryList.getListControl(null));
			
			Composite additionalControls= new Composite(inner, SWT.NONE);
			additionalControls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			LayoutUtil.doDefaultLayout(additionalControls, new DialogField[] { fMaxEntriesField }, false);
			LayoutUtil.setHorizontalGrabbing(fMaxEntriesField.getTextControl(null));
			
			applyDialogFont(composite);		
			return composite;
		}

		private void doCustomButtonPressed() {
			fHistoryList.removeElements(fHistoryList.getSelectedElements());
			fHistoryList.selectFirstElement();
		}
		
		private void doDoubleClicked() {
			okPressed();
		}
		
		private void doSelectionChanged() {
			List selected= fHistoryList.getSelectedElements();
			if (selected.size() >= 1) {
				fResult= selected.get(0);
			} else {
				fResult= null;
			}
			fHistoryList.enableButton(0, selected.size() != 0);
		}
				
		public Object getResult() {
			return fResult;
		}
		
		public List getRemaining() {
			return fHistoryList.getElements();
		}
		
		public int getMaxEntries() {
			return fMaxEntries;
		}
		
		/*
		 * @see org.eclipse.jface.dialogs.StatusDialog#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaHelpContextIds.HISTORY_LIST_DIALOG);
		}

		/*
		 * @see org.eclipse.jface.dialogs.StatusDialog#create()
		 */
		public void create() {
			setShellStyle(getShellStyle() | SWT.RESIZE);
			super.create();
		}

	}
	
	private final class TestRunLabelProvider extends LabelProvider {
		private final HashMap fImages= new HashMap();

		public String getText(Object element) {
			return fHistory.getText(element);
		}

		public Image getImage(Object element) {
			ImageDescriptor imageDescriptor= fHistory.getImageDescriptor(element);
			return getCachedImage(imageDescriptor);
		}

		private Image getCachedImage(ImageDescriptor imageDescriptor) {
			Object cached= fImages.get(imageDescriptor);
			if (cached != null)
				return (Image) cached;
			Image image= imageDescriptor.createImage(fHistory.getShell().getDisplay());
			fImages.put(imageDescriptor, image);
			return image;
		}

		public void dispose() {
			for (Iterator iter= fImages.values().iterator(); iter.hasNext();) {
				Image image= (Image) iter.next();
				image.dispose();
			}
			fImages.clear();
		}
	}
	
	private ViewHistory fHistory;
	
	public HistoryListAction(ViewHistory history) {
		fHistory= history;
		fHistory.configureHistoryListAction(this);
	}
		
	/*
	 * @see IAction#run()
	 */
	public void run() {
		HistoryListDialog dialog= new HistoryListDialog();
		if (dialog.open() == Window.OK) {
			fHistory.setHistoryEntries(dialog.getRemaining(), dialog.getResult());
			fHistory.setMaxEntries(dialog.getMaxEntries());
		}
	}

}

