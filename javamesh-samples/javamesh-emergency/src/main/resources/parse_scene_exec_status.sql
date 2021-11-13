DROP FUNCTION IF EXISTS parse_scene_exec_status;$$$
CREATE FUNCTION `parse_scene_exec_status`(
	execId INT ( 11 ),
	sceneId INT ( 11 )) RETURNS varchar(255) CHARSET utf8
BEGIN
	DECLARE
		sum_count INT ( 11 );
	DECLARE wait_count INT ( 11 );
	DECLARE process_count INT ( 11 );
	DECLARE finish_count INT ( 11 );
	DECLARE error_count INT ( 11 );
	IF sceneID = 0 then
	SELECT
		count( 1 ),
		sum( CASE WHEN `status` = '0' THEN 1 ELSE 0 END ),
		sum( CASE WHEN `status` = '1' THEN 1 ELSE 0 END ),
		sum( CASE WHEN `status` IN ( '2', '5' ) THEN 1 ELSE 0 END ),
		sum( CASE WHEN `status` IN ( '3', '4', '6' ) THEN 1 ELSE 0 END ) INTO sum_count,
		wait_count,
		process_count,
		finish_count,
		error_count 
	FROM
		emergency_exec_record 
	WHERE
		exec_id = execId 
		AND is_valid = '1';
	else
	SELECT
		count( 1 ),
		sum( CASE WHEN `status` = '0' THEN 1 ELSE 0 END ),
		sum( CASE WHEN `status` = '1' THEN 1 ELSE 0 END ),
		sum( CASE WHEN `status` IN ( '2', '5' ) THEN 1 ELSE 0 END ),
		sum( CASE WHEN `status` IN ( '3', '4', '6' ) THEN 1 ELSE 0 END ) INTO sum_count,
		wait_count,
		process_count,
		finish_count,
		error_count 
	FROM
		emergency_exec_record 
	WHERE
		exec_id = execId 
		AND scene_id = sceneId 
		AND is_valid = '1';
	END IF;
	
	IF
		error_count > 0 THEN
			RETURN '3';
		
	END IF;
	IF
		process_count > 0 THEN
			RETURN '1';
		
	END IF;
	IF
		finish_count = sum_count THEN
			RETURN '2';
		
	END IF;
	IF
		wait_count > 0 THEN
			RETURN '0';
		
	END IF;
	
	IF sum_count = 0 then
	 return '2';
	END IF;
	
	RETURN '';

END;$$$