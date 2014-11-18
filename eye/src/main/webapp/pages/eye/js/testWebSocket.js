var ws = null;  
function startWebSocket() {  
    if ('WebSocket' in window)  
        ws = new WebSocket("ws://localhost:80/sniper/app/watch.do");  
    else if ('MozWebSocket' in window)  
        ws = new MozWebSocket("ws://localhost:80/sniper/app/watch.do");  
    else  
        alert("not support");  
      
      
    ws.onmessage = function(evt) {  
        alert(evt.data);  
    };  
      
    ws.onclose = function(evt) {  
        alert("close");  
    };  
      
    ws.onopen = function(evt) {  
        alert("open");  
    };  
}  
  
function sendMsg() {  
    ws.send(document.getElementById('writeMsg').value);  
} 