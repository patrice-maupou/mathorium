/* 
 * Copyright (C) 2015 Patrice.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.maupou.expressions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Patrice Maupou
 */
public class ChildReplace {

  private final String name; // ex : a
  private final String replacement; // remplacement éventuel ex : (a)
  private final List<String> nodeNames; // noms possibles du noeud pour le remplacement ex : ADD,SUB

  public ChildReplace(String name, String replacement, List<String> conditions) {
    this.name = name;
    this.replacement = replacement;
    this.nodeNames = conditions; // exemple : ADD,SUB
  }



  @Override
  public String toString() {
    String ret = name + "->" + replacement + " if in " + getNodeNames();
    return ret;
  }

  public String getName() {
    return name;
  }

  public String getReplacement() {
    return replacement;
  }

  public List<String> getNodeNames() {
    return nodeNames;
  }



}
