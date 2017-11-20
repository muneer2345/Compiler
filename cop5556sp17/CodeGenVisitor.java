package cop5556sp17;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.BARARROW;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slot_count = 0;
	int array_index = 0;
	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		//TODO  visit the local variables
		Block b=program.getB();
		ArrayList<Dec> D=b.getDecs();
		for(int i=0;i<D.size();i++)
		{   
			String type=""; 
			if(D.get(i).getTypeName()==TypeName.INTEGER)
			{
				type="I";
			}
			else if(D.get(i).getTypeName()==TypeName.BOOLEAN)
			{
				type="Z";
			}
			else if(D.get(i).getTypeName()==TypeName.FRAME)
			{
				type="Lcop5556sp17/PLPRuntimeFrame;";

			}
			else if(D.get(i).getTypeName()==TypeName.IMAGE)
			{
				type="Ljava/awt/image/BufferedImage;";
			}

			String fieldName = D.get(i).getIdent().getText();
			mv.visitLocalVariable(fieldName,type,null,startRun,endRun,D.get(i).getSlot_number());


		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain c0=binaryChain.getE0();

		Token t=binaryChain.getArrow();
		ChainElem c1=binaryChain.getE1();

		c0.visit(this,"L");

		if(c0.getTypeName()==URL)
		{	
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", "(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);
		}
		else if(c0.getTypeName()==TypeName.FILE)
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", "(Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);      
		}
		else if(c0.getTypeName()==TypeName.NONE)
		{
			mv.visitInsn(POP);


		}
		if(c1 instanceof FilterOpChain)
		{
			if(t.isKind(ARROW))
			{
				mv.visitInsn(ACONST_NULL);
			}

			else if(t.isKind(BARARROW))
			{
				mv.visitInsn(DUP);
			}
		}

		c1.visit(this, "R");


		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		//TODO  Implement this
		Expression e0=binaryExpression.getE0();
		Expression e1=binaryExpression.getE1();
		e0.visit(this, null);
		e1.visit(this, null);
		Token op=binaryExpression.getOp();
		TypeName t0=e0.getTypeName();
		TypeName t1=e1.getTypeName();

		if(t0.isType(TypeName.INTEGER) && t1.isType(TypeName.INTEGER))
		{
			switch(op.kind)
			{
			case PLUS:
				mv.visitInsn(IADD);
				break;
			case MINUS:
				mv.visitInsn(ISUB);
				break;
			case TIMES:
				mv.visitInsn(IMUL);
				break;

			case DIV:
				mv.visitInsn(IDIV);
				break;
				/*	case AND:
        mv.visitInsn(IAND);
        break;    */
			case MOD:
				mv.visitInsn(IREM);
				break;
				/* case OR:
        mv.visitInsn(IOR);
        break;  */ 

			case LT:{
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPLT, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}

			case LE: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}

			case GT: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			case GE: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			case EQUAL: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			case NOTEQUAL: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			}
		}
		else if(t0.isType(TypeName.BOOLEAN) && t1.isType(TypeName.BOOLEAN))
		{
			switch(op.kind)
			{

			case AND:
				mv.visitInsn(IAND);
				break;   
			case OR:
				mv.visitInsn(IOR);
				break;  

			case LT:{
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPLT, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}

			case LE: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPLE, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}

			case GT: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPGT, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			case GE: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPGE, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			case EQUAL: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			case NOTEQUAL: {
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ICMPNE, s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}
			}


		}
		else if(t0.isType(TypeName.IMAGE) && t1.isType(TypeName.IMAGE))
		{
			switch(op.kind)
			{
			case PLUS:
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
				break;
			case MINUS:
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
				break;
			case EQUAL:{
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ACMPEQ , s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}

			case NOTEQUAL:{
				Label s1 = new Label();
				Label s2 = new Label();
				mv.visitJumpInsn(IF_ACMPNE , s1);
				mv.visitLdcInsn(0);
				mv.visitJumpInsn(GOTO, s2);
				mv.visitLabel(s1);
				mv.visitLdcInsn(1);
				mv.visitLabel(s2);
				break;
			}	
			}


		}
		else if(t0.isType(TypeName.IMAGE) && t1.isType(TypeName.INTEGER))
		{
			switch(op.kind)
			{
			case TIMES:
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);	
				break;
			case DIV:
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
				break;
			case MOD:
				//mv.visitInsn(IREM);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);
				break;		

			}


		}
		else if(t0.isType(TypeName.INTEGER) && t1.isType(TypeName.IMAGE))
		{
			switch(op.kind)
			{
			case TIMES:
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", "(Ljava/awt/image/BufferedImage;I)Ljava/awt/image/BufferedImage;", false);	
				break;

			}


		}


		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		ArrayList<Dec> d=block.getDecs();
		ArrayList<Statement> s=block.getStatements();
		for(int i=0;i<d.size();i++){
			d.get(i).visit(this, null);
		}		

		for(int i=0;i<s.size();i++){
			Statement st=s.get(i);
			s.get(i).visit(this, null);
			if(st instanceof BinaryChain)
			{
				mv.visitInsn(POP);
			}

		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		String s=booleanLitExpression.getValue().toString();
		if(s.equals("true")) {
			mv.visitInsn(ICONST_1);
		}
		else {
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {

		if(constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH))
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth","()I", false);
		}
		else if(constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT))
		{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", "()I", false);
		}
		return null;

	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		declaration.setSlot_number(++slot_count);
		if(declaration.getTypeName()==IMAGE || declaration.getTypeName()==FRAME)
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE,declaration.getSlot_number());
		}
		else if(declaration.getTypeName()==TypeName.INTEGER|| declaration.getTypeName()==TypeName.BOOLEAN)	
		{
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE,declaration.getSlot_number());

		}
		

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		String s=filterOpChain.getFirstToken().kind.getText();
		//Tuple t=filterOpChain.getArg();
		//t.visit(this,null);
		switch(s)
		{
		case "blur":

			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			break;
		case "gray":
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);	
			break;
		case "convolve":
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", "(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
			break;

		}

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		String s=frameOpChain.getFirstToken().kind.getText();

		frameOpChain.getArg().visit(this, null);
		switch(s)
		{
		case "show":
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);

			break;
		case "hide":
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);

			break;
		case "move":
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
			break;	
		case "xloc":
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
			break;
		case "yloc":
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
			break;

		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		//	assert false : "not yet implemented";
		String direction=(String) arg;
		if(direction.equals("L"))
		{
			Dec d=identChain.getD();
			if(d instanceof ParamDec)
			{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getFirstToken().getText(), identChain.getD().getTypeName().getJVMTypeDesc());
			}
			else{
				if(d.getTypeName()==TypeName.INTEGER || d.getTypeName()==TypeName.BOOLEAN)
				{
					mv.visitVarInsn(ILOAD, d.getSlot_number());
				}
				else
				{
					mv.visitVarInsn(ALOAD, d.getSlot_number());
				}
			}

		}
		else if(direction.equals("R"))
		{
			Dec d = identChain.getD();
			if(d instanceof ParamDec) {
				if(d.getTypeName()==TypeName.INTEGER || d.getTypeName()==TypeName.BOOLEAN)
				{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, d.getIdent().getText(), d.getTypeName().getJVMTypeDesc());
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), d.getTypeName().getJVMTypeDesc());

				}
				else if(d.getTypeName()==TypeName.FILE)
				{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), d.getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD,0);
					mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), d.getTypeName().getJVMTypeDesc());

				}

			}
			else
			{
				if(d.getTypeName()==TypeName.INTEGER)
				{
					mv.visitVarInsn(ISTORE, d.getSlot_number());
					mv.visitVarInsn(ILOAD, d.getSlot_number());
				}
				else if(d.getTypeName()==TypeName.IMAGE)
				{
					mv.visitVarInsn(ASTORE, d.getSlot_number());
					mv.visitVarInsn(ALOAD, d.getSlot_number());
				}
				else if(d.getTypeName()==TypeName.FRAME)
				{
					mv.visitVarInsn(ALOAD, d.getSlot_number());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitVarInsn(ASTORE, d.getSlot_number());
					mv.visitVarInsn(ALOAD, d.getSlot_number());
				}
			}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		Dec d = identExpression.getD();
		if(d instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			if(d.getTypeName() == TypeName.INTEGER)
				mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), "I");
			else if(d.getTypeName() == TypeName.BOOLEAN)
				mv.visitFieldInsn(GETFIELD, className, d.getIdent().getText(), "Z");
		}
		else {
			if(d.getTypeName()==IMAGE)
			{
				mv.visitVarInsn(ALOAD, d.getSlot_number());
				
			}
			else if(d.getTypeName()==TypeName.INTEGER || d.getTypeName()==TypeName.BOOLEAN){
			mv.visitVarInsn(ILOAD, d.getSlot_number());
		}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		Dec d = identX.getD();
		if(d instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			//  mv.visitInsn(SWAP);
			if(d.getTypeName() == TypeName.INTEGER)
			{
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, identX.getText(), "I");}
			else if(d.getTypeName() == TypeName.BOOLEAN){
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, identX.getText(), "Z");}
		}
		else {
			if(d.getTypeName()==IMAGE)
			{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE,d.getSlot_number());
			}
			
			else if(d.getTypeName()==TypeName.INTEGER || d.getTypeName()== TypeName.BOOLEAN )
			{
			mv.visitVarInsn(ISTORE, d.getSlot_number());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Label s1=new Label();
		Label s2=new Label();

		Expression e0=ifStatement.getE();
		Block b0=ifStatement.getB();
		mv.visitLabel(s1);
		e0.visit(this, null);
		mv.visitJumpInsn(IFEQ, s2);
		b0.visit(this,null);
		mv.visitLabel(s2);

		ArrayList<Dec> d=ifStatement.getB().getDecs();
		for(int i=0;i<d.size();i++)
		{
			if(d.get(i).getTypeName()==TypeName.INTEGER)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "I", null, s1, s2, d.get(i).getSlot_number());
			}
			else if(d.get(i).getTypeName()==TypeName.BOOLEAN)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "Z", null, s1, s2, d.get(i).getSlot_number());
			}
			else if(d.get(i).getTypeName()==TypeName.FRAME)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "Lcop5556sp17/PLPRuntimeFrame;", null, s1, s2, d.get(i).getSlot_number());
			}
			else if(d.get(i).getTypeName()==TypeName.IMAGE)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "Ljava/awt/image/BufferedImage;", null, s1, s2, d.get(i).getSlot_number());
			}
		}
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, null);

		String s=imageOpChain.getFirstToken().getText();

		switch(s)
		{
		case "width":
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);

			break;
		case "height":
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);		
			break;
		case "scale":
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
			break;	


		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.getFirstToken().intVal());
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		if(paramDec.getTypeName() == TypeName.INTEGER) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, new Integer(0));
			fv.visitEnd();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, array_index++);
			mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(paramDec.getTypeName() == TypeName.BOOLEAN) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, new Boolean(false));
			fv.visitEnd();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, array_index++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		else if(paramDec.getTypeName() == TypeName.FILE	) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/io/File;", null, null);
			fv.visitEnd();



			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, array_index++);
			mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");

		}

		else if(paramDec.getTypeName() == TypeName.URL	) {
			fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/net/URL;", null, null);
			fv.visitEnd();

			// mv.visitTypeInsn(NEW, "Ljava/awt/image/BufferedImage");
			// mv.visitInsn(DUP);

			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, array_index++);

			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", "([Ljava/lang/String;I)Ljava/net/URL;", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");


		}
		/*  else if(paramDec.getTypeName() == TypeName.IMAGE	) {
            fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/awt/image/BufferedImage;", null, null);
            fv.visitEnd();

            mv.visitTypeInsn(NEW, "Ljava/awt/image/BufferedImage");
            mv.visitInsn(DUP);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitIntInsn(SIPUSH, array_index++);
            mv.visitInsn(AALOAD);

            mv.visitMethodInsn(INVOKESPECIAL, "java/awt/image/BufferedImage", "<init>", "(III)V", false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/awt/image/BufferedImage;");


        }
        else if(paramDec.getTypeName() == TypeName.IMAGE	) {
            fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Ljava/awt/image/BufferedImage;", null, null);
            fv.visitEnd();

            mv.visitTypeInsn(NEW, "Ljava/awt/image/BufferedImage");
            mv.visitInsn(DUP);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitIntInsn(SIPUSH, array_index++);
            mv.visitInsn(AALOAD);

            mv.visitMethodInsn(INVOKESPECIAL, "java/awt/image/BufferedImage", "<init>", "(III)V", false);
            mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/awt/image/BufferedImage;");


        }*/



		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//assert false : "not yet implemented";
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> e= tuple.getExprList();
		for(int i=0;i<e.size();i++)
		{
			e.get(i).visit(this,null);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Label s1 = new Label();
		Label s2 = new Label();
		Label guard = new Label();
		mv.visitJumpInsn(GOTO, guard);
		Expression e0=whileStatement.getE();
		Block b0=whileStatement.getB();

		mv.visitLabel(s1);
		b0.visit(this, null);
		mv.visitLabel(s2);

		mv.visitLabel(guard);
		e0.visit(this, null);
		mv.visitJumpInsn(IFNE, s1);	
		ArrayList<Dec> d=whileStatement.getB().getDecs();
		for(int i=0;i<d.size();i++)
		{
			if(d.get(i).getTypeName()==TypeName.INTEGER)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "I", null, s1, s2, d.get(i).getSlot_number());
			}
			else if(d.get(i).getTypeName()==TypeName.BOOLEAN)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "Z", null, s1, s2, d.get(i).getSlot_number());
			}
			else if(d.get(i).getTypeName()==TypeName.FRAME)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "Lcop5556sp17/PLPRuntimeFrame;", null, s1, s2, d.get(i).getSlot_number());
			}
			else if(d.get(i).getTypeName()==TypeName.IMAGE)
			{
				mv.visitLocalVariable(d.get(i).getIdent().getText(), "Ljava/awt/image/BufferedImage;", null, s1, s2, d.get(i).getSlot_number());
			}
		}
		return null;
	}

}
