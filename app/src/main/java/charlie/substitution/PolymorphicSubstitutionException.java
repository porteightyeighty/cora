/**************************************************************************************************
 Copyright 2026 Cynthia Kop

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

import charlie.util.UserException;
import charlie.types.TVar;
import charlie.terms.replaceable.Replaceable;

/**
 * A PolymorphicSubstitutionException is thrown when a substitution is applied to a replaceable
 * where the substitution itself is not in the domain of the substitution, but it has a type
 * which includes a variable that is.
 */
public class PolymorphicSubstitutionException extends UserException {
  public PolymorphicSubstitutionException(Replaceable replaceable, TVar alpha) {
    super("Incorrect management of polymorphism: the domain does not include ",
      replaceable.queryReplaceableKind() == Replaceable.Kind.METAVAR ? "meta-" : "",
      "variable ", replaceable, " even though the type variable ", alpha, " that occurs in " +
      "its type ", replaceable.queryType(), " is substituted.");
  }
}

