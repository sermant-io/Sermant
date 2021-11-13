DROP FUNCTION IF EXISTS `parse_exec_status`;$$$
CREATE FUNCTION `parse_exec_status` ( exec_status VARCHAR ( 255 ) ) RETURNS VARCHAR ( 255 ) CHARSET utf8 BEGIN
	DECLARE
resultStr VARCHAR ( 255 ) DEFAULT '';
	IF exec_status = '0' THEN
			SET resultStr = '待执行';
		ELSEIF exec_status = '1' THEN
		SET resultStr = '正在执行';
		ELSEIF exec_status = '2' THEN
		SET resultStr = '执行成功';
		ELSEIF exec_status = '3' THEN
		SET resultStr = '执行失败';
		ELSEIF exec_status = '4' THEN
		SET resultStr = '执行取消';
		ELSEIF exec_status = '5' THEN
		SET resultStr = '人工确认成功';
		ELSEIF exec_status = '6' THEN
		SET resultStr = '人工确认失败';
ELSE
			SET resultStr = exec_status;
END IF;
RETURN resultStr;
END;$$$