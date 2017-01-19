//字体自适应
window.onload = function(){
	fontSelf();
}
window.onresize = function () {
	fontSelf();
};
function fontSelf() {
	var wHtml = document.getElementById('html');
	var w = document.documentElement.clientWidth;
	w =  w > 768 ? 768 : document.documentElement.clientWidth;
	wHtml.style.fontSize = w * 0.045 + 'px';
};
//定位
window.onload=function(){

	var geolocation = new qq.maps.Geolocation("OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77", "myapp");
    geolocation.getLocation(showPosition, showErr, options);
	var options = {timeout: 8000};
	function showPosition(position) {
   	 	document.getElementById("position").appendChild(document.createElement('pre')).innerHTML = position.province+position.city+position.addr;
	}
	function showErr() {
    	//document.getElementById("position").appendChild(document.createElement('p')).innerHTML = "定位失败！";
    	document.getElementById("pos-area").scrollTop = document.getElementById("pos-area").scrollHeight;
	}
}
