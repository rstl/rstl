/*
 * Copyright IBM Corp. 2012
 */

package org.rstl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VariableImpl extends GenericStatementImpl implements Variable {
	
	String variableName;
	List<String> filters = new ArrayList<String>();
	List<String> filterArgs = new ArrayList<String>();

	protected VariableImpl(String id) {
		this(id, 0);
	}
	
	//TODO: let the grammar correctly separate out the filters this implementation does not allow
	// the character '|' in the filter argument
	protected VariableImpl(String id, int line){
		super(StatementType.variablestatement, id, line);
		String variableName = id;
		if (id.startsWith("^")) {
			int metaStop = id.indexOf('^', 1);
			String meta = id.substring(1, metaStop);
			String[] tokens = meta.split(",");
			ArrayList<Integer> offsets = new ArrayList<Integer>();
			for (String token: tokens) {
				offsets.add(Integer.parseInt(token));
			}
			String varData = variableName.substring(metaStop+1);
			this.variableName = varData.substring(0, offsets.get(0));
			int ix = 0;
			for (ix = 0; ix < offsets.size() - 1; ix++) {
					processFilter(varData.substring(offsets.get(ix), offsets.get(ix+1)));
			}
			processFilter(varData.substring(offsets.get(ix)));
		} else {
			this.variableName = variableName;
		}
	}
	
	private void processFilter(String filterString) {
		int filterArgAt = filterString.indexOf(':');
		String fName=null;
		String fArg=null;
		if (filterArgAt > 0) {
			fName = filterString.substring(0, filterArgAt);
			fArg = filterString.substring(filterArgAt + 1);
		} else {
			fName = filterString;
		}
		filters.add(fName);
		filterArgs.add(fArg);
	}

	public String getAppliedFilterPrefixString() {
		StringBuilder sb = new StringBuilder();
		for (int ix = filters.size()  - 1; ix >= 0; ix-- ) {
			sb.append(VarUtil.getFilterFunction(filters.get(ix)));
		}
		return sb.toString();
	}

	public String getAppliedFilterSuffixString() {
		StringBuilder sb = new StringBuilder();
		for (int ix = 0; ix < filters.size(); ix++) {
			sb.append(")");
		}
		return sb.toString();
	}

	public List<String> getFilters() {
		return filters;
	}
	
	public List<String> getFilterArgs() {
		return filterArgs;
	}

	public String getVariableName() {
		return variableName;
	}

}
