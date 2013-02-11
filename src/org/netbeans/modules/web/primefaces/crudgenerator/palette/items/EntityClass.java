/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.primefaces.crudgenerator.palette.items;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Kay Wrobel
 * @author Po-Ting Wu
 */
public abstract class EntityClass extends org.netbeans.modules.web.jsf.palette.items.EntityClass {
    
    //2013-01-25 Kay Wrobel: Provide a method to determine if an entity field's value is autogenerated
    static boolean isAutoGenerated(ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(method) : method;
        if (element != null) {
            if (JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.GeneratedValue") ) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    static boolean isId(ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(method) : method;
        if (element != null) {
            if (JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.Id") || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.EmbeddedId")) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    static String getTemporal(ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(method) : method;
        if (element != null) {
            AnnotationMirror annotationMirror = JpaControllerUtil.findAnnotation(element, "javax.persistence.Temporal"); // NOI18N
            if (annotationMirror != null) {
                Collection<? extends AnnotationValue> attributes = annotationMirror.getElementValues().values();
                if (attributes.iterator().hasNext()) {
                    AnnotationValue annotationValue = attributes.iterator().next();
                    if (annotationValue != null) {
                        return annotationValue.getValue().toString();
                    }
                }
            }
        }
        return null;
    }

    static ClasspathInfo createClasspathInfo(FileObject fileObject) {
        return ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT),
                ClassPath.getClassPath(fileObject, ClassPath.COMPILE),
                ClassPath.getClassPath(fileObject, ClassPath.SOURCE)
                );
    }
    
    static String getDateTimeFormat(String temporal) {
        if ("DATE".equals(temporal)) {
            return "MM/dd/yyyy";
        } else if ("TIME".equals(temporal)) {
            return "HH:mm:ss";
        } else {
            return "MM/dd/yyyy HH:mm:ss";
        }
    }
}
