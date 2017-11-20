package cop5556sp17;



import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;
import cop5556sp17.AST.Dec;



public class SymbolTable {

	/** 
	 * to be called when block entered
	 */
	Stack <Integer> scope_stack;
	int current_scope, next_scope=-1; 
	final HashMap< String, ArrayList < HashMap<Integer,Dec>> > table;
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		current_scope=next_scope++; //increment scope
		scope_stack.push(current_scope); // push onto scope stack
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		scope_stack.pop();        // pop element from top of stack
	    current_scope = scope_stack.peek();   //update current scope


	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
	HashMap<Integer,Dec> list= new HashMap<Integer,Dec>();
	list.put(current_scope, dec);
	ArrayList<HashMap<Integer,Dec>> c=table.get(ident);
	
	if(c==null)
	{
		ArrayList<HashMap<Integer, Dec>> newList = new ArrayList<HashMap<Integer, Dec>>();
		newList.add(list);
		table.put(ident, newList);
	}

	else{
		for(HashMap<Integer, Dec> a:c){
			for(Integer key: a.keySet()){
				if(key==current_scope){   // already in current _ scope
					return false;
				}
			}
		}
		c.add(list);
	}
	return true;
	}
	public Dec lookup(String ident){
	
		Dec d=null;
		ArrayList<HashMap<Integer, Dec>> list1 = table.get(ident);
		if(list1!=null){
			for(HashMap<Integer, Dec> a:list1){
				for(Integer key: a.keySet()){
					if(scope_stack.search(key)!=-1){
						d = a.get(key);
					}
				}
			}
		}
		if(d!=null){
			return d;
		}
		return null;
	}
	
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		scope_stack = new Stack<Integer>();   /// initialize variables for Symbol Table
	    table= new HashMap<String, ArrayList< HashMap<Integer,Dec> > >();
	    enterScope();
	    }


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return table.toString();
	}
	
	


}
