//获取datagrid表格的页码和页面大小
function getPageInfo(id){
    var pageopt = $('#'+id).datagrid('getPager').data("pagination").options;        //其中,datagridID是datagrid的ID
    var pageNumber = pageopt.pageNumber;    //当前是第几页
    var pageSize = pageopt.pageSize; //每页显示多少条数据
    var pages = new Array(pageSize, pageNumber);
    return pages;
}
//获取选中节点的ID作为PID
function getPid(){
    var node = getSelected();
    if(node != null){
        return node.id;
    }
    return null;
}
//获取选中节点的路径
function getPath(){
    var node = getSelected();
    if(getSelected() != null){
        return node.attributes;
    }
    return null;
}
//获取选中节点
function getSelected(){
    return $('#folderList').tree('getSelected');
}
//获取选中节点的名字
function getName(){
    var node = getSelected();
    if(node != null){
        return node.text;
    }
    return null;
}
//js打开本地文件夹或者文件
function openLocalFile2(filename){
    try{
        var obj=new ActiveXObject("wscript.shell");
        if(obj){
            obj.Run("\""+filename+"\"", 1, false );
            //obj.run("osk");/*打开屏幕键盘*/
            //obj.Run('"'+filename+'"');
            obj=null;
        }
    }catch(e){
        alert("请确定是否存在该盘符或文件");
    }
}
//调用后台java代码打开文件
function openLocalFile(filePath){
    $.ajax({
        url : "/openLocalFile",
        type : "post",
        dataType : 'json',
        async : true,
        data : {filePath : filePath},
        success : function(data) {
            if(data.code != 0){
                $.messager.alert('提示', data.msg, 'info');
            } else {
                $.messager.show({
                    title:'提醒',
                    msg:'该文件正在打开！',
                    timeout:3000,
                    showType:'slide'
                });

            }
        }
    });
}
//获取父节点
function getParent(target){
    return $('#folderList').tree('getParent', target);
}


function  getDg_Path(index){
   var path = $("#dgFile").datagrid('getRows')[index]["path"];
    openLocalFile(path);
}

//时间格式 2019-08-01 09:55:26
function timestampToTime(timestamp) {
    var date = new Date();//时间戳为10位需*1000，时间戳为13位的话不需乘1000
    var Y = date.getFullYear() + '-';
    var M = (date.getMonth()+1 < 10 ? '0'+(date.getMonth()+1) : date.getMonth()+1) + '-';
    var D = (date.getDate() < 10 ? '0'+date.getDate() : date.getDate()) + ' ';
    var h = (date.getHours() < 10 ? '0'+date.getHours() : date.getHours()) + ':';
    var m = (date.getMinutes() < 10 ? '0'+date.getMinutes() : date.getMinutes()) + ':';
    var s = (date.getSeconds() < 10 ? '0'+date.getSeconds() : date.getSeconds());
    strDate = Y+M+D+h+m+s;
    return strDate;
}
//文件大小单位换算
function unitConvert(size){
    if (size > 1024 * 1024) {
        return  size = (Math.round(size * 100 / (1024 * 1024)) / 100).toString() + 'MB';
    } else {
        return size = (Math.round(size * 100 / 1024) / 100).toString() + 'KB';
    }
}

//dategrid回到之前的页面
function gotoPage(datagridId,tableData,methodName){
    var pageSize = parent.$("#pageSize").val();
    var pageIndex = parent.$("#pageIndex").val();
    eval(methodName + '('+ pageSize +','+ pageIndex +')');
    $("#" + datagridId).datagrid('getPager').pagination('refresh',{pageSize : pageSize});
    $("#" + datagridId).datagrid('gotoPage', parseInt(pageIndex));
    $("#" + datagridId).datagrid('loadData', tableData);
    parent.$("#pageIndex").val(defaultPageIndex);
    parent.$("#pageSize").val(defaultPageSize);
}
