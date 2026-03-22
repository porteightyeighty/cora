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
import charlie.terms.replaceable.Replaceable;
import charlie.terms.MetaVariable;
import charlie.terms.Term;
import charlie.terms.Variable;
import charlie.terms.TermFactory;
import charlie.terms.TypingException;

/**
 * A MutableSubstitution is a substitution that can have mappings added, changed and removed.
 * We consider basic mutable substitutions (which do not allow for type variables and
 * consequently admit certain optimisations) and polymorphic mutable substitutions (which come with
 * additional safeguards and restrictions).
 */
public sealed abstract class MutableSubstitution implements Substitution
    permits BasicMutableSubstitution {
  
  protected HashMap<Replaceable,Term> _mapping;

  /**
   * This creates an empty mutable substitution whose domain may contain variables and
   * meta-variables, but not type variables.
   */
  public static MutableSubstitution createBasic() {
    return new BasicMutableSubstitution();
  }

  /** Creates an empty substitution, with empty domain. */
  protected MutableSubstitution() {
    _mapping = new HashMap<Replaceable,Term>();
  }

  /** Returns a copy of the current substitution */
  public abstract MutableSubstitution copy();

  /** @return the term that x is mapped to, or null if x is not mapped to anything */
  public final Term get(Replaceable x) {
    return _mapping.get(x);
  }

  /**
   * Adds the key/value pair to the substitution.
   * This will check that the mapping is permitted, e.g., the types of key and value match.
   * If not, a TypingException or PolymorphicSubstitutionException will be thrown.
   * Then, if there is an existing value for the key, false is returned (and no update made); and
   * if there is not, then true is returned and the key/value pair added.
   */
  public abstract boolean extend(Replaceable key, Term value);

  /**
   * Adds the key/value pair to the substitution, replacing an existing pair for key if there is
   * one (in this case true is returned, in the alternative case false).
   */
  public final boolean replace(Replaceable key, Term value) {
    boolean overriding = !extend(key, value);
    if (overriding) _mapping.put(key, value);
    return overriding;
  }

  /** Remove the given key/value pair. */
  public final void delete(Replaceable key) {
    _mapping.remove(key);
  }

  /** This replaces the current substitution γ by γ δ. */
  public abstract void combine(Substitution delta);

  /**
   * This returns the set of variables and meta-variables which are mapped to something
   * (possibly themselves).
   */
  public final Set<Replaceable> domain() {
    return _mapping.keySet();
  }

  /** Applies the current substitution to the given term and returns the result. */
  public abstract Term applySubstitution(Term term);

  /**
   * This puts an immutable wrapper around the current substitution and returns the result.
   * Note that the current substitution itself can still be modified.
   */
  public Substitution makeImmutable() {
    return new ImmutableSubstitution(this);
  }

  /** Creates a mutable substitution [x:=value] */
  public static MutableSubstitution createBasic(Replaceable x, Term value) {
    MutableSubstitution ret = MutableSubstitution.createBasic();
    ret.extend(x, value);
    return ret;
  }

  /** Creates a mutable substitution [x1:=s1,x2:=s2] */
  public static MutableSubstitution createBasic(Replaceable x1, Term s1, Replaceable x2, Term s2) {
    MutableSubstitution ret = MutableSubstitution.createBasic();
    ret.extend(x1, s1);
    ret.extend(x2, s2);
    return ret;
  }

  /** Creates a mutable substitution [x1:=s1,x2:=s2,x3:=s3] */
  public static MutableSubstitution createBasic(Replaceable x1, Term s1, Replaceable x2, Term s2,
                                                Replaceable x3, Term s3) {
    MutableSubstitution ret = MutableSubstitution.createBasic();
    ret.extend(x1, s1);
    ret.extend(x2, s2);
    ret.extend(x3, s3);
    return ret;
  }
}
