DELIMITER $$

USE `idgen2.0`$$

DROP PROCEDURE IF EXISTS `get_new_id_section_one`$$

CREATE DEFINER=`root`@`%` PROCEDURE `get_new_id_section_one`(IN in_id_name VARCHAR(128),
                                                             IN in_range BIGINT(20),
                                                             IN in_load_time BIGINT(20))
BEGIN
    DECLARE v_error TINYINT DEFAULT 1; 
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET v_error = -1; 
    
    IF (in_id_name IS NOT NULL AND in_range IS NOT NULL AND in_load_time IS NOT NULL) THEN
    BEGIN
        START TRANSACTION;
        SELECT project_name,
               id_name,
               id_prifx,
               id_start,
               last_range,
               max_range,
               min_range,
               last_load_time,
               applicant,
               redis_flag,
               state,
               min_load_interval,
               load_percentage,
               lock_expire,
               other_info 
          FROM id_info 
         WHERE id_name = in_id_name 
           AND state = 1;
        
        UPDATE id_info t 
           SET t.last_range = in_range, 
               t.last_load_time = in_load_time, 
               t.id_start = t.id_start + in_range
         WHERE t.id_name = in_id_name 
           AND t.state = 1;
        
        IF v_error = -1 THEN
            ROLLBACK;
        ELSE 
            COMMIT;
        END IF;        
    END;    
    END IF;
    
END$$

DELIMITER ;