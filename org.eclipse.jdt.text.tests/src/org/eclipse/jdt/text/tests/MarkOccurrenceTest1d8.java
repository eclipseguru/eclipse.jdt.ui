/*******************************************************************************
 * Copyright (c) 2011, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.eclipse.jdt.testplugin.JavaProjectHelper;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.eclipse.jdt.internal.core.manipulation.search.IOccurrencesFinder;
import org.eclipse.jdt.internal.core.manipulation.search.IOccurrencesFinder.OccurrenceLocation;
import org.eclipse.jdt.internal.corext.dom.IASTSharedValues;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.tests.core.rules.Java1d8ProjectTestSetup;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.search.ExceptionOccurrencesFinder;
import org.eclipse.jdt.internal.ui.search.MethodExitsFinder;

/**
 * Those tests are made to run on Java Spider 1.8 .
 */
public class MarkOccurrenceTest1d8 {
	@Rule
	public Java1d8ProjectTestSetup f18p= new Java1d8ProjectTestSetup();

	private ASTParser fParser;

	private IOccurrencesFinder fFinder;

	private IJavaProject fJProject1;

	private IPackageFragmentRoot fSourceFolder;

	@Before
	public void setUp() throws Exception {
		fParser= ASTParser.newParser(IASTSharedValues.SHARED_AST_LEVEL);

		fJProject1= f18p.getProject();
		fSourceFolder= JavaProjectHelper.addSourceContainer(fJProject1, "src");
		JavaPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
	}

	@After
	public void tearDown() throws Exception {
		JavaProjectHelper.clear(fJProject1, f18p.getDefaultClasspath());
	}

	private OccurrenceLocation[] getHighlights(String source, int offset, int length) throws Exception {
		CompilationUnit root= createCompilationUnit(source);
		String errorString= fFinder.initialize(root, offset, length);
		assertNull(errorString, errorString);
		return fFinder.getOccurrences();
	}

	private CompilationUnit createCompilationUnit(String source) throws JavaModelException {
		IPackageFragment pack1= fSourceFolder.createPackageFragment("test1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", source, true, null);
		fParser.setSource(cu);
		fParser.setResolveBindings(true);
		return (CompilationUnit)fParser.createAST(null);
	}

	private void checkSelection(String s, int offset, int length, OccurrenceLocation[] expected) throws Exception {
		OccurrenceLocation[] selectedNodes= getHighlights(s, offset, length);
		assertEquals("number of selections", expected.length, selectedNodes.length);
		sortByStartIndex(selectedNodes);
		sortByStartIndex(expected);
		for (int i= 0; i < selectedNodes.length; i++) {
			assertEquals(expected[i].getOffset(), selectedNodes[i].getOffset());
			assertEquals(expected[i].getLength(), selectedNodes[i].getLength());
		}
	}

	private void sortByStartIndex(OccurrenceLocation[] OccurrenceLocations) {
		Arrays.sort(OccurrenceLocations, (node0, node1) -> node0.getOffset() - node1.getOffset());
	}

	//pattern must be found - otherwise it's assumed to be an error
	private OccurrenceLocation find(String s, String pattern, int ithOccurrence) {
		if (ithOccurrence < 1)
			throw new IllegalStateException("ithOccurrence = " + ithOccurrence);
		return find(s, pattern, ithOccurrence, 0);
	}

	private OccurrenceLocation find(String s, String pattern, int ithOccurrence, int startIdx) {
		if (startIdx < 0 || startIdx > s.length())
			throw new IllegalStateException("startIdx = " + startIdx);
		int idx= s.indexOf(pattern, startIdx);
		if (idx == -1)
			throw new IllegalStateException("not found \"" + pattern + "\" in \"" + s.substring(startIdx));
		if (ithOccurrence == 1)
			return new OccurrenceLocation(idx, pattern.length(), 0, "");
		return find(s, pattern, ithOccurrence - 1, idx + 1);
	}

	@Test
	public void testThrowingAnnotatedException1() throws Exception {
		String s= "" +
				"package test1;\n" +
				"import java.lang.annotation.Documented;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Retention;\n" +
				"import java.lang.annotation.RetentionPolicy;\n" +
				"import java.lang.annotation.Target;\n" +
				"\n" +
				"public class E {\n" +
				"    @Target(ElementType.TYPE_USE)\n" +
				"    @Retention(RetentionPolicy.RUNTIME)\n" +
				"    @Documented\n" +
				"    static @interface Critical {\n" +
				"        String msg() default \"We're all going to die!\";\n" +
				"    }\n" +
				"\n" +
				"    class InnerException extends Exception {\n" +
				"        private static final long serialVersionUID = 1L;\n" +
				"    }\n" +
				"\n" +
				"    /**\n" +
				"     * @throws E.InnerException\n" +
				"     */\n" +
				"    void foo() throws @Critical() E.@Critical() InnerException, IllegalArgumentException {\n" +
				"        if (Boolean.TRUE)\n" +
				"            throw new @Critical() InnerException();\n" +
				"        else\n" +
				"            throw new @Critical() E.@Critical() InnerException();\n" +
				"    }\n" +
				"\n" +
				"    void tryCatch() {\n" +
				"        try {\n" +
				"            foo();\n" +
				"        } catch (@Critical() E.@Critical() InnerException e) {\n" +
				"        } catch (RuntimeException e) {\n" +
				"        }\n" +
				"        try {\n" +
				"            foo();\n" +
				"        } catch (RuntimeException | @Critical(msg=\"He\"+\"llo\") E.@Critical() InnerException e) {\n" +
				"        }\n" +
				"    }\n" +
				"}\n";

		fFinder= new ExceptionOccurrencesFinder();
		int offset= 8 + s.indexOf("@throws E.InnerException"); // in Javadoc
		int length= 0;
		OccurrenceLocation[] ranges= { find(s, "E.InnerException", 1), find(s, "@Critical() E.@Critical() InnerException", 1),
				find(s, "throw", 3), find(s, "throw", 4) };
		checkSelection(s, offset, length, ranges);

		fFinder= new ExceptionOccurrencesFinder();
		offset= 1 + s.indexOf("@Critical() E.@Critical() InnerException"); // in annotation
		length= 0;
		checkSelection(s, offset, length, ranges);

		fFinder= new ExceptionOccurrencesFinder();
		offset= s.indexOf("E.@Critical() InnerException"); // in annotated type qualifier
		length= 1;
		checkSelection(s, offset, length, ranges);

		fFinder= new ExceptionOccurrencesFinder();
		offset= 1 + s.indexOf("InnerException e)"); // in annotated catch type (does NOT include modifier "@Critical() "!)
		length= 0;
		ranges= new OccurrenceLocation[] { find(s, "foo", 2), find(s, "E.@Critical() InnerException", 3)};
		checkSelection(s, offset, length, ranges);

		fFinder= new ExceptionOccurrencesFinder();
		offset= s.indexOf("RuntimeException |"); // in annotated union type
		length= 0;
		ranges= new OccurrenceLocation[] { find(s, "foo", 3), find(s, "RuntimeException", 2)};
		checkSelection(s, offset, length, ranges);

		fFinder= new ExceptionOccurrencesFinder();
		offset= s.indexOf("He"); // in annotation expression
		length= 0;
		ranges= new OccurrenceLocation[] { find(s, "foo", 3), find(s, "@Critical(msg=\"He\"+\"llo\") E.@Critical() InnerException", 1)};
		checkSelection(s, offset, length, ranges);
	}

	@Test
	public void testThrownExceptionInLambda() throws Exception {
		String s= "" +
				"package test1;\n" +
				"\n" +
				"import java.io.IOException;\n" +
				"\n" +
				"public class E {\n" +
				"    void test(int i) throws IOException {\n" +
				"        if (i == 0) {\n" +
				"            throw new IOException();\n" +
				"        } else {\n" +
				"            FI fi = () -> { throw new IOException(); };\n" +
				"        }\n" +
				"    }\n" +
				"}\n" +
				"\n" +
				"@FunctionalInterface\n" +
				"interface FI {\n" +
				"    void foo() throws IOException;\n" +
				"}\n";

		fFinder= new ExceptionOccurrencesFinder();
		int offset= s.indexOf("IOException {");
		int length= 0;
		OccurrenceLocation[] ranges= { find(s, "IOException", 2), find(s, "throw", 2) };
		checkSelection(s, offset, length, ranges);
	}

	@Test
	public void testTryCatchInLambda() throws Exception {
		String s= "" +
				"package test1;\n" +
				"\n" +
				"import java.io.FileInputStream;\n" +
				"import java.io.FileNotFoundException;\n" +
				"\n" +
				"public class E {\n" +
				"    Runnable lambda = () -> {\n" +
				"        try {\n" +
				"            new FileInputStream(\"dummy\");\n" +
				"        } catch (FileNotFoundException e) {\n" +
				"        }\n" +
				"    };\n" +
				"}\n";

		fFinder= new ExceptionOccurrencesFinder();
		int offset= s.indexOf("FileNotFoundException e");
		int length= 0;
		OccurrenceLocation[] ranges= { find(s, "FileInputStream", 2), find(s, "FileNotFoundException", 2) };
		checkSelection(s, offset, length, ranges);
	}

	@Test
	public void testMarkMethodExitsBug565387() throws Exception {
		fFinder= new MethodExitsFinder();
		String s= "" +
				"import java.util.function.Function;\n\n" +
				"public class TestClass {\n" +
				"	String foo() {\n" +
				"		Function<Object,Object> f = o -> {\n" +
				"			return o;\n" +
			    "		};\n" +
				"		return null;\n" +
			    "	}\n" +
				"}\n";

		int offset= 1 + s.indexOf("String");//middle of word
		int length= 0;
		OccurrenceLocation[] ranges= { find(s, "String", 1), find(s, "return null;", 1) };
		checkSelection(s, offset, length, ranges);
	}
}
