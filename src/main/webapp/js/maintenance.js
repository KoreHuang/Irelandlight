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
//图片Ajax
$.ajax({
	type:"post",
	url:"http://192.168.1.162:8080/container/dangerDetail",
	async:true,
	success:function(mgs){
		console.log(mgs);
		var obj = JSON.parse(mgs);
		console.log(obj);
		$('.equipment>img').attr('src',obj['pic']);
		$('.title').html(obj['title']);
		$('.title').attr('id',obj['itemNo']);
		$('.describeAjax').html(obj['descript']);
		for(var attr in obj['list']){
			if(attr%2 == 1){
				console.log(attr);
				var $Li = $('<li class="clearfix"></li>');
				$('.start').append($Li);
				var $Img = $("<img src='' width='10' height='10'>" );
				console.log(obj['list'][attr-1]);
				$Img.attr('src',obj['list'][attr-1]);
				$Li.append($Img);
				var $Span = $("<span class='list'></span>");
				console.log(obj['list'][attr])
				$Span.html(obj['list'][attr]);
				$Li.append($Span);
				var $Imgp = $("<img id='1' class='yesone' src='img/yesone.png' width='20' height='20'>");
				$Li.append($Imgp);
			}
		}
		$('.yesone').click(checkedYes);
	}
});
//维修完成按钮
$('.done').click(function(){
	$.ajax({
		type:"get",
		url:"http://192.168.1.162:8080/container/fix?itemNo="+$('.title').attr('id'),
		async:true
	});
})
//选框点击
function checkedYes(){
	var flag = 1;
	if($(this).attr('id') == 1){
		$(this).attr('src','img/yestwo.png');
		$(this).attr('id','2');
	}else{
		$(this).attr('src','img/yesone.png');
		$(this).attr('id','1');
	}
	for(var i = 0;i < $('.start li').length;i++){
		if($('.start li>.yesone').eq(i).attr("id") == 1){
			flag = 0;
		}
	}
	if(flag == 1){
		$('.done').css('background-color','#5cb85c');
		$('.done').removeAttr("disabled");
	}else{
		$('.done').css('background-color','lightgray');
		$('.done').attr("disabled","disabled");
	}
}
/*{
	"pic":"",
	"title":'A号楼拐角',
	"descript":'第三代不会变粗大洪水杀病毒合并时',
	"list":[
		"http://pic32.nipic.com/20130827/12906030_123121414000_2.png",
		"补充气体存储量",
		"http://pic32.nipic.com/20130827/12906030_123121414000_2.png",
		"补充气体存储量"
	]
}*/


