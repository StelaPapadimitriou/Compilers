import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.Start;

public class ParserTest1
{
  public static void main(String[] args)
  {
    try
    {
      Parser parser =
        new Parser(
        new Lexer(
        new PushbackReader(
        new FileReader(args[0].toString()), 1024)));

      Start ast = parser.parse();
	  FillSymbolTable fst = new FillSymbolTable();
	ast.apply(fst);
	myVisitor mvst = new myVisitor(fst.getFuncDef(),fst.getGlobVar());
	ast.apply(mvst);
	if(fst.Errors() + mvst.Errors() != 0 ) {
		System.out.println("\n\n*************************");
		System.out.println("* E R R O R S--> " + (fst.Errors() + mvst.Errors())+ "      *");
		System.out.println("* W A R N I N G S--> " + fst.Warnings() + "  *");
		System.out.println("*************************");
	}else{
		System.out.println("\n\n********************************************");
		System.out.println("**                                        **");
		System.out.println("***                                      ***");
		System.out.println("****                                    ****");
		System.out.println("*****                                  *****");
		System.out.println("*******                               ******");
		System.out.println("******** SUCCESSFUL COMPILE :) :) :D *******");
		System.out.println("*******                               ******");
		System.out.println("*****                                  *****");
		System.out.println("****                                    ****");
		System.out.println("***                                      ***");
		System.out.println("**                                        **");
		System.out.println("********************************************");
    }
	if(fst.Warnings() !=0){System.out.println("\n\n* W A R N I N G S--> " + fst.Warnings() + "  *");}
	
    }catch (Exception e)
    {
      System.err.println(e);
    }
  }
}

