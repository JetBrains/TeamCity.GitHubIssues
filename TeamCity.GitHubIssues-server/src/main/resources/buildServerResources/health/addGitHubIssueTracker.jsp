<%--suppress XmlPathReference --%>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include-internal.jsp"%>
<jsp:useBean id="buildType" type="jetbrains.buildServer.serverSide.SBuildType" scope="request"/>
<%--@elvariable id="healthStatusItem" type="jetbrains.buildServer.serverSide.healthStatus.BuildTypeSuggestedItem"--%>

<%--@elvariable id="suggestedTrackers" type="java.util.ArrayList<java.util.Map<java.lang.String, java.lang.Object>>"--%>
<c:set var="suggestedTrackers" value="${healthStatusItem.additionalData['suggestedTrackers']}"/>
<c:set var="numSuggestedTrackers" value="${fn:length(suggestedTrackers)}"/>

<div class="suggestionItem">
  There <bs:are_is val="${numSuggestedTrackers}"/>
  <c:choose>
    <c:when test="${numSuggestedTrackers == 1}"> a </c:when>
    <c:otherwise> ${numSuggestedTrackers} </c:otherwise>
  </c:choose>
  VCS root<bs:s val="${numSuggestedTrackers}"/> in <admin:editBuildTypeLinkFull buildType="${buildType}"/>
  that point<c:if test="${numSuggestedTrackers == 1}">s</c:if> to GitHub. Do you want to use GitHub issue tracker<bs:s val="${numSuggestedTrackers}"/> as well?
  <c:forEach var="item" items="${suggestedTrackers}">
     <div class="suggestionAction">
        <%--suppress XmlPathReference --%>
      <c:url var="url" value="/admin/editProject.html?init=1&projectId=${buildType.projectExternalId}&tab=issueTrackers&#addTracker=${item['type']}&repoUrl=${util:urlEscape(item['repoUrl'])}&suggestedName=${util:urlEscape(item['suggestedName'])}"/>
      <a class="addNew" href="${url}">Use GitHub issue tracker for ${item['suggestedName']}</a>
    </div>
  </c:forEach>
</div>