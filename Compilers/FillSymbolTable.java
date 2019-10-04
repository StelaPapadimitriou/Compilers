import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class FillSymbolTable extends DepthFirstAdapter 
{
	//hash table with functions
	 private Hashtable functionsDefinition;
	 private Hashtable globalVariables;
	 String nameOfCurFunc ;
	  private static int errors =0;
	 private static int warnings = 0;
	 private boolean inFunction = false;
	 //count arguments in a function
	 private int numOfParOfCurFunc =0;
	
	 /*
	 * Constructor
	 */
	 
	public FillSymbolTable(){
		functionsDefinition = new Hashtable();
		globalVariables = new Hashtable();
	}
	
	/*
	*Return the number of errors
	*/
	public int Errors(){
		return errors;
	}
	/*
	*Return the number of warnings
	*/
	public int Warnings(){
		return warnings;
	}
	
	/*
	 * Get the hash with functions declarations
	*/
	public Hashtable getFuncDef(){
		return functionsDefinition;
	}
	
	/*
	 * Get the hash with variables declarations
	*/
	public Hashtable getGlobVar(){
		return globalVariables;
	}
	
	public void inAArgArgument(AArgArgument list){
		
		numOfParOfCurFunc++;
		 
	}
	
	public void inAMoreArguments(AMoreArguments list){
		numOfParOfCurFunc++;
		 
	}
	
	public void inAFuncdefineFunction(AFuncdefineFunction node){
		numOfParOfCurFunc=0;
		String name = node.getId().toString();  
		int line = ((TId) node.getId()).getLine();
		inFunction = true;
		nameOfCurFunc = name;
		if(functionsDefinition.containsKey(name)){
			System.out.println("Warning!! Your function has the same name with a previous one.Name: " + name +" Line: " + (line+1)/2);
			warnings++;
		}else{
			Function func = new Function();
			func.setName(name);
			functionsDefinition.put(name,func);
			Hashtable argCurrFunc = new Hashtable();
			LinkedList<AArgArgument> arguments = node.getArgument();
			if(node.getArgument().isEmpty()) return;
	
			TId a_id = (TId) arguments.get(0).getId();
				 
			func.insertArgToArglist(a_id.toString(),"");
			func.insertName(a_id.toString());
			func.insertType("");
			//Get the assign_value production for the first argument.
			LinkedList<AArValueArgValue> assignValue = new LinkedList<AArValueArgValue>();
			 
			for(Object p_assign : arguments.get(0).getArgValue()){				 
			   assignValue.add((AArValueArgValue)p_assign);
			}
			
			//Get all the rest arguments.
			LinkedList<AMoreArguments> more_args = new LinkedList<AMoreArguments>(); 
			
			for(Object p_args : arguments.get(0).getMoreArguments()){
			   more_args.add((AMoreArguments)p_args);	
			}
			
			if(assignValue.size() != 0){			
				func.increaseDefaultArgs();
				//If the value is a number , set the type of the variable to "int"
				if(assignValue.get(0).getValue() instanceof ANumValue)
				{
					 
					////////////////////////////////////////
					func.insertArgToArglist(a_id.toString(),"int");
					func.removeType(0);
					func.insertType("int");
					///////////////////////////////////////
				}else{
					////////////////////////////////////////
					func.insertArgToArglist(a_id.toString(),"string");
					func.removeType(0);
					func.insertType("string");
					///////////////////////////////////////
				}
				
			}
			 
			//For all the rest arguments...
			for(int j = 0; j<more_args.size(); j++)	{
				TId a_id1 = (TId) more_args.get(j).getId();
				 
				func.insertArgToArglist(a_id1.toString(),"");
				func.insertType("");
				func.insertName(a_id1.toString());
				//Get the assign_value production for the current argument.
				LinkedList<AArValueArgValue> values = new LinkedList<AArValueArgValue>();
				for(Object p_assign : more_args.get(j).getArgValue()){					
				   values.add((AArValueArgValue)p_assign);
				}
			 
				//If the current argument has not a default value.
				if(values.size() != 0){	
					 func.increaseDefaultArgs();
					//If the value is a number , set the type of the variable to "int"
				if(values.get(0).getValue() instanceof ANumValue){ 
					////////////////////////////////////////
					////////////////////////////////////////
					func.insertArgToArglist(a_id1.toString(),"int");
					func.removeType(j+1);
					func.insertType("int");
					///////////////////////////////////////
				}else{
					 
					////////////////////////////////////////
					func.insertArgToArglist(a_id1.toString(),"string");
					func.removeType(j+1);
					func.insertType("string");
					///////////////////////////////////////
				}
					
				}
			}
		
		 
	
	    }
	
	}
	
	/*
	*Set the return type of function
	*/
	public void outAReturnStatement(AReturnStatement node){
		
			if(node.getExpression() instanceof AValExpression){
				//Cast the expression to a value expression.
				AValExpression val_exp = (AValExpression)node.getExpression();
				//If the type of the value is not INTEGER , print error message.
				if(val_exp.getValue() instanceof ANumValue){
					Function temp =(Function) functionsDefinition.get(nameOfCurFunc);
					temp.setReturn("int");
					functionsDefinition.remove(nameOfCurFunc);
					//to kno ++ gt den pianei to teleytaio panta 
					functionsDefinition.put(nameOfCurFunc,temp);
				}else if(val_exp.getValue() instanceof AStringValue){
					Function temp =(Function) functionsDefinition.get(nameOfCurFunc);
					temp.setReturn("string");
					functionsDefinition.remove(nameOfCurFunc);
					//to kno ++ gt den pianei to teleytaio panta 
					functionsDefinition.put(nameOfCurFunc,temp);
				}	 
			}
	}
	
	public void outAFuncdefineFunction(AFuncdefineFunction node){
		String name = node.getId().toString(); 
		inFunction = false;
		Function temp =(Function) functionsDefinition.get(name);
		temp.setArg(numOfParOfCurFunc);
		functionsDefinition.remove(name);
		functionsDefinition.put(name,temp);
		nameOfCurFunc= "";
			
	}
	
	public void inAIdExpression(AIdExpression node){
		String name = node.getId().toString();
		int line = ((TId) node.getId()).getLine();
			if (!inFunction){
				if(!globalVariables.containsKey(name)){
					System.out.println("Error!! Not defined variable.Name: " + name + " Line: " + (line+1)/2);
					errors++;
				}
			}
	}
	
	public void inAAssignStatement(AAssignStatement st){
		String name = st.getId().toString();
		int line = ((TId) st.getId()).getLine();
		 
			if (!inFunction){
				 if(st.getExpression() instanceof AValExpression){
			       //get the value.
					AValExpression value = (AValExpression)st.getExpression();
					if(value.getValue() instanceof ANumValue){
						globalVariables.put(name, "int");
					}else if(value.getValue() instanceof AStringValue){
					
						globalVariables.put(name, "string");
					}
				}else if(st.getExpression() instanceof AIdExpression){
					AIdExpression idex = (AIdExpression) st.getExpression();
					if(globalVariables.containsKey(idex.getId().toString())){
						globalVariables.put(name, (globalVariables.get(idex.getId().toString())).toString());
					}
				}					
			}	
	}
		
	public void inAFuncCallFunctionCall(AFuncCallFunctionCall node){
		try{
			String name = node.getId().toString();  
			int line = ((TId) node.getId()).getLine();
			if(functionsDefinition.containsKey(name)){
				Function fun = (Function) functionsDefinition.get(name);
				ArrayList<String> tempType = (ArrayList) fun.getType();
				fun.setIsCalled(true);	 
				LinkedList<AArglist> arguments = node.getArglist();
			 
				if(node.getArglist().isEmpty()) {return;}	 
				//Get all the rest arguments.
				LinkedList<AMoreListArguments> more_args = new LinkedList<AMoreListArguments>(); 
			
				for(Object p_args : arguments.get(0).getMoreListArguments()){
					more_args.add((AMoreListArguments)p_args);
				}
			
				if(arguments.get(0).getExpression() instanceof AValExpression){
					AValExpression tempp = (AValExpression) arguments.get(0).getExpression(); 
					//If the value is a number , set the type of the variable to "int"
					if(tempp.getValue() instanceof ANumValue){					 
						tempType.set(0,"int");					
					 
					}else{  
						tempType.set(0,"string");			
					}
				
				}else if(arguments.get(0).getExpression() instanceof AIdExpression){
					AIdExpression tempp = (AIdExpression) arguments.get(0).getExpression(); 
				
					if(globalVariables.containsKey(tempp.toString())){
						tempType.set(0,(globalVariables.get(tempp.toString()).toString()));
					}
				}
	 		
			
			
			//For all the rest arguments...
			for(int j = 0; j<more_args.size(); j++)	{
				 
				if(more_args.get(j).getExpression() instanceof AValExpression){ 
					//If the value is a number , set the type of the variable to "int"
					AValExpression tempp = (AValExpression) more_args.get(j).getExpression(); 
					//If the value is a number , set the type of the variable to "int"
					if(tempp.getValue() instanceof ANumValue){
						tempType.set(j+1,"int");
					}else{
						tempType.set(j+1,"string");
					}
				 
				}else if(more_args.get(j).getExpression() instanceof AIdExpression){
					AIdExpression tempp = (AIdExpression) more_args.get(j).getExpression();
            			
					if(globalVariables.containsKey(tempp.toString())){
			
						tempType.set(j+1,(globalVariables.get(tempp.toString()).toString()));
					}
				}
			
			}
			ArrayList<String> tempName = fun.getArgName();
			for (int i =0; i <tempType.size();i++) {
				fun.insertArgToArglist(tempName.get(i),tempType.get(i));
			}
		
		}
			
	}catch(IndexOutOfBoundsException e){
		int line = ((TId) node.getId()).getLine();
		System.out.println("Wrong number of arguments.Line : " + (line +1)/2);
		errors++;
		//System.exit(0);
		
	}
	
	
	}
}