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

package org.netbeans.modules.web.primefaces.crudgenerator;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.primefaces.crudgenerator.api.ConfigurationUtils;
import org.netbeans.modules.web.primefaces.crudgenerator.api.facesmodel.Converter;
import org.netbeans.modules.web.primefaces.crudgenerator.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.primefaces.crudgenerator.api.facesmodel.FacesValidator;
import org.netbeans.modules.web.primefaces.crudgenerator.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.primefaces.crudgenerator.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.ClientBehaviorRenderer;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.Component;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.FacesConverter;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.JsfModel;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.JsfModelFactory;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.Renderer;
import org.netbeans.modules.web.primefaces.crudgenerator.api.metamodel.Validator;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author petr
 * @author Po-Ting Wu
 */
public class JSFConfigUtilities {

    private static final Logger LOGGER = Logger.getLogger(JSFConfigUtilities.class.getName());

    private static String CONFIG_FILES_PARAM_NAME = "javax.faces.CONFIG_FILES"; //NOI18N
    private static String FACES_PARAM = "javax.faces";  //NOI18N
    private static String DEFAULT_FACES_CONFIG_PATH = "WEB-INF/faces-config.xml"; //NOI18N
    private static final String JSF_PRESENT_PROPERTY= "jsf.present";    //NOI18N
    private static final Class[] types = new Class[] {
        FacesManagedBean.class,
        Component.class,
        FacesValidator.class,
        FacesConverter.class,
        ClientBehaviorRenderer.class,
        ResourceBundle.class,
        Validator.class,
        Converter.class,
        Renderer.class
    };
    private static final Set<String> JSF_RESOURCES = new HashSet<String>(Arrays.asList(
            "javax.faces.bean.ManagedBean", //NOI18N
            "javax.faces.component.behavior.FacesBehavior", //NOI18N
            "javax.faces.convert.FacesConverter", //NOI18N
            "javax.faces.component.FacesComponent", //NOI18N
            "javax.faces.validator.FacesValidator", //NOI18N
            "javax.faces.render.FacesBehaviorRenderer", //NOI18N
            "javax.faces.render.FacesRenderer", //NOI18N
            "javax.faces.event.ListenerFor" //NOI18N
        ));
    private static List<ElementHandle> jsfResourcesElementHandles;

    public static boolean hasJsfFramework(FileObject fileObject) {
        if (fileObject != null) {
            WebModule webModule = WebModule.getWebModule(fileObject);
            // Issue #210646 - ideally shouldn't happen since fileObject is got as wm.getDocumentBase().
            // Probably related to hacks with recreation of webModule by saving maven's web project's server.
            // Should happen rarely since the hack was almost fixed. Anyway nothing better to do with that here.
            if (webModule == null) {
                return false;
            }
            //Check for faces-config is present
            String[] configFiles = JSFConfigUtilities.getConfigFiles(webModule);
            if (configFiles != null && configFiles.length > 0) {
                return true;
            }
            //Check Faces Servlet or any other javax.faces parameters present
            FileObject dd = webModule.getDeploymentDescriptor();
            if (dd != null) {
                //Check Faces Servlet
                if (ConfigurationUtils.getFacesServlet(webModule)!=null) {
                    return true;
                }
                try {
                    //Check javax.faces
                    WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
                    if (ddRoot != null) {
                        InitParam[] parameters = ddRoot.getContextParam();
                        for (InitParam param: parameters) {
                            if (param.getParamName().startsWith(FACES_PARAM)) {
                                return true;
                            }
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage());
                }
            }

            final Project project = FileOwnerQuery.getOwner(fileObject);
            Preferences preferences = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
            if (!preferences.get(JSF_PRESENT_PROPERTY, "").equals("true")) {
                long time = System.currentTimeMillis();
                try {
                    Future<Boolean> future =  JsfModelFactory.getModel(project).runReadActionWhenReady(new MetadataModelAction<JsfModel, Boolean>() {

                        @Override
                        public Boolean run(JsfModel metadata) throws Exception {
                            for (Class clazz: types) {
                                if (!metadata.getElements(clazz).isEmpty()) {
                                    return Boolean.TRUE;
                                }
                            }
                            return Boolean.FALSE;
                        }
                    });
                    if (future.isDone() && future.get()) {
                        // if anything suspicious found, search finely (just in source root)
                        if (jsfArtifactsInSourceRoot(webModule)) {
                            ProjectUtils.getPreferences(project, ProjectUtils.class, true).put(JSF_PRESENT_PROPERTY, "true");
                            return true;
                        }
                    }
                    return false;
                } catch(NullPointerException npe){
                    //source path is null, nothing to do here, just return false
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage());
                } finally {
                    LOGGER.log(Level.INFO, "Total time spent = {0} ms", (System.currentTimeMillis() - time));
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private static boolean jsfArtifactsInSourceRoot(WebModule webModule) throws IOException {
        // looks for faces-configs
        FileObject[] facesConfigFiles = ConfigurationUtils.getFacesConfigFiles(webModule);
        if (facesConfigFiles.length > 0) {
            return true;
        }

        // looks for classes in source root
        final AtomicBoolean resourceFound = new AtomicBoolean(false);
        JavaSource js = createJavaSource(webModule);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                resourceFound.set(containsAnnotatedJsfResource(parameter));
            }
        }, true);
        return resourceFound.get();
    }

    private static JavaSource createJavaSource(WebModule webModule) {
        Project project = FileOwnerQuery.getOwner(webModule.getDocumentBase());
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        ClassPath bootCP = cpp.findClassPath(webModule.getDocumentBase(), ClassPath.BOOT);
        ClassPath compileCP = cpp.findClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        ClassPath sourceCP = cpp.findClassPath(webModule.getDocumentBase(), ClassPath.SOURCE);
        return JavaSource.create(ClasspathInfo.create(bootCP, compileCP, sourceCP), Collections.EMPTY_LIST);
    }

    private static boolean containsAnnotatedJsfResource(CompilationController parameter) {
        if (jsfResourcesElementHandles == null) {
            loadJsfResourcesElementsHandles(parameter);
        }

        ClassIndex classIndex = parameter.getClasspathInfo().getClassIndex();
        for (ElementHandle jsfResourceElementHandle : jsfResourcesElementHandles) {
            Set<ElementHandle<TypeElement>> elements = classIndex.getElements(
                    jsfResourceElementHandle,
                    EnumSet.of(ClassIndex.SearchKind.TYPE_REFERENCES),
                    EnumSet.of(ClassIndex.SearchScope.SOURCE));
            for (ElementHandle<TypeElement> handle : elements) {
                TypeElement element = handle.resolve(parameter);
                List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
                for (AnnotationMirror annotationMirror : annotationMirrors) {
                    if (ElementHandle.create(annotationMirror.getAnnotationType().asElement())
                            .equals(jsfResourceElementHandle)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void loadJsfResourcesElementsHandles(CompilationController parameter) {
        jsfResourcesElementHandles = new ArrayList<ElementHandle>(JSF_RESOURCES.size());
        for (String canonicalName : JSF_RESOURCES) {
            TypeElement typeElement = parameter.getElements().getTypeElement(canonicalName);
            if (typeElement != null) {
                jsfResourcesElementHandles.add(ElementHandle.create(typeElement));
            }
        }
    }

    public static Set extendJsfFramework(FileObject fileObject, boolean createWelcomeFile) {
        Set result = Collections.EMPTY_SET;
        if (fileObject == null) {
            return result;
        }

        List<WebFrameworkProvider> frameworks = WebFrameworks.getFrameworks();
        for (WebFrameworkProvider framework : frameworks) {
            if (framework instanceof JSFFrameworkProvider) {
                WebModule webModule = WebModule.getWebModule(fileObject);

                // this is quite corner case since the project doesn't contain document root and the project
                // extending by framework can't do much of work then (since the extender gets just the webModule)
                // ... so, don't extend it at all for now
                if (webModule.getDocumentBase() != null) {
                    ((JSFFrameworkProvider) framework).setCreateWelcome(createWelcomeFile);
                    WebModuleExtender extender = framework.createWebModuleExtender(webModule, ExtenderController.create());
                    result = extender.extend(webModule);
                }

                return result;
            }
        }

        return result;
    }
    
//    public static NavigationRule findNavigationRule(JSFConfigDataObject data, String fromView){
//        NavigationRule navigationRule = null;
//        FacesConfig config = ConfigurationUtils.getConfigModel(data.getPrimaryFile(), true).getRootComponent();
//        Collection<NavigationRule> rules = config.getNavigationRules();
//        for (Iterator<NavigationRule> it = rules.iterator(); it.hasNext();) {
//            NavigationRule nRule = it.next();
//            if ((fromView != null && fromView.equals(nRule.getFromViewId()))
//                    || (fromView == null && (nRule.getFromViewId() == null || nRule.getFromViewId().trim().length()==0))){
//                navigationRule = nRule;
//                continue;
//            }
//        }
//        return navigationRule;
//    }
    
    /** Returns the navigation rule, where the FromViewID is the parameter. If the rule doesn't exist
     * then returns null.
     */
    //    public static NavigationRule findNavigationRule(FacesConfig config, String fromView){
    //        if (fromView != null){
    //            FacesConfig config = getConfigModel(data.getPrimaryFile(), true).getRootComponent();
    //            NavigationRule [] rules = config.getNavigationRule();
    //            for (int i = 0; i < rules.length; i++)
    //                if (fromView.equals(rules[i].getFromViewId()))
    //                    return rules[i];
    //        }
    //        return null;
    //    }
    
    /** Returns WebPages for the project, where the fo is located.
     */
    public static SourceGroup[] getDocBaseGroups(FileObject fileObject) throws java.io.IOException {
        Project proj = FileOwnerQuery.getOwner(fileObject);
        if (proj==null) return new SourceGroup[]{};
        Sources sources = ProjectUtils.getSources(proj);
        return sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
    }
    
    public static String getResourcePath(SourceGroup[] groups,FileObject fileObject, char separator, boolean withExt) {
        for (int i=0;i<groups.length;i++) {
            FileObject root = groups[i].getRootFolder();
            if (FileUtil.isParentOf(root,fileObject)) {
                String relativePath = FileUtil.getRelativePath(root,fileObject);
                if (relativePath!=null) {
                    if (separator!='/') relativePath = relativePath.replace('/',separator);
                    if (!withExt) {
                        int index = relativePath.lastIndexOf((int)'.');
                        if (index>0) relativePath = relativePath.substring(0,index);
                    }
                    return relativePath;
                } else {
                    return "";
                }
            }
        }
        return "";
    }
    
    public static boolean validateXML(FileObject deploymentDesc){
        boolean value = false;  // the default value of the com.sun.faces.validateXml
        if (deploymentDesc != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.validateXml"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return value;
    }
    
    public static boolean verifyObjects(FileObject deploymentDesc){
        boolean value = false; // the default value of the com.sun.faces.verifyObjects
        if (deploymentDesc != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "com.sun.faces.verifyObjects"); //NOI18N
                if (param != null)
                    value = "true".equals(param.getParamValue().trim());
            } catch (java.io.IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return value;
    }
    
    /** Returns relative path for all jsf configuration files in the web module. If there is no
     *  configuration file, then returns String array with lenght = 0.
     */
    public static String[] getConfigFiles(WebModule webModule) {
        if (webModule == null) {
            return new String[0];
        }

        return getConfigFiles(webModule, webModule.getDeploymentDescriptor());
    }

    public static String[] getConfigFiles(FileObject deploymentDesc){
        if (deploymentDesc == null) {
            return new String[0];
        }

        return getConfigFiles(WebModule.getWebModule(deploymentDesc), deploymentDesc);
    }

    public static String[] getConfigFiles(WebModule webModule, FileObject deploymentDesc){
        ArrayList<String> files = new ArrayList();
        String[]  filesURI;
        if (webModule != null) {
            // looking for WEB-INF/faces-config.xml
            FileObject baseDir = webModule.getDocumentBase();
            if (baseDir != null) {
                FileObject fileObject = baseDir.getFileObject(DEFAULT_FACES_CONFIG_PATH);
                if (fileObject != null)
                    files.add(DEFAULT_FACES_CONFIG_PATH);
            }
            if (deploymentDesc != null) {
                InitParam param = null;
                try{
                    WebApp webApp = DDProvider.getDefault().getDDRoot(deploymentDesc);
                    if (webApp != null) {
                        // Fix for #117845. Cannot be used the method webApp.findBeanByName,
                        // because this method doesn't trim the names of attribute.
                        InitParam[] params = webApp.getContextParam();
                        for (int i = 0; i < params.length; i++) {
                            InitParam initParam = params[i];
                            if (CONFIG_FILES_PARAM_NAME.equals(initParam.getParamName().trim())) {
                                param = initParam;
                                break;
                            }
                        }
                    }
                } catch (java.io.IOException e) {
                    Exceptions.printStackTrace(e);
                }

                if (param != null){
                    // the configuration files are defined
                    String value = param.getParamValue().trim();
                    if (value != null){
                        filesURI = value.split(",");
                        for (int i = 0; i < filesURI.length; i++) {
                            String file = filesURI[i].trim();
                            // prevent to be default faces config twice listed twice
                            if (!DEFAULT_FACES_CONFIG_PATH.equals(file) // need to check absolute and relative path
                                    && !("/" + DEFAULT_FACES_CONFIG_PATH).equals(file)) { //NOI18N
                                files.add(file);
                            }
                        }
                    }
                }
            }
        }
        filesURI = new String[files.size()];
        return files.toArray(filesURI);
    }
}
