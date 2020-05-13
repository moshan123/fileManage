CREATE DEFINER=`root`@`%` FUNCTION `selectChildByPid`(`parent_id` varchar(255)) RETURNS varchar(4000) CHARSET utf8
BEGIN
# 定义一个变量用来返回结果
DECLARE finalVar VARCHAR(2000);
# 定义一个临时变量
DECLARE tempVar VARCHAR(2000);

# 设置默认值
SET finalVar='$';
# 转换入参类型
SET tempVar = CAST(parent_id AS CHAR);

# 循环体，如果当前的临时变量中没有值，为空的情况下跳出循环，也就是说没有子节点了
WHILE tempVar IS NOT NULL DO

# 将得到的子节点保存到变量中
SET finalVar= CONCAT(finalVar,',',tempVar);
# 根据父Id查询所有的子节点
SELECT GROUP_CONCAT(t.ID) INTO tempVar FROM ts_file_info t WHERE FIND_IN_SET(t.PID,tempVar)>0;

# 结束循环
END WHILE;
# 返回结果,得到的是包含入参以及下面的所有子节点
RETURN finalVar;
END