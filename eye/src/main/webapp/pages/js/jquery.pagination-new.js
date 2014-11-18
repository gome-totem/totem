var pageNav = {
	pre : "上一页",
	next : "下一页",
	nav : function(p, pn,type) {
		var html = "";
		if (pn <= 1 || p > pn) {
			html = this.pager(1, 1,type);
		} else {
			html += (p == 1) ? this.showPre(0,null,type) : this.showPre(1, p,type);
			if(type!='config'){
				if (pn > 6) {
					var c = 1;
					if (p >= 5 && p <= pn) {
						html += (p == 1) ? this.numStatusHTML(0, 1,type) : this.numStatusHTML(1, 1,type);
					} else {
						for ( var i = 1; i < 4; i++) {
							html += (p == i) ? this.numStatusHTML(0, i,type) : this.numStatusHTML(1, i,type);
						}
					}
					html += (p >= 5) ? "<span class='placeholder'></span>" : "";
					c = p - 2; if (c >= 3 && c < pn - 2) html += (p == c) ? this.numStatusHTML(0, c,type) : this.numStatusHTML(1, c,type);
					c = p - 1; if (c > 3 && c < pn - 2) html += (p == c) ? this.numStatusHTML(0, c,type) : this.numStatusHTML(1, c,type);
					c = p;     if (c > 3 && c < pn - 2) html += (p == c) ? this.numStatusHTML(0, c,type) : this.numStatusHTML(1, c,type);
					c = p + 1; if (c > 3 && c < pn - 2) html += (p == c) ? this.numStatusHTML(0, c,type) : this.numStatusHTML(1, c,type);
					c = p + 2; if (c > 3 && c < pn - 2) html += (p == c) ? this.numStatusHTML(0, c,type) : this.numStatusHTML(1, c,type);
					html += (p <= pn - 4) ? "<span class='placeholder'></span>" : "";
					if (p <= pn - 4) {
						html += (p == pn) ? this.numStatusHTML(0, pn,type) : this.numStatusHTML(1, pn,type);
					} else {
						for ( var i = pn - 2; i <= pn; i++) {
							html += (p == i) ? this.numStatusHTML(0, i,type) : this.numStatusHTML(1, i,type);
						}
					}
				} else {
					for ( var i = 1; i <= pn; i++) {
						html += (p == i) ? this.numStatusHTML(0, i,type) : this.numStatusHTML(1, i,type);
					}
				}
			}
			html += (p == pn) ? this.showNext(0,null,type) : this.showNext(1, p,type);
		}
		return html;
	},
	pager : function(p, pn,type) {
		var html = "";
		if (pn <= 1) {
			this.p = p;
			this.pn = pn;
			if(type=="config"){
				html = this.showPre(0,null,type) + this.showNext(0,null,type);
			}else{
				html = this.showPre(0,null,type) + this.numStatusHTML(0, p,type) + this.showNext(0,null,type);
			}
		}
		return html;
	},
	go : function(p, pn,type) {
		var html = this.nav(p, pn,type) + this.btnHTML(p, pn,type);
		return html;
	},
	numStatusHTML : function(t, p,type) {
		if(type=="config"){
			return (t == 0) ? ("<span class='cur'>" + p + "</span>") : "<a href='javascript:void(0);' onclick='javascript:doPageConfigSearch("	+ p + ");return false;'>" + p + "</a>";
		}else{
			return (t == 0) ? ("<span class='cur'>" + p + "</span>") : "<a href='javascript:void(0);' onclick='javascript:doPageNumSearch("	+ p + ");return false;'>" + p + "</a>";
		}
	},
	showPre : function(t, p,type) {
		if(type=="config"){
			var $disable = "<a class='prev disable' href='javascript:void(0);'>&nbsp;"
					+ this.pre + "<s class='icon-prev'></s><i></i></a>";
			var $able = "<a class='prev' href='javascript:void(0);' onclick='javascript:doPageConfigSearch("
					+ (p - 1) + ");return false;'>&nbsp;" + this.pre
					+ "<s class='icon-prev'></s><i></i></a>";
			return (t == 0) ? $disable : $able;
		}else{
			var $disable = "<a class='prev disable' href='javascript:void(0);'>&nbsp;"
				+ this.pre + "<s class='icon-prev'></s><i></i></a>";
			var $able = "<a class='prev' href='javascript:void(0);' onclick='javascript:doPageNumSearch("
					+ (p - 1) + ");return false;'>&nbsp;" + this.pre
					+ "<s class='icon-prev'></s><i></i></a>";
			return (t == 0) ? $disable : $able;
		}
		
	},
	showNext : function(t, p,type) {
		if(type=="config"){
			var $disable = "<a class='next disable' href='javascript:void(0);'>"
				+ this.next + "<s class='icon-next'></s><i></i></a>";
			var $able = "<a class='next' href='javascript:void(0);' onclick='javascript:doPageConfigSearch("
					+ (p + 1) + ");return false;'>" + this.next + "<s class='icon-next'></s><i></i></a>";
			return (t == 0) ? $disable : $able;
		}else{
			var $disable = "<a class='next disable' href='javascript:void(0);'>"
				+ this.next + "<s class='icon-next'></s><i></i></a>";
			var $able = "<a class='next' href='javascript:void(0);' onclick='javascript:doPageNumSearch("
					+ (p + 1) + ");return false;'>" + this.next + "<s class='icon-next'></s><i></i></a>";
			return (t == 0) ? $disable : $able;
		}
		
	},
	btnHTML : function(p, pn,type) {
		if(type=="config"){
			var html = "<label for='pagenum' class='txt-wrap'>到<input type='text' id='zNum' class='txt' bNum='"+ pn +"' value='" + p + "'>页</label>"
			+ "<a href='javascript:void(0)' zdx='nBtn' class='btn'>确定</a>";
			return html;
		}else{
			var html = "<label for='pagenum' class='txt-wrap'>到<input type='text' id='pNum' class='txt' bNum='"+ pn +"' value='" + p + "'>页</label>"
			+ "<a href='javascript:void(0)' zdx='nBtn' class='btn'>确定</a>";
			return html;
		}
		
	}
}