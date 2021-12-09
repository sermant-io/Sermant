<#--
  #%L
  Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
  %%
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  %%
      http://www.apache.org/licenses/LICENSE-2.0
  %%
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  Based on third-party-file-groupByLicense.ftl from License Maven Plugin
    #%L
    Copyright (C) 2012 Codehaus, Tony Chemit
    %%
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Lesser Public License for more details.
    You should have received a copy of the GNU General Lesser Public
    License along with this program.  If not, see
    <http://www.gnu.org/licenses/lgpl-3.0.html>.
    #L%
  -->
<#--
  author: HapThorin
  date: 2021/12/08
  -->
<#function buildLine project>
  <#if (project.name?index_of('Unnamed') >= 0)>
    <#return project.artifactId + " (" + project.groupId + ":" + project.artifactId + ")">
  <#else>
    <#return project.name + " (" + project.groupId + ":" + project.artifactId + ")">
  </#if>
</#function>
<#function buildLines projects>
  <#assign result = ""/>
  <#assign line = ""/>
  <#assign url = ""/>
  <#list projects as project>
    <#assign newline = buildLine(project)/>
    <#if (line != newline)>
      <#assign line = newline/>
      <#assign url = (project.url!"no url defined")/>
      <#assign result = result + "\n" + line + " - " + (project.url!"no url defined")/>
    <#else>
      <#assign newUrl = (project.url!"no url defined")/>
      <#if (url != newUrl)>
        <#assign url = newUrl/>
        <#assign result = result + " | " + url/>
      </#if>
    </#if>
  </#list>
  <#return result>
</#function>
<#function getOtherLicenseCount licenseMap apacheLicense>
  <#assign count = 0/>
  <#list licenseMap as entry>
    <#assign projects = entry.getValue()/>
    <#if (projects?size > 0 && license != apacheLicense)>
      <#assign count = count + projects?size/>
    </#if>
  </#list>
  <#return count>
</#function>

<#if (licenseMap?size != 0)>
  <#assign apacheLicense = 'Apache License, Version 2.0'/>
  <#list licenseMap as entry>
    <#assign license = entry.getKey()/>
    <#assign projects = entry.getValue()/>
    <#if (projects?size > 0 && license == apacheLicense)>

================================================================================
This project bundles some components that are also licensed under the Apache License Version 2.0:

${buildLines(projects)}

    </#if>
  </#list>
  <#if (getOtherLicenseCount(licenseMap, apacheLicense) > 0)>

================================================================================
This product bundles various third-party components under other open source licenses.
This section summarizes those components and their licenses. See licenses-binary/ for text of these licenses.

    <#list licenseMap as entry>
      <#assign license = entry.getKey()/>
      <#assign projects = entry.getValue()/>
      <#if (projects?size > 0 && license != apacheLicense)>

${license}:
----------------------------------------
${buildLines(projects)}

      </#if>
    </#list>
  </#if>
</#if>