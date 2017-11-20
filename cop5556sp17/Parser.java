package cop5556sp17;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;
public class Parser {
	

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
//	@SuppressWarnings("serial")	
//	public static class UnimplementedFeatureException extends RuntimeException {
//		public UnimplementedFeatureException() {
//			super();
//		}
//	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 * @throws IllegalNumberException 
	 * @throws NumberFormatException 
	 */
	Program parse() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Program po=null;
		po=program();
		matchEOF();
		return po;
	}

	Expression expression() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
Token first=t;
Expression e0=null;
Expression e1=null;
		e0=term();

		while(t.isKind(LT)||t.isKind(LE)||t.isKind(GT)||t.isKind(GE)||t.isKind(EQUAL)||t.isKind(NOTEQUAL))
		{ 
		Token operator=t;
			consume();	
			e1=term();
			e0=new BinaryExpression(first,e0,operator,e1);
		}
		//throw new SyntaxException("Error in Expression");
return e0;
	}

	Expression term() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token first=t;
		Expression e0=null;
		Expression e1=null;
		e0=elem();
		while(t.isKind(MINUS)||t.isKind(PLUS)||t.isKind(OR)){
			Token operator=t;

			consume();

			e1=elem();
           e0=new BinaryExpression(first,e0,operator,e1);
		}
		//throw new SyntaxException("Error in Term");
		return e0;
	}

	Expression elem() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token first=t;
		Expression e0=null;
		Expression e1=null;

		e0=factor();
		while(t.isKind(TIMES)||t.isKind(DIV)||t.isKind(MOD)||t.isKind(AND))
		{
			Token operator=t;
			consume();
			e1=factor();
			e0=new BinaryExpression(first,e0,operator,e1);
		}
		return e0;
		//throw new SyntaxException("Error in elem");
	}

	Expression factor() throws SyntaxException, NumberFormatException, IllegalNumberException {
		Kind kind = t.kind;
		Token first=t;
		Expression e0=null;
		switch (kind) {
		case IDENT: {
			consume();
			e0=new IdentExpression(first);
		}
		break;
		case INT_LIT: {
			consume();
			e0=new IntLitExpression(first);
		}
		break;
		case KW_TRUE:
		case KW_FALSE: {
			consume();
			e0=new BooleanLitExpression(first);
		}
		break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			consume();
			e0=new ConstantExpression(first);
		}
		break;
		case LPAREN: {
			consume();
			e0=expression();
			match(RPAREN);
		}
		break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return e0;
	}

	Block block() throws SyntaxException,NumberFormatException, IllegalNumberException {
		//TODO
	
		Block b0=null;
		Token first=t;
		ArrayList<Dec> d=new ArrayList<>();
		ArrayList<Statement> s=new ArrayList<>();
		Dec d0=null;
		Statement s0=null;
		if(t.isKind(LBRACE))
		{
			consume();

			while(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)||t.isKind(OP_SLEEP)||t.isKind(KW_WHILE)||t.isKind(KW_IF)||t.isKind(IDENT)||t.isKind(OP_CONVOLVE)||t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
			{
				if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME))
				{
					d0=dec();
					d.add(d0);

				}
				else if(t.isKind(OP_SLEEP)||t.isKind(KW_WHILE)||t.isKind(KW_IF)||t.isKind(IDENT)||t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
				{
					s0=statement();
					s.add(s0);
				}
			}
			match(RBRACE);
			b0=new Block(first, d, s);
				}

		else throw new SyntaxException("error in block");
		
		return b0;
	}

	Program program() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Program p0=null;
		Token first=t;
		Block b0=null;
		ArrayList<ParamDec> a=new ArrayList<>();
		ParamDec p=null;
		if(t.isKind(IDENT))
		{
			consume();
			if(t.isKind(LBRACE))
			{

				b0=block();
				p0=new Program(first, a, b0);
			}
			else if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN))
			{
				p=paramDec();
				a.add(p);
				while(t.isKind(COMMA))
				{
					consume();
					p=paramDec();
					a.add(p);
				}
				b0=block();
				p0=new Program(first, a, b0);
			}
		}
		else
			throw new SyntaxException("Missing Ident");
		
		return p0;

		//throw new SyntaxException("Error in program");
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		ParamDec p0=null;
		Token first=t;
		Token last;
		/*if(t.isKind(LPAREN))
		{
			consume();
		 */	if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)){
			 consume();
last=t;
			 match(IDENT);     
			 p0=new ParamDec(first, last);

		 }
		 /* match(RPAREN);*/



		 else throw new SyntaxException("Error in paramDec");
		 
		 return p0;
	}

	Dec dec() throws SyntaxException {
		//TODO
		Dec d0=null;
		Token first=t;
		Token last;
		/*if(t.isKind(LPAREN))
		{
			consume();*/
		if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME)){
			consume();
			last=t;
			match(IDENT);
			d0=new Dec(first, last);
	
		}
		/*        match(RPAREN);*/


		else throw new SyntaxException("Error in dec");
		
		return d0;
	}

	Statement statement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Statement s0=null;
		Expression e0=null;
		
		Block b0=null;
		Token first=t;
		if(t.isKind(OP_SLEEP))
		{
			consume();
			e0=expression();
			match(SEMI);
			s0=new SleepStatement(first, e0);

		}
		else if(t.isKind(KW_WHILE))
		{
			consume();
			if(t.isKind(LPAREN))
			{
				consume();
				
				e0=expression();
			
				match(RPAREN);
			}
			b0=block();
			s0=new WhileStatement(first, e0, b0);
		}
		else if(t.isKind(KW_IF))
		{
			consume();
			if(t.isKind(LPAREN))
			{
				consume();
				e0=expression();
				match(RPAREN);
				
			}
			b0=block();// Fix for 2nd assignment error
			s0=new IfStatement(first, e0, b0);
		}
		else if(t.isKind(IDENT)&& scanner.peek().isKind(ASSIGN))
		{
            IdentLValue a=new IdentLValue(first);
			consume();


			if(t.isKind(ASSIGN))
			{
				consume();
				e0=expression();

			}

			match(SEMI);
			s0=new AssignmentStatement(first, a, e0);
			
		}

		else 
		{
			s0=chain();
			match(SEMI);

		}

return s0;
		//	throw new SyntaxException("error in statement");
	}

	Chain chain() throws SyntaxException, NumberFormatException, IllegalNumberException {
		//TODO
		Token first=t;
        Chain c0=null;
        ChainElem c1=null;
        Token arr;
        Token arr1;
		if(t.isKind(IDENT)||t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
		{


			c0=chainElem();
			if(t.isKind(ARROW)||t.isKind(BARARROW))
			{
				arr=t;
				consume();
				c1=chainElem();
				c0=new BinaryChain(first, c0, arr, c1);

				while(t.isKind(ARROW)||t.isKind(BARARROW))
				{
                      arr1=t;
					consume();
					//else if(t.isKind(IDENT)||t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE)||t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC)||t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))

					c1=chainElem();
					c0=new BinaryChain(first, c0, arr1,c1);
				}
			}
			else throw new SyntaxException("Chain");
		}

		else throw new SyntaxException("Chain1");
		
		return c0;
	}
	//throw new SyntaxException("Error in Chain");


	ChainElem chainElem() throws SyntaxException,NumberFormatException, IllegalNumberException {
		//TODO
		ChainElem c0=null;
		Tuple t0=null;
		Token first=t;
		if(t.isKind(IDENT))
		{
			consume();
			c0=new IdentChain(first);
		}
		else if(t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE))
		{
			consume();
       	t0=arg();
       	c0=new FilterOpChain(first, t0);



		}
		else if(t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC))
		{
			consume();

			t0=arg();	
            c0=new FrameOpChain(first, t0);

		}
		else if(t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
		{
			consume();
			

			t0=arg();	
			c0=new ImageOpChain(first, t0);


		}
		else{
			throw new SyntaxException("Error in chainElem");
		}
		return c0;
		//throw new SyntaxException("Error in chainElem");
	}

	Tuple arg() throws SyntaxException,NumberFormatException, IllegalNumberException {
		//TODO
	Token first=t;
	Expression e0=null;
	//Expression e1=null;
	Tuple t0=null;
	List<Expression> a=new ArrayList<Expression>();
		if(t.isKind(LPAREN))
		{
			consume();
			e0=expression();
			a.add(e0);
			while(t.isKind(COMMA))
			{

				consume();
				e0=expression();
				a.add(e0);
			}
			match(RPAREN);
			t0=new Tuple(first, a);
			return t0;
			}
		else 
		{
			t0=new Tuple(first,a);
			return t0 ;
		}

		//throw new SyntaxException("Arg");
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	//	private Token match(Kind... kinds) throws SyntaxException {
	//		// TODO. Optional but handy
	//		return null; //replace this statement
	//	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
