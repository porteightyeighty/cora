/**************************************************************************************************
 Copyright 2025--2026 Cynthia Kop

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

import java.util.Set;
import charlie.types.Type;
import charlie.types.TVar;
import charlie.terms.replaceable.Replaceable;
import charlie.terms.Term;
import charlie.terms.TermFactory;

/**
 * A substitution is a function that maps a finite set of replaceables to terms of the same type.
 * Substitutions can be both immutable, extendable (but not otherwise changeable), or fully mutable
 * (in which case also existing mappings can be altered).
 *
 * In polymorphic systems, substitutions may also map type variables to types.  In this case, some
 * care should be taken when applying the substitution, for it is not permitted to substitute a type
 * variable in the type of a replaceable without also substituting the replaceable itself.  (Doing
 * so will yield a PolymorphicSubstitutionException.)
 */
public interface Substitution extends Term.ISubstitution {
  /** This stores a fixed empty immutable substitution (also accessible through the of() method). */
  public static Substitution EMPTY = new ExtendableSubstitution().makeImmutable();

  /** Returns the Term that x is mapped to, or null if x is not mapped to anything. */
  Term get(Replaceable x);

  /** Returns the Type that alpha is mapped to, or null if alpha is not mapped to anything. */
  Type get(TVar alpha);

  /**
   * Returns the Term that x is mapped to, if anything, and if x is not mapped to anything then this
   * returns either x itself (if x is a variable) or λy1...yn.x[y1,...,yn] (if x is a meta-variable of
   * arity n).
   */
  default Term getReplacement(Replaceable x) {
    Term ret = get(x);
    if (ret == null) return TermFactory.makeTerm(x);
    return ret;
  }

  /**
   * Returns the Type that alpha is mapped to, if any, and alpha itself if the substitution does not
   * map this type variable to anything.
   */
  default Type getReplacement(TVar alpha) {
    Type ret = get(alpha);
    if (ret == null) return alpha;
    return ret;
  }

  /** 
   * This method replaces each variable x in the term by get(x) (or leaves x alone if x is not
   * in our domain), and similarly replaces Z⟨s1,...,sk⟩ with gamma(Z) = λx1...xk.t by
   * t[x1:=s1 gamma,...,xk:=sk gamma].  If applicable, type variables are also substituted.  The
   * result is returned.
   *
   * Both the original term and the current substitution are unaltered, as they are in principle
   * immutable objects.  However, for the sake of efficiency, substituting does *temporarily*
   * alter the Substitution when substituting lambda-expressions (as the binder is added to the
   * domain of the substitution and removed again after the substitution has been applied to the
   * subterm).  This may be relevant and require changing if Charlie is used in a concurrent way.
   *
   * Note that the result of substituting is a term where all binders in lambdas are freshly
   * generated.
   *
   * WARNING: in the case of a term with type variables and a polymorphic substitution, this may
   * throw a PolymorphicSubstitutionException when a variable is *not* substituted but some type
   * variable occurring in its type is.
   */
  Term applySubstitution(Term term);

  /**
   * This method replaces each type variable alpha in the type by get(alpha) (or leaves alpha
   * alone if alpha is not in our domain).  The result is returned, and neither the substitution
   * itself nor the given type are altered.
   */
  Type applySubstitution(Type type);

  /**
   * Returns the set of replaceables which are mapped to a term, including those which are mapped
   * to themselves.
   */
  Set<Replaceable> domain();

  /**
   * Returns the set of type variables which are mapped to a type, including those which are mapped
   * to themselves.
   */
  Set<TVar> typeDomain();

  /** Returns a copy of the current substitution. */
  public ExtendableSubstitution copy();

  /**
   * Puts an immutable wrapper around the present Substitution.  Beware: this does not create a
   * copy!  Changing the present substitution can still cause mutations to the result; only the
   * objects that receive the immutable wrapper cannot cause alterations to either it or the
   * underlying Renaming.
   */
  public Substitution makeImmutable();

  /** Creates an empty immutable substitution */
  public static Substitution of() {
    return EMPTY;
  }

  /** Creates an immutable substitution [x:=value] */
  public static Substitution of(Replaceable x, Term value) {
    return new ExtendableSubstitution(x, value).makeImmutable();
  }

  /** Creates an immutable substitution [x1:=s1,x2:=s2] */
  public static Substitution of(Replaceable x1, Term s1, Replaceable x2, Term s2) {
    ExtendableSubstitution ret = new ExtendableSubstitution(x1, s1);
    ret.extend(x2, s2);
    return ret.makeImmutable();
  }

  /** Creates an immutable substitution [x1:=s1,x2:=s2,x3:=s3] */
  public static Substitution of(Replaceable x1, Term s1, Replaceable x2, Term s2,
                                Replaceable x3, Term s3) {
    ExtendableSubstitution ret = new ExtendableSubstitution(x1, s1);
    ret.extend(x2, s2);
    ret.extend(x3, s3);
    return ret.makeImmutable();
  }
}

