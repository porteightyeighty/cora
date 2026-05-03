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

/**
 * A DuplicateMappingInSubstitutionException is thrown when someone tries to extend a substitution
 * with a variable (or type variable) that is already in the domain, but a different value.
 */
public class DuplicateMappingInSubstitutionException extends UserException {
  public DuplicateMappingInSubstitutionException(Object key, Object origmap, Object newmap) {
    super("Incorrect attempt to extend a substitution: ", key, " is already mapped to ", origmap,
      " so cannot now be mapped to ", newmap, ".");
  }
}

