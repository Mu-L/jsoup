package org.jsoup.select.ng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Evaluator;

public class OrSelector extends Evaluator {
	List<Evaluator> selectors;

	public OrSelector(Collection<Evaluator> selectors) {
		super();
		this.selectors = new ArrayList<Evaluator>();
		this.selectors.addAll(selectors);
	}
	
	public void add(Evaluator e) {
		selectors.add(e);
	}


	@Override
	public boolean matches(Element node) {
		for(Evaluator s : selectors) {
			if(s.matches(node))
				return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return String.format(":or%s", selectors);
	}
}