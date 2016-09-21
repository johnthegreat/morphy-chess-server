/*
 *   Morphy Open Source Chess Server
 *   Copyright (C) 2008-2010  http://code.google.com/p/morphy-chess-server/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package morphy.user;

import org.apache.commons.lang.StringUtils;

import morphy.game.NewMatchParams;
//import morphy.service.GameService;
import static morphy.game.NewMatchParams.*;

public class Formula {
	protected static final String[] validKeywords = { "abuser", "assessdraw", "assessloss",
			"assesswin", "black", "blitz", "bughouse", "computer",
			"crazyhouse", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9",
			"inc", "lightning", "losers", "maxtime", "mymaxtime", "myrating",
			"nocolor", "nonstandard", "private", "ratingdiff", "rating",
			"rated", "registered", "suicide", "standard", "time", "timeseal",
			"unrated", "untimed", "white", "wild" };
	
	// some parsing guidelines:
	// there are some expressions that are booleans, eg: !abuser , timeseal, 1 , 0
	// there are some expressions that require comparisons, eg: assesswin >= 5
	// all comparisons need to work, eg: >, >=, =, ==, <=, <, !=, <>
	// note that boolean variables (eg registered) still need to work with ALL COMPARISIONS!! (eg according to FICS, "registered < 5" is valid.)
	// we should convert all boolean variable values into integers (n>1 [use 1] being true, 0 being false)
	// (try to make the logic smart to where something like "assesswin <= 0" will be ignored)
	// when it comes to parsing f1-f9, these should be evaluated first
	// "|", "||", "or" should all mean the same thing (lenient parsing)
	// "&", "&&", "and" should all the mean the same thing (lenient parsing)
	// text in parenthesis "()" should always be evaluated first, even as according to order of operations.
	
	/*public static boolean isValidFormula(String formula) {
		// if any word outside of formula is not in validKeywords, (except comment) it is a bad formula.
		// if two variants are &&'d together (blitz && bughouse) it is invalid.
		// comment symbol is #.
		// also look for stupid things like rated && !rated.
		// things like !> is not valid. <= should be used instead.
		// if formula starts with #, or always will evaluate to true (e.g. '1') allow anything.
		// if formula is something that will always evaluate to false (e.g. rating>9999) disallow everything.
		
		
		return false;
	}*/
	
	public static void main(String[] args) {
		java.util.Arrays.sort(validKeywords);
		
		String formula = "(time >= 3 & inc = 0) || 1";
		Formula f = new Formula(formula);
		
		NewMatchParams m = new NewMatchParams();
		m.setParam(Key.time,3);
		m.setParam(Key.inc,0);
		m.setParam(Key.rated,true);
		f.matches(m);
		//System.out.println();
	}
	
	public boolean matches(NewMatchParams params) {
		String[][] patternfixes = { 
				/*{"\\s+",""},
				{"(\\s+and\\s+)"," && "}, 
				{"(\\s+&\\s+)"," && "},
				{"(\\s+\\|\\s+)"," || "},
				{"(\\s+or\\s+)"," || "}*/
				{"\\s+",""},
				{"&"," && "},
				{"and"," && "}, 
				{"\\s*?\\|\\s*?"," || "},
				{"or"," || "}
		};
		String tmpformula = formula.toLowerCase();
		
		for(int i=0;i<patternfixes.length;i++) {
			tmpformula = tmpformula.replaceAll(patternfixes[i][0],patternfixes[i][1]);
		}
		
		System.out.println(tmpformula);
		
		String[] operators = { ">=","<=",">","<","!=","<>","=" };
		
		int parenCount = 0;
		String[] arr = tmpformula.split(" ");
		System.out.println(java.util.Arrays.toString(arr));
		for(int i=0;i<arr.length;i++) {
			String s = arr[i];
			if (s.startsWith("(")) parenCount++;
			for(int j=0;j<operators.length;j++) {
				if (s.contains(operators[j])) {
					s = s.replace(operators[j]," " + operators[j] + " ");
					break;
				}
			}
			
			String[] tmp = s.split(" ");
			//System.out.println(java.util.Arrays.toString(tmp));
			
			if (StringUtils.isNumeric(tmp[0]) && tmp.length == 1) {
				//int k = Integer.parseInt(tmp[0]);
				//boolean b = k>0?true:false; 
			} else if (tmp.length == 3) {
				// this has to a variable comparison expression
				String variable = tmp[0].replace("(","");
				String cmpoper = tmp[1];
				String value = tmp[2].replace(")","");
				Key k = Key.valueOf(variable);
				System.out.println(variable + " " + cmpoper + " " + value + ": " + cmp(params.getParam(k), cmpoper, value));
				s = ""+cmp(params.getParam(k), cmpoper, value);
			} else if (tmp.length == 1) {
				// this has be a boolean expression
			} else {
				System.out.println("invalid expression: " + s);
			}
			
			//s = s.replace("(","").replace(")","");
			arr[i] = s;
			if (s.endsWith(")")) parenCount--;
		}
		System.out.println(java.util.Arrays.toString(arr));
		
		
		//MorphyStringTokenizer tok = new MorphyStringTokenizer(this.formula,"", isEatingBlocksOfDelimiters)
		
		return false;
	}
	
	private boolean cmp(Object o,String cmpoper,String value) {
		// String[] operators = { ">=","<=",">","<","!=","<>","=" };
		int intobj = 0;
		int intval = 0;
		if (o instanceof Integer) intobj = objToInt(o);
		if (o instanceof Boolean) intobj = objToBoolean(o)?1:0;
		if (StringUtils.isNumeric(value)) intval = Integer.parseInt(value);
		
		if (cmpoper.equals(">=")) return intobj >= intval;
		if (cmpoper.equals(">")) return intobj > intval;
		if (cmpoper.equals("<=")) return intobj <= intval;
		if (cmpoper.equals("<")) return intobj < intval;
		if (cmpoper.equals("=")) return intobj == intval;
		if (cmpoper.equals("!=") || cmpoper.equals("<>")) return intobj >= intval;
		return false;
	}

	public String formula;

	public Formula() {

	}

	public Formula(String formula) {
		this.formula = formula;
	}
}
