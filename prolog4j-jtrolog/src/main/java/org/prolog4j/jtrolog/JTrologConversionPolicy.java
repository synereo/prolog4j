package org.prolog4j.jtrolog;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jTrolog.engine.Prolog;
import jTrolog.terms.Int;
import jTrolog.terms.Struct;
import jTrolog.terms.StructAtom;
import jTrolog.terms.Term;

import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;

/**
 * jTrolog implementation of the conversion policy.
 */
public class JTrologConversionPolicy extends ConversionPolicy {

	/** Converts an Integer object to a term. */
	private static final Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
		@Override
		public Object convert(Integer i) {
			return new Int(i);
		}
	};
	/** Converts a Long object to a term. */
	private static final Converter<Long> LONG_CONVERTER = new Converter<Long>() {
		@Override
		public Object convert(Long value) {
			return new jTrolog.terms.Long(value);
		}
	};
	/** Converts a Float object to a term. */
	private static final Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
		@Override
		public Object convert(Float value) {
			return new jTrolog.terms.Float(value);
		}
	};
	/** Converts a Double object to a term. */
	private static final Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
		@Override
		public Object convert(Double value) {
			return new jTrolog.terms.Double(value);
		}
	};
	/** Converts a String object to a term. */
	private static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		@Override
		public Object convert(String s) {
			return new StructAtom(s);
		}
	};
	/** Converts an jTrolog.terms.Int term to an Integer object. */
	private static final Converter<Int> INT_TERM_CONVERTER = new Converter<Int>() {
		@Override
		public Object convert(Int value) {
			return value.intValue();
		}
	};
	/** Converts an jTrolog.terms.Long term to a Long object. */
	private static final Converter<jTrolog.terms.Long> LONG_TERM_CONVERTER = 
		new Converter<jTrolog.terms.Long>() {
			@Override
			public Object convert(jTrolog.terms.Long value) {
				return value.longValue();
			}
		};
	/** Converts an jTrolog.terms.Float term to a Float object. */
	private static final Converter<jTrolog.terms.Float> FLOAT_TERM_CONVERTER = 
		new Converter<jTrolog.terms.Float>() {
			@Override
			public Object convert(jTrolog.terms.Float value) {
				return value.floatValue();
			}
		};
	/** Converts an jTrolog.terms.Double term to a Double object. */
	private static final Converter<jTrolog.terms.Double> DOUBLE_TERM_CONVERTER = 
		new Converter<jTrolog.terms.Double>() {
			@Override
			public Object convert(jTrolog.terms.Double value) {
				return value.doubleValue();
			}
		};
	/** Converts an jTrolog.terms.StructAtom term to a String object. */
	private static final Converter<StructAtom> ATOM_TERM_CONVERTER = new Converter<StructAtom>() {
		@Override
		public Object convert(StructAtom value) {
			return value.name;
		}
	};

	/**
	 * Constructs a conversion policy for jTrolog.
	 */
	public JTrologConversionPolicy() {
		addObjectConverter(Long.class, LONG_CONVERTER);
		addObjectConverter(Float.class, FLOAT_CONVERTER);
		addObjectConverter(Double.class, DOUBLE_CONVERTER);
		addObjectConverter(Integer.class, INTEGER_CONVERTER);
		addObjectConverter(String.class, STRING_CONVERTER);
		addObjectConverter(Object[].class, new Converter<Object[]>() {
			@Override
			public Object convert(Object[] array) {
				Struct pList = Term.emptyList;
				for (int i = array.length - 1; i >= 0; --i) {
					pList = new Struct(".", 
							new Term[]{(Term) convertObject(array[i]), pList});
				}
				return pList;
			}
		});
		addObjectConverter(List.class, new Converter<List>() {
			@Override
			public Object convert(List list) {
				Struct pList = Term.emptyList;
				ListIterator<?> it = list.listIterator(list.size());
				while (it.hasPrevious()) {
					pList = new Struct(".", new Term[]{(Term) convertObject(it
							.previous()), pList});
				}
				return pList;
			}
		});
		addObjectConverter(Compound.class, new Converter<Compound>() {
			@Override
			public Object convert(Compound value) {
				String functor = value.getFunctor();
				Object[] args = value.getArgs();
				Term[] tArgs = new Term[value.getArity()];
				for (int i = 0; i < args.length; ++i) {
					tArgs[i] = (Term) convertObject(args[i]);
				}
				return new Struct(functor, tArgs);
			}
		});
		addObjectConverter(Term.class, new Converter<Term>() {
			@Override
			public Object convert(Term value) {
				return value;
			}
		});
		addTermConverter(Int.class, INT_TERM_CONVERTER);
		addTermConverter(jTrolog.terms.Long.class, LONG_TERM_CONVERTER);
		addTermConverter(jTrolog.terms.Float.class,
				FLOAT_TERM_CONVERTER);
		addTermConverter(jTrolog.terms.Double.class,
				DOUBLE_TERM_CONVERTER);
		addTermConverter(Struct.class, new Converter<Struct>() {
			@Override
			public Object convert(Struct value) {
				if (isList(value)) {
					int length = listSize(value);
					Object[] array = new Object[length];
					for (int i = 0; i < length; ++i) {
						Term t = value.getArg(0);
//						array[i] = policy.convertTerm(t.getTerm());
//						value = (Struct) value.getArg(1).getTerm();
						array[i] = convertTerm(t);
						value = (Struct) value.getArg(1);
					}
					return array;
				}
				int arity = value.arity;
				Object[] args = new Object[arity];
				for (int i = 0; i < arity; ++i) {
//					args[i] = policy.convertTerm(value.getArg(i).getTerm());
					args[i] = convertTerm(value.getArg(i));
				}
				return new Compound(value.name, args);
				// return Terms.getInstance().toObject(value);
			}

			@Override
			public <R> R convert(Struct value, java.lang.Class<R> to) {
				if (isList(value) && to == List.class) {
					int length = listSize(value);
					List list = new ArrayList(length);
					for (int i = 0; i < length; ++i) {
						Term t = value.getArg(0);
//						list.add(policy.convertTerm(t.getTerm()));
//						value = (Struct) value.getArg(1).getTerm();
						list.add(convertTerm(t));
						value = (Struct) value.getArg(1);
					}
					return (R) list;
				}
				return null;
			}
		});
		addTermConverter(jTrolog.terms.StructAtom.class,
				ATOM_TERM_CONVERTER);
	}

	/**
	 * Determines whether a term is a list or not.
	 * 
	 * @param term the term
	 * @return <code>true</code> if the term is a list, otherwise <code>false</code>
	 */
	private static boolean isList(Term term) {
		if (!(term instanceof Struct)) {
			return false;
		}
		Struct list = (Struct) term;
		while (list != Term.emptyList) {
			if (!list.name.equals(".") || list.arity != 2 || !(list.getArg(1) instanceof Struct)) {
				return false;
			}
			list = (Struct) list.getArg(1);
		}
		return true;
	}

	/**
	 * Returns the size of a list.
	 * 
	 * @param list the list
	 * @return the size of the list
	 */
	private static int listSize(Struct list) {
		int size = 0;
		while (list != Term.emptyList) {
			if (!list.name.equals(".") || list.arity != 2 || !(list.getArg(1) instanceof Struct)) {
				throw new RuntimeException("It's not a list!");
			}
			++size;
			list = (Struct) list.getArg(1);
		}
		return size;
	}

	@Override
	public boolean match(Object term1, Object term2) {
		if (term1 instanceof Term && term2 instanceof Term) {
			return Prolog.match((Term) term1, (Term) term2);
		}
		return false;
	}

	@Override
	public boolean isInteger(Object term) {
		return term instanceof Int;
	}

	@Override
	public boolean isDouble(Object term) {
		return term instanceof jTrolog.terms.Double;
	}

	@Override
	public boolean isAtom(Object term) {
		return term instanceof StructAtom;
	}

	@Override
	public boolean isCompound(Object term) {
		return term instanceof Struct;
	}

	@Override
	public Object term(int value) {
		return new Int(value);
	}

	@Override
	public Object term(double value) {
		return new jTrolog.terms.Double(value);
	}

	@Override
	public Object term(String name) {
		return new StructAtom(name);
	}

	@Override
	public Object term(String name, Object... args) {
		Term[] tArgs = new Term[args.length];
		for (int i = 0; i < tArgs.length; ++i) {
			tArgs[i] = (Term) convertObject(args[i]);
		}
		return new Struct(name, tArgs);
	}

	@Override
	public int intValue(Object term) {
		return ((jTrolog.terms.Number) term).intValue();
	}

	@Override
	public double doubleValue(Object term) {
		return ((jTrolog.terms.Number) term).doubleValue();
	}

	@Override
	protected String getName(Object compound) {
		return ((Struct) compound).name;
	}

	@Override
	protected int getArity(Object compound) {
		return ((Struct) compound).arity;
	}

	@Override
	protected Object getArg(Object compound, int index) {
		return convertTerm(((Struct) compound).getArg(index));
	}

//	@Override
//	public org.prolog4j.Term pattern(String term) {
//		// TODO
//		throw new UnsupportedOperationException();
//	}

}
