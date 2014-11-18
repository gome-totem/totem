<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="span2" style="display:none;">
	<div class="well">
		<ul id="zooNav">
			<li class="zookeeper-node"><div class="progress progress-striped active"><div class="bar" style="width: 10%;"></div></div>
				<ul>
				</ul>
			</li>
		</ul>
	</div>
	<div id="off-line" class="well" style="">
		<h4 style="color:black;"><span class="ui-icon">off-line</span>&nbsp;&nbsp;Out of service</h4>
	</div>
</div>
<div>
	<div style="display:none;">
		<li id="zooNodeTmpId" count="1">
			<h2>
				<div>Zoo Information</div>
				<table class="table table-bordered">
					<tbody>
						<tr><td>Router Num</td><td jsselect="routerSize" jscontent="$this">0</td></tr>
						<tr><td>App Num</td><td jsselect="appSize" jscontent="$this">0</td></tr>
					</tbody>
				</table>
				<button class="btn" style="margin-top:4px;" onclick="detailOfZookeeper()">Detail</button>
			</h2>
			<ul jsselect="server" jsvars="$rCount:$this.count">
				<li jsselect="routers" jsvalues="class:cla($this);count:$rCount;st:$this.state">
					<div class='progress progress-striped active' jsdisplay="$this.state != 0"><div class='bar' style='width: 100%;'></div></div>
					<table class='table table-bordered' jsdisplay="$this.state == 0">
						<tbody class="progress progress-striped active">
							<tr><td colspan='2' jshtml="'router ' + $this.ip">title</td></tr>
							<tr jsselect="routerInfo"><td>maxMem</td><td jscontent="$this.allocatedMemory">maxMemory</td></tr>
							<tr jsselect="routerInfo"><td>useMem</td><td jscontent="$this.usedMemory">useMemory</td></tr>
							<tr jsselect="routerInfo"><td>freeMem</td><td jscontent="$this.freeMemory">freeMemory</td></tr>
							<tr jsselect="routerInfo"><td>aThread</td><td jscontent="$this.totalThread">totalThread</td></tr>
							<tr jsselect="routerInfo"><td>bThread</td><td jscontent="$this.blockedThread">blockedThread</td></tr>
							<tr jsselect="routerInfo"><td>jobCount</td><td jscontent="$this.jobCount">jobCount</td></tr>
						</tbody>
					</table>
					<div class='btn-group' jsdisplay="$this.state == 0">
						<button onclick='operations.monitorOperation(this.name)' jsvalues="name:$this.nickName;id:routerId($this)" class='btn btn-mini'>Monitor</button>
						<button onclick='operations.offlineOperation(this.name)' jsvalues="name:$this.nickName;id:routerId($this)" class='btn btn-mini'>OffLine</button>
						<button onclick='operations.readAppsOperation(this.name)' jsvalues="name:$this.nickName;id:routerId($this)" class='btn btn-mini'>Apps</button>
					</div>
					<ul jsvars="$count:$this.appServerCount">
						<li jsselect="appservers" jsvalues="class:claApp($this);count:$count">
							<div class='progress progress-striped active' jsdisplay="$this.state != 0"><div class='bar' style='width: 100%;'></div></div>
							<table class='table table-bordered' jsdisplay="$this.state == 0">
								<tbody class="progress progress-striped active">
									<tr><td colspan='2' jshtml="'app ' + $this.ip">title</td></tr>
									<tr jsselect="appInfo"><td>maxMem</td><td jscontent="$this.allocatedMemory">maxMemory</td></tr>
									<tr jsselect="appInfo"><td>useMem</td><td jscontent="$this.usedMemory">allocatedMemory</td></tr>
									<tr jsselect="appInfo"><td>freeMem</td><td jscontent="$this.freeMemory">freeMemory</td></tr>
									<tr jsselect="appInfo"><td>aThread</td><td jscontent="$this.totalThread">totalThread</td></tr>
									<tr jsselect="appInfo"><td>bThread</td><td jscontent="$this.blockedThread">blockedThread</td></tr>
									<tr jsselect="appInfo"><td>jobCount</td><td jscontent="$this.jobCount">jobCount</td></tr>
								</tbody>
							</table>
							<div class='btn-group' jsdisplay="$this.state == 0">
								<button onclick='operations.monitorOperation(this.name)' jsvalues="name:$this.nickName;id:$this.id" class='btn btn-mini'>Monitor</button>
								<button onclick='operations.offlineOperation(this.name)' jsvalues="name:$this.nickName;id:$this.id" class='btn btn-mini'>OffLine</button>
							</div>
						</li>
					</ul>
				</li>
			</ul>
		</li>
	</div>
	<div style="display:none;">
		<table id="zooInfoTmpId" class="table table-bordered" jsselect="server">
			<tbody>
				<tr jsselect="routers" jsdisplay="showDetailFiler($this)"><td jsvalues="title:readTitle($this)" jscontent="$this.id"></td><td jscontent="$this.ip"></td></tr>
			</tbody>
			<tbody jsselect="routers">
				<tr jsselect="appservers" jsdisplay="showDetailFiler($this)"><td jsvalues="title:readTitle($this)" jscontent="$this.id"></td><td jscontent="$this.ip"></td></tr>
			</tbody>
		</table>
	</div>
</div>
