import java.util.*;
public class Function{
	private int arguments;
	private String returntype;
	private String name;
	private Hashtable args = new Hashtable();
	private String argumentList;
	private ArrayList<String> argType = new ArrayList();
	private ArrayList<String> argName = new ArrayList();
	private int defaultArgs=0;
	private boolean isCalled = false;
	
	
	
	public Function(){}
	
	public void setArg(int arg){
		this.arguments = arg;
	}
	
	public boolean getIsCalled(){
		return isCalled;
	}
	
	public void setIsCalled(boolean val){
		isCalled = val;
	}
	
	public void setReturn(String returntype){	
		this.returntype =returntype;
	}
	
	public void setName(String name){
		this.name= name;
	}
	
	
	public String getName(){
		return name;
	}
	
	public String getReturn(){
		
		return returntype;
	}
	
	public int getArgs(){
		
		return arguments;
		
	}
	
	public Hashtable getArgList(){
		return args;
	}
	
	public ArrayList getType(){
		return argType;
	}
	public ArrayList getArgName(){
		return argName;
	}
	
	public void insertArgToArglist(String key, Object value){
		if(args.containsKey(key)){
			args.remove(key);
		}
		    args.put(key,value);
			
	}
	
	public int getDefaultArgs(){
		return defaultArgs;
	}
	
	public void increaseDefaultArgs(){
		++defaultArgs;
	}
		
	
	public void insertType(String value){
		argType.add(value);	
	}
	public void insertName(String value){
		argName.add(value);	
	}
	
	public void removeType(int pla){
		argType.remove(pla);	
	}
	
}