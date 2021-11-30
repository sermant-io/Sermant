DROP FUNCTION IF EXISTS `parse_record_exec_status`;$$$
CREATE FUNCTION `parse_record_exec_status`(
	recordId INT ( 11 )) RETURNS varchar(255) CHARSET utf8
BEGIN
	DECLARE
sum_count INT ( 11 );
	DECLARE
wait_count INT ( 11 );
	DECLARE
process_count INT ( 11 );
	DECLARE
finish_count INT ( 11 );
	DECLARE
error_count INT ( 11 );
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
    emergency_exec_record_detail
WHERE
        record_id = recordId
  AND is_valid = '1';
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
	IF
sum_count = 0 THEN
			RETURN '2';

END IF;
RETURN '';

END;$$$