<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="span12" id="right_head"></div>
<div> 
	<div id="zooTree-original" style="display: none;"></div>
	<div id="zooTree"></div>
</div>

<div class="span12 show-main detail-info" style="display:none;"></div>

<div id="add-router-form" title="Online Router" style="display: none;">
	<fieldset>
		<label for="name">Router IP</label>
		<input type="text" name="name" id="online-router-ip" class="text ui-widget-content ui-corner-all" />
	</fieldset>
</div>

<div id="add-app-form" title="Online AppServer" style="display: none;">
	<fieldset>
		<label for="name">AppServer IP</label>
		<input type="text" name="name" id="online-appserver-ip" class="text ui-widget-content ui-corner-all" />
	</fieldset>
</div>

<div id="show-router-info-div" style="display:none;"></div>

<div id="show-app-info-div" style="display:none;"></div>

<div id="detail-information-of-zookeeper" style="display:none;"></div>

<div id="apps_in_router" title="Apps in this router" style="display:none;"></div>