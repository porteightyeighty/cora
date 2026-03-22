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

/**
 * A substitution is a function that maps a finite set of replaceables to terms of the same type.
 * Substitutions can be both mutable and immutable.
 *
 * In polymorphic systems, substitutions may also map type variables to types.  In this case, some
 * care should be taken with applying the substitution, for it is not permitted to substitute a type
 * variable in the type of a replaceable without also substituting the replaceable itself.  (Doing
 * so will yield a PolymorphicSubstitutionException.)
 */
public interface Substitution extends Term.ISubstitution {
  /** Returns the Term that x is mapped to, or null if x is not mapped to anything. */
  Term get(Replaceable x);

  /** Returns the Type that alpha is mapped to, or null if alpha is not mapped to anything. */
  Type get(TVar alpha);

  /**
   * Returns the Term that x is mapped to, if anything, and if x is not mapped to anything then this
   * returns either x itself (if x is a variable) or λy1...yn.x[y1,...,yn] (if x is a meta-variable of
   * arity n).
   */
  Term getReplacement(Replaceable x);

  /**
   * Returns the Type that alpha is mapped to, if any, and alpha itself if the substitution does not
   * map this type variable to anything.
   */
  Type getReplacement(TVar alpha);

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
   */
  Term applySubstitution(Term term);

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
  public MutableSubstitution copy();

  /**
   * Puts an immutable wrapper around the present Substitution.  Beware: this does not create a
   * copy!  Changing the present substitution can still cause mutations to the result; only the
   * objects that receive the immutable wrapper cannot cause alterations to either it or the
   * underlying Renaming.
   */
  public Substitution makeImmutable();

  /** Creates an empty immutable substitution */
  public static Substitution of() {
    return MutableSubstitution.createBasic().makeImmutable();
  }

  /** Creates an immutable substitution [x:=value] */
  public static Substitution of(Replaceable x, Term value) {
    return MutableSubstitution.createBasic(x, value).makeImmutable();
  }

  /** Creates an immutable substitution [x1:=s1,x2:=s2] */
  public static Substitution of(Replaceable x1, Term s1, Replaceable x2, Term s2) {
    return MutableSubstitution.createBasic(x1, s1, x2, s2).makeImmutable();
  }

  /** Creates an immutable substitution [x1:=s1,x2:=s2,x3:=s3] */
  public static Substitution of(Replaceable x1, Term s1, Replaceable x2, Term s2,
                                Replaceable x3, Term s3) {
    return MutableSubstitution.createBasic(x1, s1, x2, s2, x3, s3).makeImmutable();
  }
}

