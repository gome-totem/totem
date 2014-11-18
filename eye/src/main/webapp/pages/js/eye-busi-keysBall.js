var Ball = function() {
	this.radius = 150;
	this.dtr = Math.PI / 180;
	this.d = 300;
	this.mcList = [];
	this.active = false;
	this.lasta = 1;
	this.lastb = 1;
	this.distr = true;
	this.tspeed = 0.7;
	this.size = 250;
	this.mouseX = 0;
	this.mouseY = 0;
	this.howElliptical = 1;
	this.aA = null;
	this.oDiv = null;
}

Ball.prototype = {
	ballInit : function() {
		var _this = this, i = 0, oTag = null;
		_this.oDiv = document.getElementById('question');
		_this.aA = _this.oDiv.getElementsByTagName('a');

		for (i = 0; i < _this.aA.length; i++) {
			oTag = {};
			oTag.offsetWidth = _this.aA[i].offsetWidth;
			oTag.offsetHeight = _this.aA[i].offsetHeight;
			_this.mcList.push(oTag);
		}

		_this.sineCosine(0, 0, 0);
		_this.positionAll();

		_this.oDiv.onmouseover = function() {
			_this.active = false;
		};

		_this.oDiv.onmouseout = function() {
			_this.active = true;
		};

		_this.oDiv.onmousemove = function(ev) {
			var oEvent = window.event || ev;
			_this.mouseX = oEvent.clientX	- (_this.oDiv.offsetLeft + _this.oDiv.offsetWidth / 2);
			_this.mouseY = oEvent.clientY	- (_this.oDiv.offsetTop + _this.oDiv.offsetHeight / 2);
			_this.mouseX /= 5;
			_this.mouseY /= 5;
		};

		setInterval(function(){_this.update()}, 30);
	},
	update : function() {
		var _this = this;
		var a, b;

		if (_this.active) {
			a = (-Math.min(Math.max(-_this.mouseY, -_this.size), _this.size) / _this.radius)
					* _this.tspeed;
			b = (Math.min(Math.max(-_this.mouseX, -_this.size), _this.size) / _this.radius)
					* _this.tspeed;
		} else {
			a = _this.lasta * 0.98;
			b = _this.lastb * 0.98;
		}

		_this.lasta = a;
		_this.lastb = b;

		if (Math.abs(a) <= 0.01 && Math.abs(b) <= 0.01) {
			return;
		}

		var c = 0;
		_this.sineCosine(a, b, c);
		for ( var j = 0; j < _this.mcList.length; j++) {
			var rx1 = _this.mcList[j].cx;
			var ry1 = _this.mcList[j].cy * ca + _this.mcList[j].cz * (-sa);
			var rz1 = _this.mcList[j].cy * sa + _this.mcList[j].cz * ca;

			var rx2 = rx1 * cb + rz1 * sb;
			var ry2 = ry1;
			var rz2 = rx1 * (-sb) + rz1 * cb;

			var rx3 = rx2 * cc + ry2 * (-sc);
			var ry3 = rx2 * sc + ry2 * cc;
			var rz3 = rz2;

			_this.mcList[j].cx = rx3;
			_this.mcList[j].cy = ry3;
			_this.mcList[j].cz = rz3;

			per = _this.d / (_this.d + rz3);

			_this.mcList[j].x = (_this.howElliptical * rx3 * per)	- (_this.howElliptical * 2);
			_this.mcList[j].y = ry3 * per;
			_this.mcList[j].scale = per;
			_this.mcList[j].alpha = per;
			_this.mcList[j].alpha = (_this.mcList[j].alpha - 0.6) * (10 / 6);
		}

		_this.doPosition();
		_this.depthSort();
	},
	doPosition : function() {
		var _this = this;
		var l = _this.oDiv.offsetWidth / 2;
		var t = _this.oDiv.offsetHeight / 2;
		for ( var i = 0; i < _this.mcList.length; i++) {
			_this.aA[i].style.left = _this.mcList[i].cx + l	- _this.mcList[i].offsetWidth / 2 + 'px';
			_this.aA[i].style.top = _this.mcList[i].cy + t - _this.mcList[i].offsetHeight / 2 + 'px';
			_this.aA[i].style.fontSize = Math.ceil(12 * _this.mcList[i].scale / 2) + 8 + 'px';
			_this.aA[i].style.filter = "alpha(opacity=" + 100 * _this.mcList[i].alpha + ")";
			_this.aA[i].style.opacity = _this.mcList[i].alpha;
		}
	},
	depthSort : function() {
		var _this = this, i = 0, aTmp = [];

		for (i = 0; i < _this.aA.length; i++) {
			aTmp.push(_this.aA[i]);
		}

		aTmp.sort(function(vItem1, vItem2) {
			if (vItem1.cz > vItem2.cz) {
				return -1;
			} else if (vItem1.cz < vItem2.cz) {
				return 1;
			} else {
				return 0;
			}
		});

		for (i = 0; i < aTmp.length; i++) {
			aTmp[i].style.zIndex = i;
		}
	},
	sineCosine : function(a, b, c) {
		var _this = this;
		sa = Math.sin(a * _this.dtr);
		ca = Math.cos(a * _this.dtr);
		sb = Math.sin(b * _this.dtr);
		cb = Math.cos(b * _this.dtr);
		sc = Math.sin(c * _this.dtr);
		cc = Math.cos(c * _this.dtr);
	},
	positionAll : function() {
		var _this = this;
		var phi = 0, theta = 0, max = _this.mcList.length, i = 0, aTmp = [], 
		oFragment = document.createDocumentFragment();

		// 随机排序
		for (i = 0; i < _this.aA.length; i++) {
			aTmp.push(_this.aA[i]);
		}

		aTmp.sort(function() {
			return Math.random() < 0.5 ? 1 : -1;
		});

		for (i = 0; i < aTmp.length; i++) {
			oFragment.appendChild(aTmp[i]);
		}

		_this.oDiv.appendChild(oFragment);

		for ( var i = 1; i < max + 1; i++) {
			if (_this.distr) {
				phi = Math.acos(-1 + (2 * i - 1) / max);
				theta = Math.sqrt(max * Math.PI) * phi;
			} else {
				phi = Math.random() * (Math.PI);
				theta = Math.random() * (2 * Math.PI);
			}
			// 坐标变换
			_this.mcList[i - 1].cx = _this.radius * Math.cos(theta) * Math.sin(phi);
			_this.mcList[i - 1].cy = _this.radius * Math.sin(theta) * Math.sin(phi);
			_this.mcList[i - 1].cz = _this.radius * Math.cos(phi);

			_this.aA[i - 1].style.left = _this.mcList[i - 1].cx + _this.oDiv.offsetWidth / 2 - _this.mcList[i - 1].offsetWidth / 2 + 'px';
			_this.aA[i - 1].style.top = _this.mcList[i - 1].cy + _this.oDiv.offsetHeight / 2 - _this.mcList[i - 1].offsetHeight / 2 + 'px';
		}
	}
}
