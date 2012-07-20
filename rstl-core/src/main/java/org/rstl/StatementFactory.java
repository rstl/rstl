/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;


public class StatementFactory {
	int identifier;
	
	public StatementFactory() {
		identifier = 0;
	}
	
	public int getStatementId() {
		int ret = identifier;
		identifier ++;
		return ret;
	}

	private Statement create(StatementType type, int line, String... args) {
		switch (type) {
		case extendsstatement:
		case layoutstatement:
			String[] params = args[0].split("\\^", -2);
			String ident = params[0];
			int startIndex = 0;
			int stopIndex = 0;
			if (params.length > 1) {
				try {
					startIndex = Integer.parseInt(params[1]);
					stopIndex =  Integer.parseInt(params[2]);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
			return new GenericStatementImpl(type, ident, line, startIndex, stopIndex);
		case includestatement:
		case includeoncestatement:
		case rgroupendstatement:
			return new GenericStatementImpl(type, args[0], line);
		case forstatement:
			return new ForLoopImpl(args[0], args[1], line, this);
		case blockstatement:
			return new BlockImpl(args[0],line, this);
		default:
			if (args.length < 1) {
				throw new IllegalArgumentException(
						"Need atleast one specifier for a custom statement");
			}
			return new GenericStatementImpl(type, args[0], line);
		}
	}
	
	public Statement createExtends(String encodedIdentText, int line) {
		return create(StatementType.extendsstatement, line, encodedIdentText);
	}
	
	public Statement createLayout(String encodedIdentText, int line) {
		return create(StatementType.layoutstatement, line, encodedIdentText);
	}

	public  BlockImpl createBlock(String name) {
		return createBlock(name, 0);
	}
	
	public  BlockImpl createBlock(String name, int line) {
		return new BlockImpl(name, line, this);
	}

	public  Chunk createChunk(String value) {
		return createChunk(value, 0);
	}
	
	public  Chunk createChunk(String value, int line) {
		return new Chunk("" + getStatementId(), TemplateUtil.literalize(value), value, line);
	}

	public  VariableImpl createVariable(String name) {
		return createVariable(name, 0);
	}
	
	public  VariableImpl createVariable(String name, int line) {
		return new VariableImpl(name, line);
	}

	public  GenericStatementImpl createSuperBlockStatement(String name) {
		return createSuperBlockStatement(name, 0);
	}
	
	public  GenericStatementImpl createSuperBlockStatement(String name, int line) {
		return (GenericStatementImpl) create(StatementType.superblockstatement, line, name);
	}
	
	public  GenericStatementImpl createSuperRGroupStatement(String name) {
		return createSuperRGroupStatement(name, 0);
	}
	
	public  GenericStatementImpl createSuperRGroupStatement(String name, int line) {
		return (GenericStatementImpl) create(StatementType.superrgroupstatement, line, name);
	}
	
	public  GenericStatementImpl createRGroupTerminator(String name) {
		return createRGroupTerminator(name, 0);
	}
	
	public  GenericStatementImpl createRGroupTerminator(String name, int line) {
		return (GenericStatementImpl) create(StatementType.rgroupendstatement, line, name);
	}
	
	public  GenericStatementImpl createPreconditon(String name) {
		return createPrecondition(name, 0);
	}
	
	public  GenericStatementImpl createPrecondition(String name, int line) {
		return (GenericStatementImpl) create(StatementType.preconditionstatement, line, name);
	}
	
	public  GenericStatementImpl createInclude(String name) {
		return createInclude(name, 0);
	}
	
	public  GenericStatementImpl createInclude(String name, int line) {
		return (GenericStatementImpl) create(StatementType.includestatement, line, name);
	}
	
	public  GenericStatementImpl createIncludeOnce(String name) {
		return createIncludeOnce(name, 0);
	}
	
	public  GenericStatementImpl createIncludeOnce(String name, int line) {
		return (GenericStatementImpl) create(StatementType.includeoncestatement, line, name);
	}
	
	public Statement createCustom(String name, int line) {
		return create(StatementType.customstatement, line, name);
	}
	
	/**
	 * Create a for loop with the specified string
	 * @param forloop '^' delimited arguments, the first mandatory argument is a key, followed by an optional value
	 * the third argument is a collection, followed by an optional argument "reversed" to mean iterate through the 
	 * collection in reverse order.
	 * @return a ForLoop statement
	 */
	public  ForLoopImpl createForLoop(String forloop) {
		return createForLoop(forloop, 0);
	}
	
	/**
	 * Create a for loop with the specified string
	 * @param forloop '^' delimited arguments, the first mandatory argument is a key, followed by an optional value
	 * the third argument is a collection, followed by an optional argument "reversed" to mean iterate through the 
	 * collection in reverse order.
	 * @param line the line number where the forloop was declared
	 * @return a ForLoop statement
	 */
	public  ForLoopImpl createForLoop(String forloop, int line) {
		String[] args = forloop.split("\\^", -2);
		String key = args[0];
		String value = (args[1].isEmpty()) ? null : args[1];
		String collection = args[2];
		String rev = args[3];

		boolean reversed = (null != rev && "reversed".equalsIgnoreCase(rev)) ? true
				: false;
		return new ForLoopImpl(key, value, collection, reversed, line, this);
	}
	
	public  ResourceGroup createResourceGroup(String argument) {
		return createResourceGroup(argument, 0);
	}
	
	public  ResourceGroup createResourceGroup(String argument, int line) {
		String[] args = argument.split("\\^", -2);
		String ident = args[0];
		int startIndex = 0;
		if (args.length > 1) {
			try {
				startIndex = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return new ResourceGroup(ident, line, startIndex, this);
	}
	
	public  ResourceImpl createResourceJson(String argument) {
		return createResourceJson(argument, 0);
	}
	
	public  ResourceImpl createResourceJson(String argument, int line) {
		String[] args = argument.split("\\^", -2);
		if (args.length == 1) {
			return new ResourceImpl(args[0], "json", line);
		} else {
			return new ResourceImpl(args[0], args[1], args[2], null, "json", line);
		}
	}
	
	public  ResourceImpl createResourceXhtml(String argument) {
		return createResourceXhtml(argument, 0);
	}
	
	public  ResourceImpl createResourceXhtml(String argument, int line) {
		String[] args = argument.split("\\^", -2);
		if (args.length == 1) {
			return new ResourceImpl(args[0], "xhtml", line);
		} else {
			return new ResourceImpl(args[0], args[1], args[2], args[3], "xhtml", line);
		}
	}
	
	public  ConditionalImpl createConditional(String expr, int line) {
		return new ConditionalImpl(expr, line, this);
	}
	
}
