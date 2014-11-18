var map = function(){
	if(this.cityName != "" && this.cityName != null){
		emit(this.cityName, {"keys":[this.question]});
	}
}

var reduce = function(key, values){
	var tmp = [], put = true, result = {"keys":[]};
	for ( var i in values) {
		values[i].keys.forEach(function(key){
			put = true;
			tmp = result.keys.concat();
			for ( var int = 0; int < tmp.length; int++) {
				if (tmp[int] == key) {
					put = false;
				}
			}
			if(put){
				result.keys.push(key);
			}
			tmp = [];
		});
	}
	return result;
}
var map = function(){
	var cityName = this.cityName; 
	if(cityName != '' && cityName != null){ 
		cityName = cityName.replace('.','_'); 
		emit(cityName, {'keys':[this.question]}); 
	} 
}
var reduce = function(key, values){ 
	var tmp = [], put = true, result = {'keys':[]},len = 0;
	for ( var i in values) {
		values[i].keys.forEach(function(question){ 
			put = true; 
			tmp = result.keys.concat();
			len = tmp.length; 
			for ( var i = 0; i < len; i++) { 
				if (tmp[i] == question) { put = false; } 
			} 
			if(put){ 
				question = question.replace('.','_'); 
				result.keys.push(question); 
			}
			tmp = []; 
		}); 
	} 
	return result; 
}

var map = function(){
	var keyword = this.question;
	if(keyword != "" && keyword != null){
		keyword = keyword.replace(new RegExp('.','gm'),'_');
		keyword = keyword.replace(new RegExp('$','gm'),'_');
		emit(keyword, 1);
	}
}
var reduce = function(key, values){
	return values.length;
}