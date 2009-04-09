package org.jetbrains.plugins.clojure.psi.impl.synthetic;

import org.jetbrains.plugins.clojure.psi.api.synthetic.ClSyntheticClass;
import org.jetbrains.plugins.clojure.psi.api.ClojureFile;
import org.jetbrains.plugins.clojure.file.ClojureFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.lang.Language;
import com.intellij.util.IncorrectOperationException;
import com.intellij.openapi.util.Pair;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * @author ilyas
 *         <p/>
 *         Class to represent bytecode-compiled clojure files
 */
public class ClSyntheticClassImpl extends LightElement implements ClSyntheticClass {

  @NotNull
  private final ClojureFile myFile;

  protected ClSyntheticClassImpl(@NotNull ClojureFile file) {
    super(file.getManager(), ClojureFileType.CLOJURE_LANGUAGE);
    myFile = file;
    assert myFile.isClassDefiningFile();
  }

  public String getText() {
    return "";
  }

  @Override
  public String toString() {
    return "ClojureSynteticClass[" + getQualifiedName() + "]";
  }

  public void accept(@NotNull PsiElementVisitor psiElementVisitor) {
  }

  public PsiElement copy() {
    throw new IncorrectOperationException("nonphysical element");
  }

  public String getQualifiedName() {
    //todo implement me!
    return null;
  }

  public boolean isInterface() {
    return false;
  }

  public boolean isAnnotationType() {
    return false;
  }

  public boolean isEnum() {
    return false;
  }

  public PsiReferenceList getExtendsList() {
    //todo implement me!
    return null;
  }

  public PsiReferenceList getImplementsList() {
    return null;
  }

  @NotNull
  public PsiClassType[] getExtendsListTypes() {
    return new PsiClassType[0];
  }

  @NotNull
  public PsiClassType[] getImplementsListTypes() {
    return new PsiClassType[0];
  }

  public PsiClass getSuperClass() {
    return null;
  }

  public PsiClass[] getInterfaces() {
    //todo implement me!
    return new PsiClass[0];
  }

  @NotNull
  public PsiClass[] getSupers() {
    return new PsiClass[0];
  }

  @NotNull
  public PsiClassType[] getSuperTypes() {
    return new PsiClassType[0];
  }

  @NotNull
  public PsiField[] getFields() {
    return new PsiField[0];
  }

  @NotNull
  public PsiMethod[] getMethods() {
    return new PsiMethod[0];
  }

  @NotNull
  public PsiMethod[] getConstructors() {
    return new PsiMethod[0];
  }

  @NotNull
  public PsiClass[] getInnerClasses() {
    return new PsiClass[0];
  }

  @NotNull
  public PsiClassInitializer[] getInitializers() {
    return new PsiClassInitializer[0];
  }

  @NotNull
  public PsiField[] getAllFields() {
    return new PsiField[0];
  }

  @NotNull
  public PsiMethod[] getAllMethods() {
    return new PsiMethod[0];
  }

  @NotNull
  public PsiClass[] getAllInnerClasses() {
    return new PsiClass[0];
  }

  public PsiField findFieldByName(@NonNls String s, boolean b) {
    return null;
  }

  public PsiMethod findMethodBySignature(PsiMethod psiMethod, boolean b) {
    return null;
  }

  @NotNull
  public PsiMethod[] findMethodsBySignature(PsiMethod psiMethod, boolean b) {
    return new PsiMethod[0];
  }

  @NotNull
  public PsiMethod[] findMethodsByName(@NonNls String s, boolean b) {
    return new PsiMethod[0];
  }

  @NotNull
  public List<Pair<PsiMethod, PsiSubstitutor>> findMethodsAndTheirSubstitutorsByName(@NonNls String s, boolean b) {
    return new ArrayList<Pair<PsiMethod, PsiSubstitutor>>();
  }

  @NotNull
  public List<Pair<PsiMethod, PsiSubstitutor>> getAllMethodsAndTheirSubstitutors() {
    return new ArrayList<Pair<PsiMethod, PsiSubstitutor>>();
  }

  public PsiClass findInnerClassByName(@NonNls String s, boolean b) {
    return null;
  }

  public PsiJavaToken getLBrace() {
    return null;
  }

  public PsiJavaToken getRBrace() {
    return null;
  }

  public PsiIdentifier getNameIdentifier() {
    return null;
  }

  public PsiElement getScope() {
    return null;
  }

  public boolean isInheritor(@NotNull PsiClass psiClass, boolean b) {
    return false;
  }

  public boolean isInheritorDeep(PsiClass psiClass, @Nullable PsiClass psiClass1) {
    return false;
  }

  public PsiClass getContainingClass() {
    return null;
  }

  @NotNull
  public Collection<HierarchicalMethodSignature> getVisibleSignatures() {
    return new ArrayList<HierarchicalMethodSignature>();
  }

  public String getName() {
    return myFile.getClassName();
  }

  public PsiElement setName(@NonNls String s) throws IncorrectOperationException {
    return myFile.setClassName(s);
  }

  public PsiModifierList getModifierList() {
    //todo implement me!
    return null;
  }

  public boolean hasModifierProperty(@Modifier String s) {
    //todo implement me!
    return false;
  }

  public PsiDocComment getDocComment() {
    return null;
  }

  public boolean isDeprecated() {
    return false;
  }

  public boolean hasTypeParameters() {
    return false;
  }

  public PsiTypeParameterList getTypeParameterList() {
    return null;
  }

  @NotNull
  public PsiTypeParameter[] getTypeParameters() {
    return new PsiTypeParameter[0];
  }
}