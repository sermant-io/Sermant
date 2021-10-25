public String id() {
String splitJointId = String.valueOf(getTimeBucket());
<#list fieldsFromSource as sourceField>
    <#if sourceField.isID()>
        <#if sourceField.getTypeName() == "java.lang.String">
            splitJointId += org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR + ${sourceField.fieldName};
        <#else>
            splitJointId += org.apache.skywalking.oap.server.core.Const.ID_CONNECTOR + String.valueOf(${sourceField.fieldName});
        </#if>
    </#if>
</#list>
<#list fieldsFromSource as sourceField>
    <#if sourceField.fieldName == "copy" && sourceField.typeName == "int" >
        if (this.copy == 1) {
            splitJointId += "_copy";
        }
    </#if>
</#list>
return splitJointId;
}