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

package charlie.types;

import java.util.TreeMap;

/** A simple type substitution to be used for testing (no dependency on charlie.substitution) */
public class TestSubstitution implements Type.MSubstitution {
  TreeMap<TVar,Type> _map;
  public TestSubstitution() { _map = new TreeMap<TVar,Type>(); }
  public Type get(TVar alpha) { return _map.get(alpha); }
  public boolean extend(TVar alpha, Type value) { _map.put(alpha, value); return true; }
  public int size() { return _map.size(); }
}
