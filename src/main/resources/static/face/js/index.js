var defaultPageSize = '50';
//var defaultPageSize = '1';
var defaultPageIndex = '1';
var defaultPageList = [10,50,150,200];
var tableData = {
    total : "",
    rows : []
};
var uploader = null;
//新增文件的时候需要用的
var parentId = null;
var parentPath = null;
var treeData = null;
//根节点路径
var rootNodePath = null;
var state = 'pending';
//断点续传分片文件夹的key
var md5File;
//存入每个文件ID对应的md5File
var fileArray;
//更新按钮
function getRowIndex(target){
    var tr = $(target).closest('tr.datagrid-row');
    return parseInt(tr.attr('datagrid-row-index'));
}
function editrow(target){
    $('#dgUpload').datagrid('beginEdit', getRowIndex(target));
}
function deleterow(target){
    $.messager.confirm('Confirm','Are you sure?',function(r){
        if (r){
            uploader.removeFile( $("#dgUpload").datagrid('getRows')[getRowIndex(target)].fileId, true);
            $('#dgUpload').datagrid('deleteRow', getRowIndex(target));
        }
    });
}
function saverow(target){
    $('#dgUpload').datagrid('endEdit', getRowIndex(target));
}
function cancelrow(target){
    $('#dgUpload').datagrid('cancelEdit', getRowIndex(target));
}

/*查询表格数据*/
function query_tabledata(pageSize, pageIndex, pId) {
    var typeCode = $("#type").val();
    var name = $("#name").val();
    var path = $("#path").val();
    $.ajax({
        type : "get",
        url : "/queryInfo",
        dataType : 'json',
        async : true,
        data : {
            'pageSize' : pageSize,
            'pageIndex' : pageIndex,
            'pId' : pId,
            'typeCode' : typeCode,
            'name' : name,
            'path' : path
        },
        success : function(data) {
            if(data.code == 0){
                tableData.total = data.total;
                tableData.rows = data.rows;
                $("#dgFile" ).datagrid('loadData', tableData);
            }
        }
    });
}

//初始化树状图
function initTree(){
    $.ajax({
        type : "post",
        url : "/queryFolderList?pId=-1",
        dataType : 'json',
        async : false,
        success : function(data) {
            treeData = data;
            rootNodePath = data[0].attributes;
        }
    });

    $('#folderList').tree({
        animate:true,
        data : treeData,
        onBeforeExpand: function (node) {
            expend(node);
        },
        onClick:function(node){
            $("#folderId").val(node.id);

            if(node.attributes != rootNodePath){
                $("#text_path").text(rootNodePath + getPath());
            } else {
                $("#text_path").text( getPath());
            }
            $("#text_path").attr("onclick","openLocalFile( $(\"#text_path\").text(),'folder','')");
            initFileDatagrid(node.id);
        }
    });

}

//刷新文件列表
function initFileDatagrid(pId){
    query_File(pId);
}

/*查询*/
function query_File(pId) {
    if($("#queryform").form('validate')){
        var pageInfo = getPageInfo("dgFile");
        $("#dgFile").datagrid('getPager').pagination('refresh',{pageSize : pageInfo[0]});
        $("#dgFile").datagrid('gotoPage', parseInt(pageInfo[1]));
        query_tabledata(pageInfo[0], pageInfo[1], pId);
    }
}
//展开文件夹
function expend(node){
    $('#folderList').tree('options').url='/queryFolderList?pId='+node.id;
}
//打开 新增文件夹 的弹框
//type : add; upd
function openFolderDia(type){

        if(type == 'add'){
            //打开新增弹框
            if(getPid() == null && treeData == null){
                $("#li_filePath").show();
                $("#dialog-addFolder").dialog({title : "新增根目录"});
            }else  if(getPid() == null){
                $.messager.alert('提示', '请选择您要添加的文件！');
                return;
            }else{
                $("#li_filePath").hide();
                $("#dialog-addFolder").dialog({title : "在文件夹["+getName()+"]下新增文件"});
            }
            $("#confirm").attr('onclick', "add('folder')");
        }else if(type == 'upd'){
            //打开修改弹框
            if(getName() == null){
                $.messager.alert('提示', '请选择您要修改的文件！');
                return;
            }else if(getName() == treeData[0].text){
                $("#li_filePath").show();
                $("#folderPath").textbox('setValue', getPath());
            }else{
                $("#li_filePath").hide();
            }
            $("#dialog-addFolder").dialog({title : "修改文件夹["+getName()+"]的名称"});
            $("#folderName").textbox('setValue', getName());
            $("#confirm").attr('onclick', "upd('folder')");
        }
        $("#dialog-addFolder").dialog('open');
}
//关闭 新增文件夹 的弹框
function closeFolderDia(){
    $("#dialog-addFolder").dialog('close');
    //清除内容
    $("#swich").form("reset");
}
//修改文件
function upd(type){
    var data = null;
    if( type == "folder"){
        var folderName = $("#folderName").val();
        var path = getPath();
        var realPath ;
        if(getName() == treeData[0].text){
            realPath = $("#folderPath").val();
        }else{
            realPath = path.substr(0, path.lastIndexOf("\\"))+"\\"+folderName;
        }
        data = {
            name : folderName,
            id : getPid(),
            flag : 'folder',
            path : realPath
        };
    }else if(type == 'file'){
        data = {
            name : folderName,
            id : getPid(),
            flag : 'file'
        };
    }
    $.ajax({
        url : "/updateFileInfo",
        type : "post",
        dataType : 'json',
        async : true,
        data : data,
        success : function(data) {
            if(data.code == 0){
                $.messager.alert('提示', '修改成功！', 'success');
                $("#text_path").text(realPath);
                closeFolderDia();
                if(getName() == treeData[0].text){
                    initTree();
                }else{
                    $("#folderList").tree("reload", getParent(getSelected().target).target);
                }
            }else{
                $.messager.alert('提示', '修改失败！', 'error');
            }
        }
    });
}

//新增文件
function add(type){
    var data = null;
    //添加文件
    if( type == "folder"){
        var folderName = $("#folderName").val();
        var path =  getPath()+ "\\" + folderName;
        data={
            typeCode : "tp_folder",
            name:folderName,
            path :path,
            flag : type,
            pId : getPid(),
            rootNodePath : rootNodePath
        };
    }else if(type == 'file'){
        //无
    }
    $.ajax({
        url : "/insertFileInfo",
        type : "post",
        dataType : 'json',
        async : true,
        data : data,
        success : function(data) {
            if(data.code == 0){
                $.messager.alert('提示', '添加成功！', 'success');
                $("#folderList").tree("reload", getSelected().target);
                closeFolderDia();
            }else{
                $.messager.alert('提示', data.msg, 'error');
            }
        }
    });
}
//删除文件
function delFile(type){
   var data = null;
   var msg = '确认删除!';
    if(type == 'folder') {
        data = {
            ids : getPid(),
            flag : 'folder',
            path :  rootNodePath + getPath()
        }
        msg = '您确认想要删除该文件夹吗？（删除该文件夹里面的文件一并删除）';
    }else if(type == "file"){
        var selectrows  = $("#dgFile").datagrid("getChecked");
        var ids = "";
        var paths = [];
        if (selectrows.length > 0) {
            for (var i = 0; i < selectrows.length; i++) {
                 ids += selectrows[i].id+",";
                 paths.push(selectrows[i].path);
            }
        }else{
            $.messager.alert('提示', '请选择你要删除的文件！', 'info');
            return ;
        }
        data = {
            ids : ids,
            flag :'file',
            paths :JSON.stringify(paths)
        }
        msg = '您确认想要删除该文件吗？';
    }
    $.messager.confirm({
        width :500,
        higth :300,
        title :'确认',
        msg : msg,
        fn : function(r){
            if (r){
                $.ajax({
                    url : "/deleteByPrimaryKey",
                    type : "post",
                    dataType : 'json',
                    async : true,
                    data : data,
                    success : function(data) {
                        if(data.code == 0){
                            $.messager.alert('提示', '删除成功！', 'success');
                            if(type == 'folder'){
                                query_tabledata(defaultPageSize, defaultPageIndex, getPid());
                                $("#folderList").tree("reload", getParent(getSelected().target).target);
                                $("#text_path").text("");
                            } else {
                                query_tabledata(defaultPageSize, defaultPageIndex, getPid());
                            }
                        }else{
                            $.messager.alert('提示', '删除失败！', 'error');
                        }
                    }
                });
            }

        }
    });

}
//打开维护文件的弹框
function openFileDia(type){

    if(getPid() == null){
        $.messager.alert('提示', '请从文件目录中选择你要添加的文件夹！', 'info');
        return ;
    }
    parentId = getPid();
    parentPath = getPath();
    if(type == 'add'){
        $("#dialog-file").dialog('open');
    }else if(type == 'upd'){
        $("#dialog-file").dialog('open');
    }
    //$(".panel-tool-close").attr("onclick",gotoPage('dgFile', tableData, query_tabledata));
    state = 'pending';
    fileArray = [];

    initUpload();
    $("#dgUpload").datagrid("loadData", { total: 0, rows: [] });
    uploader.reset();
}
//开始上传按钮的点击事件
function beginUploadClick() {
    if ( state === 'uploading' ) {
        uploader.stop();
    } else {
        uploader.upload();
    }
}
//初始化上传控件
function initUpload(){
    //监听分块上传过程中的时间点
    WebUploader.Uploader.register({
        "before-send-file":"beforeSendFile",  // 整个文件上传前
        "before-send":"beforeSend",  // 每个分片上传前
        //"after-send-file": "afterSendFile"  // 分片上传完毕
    },{
        //时间点1：所有分块进行上传之前调用此函数 ，检查文件存不存在
        beforeSendFile:function(file){
            var deferred = WebUploader.Deferred();
            md5File = hex_md5(file.name+file.size);//根据文件名称，大小确定文件唯一标记，这种方式不赞成使用
            $.ajax({
                type:"POST",
                url:"/checkFile",
                data:{
                    md5File: md5File, //文件唯一标记
                    path:$("#text_path").html() + "\\" + file.name //文件全路径
                },
                async: false,  // 同步
                dataType:"json",
                success:function(response){
                    if(response){  //文件存在，跳过 ，提示文件存在
                        $('#' + file.id).find('p.state').text("file exist");

                        var rowIndes = getIndexOrRowsByFileId("dgUpload", file.id, "index");
                        $('#dgUpload').datagrid('updateRow',{
                            index: rowIndes,
                            row: {
                                uploadState : "文件已存在！"
                            }
                        });

                        uploader.skipFile(file);
                        state = "uploadFail";
                        deferred.resolve();  //文件不存在或不完整，发送该文件
                    }else{
                        var paramOb = {"md5File": md5File, "filedId": file.source.uid};
                        fileArray.push(paramOb);
                        deferred.resolve();  //文件不存在或不完整，发送该文件
                    }
                }
            } , function (jqXHR, textStatus, errorThrown) { //任何形式的验证失败，都触发重新上传
                deferred.resolve();
            } );
            return deferred.promise();
        },
        //时间点2：如果有分块上传，则每个分块上传之前调用此函数  ，判断分块存不存在
        beforeSend:function(block){
            var deferred = WebUploader.Deferred();
            var pratMd5File;
            if (fileArray.length > 0) {
                for (var i = 0; i < fileArray.length; i++) {
                    if (fileArray[i].filedId == block.file.source.uid) {
                        pratMd5File = fileArray[i].md5File;
                        break;
                    }
                }
            }
            $.ajax({
                type:"POST",
                url:"/checkChunk",
                data:{
                    md5File: pratMd5File,  //文件唯一标记
                    chunk:block.chunk,  //当前分块下标
                    total: block.total, //文件的大小
                    path:  $("#text_path").html()
                },
                async: false,  // 同步
                dataType:"json",
                success:function(response){
                    if(response){
                        deferred.reject(); //分片存在，跳过
                    }else{
                        deferred.resolve();  //分块不存在或不完整，重新发送该分块内容
                    }
                }
            }, function (jqXHR, textStatus, errorThrown) { //任何形式的验证失败，都触发重新上传
                deferred.resolve();
            });
            return deferred.promise();
        },
        //时间点3：分片上传完成后，通知后台合成分片
       /* afterSendFile: function (file) {
        }*/
    });

     uploader = WebUploader.create({
         // swf文件路径
         swf: 'Uploader.swf',
         server: '/upload',
         // 内部根据当前运行是创建，可能是input元素，也可能是flash.
         pick: '#picker',
         // 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！
         resize: false,
         //压缩的图片的选项。如果此选项为false, 则图片在上传前不进行压缩
         compress : false,
         // 是否分块
         chunked:true,
         // 每块文件大小（默认5M）
         chunkSize:10*1024*1024,
         // 开启几个并非线程（默认3个）
         threads:4,
         // 在上传当前文件时，准备好下一个文件
         prepareNextFile:true,
    });
    // 当有文件被添加进队列的时候
    uploader.on( 'fileQueued', function( file ) {
       // $("#picker").hide();//隐藏上传框
        $('#dgUpload').datagrid('appendRow', {
            id : '',
            pId : parentId,
            name : file.name,
            typeName : '',
            typeCode : '',
            path : $("#text_path").text() + "\\" + file.name,
            fileSize : unitConvert(file.size),
            createTime : timestampToTime(new Date().getTime()),
            uploadState : '',
            remarks : '',
            fileId : file.id
        });
        uploader.refresh(); // 重新实例化解决 webUploader选择文件按钮无效
    });
    uploader.on( 'all', function( type ) {
        if ( type === 'startUpload' ) {
            state = 'uploading';
        } else if ( type === 'stopUpload' ) {
            state = 'paused';
        } else if ( type === 'uploadFinished' ) {
            state = 'done';
        }

        if ( state === 'uploading' ) {
            $('#ctlBtn').text('暂停上传！');
        } else {
            $('#ctlBtn').text('开始上传');
        }
    });



    //上传添加参数
    uploader.on('uploadBeforeSend', function (obj, data, headers) {
        if (fileArray.length > 0) {
            for (var i = 0; i < fileArray.length; i++) {
                if (fileArray[i].filedId ==  obj.file.source.uid) {
                    data.md5File = fileArray[i].md5File;
                    data.path = $("#text_path").html();
                    break;
                }
            }
        }
    });

    //文件成功、失败处理
    uploader.on('uploadSuccess', function (file) {
        var chunksTotal = Math.ceil(file.size / (10*1024*1024));
        if (chunksTotal >= 1) {
            //合并请求
            var deferred = WebUploader.Deferred();
            var sucessMd5File;
            if (fileArray.length > 0) {
                for (var i = 0; i < fileArray.length; i++) {
                    if (fileArray[i].filedId ==  file.source.uid) {
                        sucessMd5File = fileArray[i].md5File;
                        fileArray.splice(i, 1);
                        break;
                    }
                }
            }
            var rows = [];
            rows.push( getIndexOrRowsByFileId("dgUpload", file.id, "row"));
            $.ajax({
                type: "POST",
                url: "/merge",
                data: {
                    name : file.name,
                    md5File : sucessMd5File,
                    chunks : chunksTotal,
                    path : $("#text_path").html(),
                    rows :  JSON.stringify(rows)
                },
                cache: false,
                async: false,  // 同步
                dataType: "json",
                success:function(response){
                    if(response){
                        //$('#' + file.id).find('p.state').text('upload success');
                        var rowIndes = getIndexOrRowsByFileId("dgUpload", file.id, "index");
                        $('#dgUpload').datagrid('updateRow',{
                            index: rowIndes,
                            row: {
                                uploadState : '上传成功！'
                            }
                        });
                        //$('#' + file.id).find('.progress').fadeOut();
                    }else{
                        var rowIndes = getIndexOrRowsByFileId("dgUpload", file.id, "index");
                        $('#dgUpload').datagrid('updateRow',{
                            index: rowIndes,
                            row: {
                                uploadState : '上传失败！'
                            }
                        });
                        //$('#' + file.id).find('p.state').text('merge error');
                        deferred.reject();
                    }
                }
            })
            return deferred.promise();
        }
    });
    uploader.on( 'uploadError', function( file ) {
        var rowIndes = getIndexOrRowsByFileId("dgUpload", file.id, "index");
        $('#dgUpload').datagrid('updateRow',{
            index: rowIndes,
            row: {
                uploadState : "上传出错！"
            }
        });
    });

    uploader.on( 'uploadComplete', function( file ) {
        //$( '#'+file.id ).find('.progress').fadeOut();
    });
    // 文件上传过程中创建进度条实时显示。
    uploader.on( 'uploadProgress', function( file, percentage ) {
        var rows = $("#dgUpload").datagrid('getRows');
        var rowIndes = getIndexOrRowsByFileId("dgUpload", file.id, "index");
        $('#dgUpload').datagrid('updateRow',{
            index: rowIndes,
            row: {
                uploadState : "上传中！",
                status : (percentage * 100).toFixed(1)
            }
        });
    });

}
//保存文件
function saveFile(){
    var rows = $("#dgUpload").datagrid('getRows');
    if(rows.length == 0){
        $.messager.alert('提示', '请选择您要上传的文件！', 'info');
        return;
    }
    var formData = new FormData();
    formData.append("rows", JSON.stringify(rows));
    $.ajax({
        async : false,
        cache : false,
        type : "post",
        data : formData,
        url : "/insertFileInfo",
        dataType : 'json',
        contentType: false, // 必须
        processData: false, // 必须
        success : function(data) {
            if(data.code == 0){
                $.messager.alert('提示', '文件上传成功！', 'success');
                query_tabledata(defaultPageSize, defaultPageIndex, getPid());
                uploader.reset();
                $("#dgUpload").datagrid("loadData", { total: 0, rows: [] });
                $("#dialog-file").dialog('close');
            }else{
                $.messager.alert('提示', '文件上传失败！', 'error');
            }
        }
    });
}
//打开修改文件的弹框
function openEditFileDia(){
    var selectrows  = $("#dgFile").datagrid("getChecked");
    if(selectrows.length == 0){
        $.messager.alert('提示', '请选择你要修改的文件！', 'info');
        return;
    }else if(selectrows.length > 1){
        $.messager.alert('提示', '只能选择一个你要修改的文件！', 'info');
        return;
    }
    $("#dialog-editFile").dialog('open');
    //$(".panel-tool-close").hide();
    //$("#remarks").val(selectrows[0].remarks);
    $("#remarks").textbox('setValue',selectrows[0].remarks);
}
//确认保存文件的编辑
function confirmEdit(){
    var selectrows  = $("#dgFile").datagrid("getChecked");
    $.ajax({
        async : false,
        cache : false,
        type : "post",
        data : {
            id : selectrows[0].id,
            remarks :  $("#remarks").val()
        },
        url : "/updateFileInfo",
        dataType : 'json',
        success : function(data) {
            if(data.code == 0){
                $.messager.alert('提示', '文件修改成功！', 'success');
                query_tabledata(defaultPageSize, defaultPageIndex, getPid());
                cancelEdit();
            }else{
                $.messager.alert('提示', '文件修改失败！', 'error');
            }
        }
    });
}
//取消保存文件的编辑
function cancelEdit(){
    $("#dialog-editFile").dialog('close');
}
//关闭新增问价的弹框
function closeDiaFile(){
    gotoPage('dgFile', tableData, query_tabledata);
}
//关闭新增文件夹的弹框
function closeAddFolder() {
    $("#swich").form("reset");
}

