/**************************************************************************************************
 Copyright 2023--2025 Cynthia Kop

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

import java.util.Map;
import java.util.Set;
import charlie.util.NullStorageException;

/**
 * A base type is the simplest form of a sort: it takes no arguments, and is uniquely identified by
 * its name.
 */
public record Base(String name) implements Type {
  public Base {
    if (name == null) {
      throw new NullStorageException("Base", "name");
    }
  }

  @Override
  public boolean isBaseType() { return true; }

  @Override
  public boolean isSort() { return true; }

  @Override
  public boolean isSimple() { return true; }

  @Override
  public boolean isZeroSort() { return true; }

  @Override
  public boolean isMonomorphic() { return true; }

  @Override
  public boolean isTheoryType() { return UniqueTypes.isTheoryType(this); }

  @Override
  public int queryNumberSubtypes() { return 0; }

  @Override
  public Type querySubtype(int index) {
    throw new IndexOutOfBoundsException("Base::querySubtype called (with index " + index + ")");
  }

  @Override
  public int queryTypeOrder() { return 0; }

  @Override
  public void storeTypeVariables(Set<TVar> storage) { }

  @Override
  public Type instantiate(Map<TVar,Type> typeSubstitution) { return this; }

  @Override
  public boolean match(Type other, Map<TVar,Type> typeSubstitution) {
    return other instanceof Base(String x) && this.name.equals(x);
  }

  @Override
  public int compareTo(Type other) {
    return switch(other) {
      case Base(String n) -> this.name.compareTo(n);
      default -> -1;
    };
  }

  @Override
  public String toString() { return this.name; }

  @Override
  public int hashCode() { return name.hashCode(); }

  @Override
  public boolean equals(Type type) {
    return switch (type) {
      case Base(String x) -> this.name.equals(x);
      default -> false;
    };
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Type t && equals(t);
  }
}

