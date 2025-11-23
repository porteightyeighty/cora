/**************************************************************************************************
 Copyright 2019--2025 Cynthia Kop

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the License for the specific language governing permissions and limitations under the License.
 *************************************************************************************************/

package charlie.types;

import java.util.List;

/**
 * Type ::= Base | Arrow(Type, Type) | Data(Type,...,Type) | TVar(name).
 *
 * Here, there are two kinds of sorts:
 * - base types: represented by a single name
 * - data types: represented by a constructor (a name) and a non-empty list of arguments
 *
 * In first-order term rewriting, all types are Base.
 *
 * In simply-typed rewriting, all types are Base or Arrow.
 *
 * Typically, data types that take arguments are used in polymorphic settings that also consider
 * type variables.  However, this is not mandatory; data types can also be useful for instance to
 * represent product types.
 *
 * Writing Arrow as the right-associative operator →, we can write all types in a form
 * σ1 → ... → σm → τ, where all σi are Types and τ is a sort or type variable.  This will often
 * be used for inductive reasoning.
 *
 * Note: all instances of Type must (and can be expected to) be immutable.
 */
public sealed interface Type permits
  Base, Arrow, Data, TVar, Product {  // TODO: remove Product

  /** Returns true for base types, false for arrow types, data types and type variables. */
  default boolean isBaseType() { return false; }

  /** Returns true for arrow types, false for base types, data types and type variables. */
  default boolean isArrowType() { return false; }

  /** Returns true for data types, false for base types, arrow types and type variables. */
  default boolean isDataType() { return false; }

  /** Returns true for type variables, false for base types, arrow types and data types. */
  default boolean isVariableType() { return false; }

// TODO: remove
  /** Returns true for product types, false for base types and arrow types. */
  default boolean isProductType() { return false; }

  /** Returns true for base types and data types, false for arrow types and type variables. */
  boolean isSort();

  /** Returns true if this type is built exclusively from base types and arrows. */
  boolean isSimple();

  /**
   * Returns true if this type is a sort that is built exclusively exclusively from base types and
   * the product constructor (so no arrows or type variables): a sort with full type order 0.
   */
  boolean isZeroSort();

  /**
   * Returns true if this is a monomorphic type: a type without any use of type variables.
   * (In first-order and simply-typed term rewriting, this is always the case.)
   */
  boolean isMonomorphic();

  /**
   * Returns true if the type is built entirely from theory sorts and arrows; type variables and
   * more sophisticated constructors are not permitted.
   * Theory sorts are the base types specifically created as theory, accessible from the type
   * factory.
   */
  boolean isTheoryType();

  /** Returns true if this is one of the pre-defined theory base types (e.g., Int, Bool). */
  default boolean isBaseTheoryType() { return isTheoryType() && isBaseType(); }

  /** For σ1 → ,,, → σm → τ with τ not an arrow type, returns m. */
  default int queryArity() { return 0; }

  /**
   * Returns the number of immediate subtypes.
   * For a base type or type variable, this is 0.
   * For an arrow type, this is 2.
   * For a data type c(A_1,...,A_n), this is n.
   */
  int queryNumberSubtypes();

  /**
   * If i is between 1 and queryNumberSubtypes(), this returns the corresponding subtype (from left
   * to right) of the type.  Otherwise, an IndexOutOfBoundsException is thrown.
   */
  Type querySubtype(int i);

  /** For σ1 → ,,, → σm → τ with τ not an arrow type, returns τ */
  default Type queryOutputType() { return this; }

  /**
   * Returns the type order of the current type, considering all type variables and data types as
   * just a sort with type order 0.
   * For base types, data types and type variables, this is 0.
   * For σ1 → ... → σm → τ, it is max(order(σ1)+1,...,order(σk)+1,order(τ)).
   */
  default int querySimpleTypeOrder() { return 0; }

  /**
   * Returns the type order of the current type, taking data types and type variables in
   * For base types, this is 0.
   * For data types c(σ1,...,σk) it is max(order(σ1),...,order(σm)).
   * For σ1 → ... → σm → τ, it is max(order(σ1)+1,...,order(σk)+1,order(τ)).
   * And for type variables, it is undefined: if any type variable occurs anywhere in the type,
   * Integer.MAX_VALUE is returned.
   */
  int queryFullTypeOrder();

  /** Returns whether the given Type is equal to us. */
  boolean equals(Type type);
}

