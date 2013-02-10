<#if comment>

  TEMPLATE DESCRIPTION:

  This is XHTML template for 'JSF Pages From Entity Beans' action. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    entityName - name of entity being modified (type: String)
    managedBean - name of managed choosen in UI (type: String)
    managedBeanProperty - name of managed bean property choosen in UI (type: String)
    item - name of property used for dataTable iteration (type: String)
    comment - always set to "false" (type: Boolean)
    entityDescriptors - list of beans describing individual entities. Bean has following properties:
        label - field label (type: String)
        name - field property name (type: String)
        dateTimeFormat - date/time/datetime formatting (type: String)
        blob - does field represents a large block of text? (type: boolean)
        primaryKey - is field a primary key field? (type: boolean)
        relationshipOne - does field represent one to one or many to one relationship (type: boolean)
        relationshipMany - does field represent one to many relationship (type: boolean)
        relationshipOwner - does the field represent the owning side of a many:many relationship? (type: boolean)
        getRelationsLabelName(String) - field name of the foreign entity field matching String (type: String)
        returnType - fully qualified data type of the field
        id - field id name (type: String)
        required - is field optional and nullable or it is not? (type: boolean)
        valuesListGetter - if item is of type 1:many or many:many relationship then use this
            getter to populate <h:selectOneMenu> or <h:selectManyMenu>
        valuesConverter - if item is of type 1:many or many:many relationship then use this
            for the converter binding of <h:selectOneMenu> or <h:selectManyMenu>
    defaultDataTableRows - will be used for DataTable Paging
    defaultDataTableRowsPerPageTemplate - will be used for DataTable Paging
    primeFacesVersion - Version of the PrimeFaces library in use (type: Version)
    searchLabels - Comma-seperated list of field name artifacts to search for labels (type: String)
                   Use in conjunction with getRelationsLabelName.
    doCreate - Provide CREATE functionality (type: boolean)
    doRead - Provide READ functionality (type: boolean)
    doUpdate - Provide UPDATE functionality (type: boolean)
    doDelete - Provide DELETE functionality (type: boolean)

  This template is accessible via top level menu Tools->Templates and can
  be found in category PrimeFaces CRUD Generator->PrimeFaces Pages from Entity Classes.

</#if>
<#assign createButton  = "createButton"/>
<#assign readButton    = "viewButton"/>
<#assign updateButton  = "editButton"/>
<#assign deleteButton  = "deleteButton"/>
<#assign ajaxUpdateIds = "">
<#if doCreate><#assign ajaxUpdateIds = ajaxUpdateIds + " " + createButton/></#if>
<#if doRead><#assign ajaxUpdateIds = ajaxUpdateIds + " " + readButton/></#if>
<#if doUpdate><#assign ajaxUpdateIds = ajaxUpdateIds + " " + updateButton/></#if>
<#if doDelete><#assign ajaxUpdateIds = ajaxUpdateIds + " " + deleteButton/></#if>
<#assign ajaxUpdateIds = ajaxUpdateIds?trim>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                xmlns:h="http://java.sun.com/jsf/html"
                xmlns:f="http://java.sun.com/jsf/core"
                xmlns:p="http://primefaces.org/ui">

        <h:form id="${entityName}ListForm">

            <p:panel header="${r"#{"}bundle.List${entityName}Title${r"}"}">

                <h:panelGroup id="messagePanel">
                    <p:messages id="listMessages"/>
                </h:panelGroup>

                <p:dataTable id="datalist" value="${r"#{"}${managedBeanProperty}${r"}"}" var="${item}"
                             selectionMode="single" selection="${r"#{"}${managedBean}${r".selected}"}"
<#if entityIdField?? && entityIdField != "">
                             rowKey="${r"#{"}${item}.${entityIdField}${r"}"}"
</#if>
<#if defaultDataTableRows?? && defaultDataTableRows != "">
                             paginator="true"
                             rows="${defaultDataTableRows}"
                             rowsPerPageTemplate="${defaultDataTableRowsPerPageTemplate}"
</#if>
            >

<#if ajaxUpdateIds?? && ajaxUpdateIds != "">
                    <p:ajax event="rowSelect"   update="${ajaxUpdateIds}"/>
                    <p:ajax event="rowUnselect" update="${ajaxUpdateIds}"/>
</#if>

<#list entityDescriptors as entityDescriptor>
  <#-- Skip this field if we are dealing with many:many -->
  <#if !entityDescriptor.relationshipMany>
    <#if entityDescriptor.relationshipOne || entityDescriptor.relationshipMany>
        <#if entityDescriptor.getRelationsLabelName(searchLabels)??>
          <#assign relationLabelName = entityDescriptor.getRelationsLabelName(searchLabels)>
        <#else>
              <#assign relationLabelName = "">
        </#if>
    </#if>
<#if entityDescriptor.relationshipOne>
                <#if relationLabelName?? && relationLabelName != "">
                    <p:column<#if doSort> sortBy="${r"#{"}${entityDescriptor.name}.${relationLabelName}${r"}"}"</#if><#if doFilter> filterBy="${r"#{"}${entityDescriptor.name}.${relationLabelName}${r"}"}"</#if>>
                <#else>
                    <#-- Disable sorting if we don't have a foreign field to sort by. -->
                    <p:column>
                </#if>
<#else>
                    <p:column<#if doSort> sortBy="${r"#{"}${entityDescriptor.name}${r"}"}"</#if><#if doFilter> filterBy="${r"#{"}${entityDescriptor.name}${r"}"}"</#if>>
</#if>
                        <f:facet name="header">
                            <h:outputText value="${r"#{"}bundle.List${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}"/>
                        </f:facet>
    <#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}">
                            <f:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
                        </h:outputText>
    <#elseif entityDescriptor.returnType?contains("boolean")>
                        <h:selectBooleanCheckbox id="${entityDescriptor.id?replace(".","_")}" value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${r"#{"}bundle.Edit${entityName}Title_${entityDescriptor.id?replace(".","_")}${r"}"}" <#if entityDescriptor.required>required="true" requiredMessage="${r"#{"}bundle.Edit${entityName}RequiredMessage_${entityDescriptor.id?replace(".","_")}${r"}"}"</#if> disabled="true"/>
    <#elseif entityDescriptor.relationshipOne>
            <#if relationLabelName?? && relationLabelName != "">
                        <h:outputText value="${r"#{"}${entityDescriptor.name}.${relationLabelName}${r"}"}"/>
            <#else>
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}">
<#if cdiEnabled?? && cdiEnabled == true>
                            <f:converter binding="${r"#{"}${entityDescriptor.valuesConverter}${r"}"}"/>
<#else>
                            <f:converter converterId="${entityDescriptor.valuesConverter}"/>
</#if>
                        </h:outputText>
            </#if>
    <#else>
                        <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}"/>
    </#if>
                    </p:column>
  </#if>
</#list>
                    <f:facet name="footer">
<#if doCreate>
                        <p:commandButton id="${createButton}" icon="ui-icon-plus"   value="${r"#{"}bundle.Create${r"}"}" actionListener="${r"#{"}${managedBean}.${r"prepareCreate}"}" update=":${entityName}CreateForm:display" oncomplete="${r"${entityName}CreateDialog.show()"}"/>
</#if>
<#if doRead>
                        <p:commandButton id="${readButton}"   icon="ui-icon-search" value="${r"#{"}bundle.View${r"}"}" update=":${entityName}ViewForm:display" oncomplete="${entityName}ViewDialog.show()" disabled="${r"#{empty "}${managedBean}${r".selected}"}"/>
</#if>
<#if doUpdate>
                        <p:commandButton id="${updateButton}"   icon="ui-icon-pencil" value="${r"#{"}bundle.Edit${r"}"}" update=":${entityName}EditForm:display" oncomplete="${entityName}EditDialog.show()" disabled="${r"#{empty "}${managedBean}${r".selected}"}"/>
</#if>
<#if doDelete>
                        <p:commandButton id="${deleteButton}" icon="ui-icon-trash"  value="${r"#{"}bundle.Delete${r"}"}" actionListener="${r"#{"}${managedBean}${r".delete}"}" update=":${entityName}ListForm:messagePanel,datalist" disabled="${r"#{empty "}${managedBean}${r".selected}"}"/>
</#if>
                    </f:facet>

                </p:dataTable>

            </p:panel>

        </h:form>

</ui:composition>