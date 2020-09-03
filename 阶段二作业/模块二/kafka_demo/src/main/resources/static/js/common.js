//获取参数
function GetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if(r != null) return decodeURI(r[2]);
    return null;
}

function sendLog(param) {
    $.ajax({
        url: "http://192.168.52.128/kafka/log",
        type: "POST",
        async: false,
        data: param,
        dataType: "json",
        success: function (data) {
            console.info("发送日志成功！")
        },
        error: function () {
            console.error("发送日志失败！")
        }
    })
}