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
import charlie.types.Type;
import charlie.terms.replaceable.Replaceable;
import charlie.terms.MetaVariable;
import charlie.terms.Term;
import charlie.terms.Variable;
import charlie.terms.TermFactory;
import charlie.terms.TypingException;

/**
 * A MutableSubstitution is a substitution that can have mappings added, changed and
 * (in the case of replaceables) also removed.
 */
public class MutableSubstitution implements Substitution {
  private HashMap<Replaceable,Term> _mapping;
  private final HashMap<TVar,Type> _typeMapping;

  /** Creates an empty mutable substitution, thus far with empty domain. */
  public MutableSubstitution() {
    _mapping = new HashMap<Replaceable,Term>();
    _typeMapping = new HashMap<TVar,Type>();
  }

  /** Creates a mutable substitution [x:=s] */
  public MutableSubstitution(Replaceable x, Term s) {
    _mapping = new HashMap<Replaceable,Term>();
    _typeMapping = new HashMap<TVar,Type>();
    extend(x, s);
  }

  /** Creates a mutable substitution with a copy of the mapping from the given substitution */
  private MutableSubstitution(MutableSubstitution copyme) {
    _mapping = new HashMap<Replaceable,Term>(copyme._mapping);
    _typeMapping = new HashMap<TVar,Type>(copyme._typeMapping);
  }

  /** Returns a copy of the current substitution */
  public MutableSubstitution copy() {
    return new MutableSubstitution(this);
  }

  /** @return the term that x is mapped to, or null if x is not mapped to anything */
  public Term get(Replaceable x) {
    return _mapping.get(x);
  }

  /** @return the type that alpha is mapped to, or null if alpha is not mapped to anything */
  public Type get(TVar alpha) {
    return _typeMapping.get(alpha);
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
   * Returns the Type that alpha is mapped to; if alpha is not in the domain, then alpha itself is
   * returned instead.
   */
  public Type getReplacement(TVar alpha) {
    Type ret = _typeMapping.get(alpha);
    return ret == null ? alpha : ret;
  }

  /**
   * This returns the set of variables and meta-variables which are mapped to something
   * (possibly themselves).
   */
  public final Set<Replaceable> domain() {
    return _mapping.keySet();
  }

  /** Returns the set of type variables which are mapped to something (possibly themselves). */
  public Set<TVar> typeDomain() {
    return _typeMapping.keySet();
  }

  /** Remove the given key/value pair. */
  public void delete(Replaceable key) {
    _mapping.remove(key);
  }

  /**
   * Adds the key/value pair to the substitution.
   * This will return false and do nothing if there is an existing value for the key.
   * If they type of key and value do not match, a TypingException will be thrown instead.
   *
   * POLYMORPHISM NOTE: if the type of key has type variables in this, then the type component of
   * the substitution may be expanded to match value, and only if this is not possible a
   * TypingException will be thrown.  For example, if the current substitution is γ:
   * - if key = X_{α → β} and value is a term of type Int → Bool → Bool, and γ(α) = Int while
   *   β is not in the domain, then this call to extend will set γ(β) := Bool → Bool and
   *   γ(X) = value
   * - if key = X_{α → β} and value :: Int → Bool → Bool, and γ(β) = β, then the substitution
   *   cannot be extended for the type to match, so a TypingException is thrown
   * Hence, we preserve the invariant that if a variable is in the domain, then all its type
   * variables are as well.  These type variable mappings are not removed if the variable should
   * ever be removed from the domain!
   *
   * TODO; considerations:
   * - if I *fail* to extend, the type variables should not be added either, so I cannot just use
   *   a match; I'd have to create a new type mapping for that (probably handle this in a separate
   *   function to avoid polluting this one)
   * - we use extend/remove as part of substituting abstractions; if we remove a binder from the
   *   substitution, we also need to remove the type variables we added for its sake (or rather:
   *   we probably should not be adding type variables for it in the first place!)
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
   * Adds the key/value pair to the substitution, replacing an existing pair for key if there is
   * one (in this case true is returned, in the alternative case false).
   */
  public boolean replace(Replaceable key, Term value) {
    boolean overriding = !extend(key, value);
    if (overriding) _mapping.put(key, value);
    return overriding;
  }

  /**
   * This replaces each mapping [x:=s] by [x := s delta], and moreover extends the substitution
   * with all mappings [y:=t] in delta where y does not yet occur in our domain.  That is, if we
   * are γ, then this results in the substitution γ δ.
   */
  public void combine(Substitution delta) {
    // handle type mappings
    for (TVar alpha : _typeMapping.keySet()) {
      _typeMapping.put(alpha, delta.applySubstitution(_typeMapping.get(alpha)));
    }
    for (TVar beta : delta.typeDomain()) {
      _typeMapping.put(beta, delta.get(beta));
    }
    // handle variable and meta-variable mappings
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
    return type.substitute(_typeMapping);
  }

  /** Applies the current substitution to the given term and returns the result. */
  public Term applySubstitution(Term term) {
    /** TODO: deal with the case that the variable has type variables in it */
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

  /** TODO: deal with the case where z has type variables in it */
  private Term substituteMetaApplication(MetaVariable z, ArrayList<Term> args) {
    // set the args to the substituted arguments
    for (int i = 0; i < args.size(); i++) args.set(i, applySubstitution(args.get(i)));
    // if we're not substituting Z, then just create a new meta-application with the updated args
    Term value = _mapping.get(z);
    if (value == null) return TermFactory.createMeta(z, args);
    // if Z is mapped to λx1...xn.t, then create t[x1:=args1,...,xn:=argsn]
    MutableSubstitution delta = new MutableSubstitution();
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

  /** TODO: ensure that all type variables in binder are already substituted */
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
    if (_typeMapping.isEmpty()) return _mapping.toString();
    else return _typeMapping.toString() + "\n" + _mapping.toString();
  }
}
