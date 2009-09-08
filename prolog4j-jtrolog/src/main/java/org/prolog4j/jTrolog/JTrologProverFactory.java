package org.prolog4j.jTrolog;

import java.util.HashMap;
import java.util.Map;

import org.prolog4j.Prover;
import org.prolog4j.IProverFactory;

/**
 * An implementation of {@link IProverFactory} which always returns
 * {@link TuPrologLogger} instances.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class JTrologProverFactory implements IProverFactory {

	final static JTrologProverFactory INSTANCE = new JTrologProverFactory();

	private Map<String, Prover> proverMap = new HashMap<String, Prover>();

	@Override
	public Prover getProver(String name) {
		Prover prover = null;
		// protect against concurrent access of the proverMap
		synchronized (this) {
			prover = proverMap.get(name);
			if (prover == null) {
				prover = new JTrologProver(name);
				proverMap.put(name, prover);
			}
		}
		return prover;
	}
}