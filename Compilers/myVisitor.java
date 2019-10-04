import minipython.analysis.*;
import minipython.node.*;
import java.util.*;

public class myVisitor extends DepthFirstAdapter{
	//This hassh table contains Functions objects
	 Hashtable functionsDefinition;
	 //globalVariables contains all variables except local variables of functions
	 Hashtable globalVariables;
	 //local variable for functions
	 Hashtable currentFunctionsVar;
	
	 private static int errors =0;
	 
	 //check if the flow of programm is in a function
	 private boolean inFunction = false;
	//count the parameters of calling function
	 private int numOfParOfCallFunc =0;
	 //holds the name of current function
	private String nameOfCurFunc;
	
	
	
	
	/*
	 * Constructor : takes the output of Filltablesymbol class
	 */
	 
	public myVisitor(Hashtable fd, Hashtable glb ){
		functionsDefinition =fd;
		globalVariables = glb;
		
		
	}

	/*
	*Return the number of errors
	*/
	public int Errors(){
		return errors;
	}
	/*
	* Count the params
	*/
	
	public void inAArglist(AArglist list){
		
		numOfParOfCallFunc++;
		
	}

	public void inAMoreListArguments(AMoreListArguments list){
		
		numOfParOfCallFunc++;
		
	}
	
	
	public void inAFuncCallFunctionCall(AFuncCallFunctionCall node){
		
     try{
       String name = node.getId().toString();  
	   int line = ((TId) node.getId()).getLine();
	   //check the table of functions
		if(!functionsDefinition.containsKey(name)){
			System.out.println("Error!! You haven't defined this function.Name: " + name + " Line: " + (line+1)/2);
			errors++;
		 
		}else{
			Function fun = (Function) functionsDefinition.get(name);
			//temp arraylist with type of function's arguments 
			ArrayList<String> tempType = (ArrayList) fun.getType();
			//temp arraylist with name of function;s arguments
			ArrayList<String> tempName = fun.getArgName();
			fun.setIsCalled(true);
			//takes a list with arguments
			LinkedList<AArglist> arguments = node.getArglist();
	     	 if(node.getArglist().isEmpty()) return;
			  
			//Get all the rest arguments.
			LinkedList<AMoreListArguments> more_args = new LinkedList<AMoreListArguments>(); 
			
			for(Object p_args : arguments.get(0).getMoreListArguments()){
				
			   more_args.add((AMoreListArguments)p_args);				
			}
			//Check if the 1st arguments is a value
			if(arguments.get(0).getExpression() instanceof AValExpression){
				AValExpression tempp = (AValExpression) arguments.get(0).getExpression(); 
				
				//If the value is a number , set the type of the variable to "int"
				if(tempp.getValue() instanceof ANumValue)
				{					 
						tempType.set(0,"int");
										
				}else{
					tempType.set(0,"string");				 
				
				}
				//check if the 1st parameter is a variable
			}else if(arguments.get(0).getExpression() instanceof AIdExpression){
				AIdExpression tempp = (AIdExpression) arguments.get(0).getExpression();
 				if(globalVariables.containsKey(tempp.toString())){
					//if the variable exists then the argument takes the same value of it
					tempType.set(0,(globalVariables.get(tempp.toString()).toString()));
				}
			}
	 		
			//For all the rest arguments...
			for(int j = 0; j<more_args.size(); j++){
				 			
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
			 
			  for (int i =0; i <tempType.size();i++) {
					//insert to the arglist of function all arguments with their type
					fun.insertArgToArglist(tempName.get(i),tempType.get(i));
				}
		}
		
		}catch(IndexOutOfBoundsException e){
			//if this exception happen then the number of arguments is different
			int line = ((TId) node.getId()).getLine();
			System.out.println("Wrong number of arguments.Line : " + (line +1)/2);
			errors++;
			System.exit(0);
		
		}
			
	}
					
	public void outAFuncCallFunctionCall(AFuncCallFunctionCall node){
	    String name = node.getId().toString();  
		int line = ((TId) node.getId()).getLine();
		if(functionsDefinition.containsKey(name)){
			
			Function fu =(Function) functionsDefinition.get(name);
			 // Check if parameters of calling function and arguments is equal and check the default args 
			if( (fu.getArgs() !=   numOfParOfCallFunc) && (numOfParOfCallFunc != fu.getArgs() - fu.getDefaultArgs())){
				System.out.println("Error!! Your calling function has different number of parameters.Name: " + name + " Line: " + (line+1)/2);
				errors++;
			}
		 numOfParOfCallFunc = 0;
		}
			
    }
	    		
	public void inAAssignStatement(AAssignStatement st){
		String name = st.getId().toString();
		if (inFunction){
				//put the variable int function var table
				currentFunctionsVar.put(name, "VariablesFunc");				
			}else{
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

	public void outAAddExpression(AAddExpression num){
		 
		 String var1="1";
		 String var2="2";
		 int line=0;
		 int count =0;
		 
		 if(globalVariables.containsKey(num.getFisrt().toString())){
			   var1=globalVariables.get(num.getFisrt().toString()).toString();
			 
		 }else{
			 //check the first element if it is variable
			 if(num.getFisrt() instanceof AIdExpression ){
				Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id1 = (AIdExpression) num.getFisrt();
				line = ((TId) id1.getId()).getLine();
				 
				String name1 = id1.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp = fun.getArgList();
				
				if(temp.containsKey(name1) ){
					var1 = temp.get(name1).toString();
					 
					if((var1 == "" ) && fun.getIsCalled()){count++;}
				
					 
				}else{return;}
				
			}
			 
				if(num.getFisrt() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getFisrt();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var1="int";
							ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var1="string";
							AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}
				//if the operation becomes with a function 
				}else if( num.getFisrt() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getFisrt();					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					Function temp =(Function) functionsDefinition.get(id.toString());
					//get return type of function	 
					var1 = temp.getReturn();
				}
				
				
				
			 
				
				
			}		 
  
		if(globalVariables.containsKey(num.getNext().toString())){
			   var2=globalVariables.get(num.getNext().toString()).toString();
			       	
		}else{
			 if(num.getNext() instanceof AIdExpression ){
				Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id2 = (AIdExpression) num.getNext();
				line = ((TId) id2.getId()).getLine();
				String name2 = id2.getId().toString();
				Hashtable temp = fun.getArgList();
			
				if(temp.containsKey(name2) ){
					var2 = temp.get(name2).toString();
					if((var2 == "" ) && fun.getIsCalled()){count++;System.out.println("Variables haven't been initialized ");errors++;}
				}else{return;}
		
			}
			if(num.getNext() instanceof AValExpression){
				//Cast the expression to a value expression.
				AValExpression val_exp = (AValExpression)num.getNext();
				if(val_exp.getValue() instanceof ANumValue){
					var2="int";
					 ANumValue num_val = (ANumValue)val_exp.getValue();
					TNumber t_num = num_val.getNumber();
					line=t_num.getLine();
				}else if(val_exp.getValue() instanceof AStringValue){
					var2="string";
					 AStringValue string_val = (AStringValue)val_exp.getValue();
					TString t_string = string_val.getString();
					line=t_string.getLine();
				}		 
			}else if( num.getNext() instanceof AFunctionCallExpression){
				AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getNext();
				AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
				//Get the identifier that the function call is assigned to.
				TId id = afunc_call.getId();
				 Function temp =(Function) functionsDefinition.get(id.toString());
			     var2 = temp.getReturn();
				}		 
				 
		}	
		 if(((var1 != var2 ) || count ==2)){
		  System.out.println("Error!! You cannot add a string with an int.Line: "+ (line+1)/2);
		  errors++;
		}
		 
	}
	
	public void outAMultiExpression(AMultiExpression num){
		 String var1="1";
		 String var2="2";
		 int line=0;
		 int count =0;
		 if(globalVariables.containsKey(num.getFisrt().toString())){
			   var1=globalVariables.get(num.getFisrt().toString()).toString();
			  	
		 }else{
			  if(num.getFisrt() instanceof AIdExpression){
				  
				Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id1 = (AIdExpression) num.getFisrt();
				line = ((TId) id1.getId()).getLine();
				
				String name1 = id1.getId().toString();
				
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp1 = fun.getArgList();
				
				if(temp1.containsKey(name1) ){
					var1 = temp1.get(name1).toString();
					
					if(var1 == "" && fun.getIsCalled()){count++;}
					 
				}else{return;}
			  }
				if(num.getFisrt() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getFisrt();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var1="int";
							ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var1="string";
							AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}	 
				}else if( num.getFisrt() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getFisrt();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var1 = temp.getReturn();
				}
			}		 
  
		if(globalVariables.containsKey(num.getNext().toString())){
			   var2=globalVariables.get(num.getNext().toString()).toString();
			   //Cast the expression to a value expression.
						 
			    	
			}else{
				
				 if(num.getNext() instanceof AIdExpression ){
				  
				Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id2 = (AIdExpression) num.getNext();
				line = ((TId) id2.getId()).getLine();
				 
				String name2 = id2.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp = fun.getArgList();
				
				if(temp.containsKey(name2) ){
					var2 = temp.get(name2).toString();
					 
					if((var2 == "" ) && fun.getIsCalled()){count++;System.out.println("Variaaables haven't initialized ");errors++;}
					 
				}else{return;}
				 }
				
				
				if(num.getNext() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getNext();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var2="int";
							 ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var2="string";
						 AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}		 
					}else if( num.getNext() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getNext();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var2 = temp.getReturn();
				}		 
				 
			}
			
		 if(((var1 != var2 ) || count ==2) ){
					  System.out.println("Error!! You cannot multiply a string with an int.Line: "+ (line+1)/2);
			 errors++;
		 
		
				  }
		 
	}
	
	public void outASubExpression(ASubExpression num){
		 String var1="1";
		 String var2="2";
		 int line=0;
		 
				AIdExpression id1 = (AIdExpression) num.getFisrt();
		 int count =0;
		 if(globalVariables.containsKey(num.getFisrt().toString())){
			   var1=globalVariables.get(num.getFisrt().toString()).toString();
			  	
		 }else{
			 
			  if(num.getFisrt() instanceof AIdExpression ){
				  
				 Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				line = ((TId) id1.getId()).getLine();
			 
				String name1 = id1.getId().toString();
			 
				 
				Hashtable temp1 = fun.getArgList();
				
				if(temp1.containsKey(name1) ){
					var1 = temp1.get(name1).toString();
					 
					if((var1 == "" ) && fun.getIsCalled()){count++;}
				
					 
				}else{return;}
			  }
			 
				if(num.getFisrt() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getFisrt();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var1="int";
							ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var1="string";
							AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}	 
				}else if( num.getFisrt() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getFisrt();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var1 = temp.getReturn();
				}
			}		 
  
		if(globalVariables.containsKey(num.getNext().toString())){
			   var2=globalVariables.get(num.getNext().toString()).toString();
			   //Cast the expression to a value expression.
						 
			    	
			}else{
				
				 if(num.getNext() instanceof AIdExpression ){
				  
				Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id2 = (AIdExpression) num.getNext();
				line = ((TId) id2.getId()).getLine();
				 
				String name2 = id2.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp = fun.getArgList();
				
				if(temp.containsKey(name2) ){
					var2 = temp.get(name2).toString();
					 
					if((var2 == "" ) && fun.getIsCalled()){count++;System.out.println("Variaaables haven't initialized ");errors++;}
					 
				}else{return;}
				 }
				if(num.getNext() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getNext();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var2="int";
							 ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var2="string";
						 AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}		 
					}else if( num.getNext() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getNext();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var2 = temp.getReturn();
				}			 
				 
			}
			
			 if(((var1 != var2 ) || count ==2) ){
					  System.out.println("Error!! You cannot substract a string with an int.Line: "+ (line+1)/2);
			 
		 errors++;
		
				  }
		 
	}
	
	public void outADivExpression(ADivExpression num){
		 String var1="1";
		 String var2="2";
		
		 int line=0;
		  int count =0;
		 if(globalVariables.containsKey(num.getFisrt().toString())){
			   var1=globalVariables.get(num.getFisrt().toString()).toString();
			  	
		 }else{
			 
			  if(num.getFisrt() instanceof AIdExpression  ){
				  
				 Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id1 = (AIdExpression) num.getFisrt();
				line = ((TId) id1.getId()).getLine();
				 
				String name1 = id1.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp1 = fun.getArgList();
				
				if(temp1.containsKey(name1) ){
					var1 = temp1.get(name1).toString();
					 
					if((var1 == "" ) && fun.getIsCalled()){count++;}
				
					 
				}else{return;}
				
			}
				if(num.getFisrt() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getFisrt();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var1="int";
							ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var1="string";
							AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}	 
				}else if( num.getFisrt() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getFisrt();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var1 = temp.getReturn();
				}
			}		 
  
		if(globalVariables.containsKey(num.getNext().toString())){
			   var2=globalVariables.get(num.getNext().toString()).toString();
			   //Cast the expression to a value expression.
						 
			    	
			}else{
				
				
				 if(num.getNext() instanceof AIdExpression ){
				   Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				//Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				AIdExpression id2 = (AIdExpression) num.getNext();
				line = ((TId) id2.getId()).getLine();
				 
				String name2 = id2.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp = fun.getArgList();
				
				if(temp.containsKey(name2) ){
					var2 = temp.get(name2).toString();
					 
					if((var2 == "" ) && fun.getIsCalled()){count++;System.out.println("Variaaables haven't initialized ");errors++;}
					 
				}else{return;}
				
				}
				if(num.getNext() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getNext();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var2="int";
							 ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var2="string";
						 AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}		 
					}else if( num.getNext() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getNext();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var2 = temp.getReturn();
				}			 
				 
			}
			
			 if(((var1 != var2 ) || count ==2) ){
					  System.out.println("Error!! You cannot div a string with an int.Line: "+ (line+1)/2);
			 
		 errors++;
		
				  }
		 
	}
	
	public void outAModExpression(AModExpression num){
		 String var1="1";
		 String var2="2";
		  
		 int line=0;
		 int count =0;
		 if(globalVariables.containsKey(num.getFisrt().toString())){
			   var1=globalVariables.get(num.getFisrt().toString()).toString();
			  	
		 }else{
			 
			 
			  if(num.getFisrt() instanceof AIdExpression  ){
				   Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				
				AIdExpression id1 = (AIdExpression) num.getFisrt();
				line = ((TId) id1.getId()).getLine();
				 
				String name1 = id1.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp1 = fun.getArgList();
				
				if(temp1.containsKey(name1)){
					var1 = temp1.get(name1).toString();
					 
					if((var1 == "" ) && fun.getIsCalled()){count++;}
				
					 
				}else{return;}
				
			}
				
				if(num.getFisrt() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getFisrt();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var1="int";
							ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var1="string";
							AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}	 
				}else if( num.getFisrt() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getFisrt();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var1 = temp.getReturn();
				}
			}		 
  
		if(globalVariables.containsKey(num.getNext().toString())){
			   var2=globalVariables.get(num.getNext().toString()).toString();
			   //Cast the expression to a value expression.
						 
			    	
			}else{
				
				 if(num.getNext() instanceof AIdExpression ){
				   Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				 
				AIdExpression id2 = (AIdExpression) num.getNext();
				line = ((TId) id2.getId()).getLine();
				 
				String name2 = id2.getId().toString();
				 
				//de isxyei akoma ayto me tis synarthseis 
				Hashtable temp = fun.getArgList();
				
				if(temp.containsKey(name2) ){
					var2 = temp.get(name2).toString();
					 
					if((var2 == "" ) && fun.getIsCalled()){count++;System.out.println("Variables haven't been initialized ");errors++;}
					 
				}else{return;}
				
				}
				if(num.getNext() instanceof AValExpression){
						//Cast the expression to a value expression.
						AValExpression val_exp = (AValExpression)num.getNext();
					
						//If the type of the value is not INTEGER , print error message.
						if(val_exp.getValue() instanceof ANumValue){
							var2="int";
							 ANumValue num_val = (ANumValue)val_exp.getValue();
							TNumber t_num = num_val.getNumber();
							line=t_num.getLine();
						}else if(val_exp.getValue() instanceof AStringValue){
							var2="string";
						 AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							line=t_string.getLine();
						}		 
					}else if( num.getNext() instanceof AFunctionCallExpression){
					
					AFunctionCallExpression tmp1 = (AFunctionCallExpression)num.getNext();
					
					 
					AFuncCallFunctionCall afunc_call = (AFuncCallFunctionCall)tmp1.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					 
					TId id = afunc_call.getId();
					 
					 Function temp =(Function) functionsDefinition.get(id.toString());
						 
					 var2 = temp.getReturn();
				}		 
				 
			}
			
			 if(((var1 != var2 ) || count ==2)){
					  System.out.println("Error!! You cannot mod a string with an int.Line: "+ (line+1)/2);
			 
		 errors++;
		
				  }
		 
	}
	
	public void inAFuncdefineFunction(AFuncdefineFunction node){
		//set the boolean true because it is in function
		inFunction =true;
		String name = node.getId().toString();  
		int line = ((TId) node.getId()).getLine();
		// set the name 
		nameOfCurFunc = name;
		currentFunctionsVar = new Hashtable();	
	}
	
	public void outAFuncdefineFunction(AFuncdefineFunction node){
		inFunction =false; 
		nameOfCurFunc = "";
	}
	
	public void inAIdExpression(AIdExpression node){
    
		String name = node.getId().toString();
		int line = ((TId) node.getId()).getLine();
		 
			if (inFunction){
				Function fun =(Function) functionsDefinition.get(nameOfCurFunc);
				Hashtable temp = fun.getArgList();
				if(!temp.containsKey(name) && !currentFunctionsVar.containsKey(name)){
					System.out.println("Error!! Not defined variable in function.Name: " + name + " Line: " + (line+1)/2);
					errors++;
				}
					
					
			} 
			
	}
	 
	public void inAPlusEqStatement(APlusEqStatement node){
    	String name = node.getId().toString();
		int line = ((TId) node.getId()).getLine();
		if (inFunction){
				if(!currentFunctionsVar.containsKey(name)){
					System.out.println("Error!! Not defined variable in function.Name: " + name + " Line: " + (line+1)/2);
					errors++;
				}
			}else{
				if(!globalVariables.containsKey(name)){
					System.out.println("Error!! Not defined variable.Name: " + name + " Line: " + (line+1)/2);
					errors++;
				}
			}
		 
		
	}
		 
	public void  inAMinusEqStatement( AMinusEqStatement node){
    
		String name = node.getId().toString();
		int line = ((TId) node.getId()).getLine();
		 
			if (inFunction){
				
				if(!currentFunctionsVar.containsKey(name)){
					System.out.println("Error!! Not defined variable in function.Name: " + name + " Line: " + (line+1)/2);
					errors++;
				}
								
			}else{
				if(!globalVariables.containsKey(name)){
					System.out.println("Error!! Not definedsds variable.Name: " + name + " Line: " + (line+1)/2);
					errors++;
				}
			}	
	}
}