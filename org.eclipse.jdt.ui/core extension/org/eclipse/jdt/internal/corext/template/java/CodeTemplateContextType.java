package org.eclipse.jdt.internal.corext.template.java;

import java.util.ArrayList;

import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.ContextType;
import org.eclipse.jdt.internal.corext.template.ContextTypeRegistry;
import org.eclipse.jdt.internal.corext.template.TemplateContext;
import org.eclipse.jdt.internal.corext.template.TemplatePosition;
import org.eclipse.jdt.internal.corext.template.TemplateVariable;

/**
  */
public class CodeTemplateContextType extends ContextType {
	
	public static final String CATCHBLOCK_CONTEXTTYPE= "catchblock_context";
	public static final String METHODBODY_CONTEXTTYPE= "methodbody_context";
	public static final String CONSTRUCTORBODY_CONTEXTTYPE= "constructorbody_context";
	public static final String NEWTYPE_CONTEXTTYPE= "newtype_context";
	public static final String TYPECOMMENT_CONTEXTTYPE= "typecomment_context";
	public static final String METHODCOMMENT_CONTEXTTYPE= "methodcomment_context";
	public static final String CONSTRUCTORCOMMENT_CONTEXTTYPE= "constructorcomment_context";
	public static final String OVERRIDECOMMENT_CONTEXTTYPE= "overridecomment_context";

	public static final String EXCEPTION_TYPE= "exception_type"; //$NON-NLS-1$
	public static final String EXCEPTION_VAR= "exception_var"; //$NON-NLS-1$
	public static final String ENCLOSING_METHOD= "enclosing_method"; //$NON-NLS-1$
	public static final String ENCLOSING_TYPE= "enclosing_type"; //$NON-NLS-1$
	public static final String BODY_STATEMENT= "body_statement"; //$NON-NLS-1$
	public static final String RETURN_TYPE= "return_type"; //$NON-NLS-1$
	
	public static final String FILENAME= "file_name"; //$NON-NLS-1$
	public static final String PACKAGENAME= "package_name"; //$NON-NLS-1$
	public static final String PROJECTNAME= "project_name"; //$NON-NLS-1$

	public static final String PACKAGE_STATEMENT= "package_statement"; //$NON-NLS-1$
	public static final String TYPE_DECLARATION= "type_declaration"; //$NON-NLS-1$
	
	public static class CodeTemplateVariable extends TemplateVariable {
		public CodeTemplateVariable(String name, String description) {
			super(name, description);
		}
		
		public String evaluate(TemplateContext context) {
			return context.getVariable(getName());
		}
	}
		
	protected static class Todo extends TemplateVariable {

		public Todo() {
			super("todo", JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.todo")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public String evaluate(TemplateContext context) {
			String todoTaskTag= StubUtility.getTodoTaskTag(((CodeTemplateContext) context).getJavaProject());
			if (todoTaskTag == null)
				return "XXX"; //$NON-NLS-1$
	
			return todoTaskTag;
		}
	}
	
	
	public CodeTemplateContextType(String contextName) {
		super(contextName);
		
		// global
		addVariable(new GlobalVariables.Dollar());
		addVariable(new GlobalVariables.Date());
		addVariable(new GlobalVariables.Year());
		addVariable(new GlobalVariables.Time());
		addVariable(new GlobalVariables.User());
		addVariable(new Todo());
		
		
		if (CATCHBLOCK_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(EXCEPTION_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.exceptiontype")));
			addVariable(new CodeTemplateVariable(EXCEPTION_VAR,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.exceptionvar")));
		} else if (METHODBODY_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(ENCLOSING_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingtype")));
			addVariable(new CodeTemplateVariable(ENCLOSING_METHOD,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingmethod")));
			addVariable(new CodeTemplateVariable(BODY_STATEMENT,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.bodystatement")));
		} else if (CONSTRUCTORBODY_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(ENCLOSING_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingtype")));
			addVariable(new CodeTemplateVariable(BODY_STATEMENT,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.bodystatement")));			
		} else if (NEWTYPE_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(PACKAGE_STATEMENT,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.packstatement")));
			addVariable(new CodeTemplateVariable(TYPE_DECLARATION,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.typedeclaration")));
			addCompilationUnitVariables();
		} else if (TYPECOMMENT_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(ENCLOSING_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingtype")));
			addCompilationUnitVariables();
		} else if (METHODCOMMENT_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(ENCLOSING_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingtype")));
			addVariable(new CodeTemplateVariable(ENCLOSING_METHOD,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingmethod")));
			addVariable(new CodeTemplateVariable(RETURN_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.returntype")));
			addCompilationUnitVariables();
		} else if (OVERRIDECOMMENT_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(ENCLOSING_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingtype")));
			addVariable(new CodeTemplateVariable(ENCLOSING_METHOD,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingmethod")));
			addCompilationUnitVariables();
		} else if (CONSTRUCTORCOMMENT_CONTEXTTYPE.equals(contextName)) {
			addVariable(new CodeTemplateVariable(ENCLOSING_TYPE,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingtype")));
			addVariable(new CodeTemplateVariable(ENCLOSING_METHOD,  JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.enclosingmethod")));
			addCompilationUnitVariables();
		}
	}
	
	private void addCompilationUnitVariables() {
		addVariable(new CodeTemplateVariable(FILENAME, JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.filename")));
		addVariable(new CodeTemplateVariable(PACKAGENAME, JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.packagename")));
		addVariable(new CodeTemplateVariable(PROJECTNAME, JavaTemplateMessages.getString("CodeTemplateContextType.variable.description.projectname")));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.template.ContextType#validateVariables(org.eclipse.jdt.internal.corext.template.TemplatePosition[])
	 */
	protected String validateVariables(TemplatePosition[] variables) {
		ArrayList required=  new ArrayList(5);
		String contextName= getName();
		if (NEWTYPE_CONTEXTTYPE.equals(contextName)) {
			required.add(PACKAGE_STATEMENT);
			required.add(TYPE_DECLARATION);
		}
		for (int i= 0; i < variables.length; i++) {
			String var= variables[i].getName();
			if (getVariable(var) == null) {
				return JavaTemplateMessages.getFormattedString("CodeTemplateContextType.validate.unknownvariable", var);
			}
			required.remove(var);
		}
		if (!required.isEmpty()) {
			String missing= (String) required.get(0);
			return JavaTemplateMessages.getFormattedString("CodeTemplateContextType.validate.missingvariable", missing);
		}
		return super.validateVariables(variables);
	}	
	
	

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.corext.template.ContextType#createContext()
	 */
	public TemplateContext createContext() {
		return null;
	}

	public static void registerContextTypes(ContextTypeRegistry registry) {
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.CATCHBLOCK_CONTEXTTYPE));
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.METHODBODY_CONTEXTTYPE));
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.CONSTRUCTORBODY_CONTEXTTYPE));
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.NEWTYPE_CONTEXTTYPE));
		
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.TYPECOMMENT_CONTEXTTYPE));
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.METHODCOMMENT_CONTEXTTYPE));
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.CONSTRUCTORCOMMENT_CONTEXTTYPE));
		registry.add(new CodeTemplateContextType(CodeTemplateContextType.OVERRIDECOMMENT_CONTEXTTYPE));		
	}
	
	

}
