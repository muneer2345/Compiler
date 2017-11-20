package cop5556sp17;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;
import static cop5556sp17.AST.Type.TypeName.FILE;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.NONE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;

import java.util.List;

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
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Chain c0=binaryChain.getE0();
		c0.visit(this,null);
		Token arrow=binaryChain.getArrow();

		ChainElem c1=binaryChain.getE1();
		c1.visit(this, null);
		Token first=c1.getFirstToken();

		if(c0.getTypeName()==URL && arrow.isKind(ARROW)&& c1.getTypeName()==Type.TypeName.IMAGE)
		{
			binaryChain.setTypeName(IMAGE);
		}
		else if(c0.getTypeName()==FILE && arrow.isKind(ARROW)&& c1.getTypeName()==Type.TypeName.IMAGE)
		{
			binaryChain.setTypeName(IMAGE);
		}
		else if(c0.getTypeName()==FRAME && arrow.isKind(ARROW)&& (first.isKind(KW_XLOC) || first.isKind(KW_YLOC))&& c1 instanceof FrameOpChain )
		{
			binaryChain.setTypeName(INTEGER);
		} 
		else if(c0.getTypeName()==FRAME && arrow.isKind(ARROW)&& (first.isKind(KW_SHOW) || first.isKind(KW_HIDE) || first.isKind(KW_MOVE))&& c1 instanceof FrameOpChain )
		{
			binaryChain.setTypeName(FRAME);
		}
		else if(c0.getTypeName()==IMAGE && arrow.isKind(ARROW)&& (first.isKind(OP_WIDTH)|| first.isKind(OP_HEIGHT)) && c1 instanceof ImageOpChain )
		{
			binaryChain.setTypeName(INTEGER);
		}
		else if(c0.getTypeName()==IMAGE && arrow.isKind(ARROW)&&  c1.getTypeName()==Type.TypeName.FRAME  )
		{
			binaryChain.setTypeName(FRAME);
		}
		else if(c0.getTypeName()==IMAGE && arrow.isKind(ARROW)&&  c1.getTypeName()==Type.TypeName.FILE  )
		{
			binaryChain.setTypeName(NONE);
		}
		else if(c0.getTypeName()==IMAGE && ( arrow.isKind(ARROW) || arrow.isKind(BARARROW))&&  (first.isKind(OP_GRAY)|| first.isKind(OP_BLUR) || first.isKind(OP_CONVOLVE)) && c1 instanceof FilterOpChain )
		{
			binaryChain.setTypeName(IMAGE);
		}
		else if(c0.getTypeName()==IMAGE && arrow.isKind(ARROW) &&   first.isKind(KW_SCALE) && c1 instanceof ImageOpChain )
		{
			binaryChain.setTypeName(IMAGE);
		}
		else if(c0.getTypeName()==IMAGE && arrow.isKind(ARROW)  && c1 instanceof IdentChain && c1.getTypeName()==Type.TypeName.IMAGE )
		{
			binaryChain.setTypeName(IMAGE);
		}
		else if(c0.getTypeName()==INTEGER && arrow.isKind(ARROW)  && c1 instanceof IdentChain && c1.getTypeName()==Type.TypeName.INTEGER )
		{
			binaryChain.setTypeName(INTEGER);
		}
		else throw new TypeCheckException("Error");
		return binaryChain.getTypeName();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName e0=(TypeName)binaryExpression.getE0().visit(this, null);
		TypeName e1=(TypeName)binaryExpression.getE1().visit(this, null);


		switch(binaryExpression.getOp().kind)
		{
		case PLUS:
			if(e0.equals(e1) && ( e0.isType(INTEGER)||e0.isType(IMAGE))){

				binaryExpression.setTypeName(e0);

			}
			else
			{
				throw new TypeCheckException("Incompatible Types PLUS E0 and E1 are not equal");
			}
			break;

		case MINUS:
			if(e0.equals(e1) && ( e0.isType(INTEGER)||e0.isType(IMAGE))){

				binaryExpression.setTypeName(e0);

			}
			else
			{
				throw new TypeCheckException("Incompatible Types MINUS E0 and E1 are not equal");
			}
			break;
		case DIV:
			if(e0.equals(e1) && (e0.isType(INTEGER))){

				binaryExpression.setTypeName(e0);

			}
			else if(e0.isType(IMAGE) && e1.isType(INTEGER))
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("Incompatible Types DIV E0 and E1 are not equal");
			}
			break;
		case TIMES:
			if(e0.equals(e1) && (e0.isType(INTEGER))){

				binaryExpression.setTypeName(e0);

			}
			else if((e0.isType(IMAGE)&&e1.isType(INTEGER)) || (e0.isType(INTEGER)&&e1.isType(IMAGE)))
			{
				binaryExpression.setTypeName(IMAGE);
			}
			else
			{
				throw new TypeCheckException("Incompatible Types TIMES E0 and E1");
			}
			break;
		case MOD:
			if(e0.equals(e1) && ( e0.isType(INTEGER))){

				binaryExpression.setTypeName(e0);

			}
			else if((e0.isType(IMAGE)&&e1.isType(INTEGER)) )
			{
				binaryExpression.setTypeName(IMAGE);
			}
		else
			{
				throw new TypeCheckException("Incompatible Types PLUS E0 and E1 are not equal");
			}
			break;

			
		case LE:case GE: case LT: case GT:

			if(e0.equals(e1) && (e0.isType(INTEGER))){

				binaryExpression.setTypeName(BOOLEAN);

			}
			else if(e0.equals(e1) && (e0.isType(BOOLEAN)))
			{
				binaryExpression.setTypeName(e0);
			}
			else
			{
				throw new TypeCheckException("Incompatible Types LE GE LT GT E0 and E1");
			}
			break;	

		case EQUAL: case NOTEQUAL:

			if(e0.equals(e1))
			{
				binaryExpression.setTypeName(BOOLEAN);

			}
			else throw new TypeCheckException("Incompatible types for equal not equal");
			break;
		case AND:
			
			if(e0.equals(e1)&& e0.isType(BOOLEAN))
			{
				binaryExpression.setTypeName(BOOLEAN);
			}
			else throw new TypeCheckException("Incompatible Types");
			break;
		case OR:
			
			if(e0.equals(e1)&& e0.isType(BOOLEAN))
			{
				binaryExpression.setTypeName(BOOLEAN);
			}
			else throw new TypeCheckException("Incompatible Types");
			break;
		}


		return binaryExpression.getTypeName();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		List<Dec> d=block.getDecs();
		List<Statement> s=block.getStatements();
		for(int i=0;i<d.size();i++)
		{
			Dec d1=d.get(i);
			d1.visit(this, null);
		}
		for(int i=0;i<s.size();i++)
		{
			Statement s1=s.get(i);
			s1.visit(this, null);
		}
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);

		return booleanLitExpression.getTypeName();

	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple t=filterOpChain.getArg();
		t.visit(this, null);

		List<Expression> e=t.getExprList();

		if(!e.isEmpty())
		{
			throw new TypeCheckException("Tuple Length");
		}
		filterOpChain.setTypeName(Type.TypeName.IMAGE);
		return filterOpChain.getTypeName();
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple t=frameOpChain.getArg();
		t.visit(this, null);
		List<Expression> e=t.getExprList();

		if(frameOpChain.getFirstToken().isKind(KW_SHOW)||frameOpChain.getFirstToken().isKind(KW_HIDE))
		{
			if(!e.isEmpty())
			{
				throw new TypeCheckException("Length not zero");
			}
			frameOpChain.setTypeName(NONE);
		}
		else if(frameOpChain.getFirstToken().isKind(KW_XLOC)||frameOpChain.getFirstToken().isKind(KW_YLOC))
		{
			if(!e.isEmpty())
			{
				throw new TypeCheckException("Length not zero");
			}
			frameOpChain.setTypeName(INTEGER);
		}
		else if(frameOpChain.getFirstToken().isKind(KW_MOVE))
		{
			if(e.size()==2)
			{
				frameOpChain.setTypeName(NONE);			
			}
			else throw new TypeCheckException("Tuple Length not 2");

		}

		else throw new TypeCheckException("bug in parser");

		return frameOpChain.getTypeName();
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token first=identChain.firstToken;
		String f=first.getText();
		Dec d=symtab.lookup(f);
		if(d==null)
		{
			throw new TypeCheckException("Not in scope");
		}
		identChain.setTypeName(d.getTypeName());
		identChain.setD(d);
		return identChain.getTypeName();
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d=symtab.lookup(identExpression.firstToken.getText());
		if(d==null)throw new TypeCheckException("Incompatible Types for visit ident expression");

		else
		{
			identExpression.setTypeName(d.getTypeName());
			identExpression.setD(d);
		}

		return identExpression.getTypeName();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, null);
		ifStatement.getB().visit(this,null);
		if(ifStatement.getE().getTypeName()!=Type.TypeName.BOOLEAN)

		{
			throw new TypeCheckException("Error in if");	 
		}

		return ifStatement;

	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);





		return intLitExpression.getTypeName();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.getE().visit(this, null);

		if(sleepStatement.getE().getTypeName()!= Type.TypeName.INTEGER)
		{
			throw new TypeCheckException("Not integer in sleep statment");
		}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub

		whileStatement.getE().visit(this, null);
		whileStatement.getB().visit(this,null);
		if(whileStatement.getE().getTypeName()!=Type.TypeName.BOOLEAN)

		{
			throw new TypeCheckException("Error in while");	 
		}

		return whileStatement;

	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token first=declaration.getFirstToken();
		TypeName firstT=Type.getTypeName(first);
		Token ident=declaration.getIdent();
		declaration.setTypeName(firstT);

		if(symtab.insert(ident.getText(), declaration)==true)
		{

		}
		else
		{
			throw new TypeCheckException("Already present in current scope");
		}
		return declaration.getTypeName();
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<ParamDec> list=program.getParams();
		Block b=program.getB();

		for(int i=0;i<list.size();i++)
		{
			ParamDec p=list.get(i);
			p.visit(this, null);
		}
		b.visit(this, null);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		IdentLValue L=assignStatement.getVar();
		L.visit(this, null);
		TypeName e0=(TypeName)assignStatement.getE().visit(this, null);
		//Expression e=assignStatement.getE();
		//e.setTypeName(e0);
		String token=L.getFirstToken().getText();
		Dec d=symtab.lookup(token);
		if(d==null)
		{
			throw new TypeCheckException("");
		}
		if(d.getTypeName()!=e0) 
		{
			throw new TypeCheckException("Error in assignment statement");
		}

		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub

		Dec d=symtab.lookup(identX.getText());
		if(d==null)
		{
			throw new TypeCheckException("Incompatible Types for visit ident expression");
		}
		else
		{
			identX.setD(d);

		}

		return d;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token first=paramDec.firstToken;
		TypeName t=Type.getTypeName(first);
		paramDec.setTypeName(t);
		String ident=paramDec.getIdent().getText();

		if(!symtab.insert(ident, paramDec))
		{
			throw new TypeCheckException("already in scope");
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)  {
		// TODO Auto-generated method stub

		constantExpression.setTypeName(INTEGER);



		return constantExpression.getTypeName();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple t=imageOpChain.getArg();
		t.visit(this, null);
		List<Expression> e=t.getExprList();
		//		for(int i=0;i<e.size();i++)
		//		{
		//			Expression e0=e.get(i);
		//			e0.visit(this, null);
		//			
		//			
		//		}
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH)||imageOpChain.getFirstToken().isKind(OP_HEIGHT))
		{
			if(!e.isEmpty())
			{
				throw new TypeCheckException("Length not zero");
			}
			imageOpChain.setTypeName(INTEGER);
		}
		else if(imageOpChain.getFirstToken().isKind(KW_SCALE))
		{
			if(e.size()==1)
			{
				imageOpChain.setTypeName(IMAGE);
			}
			else throw new TypeCheckException("Tuple Length not 2");
		}
		else throw new TypeCheckException("Bug");




		return imageOpChain.getTypeName();	
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub

		List<Expression> args=tuple.getExprList();

		for(int i=0;i<args.size();i++)
		{
			Expression e0=args.get(i);
			e0.visit(this, null);
			if(e0.getTypeName()!=INTEGER)
			{
				throw new TypeCheckException("Error in Tuple, Expression");
			}

		}
		return tuple;
	}

}
