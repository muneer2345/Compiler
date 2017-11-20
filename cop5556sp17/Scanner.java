package cop5556sp17;

import java.util.ArrayList;

public class Scanner {
	/**
	 * Kind enum
	 */

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	public static enum State {   	/* Define states of the Finite Automata*/
		START, IDENT_PART, NUMLIT, DIV, OR ,EQUAL,LESS_THAN, GREATER_THAN, NOT, MINUS	
	}
	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message){
			super(message);
		}
	}


	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}


	static int line_position = 0,line_start=0;

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  
		LinePos lineposition;

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS

			return (chars.substring(this.pos,this.pos+this.length));
		}

		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			//TODO IMPLEMENT THIS

			return this.lineposition;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.lineposition = new LinePos(line_position, pos - line_start);
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException,IllegalNumberException{
			//TODO IMPLEMENT THIS
			int val;
			try{
				val = Integer.parseInt(this.getText());
			}
			catch(Exception e) {
				throw new IllegalNumberException("Illegal Number");
			}
			return val;

		}
		public boolean isKind(Kind kind)    /* Addded is Kind Method for Assignment2*/
		{
			if(this.kind == kind)
			{
				return true;
			}
			else return false;


		}
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}



		private Scanner getOuterType() {
			return Scanner.this;
		}

	}


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();


	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		//TODO IMPLEMENT THIS!!!!
		int pos = 0; 
		int length = chars.length();
		State state = State.START;
		int startPos = 0;
		char ch;
		line_position = 0;
		line_start = pos;
		while (pos <= length) {
			ch = pos < length ? chars.charAt(pos) : (char)-1;
			switch(state){

			case START: 
				pos = skipWhiteSpace(pos);
				ch = pos < length ? chars.charAt(pos) : (char)-1;
				startPos = pos;
				switch (ch) {
				case (char)-1	:tokens.add(new Token(Kind.EOF,pos,0));pos++;break;
				case '&'		:tokens.add(new Token(Kind.AND,startPos,1));pos++;break;
				case '+'		:tokens.add(new Token(Kind.PLUS,startPos,1));pos++;break;
				case '%'		:tokens.add(new Token(Kind.MOD,startPos,1));pos++;break;
				case ','		:tokens.add(new Token(Kind.COMMA,startPos,1));pos++;break;
				case '{'		:tokens.add(new Token(Kind.LBRACE,startPos,1));pos++;break;
				case '}'		:tokens.add(new Token(Kind.RBRACE,startPos,1));pos++;break;
				case '('		:tokens.add(new Token(Kind.LPAREN,startPos,1));pos++;break;
				case ')'		:tokens.add(new Token(Kind.RPAREN,startPos,1));pos++;break;
				case '0'		:tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;break;
				case '*'		:tokens.add(new Token(Kind.TIMES,startPos,1));pos++;break;
				case ';'		:tokens.add(new Token(Kind.SEMI,startPos,1));pos++;break;
				case '/'		:state=State.DIV;pos++;break;
				case '|'		:state=State.OR;pos++;break;
				case '='		:state=State.EQUAL;pos++;break;
				case '<'		:state=State.LESS_THAN;pos++;break;
				case '>'		:state=State.GREATER_THAN;pos++;break;
				case '!'		:state=State.NOT;pos++;break;
				case '-'		:state=State.MINUS;pos++;break;
				default			:
					if (Character.isDigit(ch)) {
						state = State.NUMLIT;
						pos++;
					} 
					else if (Character.isJavaIdentifierStart(ch)){
						state = State.IDENT_PART;
						pos++;
					} 
					else {throw new IllegalCharException(
							"illegal char " +ch+" at pos "+pos);
					}
					break;

				}
				break;

			case IDENT_PART: 
				if (Character.isJavaIdentifierPart(ch)) { //Check java identifier
					pos++;
				} 
				else {

					if(Keyword(chars.substring(startPos, pos),startPos,pos)==1){ //Check Language identifier
						tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
					}
					state = State.START;
				}
				break;
			case NUMLIT: 
				if (Character.isDigit(ch)) {
					pos++;
				} 
				else {
					Token t=new Token(Kind.INT_LIT, startPos, pos - startPos);
					if(t.intVal()>=Integer.MAX_VALUE)
						throw new IllegalNumberException("illegal char " +ch+" at pos "+pos);
					else
					{
						tokens.add(t); 
						state = State.START;
					}

				}break;

			case EQUAL: 
				if (pos!=length&&chars.charAt(pos) == '=') {
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					state = State.START;
					pos++;
				} 
				else {
					throw new IllegalCharException("illegal char in equal state" +ch+" at pos "+pos);
				}
				break;

			case OR: 
				if (pos!=length&&chars.charAt(pos) == '-') {
					if((pos+1)!=length&&chars.charAt(pos+1) == '>')   // Check if it is arrow if you encounter OR
					{
						tokens.add(new Token(Kind.BARARROW, startPos, 3));
						pos=pos+2;
					} 
					else {
						tokens.add(new Token(Kind.OR, startPos, 1));
						tokens.add(new Token(Kind.MINUS, pos, 1));
						pos++;
						state = State.START;
					}
				}
				else{
					tokens.add(new Token(Kind.OR, startPos, 1));
				}
				state = State.START;
				break;

			case NOT: 
				if (pos!=length&&chars.charAt(pos) == '=') {            // CHeck for not equal to
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					pos++;
				} 
				else {
					tokens.add(new Token(Kind.NOT, startPos, 1));
				}
				state = State.START;
				break;

			case LESS_THAN: 
				if (pos!=length&&chars.charAt(pos) == '=') {   // Check for <=
					tokens.add(new Token(Kind.LE, startPos, 2));
					pos++;
				} 
				else if(pos!=length&&chars.charAt(pos)=='-'){  //check for -=
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					pos++;
				}
				else{
					tokens.add(new Token(Kind.LT, startPos, 1));
				}
				state = State.START;
				break;

			case GREATER_THAN: 
				if (pos!=length&&chars.charAt(pos) == '=') {   //check for >=
					tokens.add(new Token(Kind.GE, startPos, 2));
					pos++;
				} 
				else{
					tokens.add(new Token(Kind.GT, startPos, 1));
				}
				state = State.START;
				break;

			case MINUS: 
				if (pos!=length&&chars.charAt(pos) == '>') {         //cgeck for arrow ->
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					pos++;
				} 
				else{
					tokens.add(new Token(Kind.MINUS, startPos, 1));
				}
				state = State.START;
				break;

			case DIV:
				if((pos!=length&&chars.charAt(pos) !='*')||pos==length){
					tokens.add(new Token(Kind.DIV,startPos,1));             // if not comments
					state = State.START;
				}
				else if(chars.charAt(pos) == '*'){           //if it is a comment
					pos++;
					while(pos < length){
						if(chars.charAt(pos)!='*'){                               
							pos++;
						}
						else if(pos!=length&&chars.charAt(pos)=='*'){
							if((pos+1)!=length&&chars.charAt(pos+1)=='/'){
								pos = pos+2;
								state = State.START;
								break;
							}
							else{
								pos++;
							}
						}
					}
					if(pos==length)
						state=State.START;
				}
				break;
			}


		}

		return this;  
	}
	private int skipWhiteSpace(int pos) {

		while(pos < chars.length() ){
			if(chars.substring(pos, pos+1).contains("\n")){
				line_start=pos+1;
				line_position++;	
				pos++;
			}
			else if(Character.isWhitespace(chars.charAt(pos))){
				pos++;
			}
			else{
				break;
			}
		}
		return pos;
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}



	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS

		return t.getLinePos();

	}
	private int Keyword(String substring, int startPos, int pos) { // Language of keyword
		// TODO Auto-generated method stub
		switch(substring){
		case "integer"	:  tokens.add(new Token(Kind.KW_INTEGER,startPos,pos-startPos)); return 0; 
		case "boolean"	:  tokens.add(new Token(Kind.KW_BOOLEAN,startPos,pos-startPos)); return 0; 
		case "image"	:  tokens.add(new Token(Kind.KW_IMAGE,startPos,pos-startPos)); return 0; 
		case "url"		:  tokens.add(new Token(Kind.KW_URL,startPos,pos-startPos)); return 0; 
		case "file"	:  tokens.add(new Token(Kind.KW_FILE,startPos,pos-startPos)); return 0; 
		case "frame"	:  tokens.add(new Token(Kind.KW_FRAME,startPos,pos-startPos)); return 0; 
		case "while"	:  tokens.add(new Token(Kind.KW_WHILE,startPos,pos-startPos)); return 0; 
		case "if"		:  tokens.add(new Token(Kind.KW_IF,startPos,pos-startPos)); return 0; 
		case "true"	:  tokens.add(new Token(Kind.KW_TRUE,startPos,pos-startPos)); return 0; 
		case "false"	:  tokens.add(new Token(Kind.KW_FALSE,startPos,pos-startPos)); return 0; 
		case "screenheight"	:  tokens.add(new Token(Kind.KW_SCREENHEIGHT,startPos,pos-startPos)); return 0; 
		case "screenwidth"	:  tokens.add(new Token(Kind.KW_SCREENWIDTH,startPos,pos-startPos)); return 0; 
		case "xloc"	:  tokens.add(new Token(Kind.KW_XLOC,startPos,pos-startPos)); return 0; 
		case "yloc"	:  tokens.add(new Token(Kind.KW_YLOC,startPos,pos-startPos)); return 0; 
		case "hide"	:  tokens.add(new Token(Kind.KW_HIDE,startPos,pos-startPos)); return 0; 
		case "show"	:  tokens.add(new Token(Kind.KW_SHOW,startPos,pos-startPos)); return 0; 
		case "move"	:  tokens.add(new Token(Kind.KW_MOVE,startPos,pos-startPos)); return 0; 
		case "scale"	:  tokens.add(new Token(Kind.KW_SCALE,startPos,pos-startPos)); return 0; 
		case "blur"	:  tokens.add(new Token(Kind.OP_BLUR,startPos,pos-startPos)); return 0; 
		case "gray"	:  tokens.add(new Token(Kind.OP_GRAY,startPos,pos-startPos)); return 0; 
		case "convolve"	:  tokens.add(new Token(Kind.OP_CONVOLVE,startPos,pos-startPos)); return 0; 
		case "width"	:  tokens.add(new Token(Kind.OP_WIDTH,startPos,pos-startPos)); return 0; 
		case "height"	:  tokens.add(new Token(Kind.OP_HEIGHT,startPos,pos-startPos)); return 0; 
		case "sleep"	:  tokens.add(new Token(Kind.OP_SLEEP,startPos,pos-startPos)); return 0; 		
		}
		return 1;
	}


}
