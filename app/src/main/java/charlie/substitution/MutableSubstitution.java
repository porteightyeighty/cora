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
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import charlie.util.NullStorageException;
import charlie.types.TVar;
import charlie.types.Type;
import charlie.terms.replaceable.Replaceable;
import charlie.terms.*;

/**
 * A MutableSubstitution is a substitution that can have mappings added, changed and
 * removed.  It ONLY supports variables and meta-variables in its domain, not type variables.
 * (This is because it would be counterintuitive to for instance add a variable, remove it again
 * and have the substitution not be the same.)
 */
public class MutableSubstitution implements Substitution {
  private HashMap<Replaceable,Term> _mapping;

  /** Creates an empty mutable substitution, thus far with empty domain. */
  public MutableSubstitution() {
    _mapping = new HashMap<Replaceable,Term>();
  }

  /** Creates a mutable substitution [x:=s], provided x and s have the same type */
  public MutableSubstitution(Replaceable x, Term s) {
    _mapping = new HashMap<Replaceable,Term>();
    extend(x, s);
  }

  /** Creates a mutable substitution with a copy of the mapping from the given substitution */
  private MutableSubstitution(MutableSubstitution copyme) {
    _mapping = new HashMap<Replaceable,Term>(copyme._mapping);
  }

  /** Returns an extendable (and possibly polymorphic) copy of the current substitution */
  public ExtendableSubstitution copy() {
    return new ExtendableSubstitution(this);
  }

  /** Returns a fully mutable copy of the current substitution */
  public MutableSubstitution mutableCopy() {
    return new MutableSubstitution(this);
  }

  /** @return the term that x is mapped to, or null if x is not mapped to anything */
  public Term get(Replaceable x) {
    return _mapping.get(x);
  }

  /** @return null, because no type variables can be added to mutable substitutions */
  public Type get(TVar alpha) {
    return null;
  }

  /**
   * @return the Term that x is mapped to; if x is not in the domain, then the term corresponding
   * to x is returned instead.
   */
  public Term getReplacement(Replaceable x) {
    Term ret = _mapping.get(x);
    if (ret != null) return ret;
    return TermFactory.makeTerm(x);
  }

  /** @return alpha, since type variables are not changed by MutableSubstitutions. */
  public Type getReplacement(TVar alpha) {
    return alpha;
  }

  /**
   * This returns the set of variables and meta-variables which are mapped to something
   * (possibly themselves).
   */
  public final Set<Replaceable> domain() {
    return _mapping.keySet();
  }

  /** Returns the empty set. */
  public Set<TVar> typeDomain() {
    return Set.of();
  }

  /** Remove the given key/value pair. */
  public void delete(Replaceable key) {
    _mapping.remove(key);
  }

  /**
   * Adds the key/value pair to the substitution.
   *
   * If the key is already mapped to a different value, a DuplicateMappingInSubstitutionException
   * is thrown.
   * If the key is already mapped to the same value, false is returned (and nothing is done).
   * If the key is not yet in the domain and the types are different, a TypingException is thrown.
   * If the key is not yet in the domain and the types are the same, then the extension succeeds
   * and true is returned.
   *
   * POLYMORPHISM NOTE: unlike ExtendableSubstitution, extending a MutableSubstitution requires
   * type _equality_ between key and value.  It does not suffice if the type of key can be
   * _instantiated_ to value.
   */
  public boolean extend(Replaceable key, Term value) {
    if (key == null) throw new NullStorageException("MutableSubstitution", "key");
    if (value == null) throw new NullStorageException("MutableSubstitution", "value");
    Term existing = _mapping.get(key);
    if (existing != null) {
      if (existing.equals(value)) return false;
      throw new DuplicateMappingInSubstitutionException(key, existing, value);
    }
    if (!key.queryType().equals(value.queryType())) {
      throw new TypingException("Cannot map ", key, " to ", value, " in MutableSubstitution, " +
        "since the types are different: ", key.queryType(), " versus ", value.queryType(), ".");
    }
    arityCheck(key, value);
    _mapping.put(key, value);
    return true;
  }

  /**
   * Adds the key/value pair to the substitution, replacing an existing pair for key if there is
   * one (in this case true is returned, in the alternative case false).
   */
  public boolean replace(Replaceable key, Term value) {
    if (key == null) throw new NullStorageException("MutableSubstitution", "key");
    if (value == null) throw new NullStorageException("MutableSubstitution", "value");
    if (!key.queryType().equals(value.queryType())) {
      throw new TypingException("Cannot replace value of ", key, " to ", value,
        " in MutableSubstitution, since the types are different: ", key.queryType(), " versus ",
        value.queryType(), ".");
    }
    arityCheck(key, value);
    boolean ret = _mapping.containsKey(key);
    _mapping.put(key, value);
    return ret;
  }

  /**
   * Helper function for extend and replace: given that key has arity n, this checks if the given
   * term value has a shape λx_1...x_n.sub (where sub is still allowed to be an abstraction).
   *
   * If so, nothing happens.  If not, a TypingException is thrown.
   */
  private void arityCheck(Replaceable key, Term value) {
    int arity = key.queryArity();
    Term tmp = value;
    while (arity > 0) {
      if (!tmp.isAbstraction()) {
      throw new TypingException("Cannot map meta-variable ", key, " (with arity " +
        key.queryArity() + ") to value ", value, " in MutableSsubstitution: the value should be " +
        "an abstraction with at least " + key.queryArity() + " abstracted variables.");
      }
      arity--;
      tmp = tmp.queryAbstractionSubterm();
    }
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

  /** Applies the current substitution to the given type and returns the result. */
  public Type applySubstitution(Type type) {
    return type;
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
    else throw new IllegalArgumentException("MutableSubstitution::applySubstitution called with " +
      "a term that does not have any of the standard term shapes!");
  }

  /**
   * If z is substituted to λx1...xn.t, this returns t[x1:=arg1,...,xn:=argsn].  If z is not
   * substituted, this returns z[args1 subst, ..., argsn subst].
   */
  private Term substituteMetaApplication(MetaVariable z, ArrayList<Term> args) {
    // set the args to the substituted arguments
    for (int i = 0; i < args.size(); i++) args.set(i, applySubstitution(args.get(i)));
    // if we're not substituting Z, then just create a new meta-application with the updated args
    Term value = _mapping.get(z);
    if (value == null) return TermFactory.createMeta(z, args);
    // if Z is mapped to λx1...xn.t, then create t[x1:=args1,...,xn:=argsn]
    MutableSubstitution delta = new MutableSubstitution();
    Term origvalue = value;
    for (int i = 0; i < args.size(); i++) {
      if (!value.isAbstraction()) {
        throw new TypingException("Arity error when trying to substitute ", z, " by ", origvalue,
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

  /**
   * This puts an immutable wrapper around the current substitution and returns the result.
   * Note that the current substitution itself can still be modified.
   */
  public Substitution makeImmutable() {
    return new ImmutableSubstitution(this);
  }

  /** Purely for debugging purposes! */
  public String toString() {
    return _mapping.toString();
  }
}
