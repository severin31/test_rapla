package org.rapla.client.extensionpoints;

import org.rapla.inject.ExtensionPoint;
import org.rapla.inject.InjectionContext;

@ExtensionPoint(context = InjectionContext.swing,id="org.rapla.gui.categoryAnnotation")
public interface AnnotationEditCategoryExtension extends AnnotationEdit
{
}
