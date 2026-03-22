/**************************************************************************************************
 Copyright 2019--2026 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package charlie.substitution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import charlie.util.NullStorageException;
import charlie.types.TVar;
import charlie.terms.replaceable.Replaceable;
import charlie.terms.MetaVariable;
import charlie.terms.Term;
import charlie.terms.Variable;
import charlie.terms.TermFactory;
import charlie.terms.TypingException;

/**
 * A BasicMutableSubstitution is a substitution that can have mappings added, changed and removed.
 * Type variables are not mapped.
 */
final class BasicMutableSubstitution extends MutableSubstitution {
  /** Creates an empty substitution, with empty domain. */
  public BasicMutableSubstitution() {
    super();
  }

  /** Creates a copy of the given subtitution */
  private BasicMutableSubstitution(MutableSubstitution copyme) {
    super();
    _mapping = new HashMap<Replaceable,Term>(copyme._mapping);
  }

  /** Returns a copy of the current substitution */
  public BasicMutableSubstitution copy() {
    return new BasicMutableSubstitution(this);
  }

  /** Returns null, since type variables are not in the domain of a basic substitution. */
  public TVar get(TVar alpha) {
    return null;
  }

  /**
   * Returns the Term that x is mapped to; if x is not in the domain, then the term corresponding
   * to x is returned instead.
   */
  public Term getReplacement(Replaceable x) {
    Term ret = _mapping.get(x);
    if (ret != null) return ret;
    return TermFactory.makeTerm(x);
  }

  /**
   * Returns alpha, since type variables are not in the domain of a basic substitution.
   */
  public TVar getReplacement(TVar alpha) {
    return alpha;
  }

  /**
   * Adds the key/value pair to the substitution.
   * This will return false and do nothing if there is an existing value for the key.
   */
  public boolean extend(Replaceable key, Term value) {
    if (key == null) throw new NullStorageException("MutableSubstitution", "key");
    if (value == null) throw new NullStorageException("MutableSubstitution", "value");
    if (!key.queryType().equals(value.queryType())) {
      throw new TypingException("Cannot map key ", key, " (of type ", key.queryType(), ") to " +
        "value ", value, " (of type ", value.queryType(), ") in substitution.");
    }
    int a = key.queryArity();
    if (a > 0) {
      Term tmp = value;
      while (a > 0) {
        if (!tmp.isAbstraction()) {
          throw new TypingException("Cannot map meta-variable ", key, " (with arity " +
            key.queryArity() + ") to value ", value, " in substitution: the value should be an " +
            "abstraction with at least " + key.queryArity() + " abstracted variables.");
        }
        a--;
        tmp = tmp.queryAbstractionSubterm();
      }
    }
    if (_mapping.get(key) != null) return false;
    _mapping.put(key, value);
    return true;
  }

  /**
   * This replaces each mapping [x:=s] by [x := s delta], and moreover extends the substitution
   * with all mappings [y:=t] in delta where y does not yet occur in our domain.  That is, if we
   * are γ, then this results in the substitution γ δ.
   */
  public void combine(Substitution delta) {
    for (Replaceable x : _mapping.keySet()) {
      _mapping.put(x, delta.applySubstitution(_mapping.get(x)));
    }
    for (Replaceable y : delta.domain()) {
      if (!_mapping.containsKey(y)) {
        _mapping.put(y, delta.get(y));
      }
    }
  }

  /** Returns the empty set, since there are no type variables in our domain. */
  public Set<TVar> typeDomain() {
    return Set.of();
  }

  /** Purely for debugging purposes! */
  public String toString() {
    return _mapping.toString();
  }

  /** Applies the current substitution to the given term and returns the result. */
  public Term applySubstitution(Term term) {
    if (term.isVariable()) return getReplacement(term.queryVariable());
    else if (term.isConstant()) return term;
    else if (term.isMetaApplication()) {
      return substituteMetaApplication(term.queryMetaVariable(), term.queryMetaArguments());
    }
    else if (term.isApplication()) {
      return substituteApplication(term.queryHead(), term.queryArguments());
    }
    else if (term.isAbstraction()) {
      return substituteAbstraction(term.queryVariable(), term.queryAbstractionSubterm());
    }
    else throw new IllegalArgumentException("Substitution::applySubstitution called with a term " +
      "that does not have any of the standard term shapes!");
  }

  private Term substituteMetaApplication(MetaVariable z, ArrayList<Term> args) {
    // set the args to the substituted arguments
    for (int i = 0; i < args.size(); i++) args.set(i, applySubstitution(args.get(i)));
    // if we're not substituting Z, then just create a new meta-application with the updated args
    Term value = _mapping.get(z);
    if (value == null) return TermFactory.createMeta(z, args);
    // if Z is mapped to λx1...xn.t, then create t[x1:=args1,...,xn:=argsn]
    MutableSubstitution delta = new BasicMutableSubstitution();
    for (int i = 0; i < args.size(); i++) {
      if (!value.isAbstraction()) {
        throw new TypingException("Arity error when trying to substitute ", z, " by ", value,
          ": meta-variable takes " + args.size() + " arguments, so there should be at least " +
          "this many abstractions!");
      }   
      Variable x = value.queryVariable();
      value = value.queryAbstractionSubterm();
      delta.replace(x, args.get(i));
    }   
    return delta.applySubstitution(value);
  }

  private Term substituteApplication(Term head, ArrayList<Term> args) {
    head = applySubstitution(head);
    for (int i = 0; i < args.size(); i++) args.set(i, applySubstitution(args.get(i)));
    return head.apply(args);
  }

  private Term substituteAbstraction(Variable binder, Term subterm) {
    Variable freshvar = TermFactory.createBinder(binder.queryName(), binder.queryType());
    Term previous = _mapping.get(binder);
    _mapping.put(binder, freshvar);
    Term subtermSubstitute = null;
    RuntimeException exc = null;
    try { subtermSubstitute = applySubstitution(subterm); }
    catch (RuntimeException e) { exc = e; }
    if (previous == null) _mapping.remove(binder);
    else _mapping.put(binder, previous);
    // forward the possible exception now that we've restored the substitution
    if (exc != null) throw exc;
    return TermFactory.createAbstraction(freshvar, subtermSubstitute);
  }

  public Substitution makeImmutable() {
    return new ImmutableSubstitution(this);
  }
}
