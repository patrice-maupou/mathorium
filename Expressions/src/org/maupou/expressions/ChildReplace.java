/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.maupou.expressions;

import java.util.List;

/**
 *
 * @author Patrice Maupou
 */
public class ChildReplace {

  private String name;
  private String replacement; // remplacement éventuel
  private List<String> conditions; // noms possibles du noeud pour le remplacement

  public ChildReplace(String name, String replacement, List<String> conditions) {
    this.name = name;
    this.replacement = replacement;
    this.conditions = conditions;
  }



  @Override
  public String toString() {
    String ret = name + "->" + replacement + " if in " + getConditions();
    return ret;
  }

  public String getName() {
    return name;
  }

  public String getReplacement() {
    return replacement;
  }

  public List<String> getConditions() {
    return conditions;
  }


}
