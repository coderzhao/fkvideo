// document.write("<script language='javascript' src='js/ckplayer.js'></script>");
// var videoObject = {
//     container: '.video',//“#”代表容器的ID，“.”或“”代表容器的class
//     variable: 'player',//该属性必需设置，值等于下面的new chplayer()的对象
//     poster:'images/bg.jpg',//封面图片
//     video:'http://localhost:8080/fkvideo/images/11.flv',  //视频地址
//     // autoplay:false
// //		video:'http://localhost:8080/fkvideo/images/111.flv'//视频地址
// };
// var player=new ckplayer(videoObject);
$(document).ready(function () {
    $("#beginIdentify").click(function () {
        identifyFace();
    })
    $("#endIdentify").click(function () {
        endIdentify();
    })

    function getReply() {
        var testData = new FormData();
        $.ajax({
            url: "/fkvideo/result",
            type: "GET",
            data: testData,
            dataType: "text",
            success: function (data) {
                console.log(data);
                if (data == "nil") {
                    return false;
                }
                getResult(data);
            },
            processData: false,
            contentType: false
        });
    }

    function identifyFace() {
        if (!$("input[name='video']:checked").val()) {
            alert("请选择一个视频");
            return false;
        }
        if (!$("input[name='photo']").val()) {
            alert("请选择一个一个图片");
            return false;
        }
        var videoName = $("input[name='video']:checked").val()
        console.log(videoName)
        var picFace = new FormData();
        var videoDetect = new FormData();
        videoDetect.append("videoName", videoName);
        videoDetect.append("threshold", (document.getElementById("threshold").value) / 100);
        videoDetect.append("galleries", "test");
        var fileObj = document.getElementById("photo").files[0];
        console.log(fileObj.name)
        picFace.append("photo", fileObj); // 入人脸库的图片
        picFace.append("galleries", "test");
        console.log("准备启动fkvideo_detector");
        $.ajax({
            url: "/fkvideo/anytec/v0/face",
            type: "POST",
            data: picFace,
            dataType: "text",
            success: function (data) {
                timer = setInterval(getReply, 2000);

                $.ajax({
                    url: "/fkvideo/detector/v0/identify",
                    type: "POST",
                    data: videoDetect,
                    dataType: "text",
                    success: function (data) {
                        /* if(data=="start fkvideo_detector")
                            console.log(data); */
                        console.log(data);
                        getReply();
                        clearInterval(timer);
                        document.getElementById("endInfo").innerText = "视频检测完毕";
                        /* if(data=="finished")
                            alert("视频检测完毕"); */
                    },
                    processData: false,
                    contentType: false
                });
                url='http://localhost:8080/fkvideo/video/'+videoName;
                player.newVideo({autoplay:true,video:url})
            },
            processData: false,
            contentType: false
        });
        //getResult()

    }

    var timer;
    getVideo();
    $("#getVideosBtn").click(function () {
        getVideo();
    });
    $("#addVideo").click(function () {
        var fileObj = document.getElementById("file").files[0];
        var url = "/fkvideo/upload"; // 接收上传文件的后台地址
        upladFile(fileObj, url);
    })
})
function getVideo() {
    var content=''
    $.ajax({
        url: "/fkvideo/result",
        type: "POST",
        dataType: "text",
        success: function (data) {
            console.log(data);
            dataVideo = data.split(",");
            console.log("dataVideo.length:" + dataVideo.length);
            console.log("视频个数： " + dataVideo.length);
            for (var i = 0; i < dataVideo.length; i++) {
                if (dataVideo[i] == null || dataVideo == "")
                    continue;
                content += "<input type='radio' name='video' value='" + dataVideo[i] + "' >" + dataVideo[i] + "<br>"
            }
            var restult = "<div>" + content + "</div>"
            $("#videoList").html(restult)
        },
        processData: false,
        contentType: false
    });

}

function resetSession() {
    console.log("resetSession");
    $.ajax({
        url: "/fkvideo/reset",
        type: "GET",
        dataType: null,
        success: function (data) {
            console.log(data);
        },
        processData: false,
        contentType: false
    });
}

//上传文件方法
function upladFile() {
    var fileObj = document.getElementById("file").files[0]; // js 获取文件对象
    if (!fileObj) {
        alert("请选择上传的视频");
        return false;
    }
    var url = "/fkvideo/upload/"; // 接收上传文件的后台地址

    var form = new FormData(); // FormData 对象
    form.append("mf", fileObj); // 文件对象

    xhr = new XMLHttpRequest();  // XMLHttpRequest 对象
    xhr.open("post", url, true); //post方式，url为服务器请求地址，true 该参数规定请求是否异步处理。
    xhr.onload = function () {
        alert("上传成功")
        getVideo();
    }; //请求完成
    xhr.onerror = function () {
       alert(" 上传失败")
    }; //请求失败
    xhr.upload.onprogress = progressFunction;//【上传进度调用方法实现】
    xhr.upload.onloadstart = function () {//上传开始执行方法
        ot = new Date().getTime();   //设置上传开始时间
        oloaded = 0;//设置上传开始时，以上传的文件大小为0
    };
    xhr.send(form); //开始上传，发送form数据
}

//上传进度实现方法，上传过程中会频繁调用该方法
function progressFunction(evt) {

    var progressBar = document.getElementById("progressBar");
    var percentageDiv = document.getElementById("percentage");
    // event.total是需要传输的总字节，event.loaded是已经传输的字节。如果event.lengthComputable不为真，则event.total等于0
    if (evt.lengthComputable) {//
        progressBar.max = evt.total;
        progressBar.value = evt.loaded;
        percentageDiv.innerHTML = Math.round(evt.loaded / evt.total * 100) + "%";
    }

    var time = document.getElementById("time");
    var nt = new Date().getTime();//获取当前时间
    var pertime = (nt - ot) / 1000; //计算出上次调用该方法时到现在的时间差，单位为s
    ot = new Date().getTime(); //重新赋值时间，用于下次计算

    var perload = evt.loaded - oloaded; //计算该分段上传的文件大小，单位b
    oloaded = evt.loaded;//重新赋值已上传文件大小，用以下次计算

    //上传速度计算 单位b/s
    var speed = perload / pertime;
    var bspeed = speed;
    var units = 'b/s';//单位名称
    if (speed / 1024 > 1) {
        speed = speed / 1024;
        units = 'k/s';
    }
    if (speed / 1024 > 1) {
        speed = speed / 1024;
        units = 'M/s';
    }
    speed = speed.toFixed(1);
    //剩余时间
    var resttime = ((evt.total - evt.loaded) / bspeed).toFixed(1);
    time.innerHTML = '，速度：' + speed + units + '，剩余时间：' + resttime + 's';
    if (bspeed == 0)
        time.innerHTML = '上传已取消';
}

function getResult(data) {
    if (data == "no change") {
        console.log("data no change");
        return false;
    }
    data = JSON.parse(data);
    console.log(data['begin']);
    $("#resultShow").html("");
    var demo = "";
    if (data['faces']) {
        var beginTime = parseInt(data['begin']);
        var faces = data['faces']
        for (var i = 0; i < faces.length; i++) {
            var tmprResult = faces[i]['results'];
            var tmpTime = faces[i]['timestamp']
            var catchFace = faces[i]['catchFace']
            for (box in tmprResult) {
                var boxFaces = tmprResult[box];
                if (boxFaces.length == 0) {
                    demo += '<div class="col-xs-3 col-lg-3 col-sm-6" style="border: 1px #bce8f1 solid"> ' +
                        '<img class="col-xs-6 col-sm-6  img-rounded" src="' + catchFace + '">' +
                        '<img class=" col-xs-6 col-sm-6 img-rounded" src="' + catchFace + '">' +
                        ' <p class="col-xs-offset-2 ">视频中出现时间:' + nil + '(s)</p>'
                        + '</div>'
                } else {
                    for (var j = 0; j < boxFaces.length; j++) {
                        var boxFace = boxFaces[j]['face'];
                        //                        timePic=tmprResult[j]['photo'].split("/")[6].split(".")[0].substring(0,13);
                        //                            timePic = tmpTime.substring(45, 58)
                        second = (tmpTime - beginTime) / 1000
                        console.log(second);

                        demo += '<div class="col-xs-3 col-lg-3 col-sm-6" style="border: 1px #bce8f1 solid"> ' +
                            '<img class="col-xs-6 col-sm-6  img-rounded" src="' + catchFace + '">' +
                            '<img class=" col-xs-6 col-sm-6 img-rounded" src="' + boxFace['photo'] + '">' +
                            ' <p class="col-xs-offset-2 ">视频中出现时间:' + second + '(s)</p>'
                            + '</div>'
                    }
                }
            }

        }
    }
    $("#resultShow").html(demo)

}
//上传成功响应
function uploadComplete(evt) {
    //服务断接收完文件返回的结果
    //    alert(evt.target.responseText);
    alert("上传成功！");
}

//上传失败
function uploadFailed(evt) {

    alert("上传失败！");
}

//取消上传
function cancleUploadFile() {
    xhr.abort();
}

